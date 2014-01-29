package com.teraim.nils.flowtemplates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.teraim.nils.DataTypes.Rule;
import com.teraim.nils.DataTypes.WF_Container;
import com.teraim.nils.DataTypes;
import com.teraim.nils.R;
import com.teraim.nils.ValidatorListAdapter;

/**
 * 
 * @author Terje
 * Activity that runs a workflow that has a user interface.
 * Pressing Back button will return flow to parent workflow.
 */

public class DefaultTemplate extends Executor {



	View view;
	private ListView lv; 
	private ValidatorListAdapter mAdapter;
	private View validator_layer;
	private RelativeLayout enter_layer;
	private LinearLayout my_root;

	private TextView errorView;
	private Context me;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		me = this;
		setContentView(R.layout.wf_default);
		errorView = (TextView)findViewById(R.id.errortext);
		validator_layer = findViewById(R.id.validator_layer);
		enter_layer = (RelativeLayout)findViewById(R.id.enter_layer);
		my_root = (LinearLayout) findViewById(R.id.myRoot);
		//The list of all rules currently not ok
		//mAdapter = new ValidatorListAdapter(this,executedRules);
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
	@Override
	protected List<WF_Container> getContainers() {
		ArrayList<WF_Container> ret = new ArrayList<WF_Container>();
		ret.add(DataTypes.getSingleton().new WF_Container("plain",my_root,null));
		return ret;
	}
	




	/**
	 * Execute the workflow.
	 */
	/*
	private void execute() {
	

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
				Button bu = addNormalButton((ButtonBlock)b);
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
				if (var.getType()==Variable.Type.BOOLEAN) {
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
					//bindings.put(var, ja);

				}
				else {
					view = LayoutInflater.from(getBaseContext()).inflate(R.layout.editfield,null);
					my_root.addView(view);
					TextView tv = (TextView)view.findViewById(R.id.editfieldtext);
					tv.setText(var.getLabel());
					EditText et = (EditText)view.findViewById(R.id.editfieldinput);

					//Bind EditText to Variable and Save it.
					//bindings.put(var, et);
					if (var.getType()==Variable.Type.NUMERIC) {
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
					else if (var.getType()==Variable.Type.LITERAL) {
						Log.d("NILS","LITERAL");
						et.setInputType(InputType.TYPE_CLASS_TEXT);
						LayoutParams params = new LayoutParams();
						params.weight=4;
						params.width=200;
						et.setLayoutParams(params);

					}
					else if (var.getType()==Variable.Type.ARITMETIC) {
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
						//bindings.put(v, te);
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
			} else if (b instanceof CreateListEntriesBlock) {
				createVarListFromFile((CreateListEntriesBlock)b, my_root);
			}

		}

	}
	 */



}
