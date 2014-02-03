package com.teraim.nils.dynamic.workflow_realizations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.teraim.nils.R;
import com.teraim.nils.StoredVariable;
import com.teraim.nils.VarIdentifier;
import com.teraim.nils.Variable;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.dynamic.workflow_abstracts.EventGenerator;
import com.teraim.nils.dynamic.workflow_abstracts.Listable;

public class WF_ClickableField extends WF_ListEntry implements EventGenerator {

	TextView myHeader;
	String myKey;
	protected Map<VarIdentifier,TextView> myOutputFields = new HashMap<VarIdentifier,TextView>();
	protected Map<VarIdentifier,View> myVars = new HashMap<VarIdentifier,View>();
	final LinearLayout outputContainer, inputContainer;
	//Hack! Used to determine what is the master key for this type of element.
	//If DisplayOut & Virgin --> This is master key.
	boolean virgin=true;
	private WF_Context myContext;
	
	public  WF_ClickableField(final String headerT,final String descriptionT, WF_Context context,String id) {
		super(LayoutInflater.from(context.getContext()).inflate(R.layout.clickable_field_normal,null),context.getContext());			
		myKey = headerT;
		this.myId=id;
		myContext = context;
		myHeader = (TextView)getWidget().findViewById(R.id.editfieldtext);
		outputContainer = (LinearLayout)getWidget().findViewById(R.id.outputContainer);
		SpannableString content = new SpannableString(headerT);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		myHeader.setText(content);
		inputContainer = new LinearLayout(ctx);
		inputContainer.setOrientation(LinearLayout.VERTICAL);
		inputContainer.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 
				LinearLayout.LayoutParams.MATCH_PARENT,
				1));


		getWidget().setClickable(true);	
		getWidget().setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {

				//On click, create dialog 			
				AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
				alert.setTitle(myKey);
				alert.setMessage(descriptionT);

				alert.setPositiveButton("Spara", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {				  
						save();
						refreshValues();
						((ViewGroup)inputContainer.getParent()).removeView(inputContainer);
					}
				});
				alert.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						((ViewGroup)inputContainer.getParent()).removeView(inputContainer);
					}
				});	
				Dialog d = alert.setView(inputContainer).create();
				//WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
				//lp.copyFrom(d.getWindow().getAttributes());
				//lp.height = WindowManager.LayoutParams.FILL_PARENT;
				//lp.height = 600;

				d.show();

				//d.getWindow().setAttributes(lp);
			}		
		});	
	}


	public void addVariable(String varLabel, String varId, Unit unit, Variable.Type numType, StoredVariable.Type varType, boolean displayOut) {
		if (displayOut && virgin) {
			virgin = false;
			super.setKeyRow(varId);
		}
			
		// Set an EditText view to get user input 
		VarIdentifier varIdentifier = new VarIdentifier(ctx,varLabel,varId,numType,varType,unit);
		if (numType == Variable.Type.BOOLEAN) {
			View view = LayoutInflater.from(ctx).inflate(R.layout.ja_nej_radiogroup,null);
			RadioButton ja = (RadioButton)view.findViewById(R.id.ja);
			RadioButton nej = (RadioButton)view.findViewById(R.id.nej);
			String value = varIdentifier.getPrintedValue();
			if(value!=null) {
				if(value.equals("1"))
					ja.setEnabled(true);
				else
					nej.setEnabled(true);
				ja.setChecked(true);
			}
			inputContainer.addView(view);
			myVars.put(varIdentifier,view);
		}
		else {
			Log.d("nils","adding variable "+varId);
			View l = LayoutInflater.from(ctx).inflate(R.layout.edit_field,null);
			TextView header = (TextView)l.findViewById(R.id.header);
			EditText view = (EditText)l.findViewById(R.id.edit);
			header.setText(varLabel+" ("+unit.name()+")");
			view.setText(varIdentifier.getPrintedValue());
			inputContainer.addView(l);
			myVars.put(varIdentifier,view);
		}
		if (displayOut) {
			TextView o = (TextView)LayoutInflater.from(ctx).inflate(R.layout.output_field,null);
			String value = varIdentifier.getPrintedValue();
			if (!value.isEmpty()) 
				o.setText(varLabel+": "+value+" ("+varIdentifier.getPrintedUnit()+")");			
			myOutputFields.put(varIdentifier,o);
			outputContainer.addView(o);
		}

	}


	private void save() {
		//for now only delytevariabler. 
		Iterator<Map.Entry<VarIdentifier,View>> it = myVars.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<VarIdentifier,View> pairs = (Map.Entry<VarIdentifier,View>)it.next();
			VarIdentifier varId = pairs.getKey();
			View view = pairs.getValue();
			if (varId.numType == Variable.Type.BOOLEAN) {
				//Get the yes radiobutton.
				RadioButton rb = (RadioButton)view;
				//If checked set value to True.
				if (rb.isChecked())
					varId.setValue(rb.isChecked()?"1":"0");
			} else {
				EditText et = (EditText)view;
				varId.setValue(et.getText().toString());

				/*} else {
					TextView tv = (TextView)pairs.getValue();
					String[] tmp = tv.getText().toString().split("=");
					pairs.getKey().setValue((tmp.length>1?tmp[1]:""));

				}
				 */
			}
		}
		myContext.registerEvent(new WF_Event_OnSave(this));
	}

	@Override
	public void refreshInputFields(){
		Log.d("nils","In refreshinputfields");
		Set<Entry<VarIdentifier, View>> vars = myVars.entrySet();
		for(Entry<VarIdentifier, View>entry:vars) {
			VarIdentifier varId = entry.getKey();
			String value = varId.getPrintedValue();
			if (value == null)
				Log.d("nils","WF_Clickable:value was null in refreshinput");
			else {
				TextView v = (TextView)entry.getValue();
				if (v!=null)
					v.setText(value);
				else
					Log.d("nils","WF_Clickable:view was null in refreshinput");
			}
			
		}
		refreshValues();
	}
	
	@Override
	public void refreshValues() {
		//Log.d("nils","refreshoutput called on "+myHeader);
		Iterator<Map.Entry<VarIdentifier,TextView>> it = myOutputFields.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<VarIdentifier,TextView> pairs = (Map.Entry<VarIdentifier,TextView>)it.next();
			//Log.d("nils","Iterator has found "+pairs.getKey()+" "+pairs.getValue());
			VarIdentifier varId = pairs.getKey();
			TextView out = pairs.getValue();
			String value = varId.getPrintedValue();
			if (!value.isEmpty())
				out.setText(varId.getLabel()+": "+value+" ("+varId.getPrintedUnit()+")");
			else
				out.setText("");
		}	}








	


}