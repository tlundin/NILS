package com.teraim.nils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.teraim.nils.DataTypes.AddRuleBlock;
import com.teraim.nils.DataTypes.Block;
import com.teraim.nils.DataTypes.ButtonBlock;
import com.teraim.nils.DataTypes.ButtonBlock.Action;
import com.teraim.nils.DataTypes.CreateFieldBlock;
import com.teraim.nils.DataTypes.LayoutBlock;
import com.teraim.nils.DataTypes.Rule;
import com.teraim.nils.DataTypes.SetValueBlock;
import com.teraim.nils.DataTypes.StartBlock;
import com.teraim.nils.DataTypes.Workflow;
import com.teraim.nils.exceptions.EvalException;
import com.teraim.nils.exceptions.RuleException;
import com.teraim.nils.expr.Aritmetic;
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
	private final ArrayList<Rule>brokenRules = new ArrayList<Rule>();	

	private ListView lv; 
	private ValidatorListAdapter mAdapter;
	private View validator_layer;
	private TextView errorView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Find out the name of the workflow to execute.
		Bundle b = getIntent().getExtras();
		String name = b.getString("workflow_name");
		if (name!=null) 
			wf = CommonVars.cv().getWorkflow(name);

		if (wf==null||name==null) {
			Log.e("NILS","Workflow "+name+" NOT found!");
			return;
		} else {
			Log.d("NILS","Now executing workflow "+name);
			setContentView(R.layout.wf_default);
			errorView = (TextView)findViewById(R.id.errortext);
			validator_layer = findViewById(R.id.validator_layer);
			//The list of all rules currently not ok
			mAdapter = new ValidatorListAdapter(this,brokenRules);
			lv = (ListView)findViewById(R.id.validatorlist);
	        lv.setAdapter(mAdapter);
	        lv.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int index, long arg3) {
					errorView.setText(brokenRules.get(index).getErrorMessage());
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
				Log.d("NILS","Text is "+bl.getText());
				bu.setText(bl.getText());
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
				view = LayoutInflater.from(getBaseContext()).inflate(R.layout.editfield,null);
				my_root.addView(view);
				TextView tv = (TextView)view.findViewById(R.id.editfieldtext);
				tv.setText(var.getName());
				EditText et = (EditText)view.findViewById(R.id.editfieldinput);

				//Bind EditText to Variable and Save it.
				bindings.put(var, et);
				if (var.getType().equals(Variable.NUMERIC)) {
					Log.d("NILS","NUMERIC");
					et.setInputType(InputType.TYPE_CLASS_NUMBER);
					LayoutParams params = new LayoutParams();
					params.width=100;
					et.setLayoutParams(params);
					//If it has a value, print it.
					et.setText(String.valueOf(((Numeric)var).value()));

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
					et.setText(String.valueOf(((Aritmetic)var).value()));

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


		}

	}

	private void save() {
		Iterator<Map.Entry<Variable,View>> it = bindings.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Variable,View> pairs = (Map.Entry<Variable,View>)it.next();
			System.out.println(pairs.getKey() + " = " + pairs.getValue());
			EditText et = (EditText)pairs.getValue();
			pairs.getKey().setValue(et.getText().toString());
		}
	}

	//Evaluate all rules.
	//Show the rules that were broken in the UI.
	private void validate() {
		
		boolean result=false;
		brokenRules.clear();
		for(Rule rule:rules) {
			try {
				//Test...
				result = rule.execute();
			}  catch (SyntaxException e) {
				Log.e("NILS","SyntaxException! "+e.getMessage());
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
				View v = bindings.get(target);
				//Found a broken rule!
				if(result==false) {
					v.setBackgroundColor(Color.RED);
					brokenRules.add(rule);
					Toast.makeText(this, rule.getErrorMessage(), Toast.LENGTH_LONG).show();
				}
				else 
					v.setBackgroundColor(0xFF051417);
				
			}
		if (brokenRules.size()>0) {
			validator_layer.setVisibility(View.VISIBLE);
			mAdapter.notifyDataSetChanged();
			lv.requestFocusFromTouch();
			lv.setSelection(0);
			errorView.setText(brokenRules.get(0).getErrorMessage());
		} else
			validator_layer.setVisibility(View.GONE);
		}

	

}
