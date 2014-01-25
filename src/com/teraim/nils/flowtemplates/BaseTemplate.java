package com.teraim.nils.flowtemplates;

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
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableLayout.LayoutParams;

import com.teraim.nils.CommonVars;
import com.teraim.nils.DataTypes.Workflow.Type;
import com.teraim.nils.Variable;
import com.teraim.nils.DataTypes.ButtonBlock;
import com.teraim.nils.DataTypes.Rule;
import com.teraim.nils.DataTypes.Workflow;
import com.teraim.nils.DataTypes.ButtonBlock.Action;
import com.teraim.nils.InputAlertBuilder;
import com.teraim.nils.InputAlertBuilder.AlertBuildHelper;
import com.teraim.nils.exceptions.RuleException;
import com.teraim.nils.expr.Bool;
import com.teraim.nils.expr.SyntaxException;
import com.teraim.nils.R;

public class BaseTemplate extends Activity {

	protected Workflow wf;
	//Keep track of input in below arraylist.
	protected Map<Variable,View> bindings = new HashMap<Variable,View>();

	protected final Map<Rule,Boolean>executedRules = new LinkedHashMap<Rule,Boolean>();	

	protected List<Rule> rules = new ArrayList<Rule>();



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		wf = getFlow();
	}




	protected Workflow getFlow() {
		Workflow wf=null;

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
			return null;
		} else {
			Log.d("NILS","Now executing workflow "+name);

		}
		return wf;
	}


	protected Button addNormalButton(final ButtonBlock b) {
		final Context me = this;
		Log.d("NILS","buttonblock found");

		Button bu = new Button(this);
		bu.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_selector));
		bu.setTextAppearance(this, R.style.WF_Text);
		bu.setText(b.getText());

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
				Action action = b.getAction();
				//ACtion = workflow to execute.
				//Commence!
				save();
				if (action!=null) {
					//Workflow?
					if (action.isWorkflow()){

						Workflow wf = CommonVars.cv().getWorkflow(action.wfName);
						if (wf == null) {
							Log.e("NILS","Cannot find wf referenced by button "+b.getName()+
									"Workflow not found is named "+wf.getName());
						} else {
							
							Intent intent = new Intent(me,wf.getWfClass());
							Bundle b = new Bundle();
							b.putString("workflow_name", action.wfName); //Your id
							intent.putExtras(b); //Put your id to your next Intent
							//save all changes

							me.startActivity(intent);
							//Validation?
						}
					} else
						validate();
				} else
					Log.e("NILS","Action was null for "+b.getName());
			}

		});
		return bu;
	}

	protected View addNormalInput(ViewGroup bg,final String headerT,final String bodyT,final String currValue, final int id, final int inputType) {
		final View v = createClickableField(bg, headerT,currValue);
		final TextView et = (TextView)v.findViewById(R.id.editfieldinput);
		final InputAlertBuilder.AlertBuildHelper abh = new AlertBuildHelper(this.getBaseContext()) {
			@Override
			public View createView() {
				// Set an EditText view to get user input 

				int typeId = (inputType ==InputType.TYPE_CLASS_NUMBER)?R.layout.edit_field:R.layout.edit_field_komma;
				final EditText input = (EditText)LayoutInflater.from(c).inflate(typeId, null);

				input.setText(et.getText());
				//input.setInputType(inputType);

				return input;
			}

			@Override
			public void setResult(int id,View inputView,View outputView) {
				setStringValue(id,((EditText)inputView).getText().toString(),outputView);
			}};

			v.setOnClickListener(InputAlertBuilder.createAlert(id,headerT,bodyT,abh,v));

			return v;		

	}

	private View createClickableField(ViewGroup bg,String headerT,String defValue) {
		return createClickableField(bg,headerT,defValue,R.layout.clickable_field_normal);
	}
	private View createClickableListField(ViewGroup bg,String headerT,String defValue) {
		return createClickableField(bg,headerT,defValue,R.layout.clickable_field_list);
	}
	private View createClickableYesNoField(ViewGroup bg,String headerT,String defValue) {
		return createClickableField(bg,headerT,defValue,R.layout.clickable_field_yes_no);
	}

	private View createClickableField(ViewGroup bg,String headerT,String defValue, int res) {
		View view = LayoutInflater.from(getBaseContext()).inflate(res,null);
		bg.addView(view);
		TextView header = (TextView)view.findViewById(R.id.editfieldtext);
		SpannableString content = new SpannableString(headerT);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		header.setText(content);
		getTextView(view).setText(defValue);
		view.setClickable(true);
		return view;	
	}

	public TextView getTextView(View v) {
		return (TextView)v.findViewById(R.id.editfieldinput);
	}

	private void setStringValue(int id, String value, View v) { 
		switch(id) {
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
		//TODO: Add rules.
		if (executedRules.size()>0) {
			/*validator_layer.setVisibility(View.VISIBLE);
			mAdapter.notifyDataSetChanged();
			lv.requestFocusFromTouch();
			lv.setSelection(0);
			Iterator<Entry<Rule, Boolean>> it = executedRules.entrySet().iterator();
			Entry<Rule, Boolean>e = null;
			e = it.next();
			if (e!=null) 
				errorView.setText(e.getKey().getErrorMessage());
			 */

		} //else
		//validator_layer.setVisibility(View.GONE);
	}


}