package com.teraim.nils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.teraim.nils.DataTypes.AddRuleBlock;
import com.teraim.nils.DataTypes.Block;
import com.teraim.nils.DataTypes.ButtonBlock;
import com.teraim.nils.DataTypes.ButtonBlock.Action;
import com.teraim.nils.DataTypes.CreateFieldBlock;
import com.teraim.nils.DataTypes.CreateListEntryBlock;
import com.teraim.nils.DataTypes.LayoutBlock;
import com.teraim.nils.DataTypes.Rule;
import com.teraim.nils.DataTypes.SetValueBlock;
import com.teraim.nils.DataTypes.StartBlock;
import com.teraim.nils.DataTypes.Workflow;
import com.teraim.nils.DataTypes.XML_Variable;
import com.teraim.nils.exceptions.EvalException;
import com.teraim.nils.exceptions.RuleException;
import com.teraim.nils.expr.Aritmetic;
import com.teraim.nils.expr.Bool;
import com.teraim.nils.expr.Numeric;
import com.teraim.nils.expr.SyntaxException;

/**
 * 
 * @author Terje
 * Activity that runs a workflow that has a user interface.
 * Pressing Back button will return flow to parent workflow.
 */

public class FlowEngineActivity extends Activity {


	Workflow wf;
	View view;
	//Keep track of input in below arraylist.
	Map<Variable,View> bindings = new HashMap<Variable,View>();
	private List<Rule> rules = new ArrayList<Rule>();
	private final Map<Rule,Boolean>executedRules = new LinkedHashMap<Rule,Boolean>();	

	private ListView lv; 
	private ValidatorListAdapter mAdapter;
	private View validator_layer;
	private RelativeLayout enter_layer;
	private TextView errorView;
	private Context me;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		me = this;
		//Find out the name of the workflow to execute.
		Bundle b = getIntent().getExtras();
		String name = b.getString("workflow_name");
		if (name!=null) 
			wf = CommonVars.cv().getWorkflow(name);

		if (wf==null||name==null) {
			Log.e("NILS","Workflow "+name+" NOT found!");
			new AlertDialog.Builder(this).setTitle("Ups!")
			.setMessage("Kan tyvärr inte hitta workflow med namn: '"+name+"'. Kan det vara en felstavning?")
			.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {

				}})
				.show();				

			String heck[] = CommonVars.cv().getWorkflowNames();
			for (int i = 0 ; i< heck.length; i++)
				Log.e("NILS","Workflow "+i+": "+heck[i]);
			return;
		} else {
			Log.d("NILS","Now executing workflow "+name);
			setContentView(R.layout.wf_default);
			errorView = (TextView)findViewById(R.id.errortext);
			validator_layer = findViewById(R.id.validator_layer);
			enter_layer = (RelativeLayout)findViewById(R.id.enter_layer);
			//The list of all rules currently not ok
			mAdapter = new ValidatorListAdapter(this,executedRules);
			lv = (ListView)findViewById(R.id.validatorlist);
			lv.setAdapter(mAdapter);		
			lv.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int index, long arg3) {
					Iterator<Entry<Rule, Boolean>> it =executedRules.entrySet().iterator();
					int i = 0; Entry<Rule, Boolean>e = null;
					while (i++<=index&&it.hasNext())
						e = it.next();
					errorView.setText(e.getKey().getErrorMessage());
			}});
			execute();
		}


	}

	/**
	 * Execute the workflow.
	 */
	private void execute() {
		LinearLayout my_root = (LinearLayout) findViewById(R.id.myRoot);
		
		List<Block>blocks = wf.getBlocks();
		for (Block b:blocks) {
			if (b instanceof StartBlock)
				Log.d("NILS","Startblock found");

			else if (b instanceof LayoutBlock) {
				Log.d("NILS","Layoutblock found");
				LayoutBlock bl = (LayoutBlock) b;
				if (bl.getLayoutDirection().equals("horizontal")) {
					LinearLayout l = new LinearLayout(this);
					l.setOrientation(LinearLayout.HORIZONTAL);
					my_root.addView(l);
					my_root = l;
				}

			}
			else if (b instanceof ButtonBlock) {
				final Context me = this;
				Log.d("NILS","buttonblock found");
				final ButtonBlock bl = (ButtonBlock)b;
				Button bu = new Button(this);
				bu.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_selector));
				bu.setTextAppearance(this, R.style.WF_Text);
				bu.setText(bl.getText());
				
				LayoutParams params = new LayoutParams();
				params.width = LayoutParams.WRAP_CONTENT;
				params.height = LayoutParams.MATCH_PARENT;
				params.leftMargin = 100;
				params.rightMargin = 100;
				//Not sure about these..
				params.bottomMargin = 10;
				params.topMargin = 10;
				
				Display display = getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				int width = size.x;
				bu.setMinimumWidth(width-200);
				
				bu.setLayoutParams(params);
				
				bu.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Action action = bl.getAction();
						//ACtion = workflow to execute.
						//Commence!
						save();
						if (action!=null) {
							//Workflow?
							if (action.isWorkflow()){
								Intent intent = new Intent(me, FlowEngineActivity.class);
								Bundle b = new Bundle();
								b.putString("workflow_name", action.wfName); //Your id
								intent.putExtras(b); //Put your id to your next Intent
								//save all changes

								me.startActivity(intent);
								//Validation?
							} else
								validate();
						} else
							Log.e("NILS","Action was null for "+bl.getName());
					}

				});

				my_root.addView(bu);
			}
			else if (b instanceof CreateFieldBlock) {
				Log.d("NILS","Createfield block found");
				CreateFieldBlock bl = (CreateFieldBlock)b;
				Log.d("NILS","Variable is "+bl.getVariableReference());
				Variable var = CommonVars.cv().getVariable(bl.getVariableReference());
				//Create a numeric input field.
				if (var==null)
					continue;
				if (var.getType().equals(Variable.BOOLEAN)) {
					view = LayoutInflater.from(getBaseContext()).inflate(R.layout.ja_nej_field,null);
					my_root.addView(view);
					Log.d("NILS","BOOLEAN");
					TextView tv = (TextView)view.findViewById(R.id.label_text);
					tv.setText(var.getLabel());
					RadioButton ja = (RadioButton)view.findViewById(R.id.ja);
					RadioButton nej = (RadioButton)view.findViewById(R.id.nej);

					Boolean val = ((Bool)var).getValue();
					if (val!=null) {
						if (val.booleanValue())
							ja.setChecked(true);
						else
							nej.setChecked(true);
					}
					bindings.put(var, ja);

				}
				else {
					view = LayoutInflater.from(getBaseContext()).inflate(R.layout.editfield,null);
					my_root.addView(view);
					TextView tv = (TextView)view.findViewById(R.id.editfieldtext);
					tv.setText(var.getLabel());
					EditText et = (EditText)view.findViewById(R.id.editfieldinput);

					//Bind EditText to Variable and Save it.
					bindings.put(var, et);
					if (var.getType().equals(Variable.NUMERIC)) {
						Log.d("NILS","NUMERIC");
						et.setInputType(InputType.TYPE_CLASS_NUMBER);
						et.setRawInputType(Configuration.KEYBOARD_12KEY);					
						LayoutParams params = new LayoutParams();
						params.width=100;
						et.setLayoutParams(params);
						//If it has a value, print it.
						double val = ((Numeric)var).value();
						if (Double.isNaN(val)) {
							et.setText("");
						}
						else
							et.setText(String.valueOf(val));					
					} 
					else if (var.getType().equals(Variable.LITERAL)) {
						Log.d("NILS","LITERAL");
						et.setInputType(InputType.TYPE_CLASS_TEXT);
						LayoutParams params = new LayoutParams();
						params.weight=4;
						params.width=200;
						et.setLayoutParams(params);

					}
					else if (var.getType().equals(Variable.ARITMETIC)) {
						Log.d("NILS","ARITMETIC");
						et.setInputType(InputType.TYPE_CLASS_NUMBER);
						LayoutParams params = new LayoutParams();
						params.width=100;
						et.setLayoutParams(params);
						//If it has a value, print it.
						double val = ((Aritmetic)var).value();
						if (val!=Double.NaN)
							et.setText(String.valueOf(val));
						else
							et.setText("");
					}
				}

			}
			else if (b instanceof SetValueBlock) {
				Log.d("NILS","SetValue block found");
				SetValueBlock bl = (SetValueBlock)b;
				try {
					bl.run();
				} catch (EvalException e) {
					Log.e("NILS","EvalException! "+e.getMessage());


				} catch (SyntaxException e) {
					Log.e("NILS","SyntaxException! "+e.getMessage());

				}
			}
			else if (b instanceof AddRuleBlock) {
				Log.d("NILS","AddRule block found");
				AddRuleBlock bl = (AddRuleBlock)b;
				rules.add(bl.getRule());
			}
			else if (b instanceof CreateListEntryBlock) {
				Log.d("NILS","CreateListEntry block found");
				final CreateListEntryBlock bl = (CreateListEntryBlock)b;
				final List<TextView> tvs = new ArrayList<TextView>();
				view = LayoutInflater.from(getBaseContext()).inflate(R.layout.list_entry,null);
				my_root.addView(view);
				TextView tv = (TextView)view.findViewById(R.id.list_name);
				LinearLayout lv = (LinearLayout)view.findViewById(R.id.list_entry_var_layout);
				tv.setText(bl.getName());
				for(XML_Variable xv:bl.getVariables()) {
					//For now, all variables here are numeric.
					final Numeric v = (Numeric)CommonVars.cv().getVariable(xv.name);
					if (v!=null) {
						TextView te = new TextView(this);
						te.setText(xv.label+"="+(Double.isNaN(v.value())?"?":v.value()));
						te.setTextColor(Color.BLACK);
						te.setTextSize(20);
						tvs.add(te);
						LayoutParams p = new LayoutParams();
						p.rightMargin=15;
						p.bottomMargin=6;
						p.topMargin=6;
						p.width=LayoutParams.WRAP_CONTENT;
						p.height=LayoutParams.WRAP_CONTENT;
						te.setLayoutParams(p);
						Log.d("NILS","CreateListEntry Adding textview for "+xv.label);						
						lv.addView(te);
						bindings.put(v, te);
					}
				}
				lv.setClickable(true);
				lv.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						enter_layer.removeAllViews();
						LinearLayout prev = null;
						LinearLayout row = null;
						int id =1;
						final List<EditText>inputs = new ArrayList<EditText>();
						for(XML_Variable xv:bl.getVariables()) {
							Log.d("NILS","LABEL "+xv.label);
							final LayoutParams pl = new LayoutParams();
							final LayoutParams pr = new LayoutParams();
							final LayoutParams outer_l = new LayoutParams();
							final LayoutParams outer_r = new LayoutParams();
							final LayoutParams row_l = new LayoutParams();

							final Numeric v = (Numeric)CommonVars.cv().getVariable(xv.name);
							row = new LinearLayout(me);
							row.setId(id+400);
							LinearLayout ll = new LinearLayout(me);



							row_l.width=LayoutParams.MATCH_PARENT;
							row_l.height=LayoutParams.WRAP_CONTENT;

							row.setOrientation(LinearLayout.HORIZONTAL);
							row.setLayoutParams(row_l);


							outer_l.width=LayoutParams.MATCH_PARENT;
							outer_l.height=LayoutParams.WRAP_CONTENT;	
							//outer_l.leftMargin = 100;
							outer_l.weight=2;

							ll.setOrientation(LinearLayout.HORIZONTAL);
							ll.setLayoutParams(outer_l);


							LinearLayout rr = new LinearLayout(me);
							rr.setOrientation(LinearLayout.HORIZONTAL);					
							outer_r.width=LayoutParams.MATCH_PARENT;
							outer_r.height=LayoutParams.WRAP_CONTENT;

							outer_r.weight=2;
							outer_r.gravity=Gravity.LEFT;
							rr.setLayoutParams(outer_r);


							String s = ""+(Double.isNaN(v.value())?"":v.value());

							TextView te = new TextView(me);
							te.setTextColor(Color.BLACK);
							te.setTextSize(25);
							te.setText(xv.label);

							EditText et = new EditText(me);
							et.setText(s);
							et.setTextColor(Color.BLACK);
							et.setMinWidth(150);
							et.setTextSize(25);
							et.setInputType(InputType.TYPE_CLASS_NUMBER);
							inputs.add(et);

							pl.width=LayoutParams.MATCH_PARENT;
							pl.height=LayoutParams.WRAP_CONTENT;

							pr.width=LayoutParams.MATCH_PARENT;
							pr.height=LayoutParams.WRAP_CONTENT;


							te.setLayoutParams(pl);	
							et.setLayoutParams(pr);

							ll.addView(te);
							rr.addView(et);
							row.addView(ll);
							row.addView(rr);
							final RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 
									RelativeLayout.LayoutParams.WRAP_CONTENT);
							if (prev !=null)
								rp.addRule(RelativeLayout.BELOW,prev.getId());
							
							prev = row;
							prev.setId(id++);
							

							enter_layer.addView(row,rp);					


						}
						//Set focus on first input field.
						
						//Add a button below last row.
						final RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 
								RelativeLayout.LayoutParams.WRAP_CONTENT);
						if (row !=null) {
							rp.addRule(RelativeLayout.BELOW,row.getId());
							rp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

						}
						Button b = new Button(me);
						b.setText("Ok");
						
						//Onclick close window and save result.
						b.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								int i = 0;
								for(TextView tv:tvs) {
									String[] tmp = tv.getText().toString().split("=");
									tv.setText(tmp[0]+"="+inputs.get(i++).getText());
									tv.invalidate();
								}
								enter_layer.setVisibility(View.GONE);
							}});
						enter_layer.addView(b,rp);
						enter_layer.setVisibility(View.VISIBLE);
						
					}});
			}

		}

	}

	private void save() {
		Iterator<Map.Entry<Variable,View>> it = bindings.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Variable,View> pairs = (Map.Entry<Variable,View>)it.next();
			Variable var = pairs.getKey();
			if (var.getType().equals(Variable.BOOLEAN)) {
				//Get the yes radiobutton.
				RadioButton rb = (RadioButton)pairs.getValue();
				//If checked set value to True.
				((Bool)var).setValue(rb.isChecked());
			} else {
				if (pairs.getValue() instanceof EditText) {
					EditText et = (EditText)pairs.getValue();
					pairs.getKey().setValue(et.getText().toString());

				} else {
					TextView tv = (TextView)pairs.getValue();
					String[] tmp = tv.getText().toString().split("=");
					pairs.getKey().setValue((tmp.length>1?tmp[1]:""));
					
				}
			}
		}
	}

	//Evaluate all rules.
	//Show the rules that were broken in the UI.
	private void validate() {

		boolean result=false;
		executedRules.clear();
		for(Rule rule:rules) {
			try {
				//Test...
				result = rule.execute();
			}  catch (SyntaxException e) {
				Log.e("NILS","SyntaxException! "+e.getMessage()+" in "+rule.getName());
				
				continue;
			}
			//Find the target.
			Variable target;
			try {
				target = rule.getTarget();
			} catch (RuleException e) {
				//If the variable does not exist, continue
				Log.e("NILS","Variable was missing in rule validation: "+rule.getName());
				continue;
			}
			Log.d("NILS","Target is "+target.getName());
			View v = bindings.get(target);
			if (v==null)
				Log.e("NILS", "TARGET NOT FOUND FROM BINDINGS!!");
			//Found a broken rule!
			if(result==false) {
				v.setBackgroundColor(Color.RED);
				
				Toast.makeText(this, rule.getErrorMessage(), Toast.LENGTH_LONG).show();
			}
			else 
				v.setBackgroundColor(getResources().getColor(R.color.background));
				
			executedRules.put(rule,result);

		}
		if (executedRules.size()>0) {
			validator_layer.setVisibility(View.VISIBLE);
			mAdapter.notifyDataSetChanged();
			lv.requestFocusFromTouch();
			lv.setSelection(0);
			Iterator<Entry<Rule, Boolean>> it = executedRules.entrySet().iterator();
			Entry<Rule, Boolean>e = null;
			e = it.next();
			if (e!=null) 
				errorView.setText(e.getKey().getErrorMessage());
		} else
			validator_layer.setVisibility(View.GONE);
	}



}
