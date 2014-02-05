package com.teraim.nils.dynamic.workflow_realizations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.teraim.nils.R;
import com.teraim.nils.StoredVariable;
import com.teraim.nils.Variable;
import com.teraim.nils.dynamic.types.VarIdentifier;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.dynamic.workflow_abstracts.EventGenerator;

public abstract class WF_ClickableField extends WF_Not_ClickableField implements  EventGenerator {


	
	final LinearLayout inputContainer;

	protected Map<VarIdentifier,View> myVars = new HashMap<VarIdentifier,View>();

	
	public abstract LinearLayout getFieldLayout();
	public abstract String getFormattedText(VarIdentifier varId, String value);
	public abstract String getFormattedUnit(VarIdentifier varId);

	@Override
	public Set<VarIdentifier> getAssociatedVariables() {
		return myVars.keySet();
	}
	
	public  WF_ClickableField(final String myId,final String descriptionT, WF_Context context,String id, View view) {
		super(myId,descriptionT,context,view);		
		//SpannableString content = new SpannableString(headerT);
		//content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		inputContainer = new LinearLayout(ctx);
		inputContainer.setOrientation(LinearLayout.VERTICAL);
		inputContainer.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 
				LinearLayout.LayoutParams.MATCH_PARENT,
				1));


		//TODO: SUPPORT FOR OTHER THAN EDITTEXT!!!
		
		//Empty all inputs and save.
		getWidget().setClickable(true);	
		getWidget().setOnLongClickListener(new OnLongClickListener(){
		    @Override
		    public boolean onLongClick(View v) {
		       for (View inf:myVars.values()) {
		    	   if (inf!=null)
		    		   ((EditText)inf).setText("");
		       }
		       save();
		        return true;
		    }
		});
		
		
		getWidget().setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {

				//On click, create dialog 			
				AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
				alert.setTitle(myId);
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
				if (inputContainer.getParent()!=null)
					((ViewGroup)inputContainer.getParent()).removeView(inputContainer);
				Dialog d = alert.setView(inputContainer).create();
				d.setCancelable(false);
				//WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
				//lp.copyFrom(d.getWindow().getAttributes());
				//lp.height = WindowManager.LayoutParams.FILL_PARENT;
				//lp.height = 600;

				d.show();

				//d.getWindow().setAttributes(lp);
			}		
		});	
	}

	@Override
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
			header.setText(varLabel+" ("+varIdentifier.getPrintedUnit()+")");
			view.setText(varIdentifier.getPrintedValue());
			inputContainer.addView(l);
			myVars.put(varIdentifier,view);
		}
		if (displayOut) {
			LinearLayout ll = getFieldLayout();

			/*
			 TextView o = (TextView)ll.findViewById(R.id.outputValueField);
			TextView u = (TextView)ll.findViewById(R.id.outputUnitField);

			String value = varIdentifier.getPrintedValue();
			if (!value.isEmpty()) {
				o.setText(varLabel+": "+value);	
				u.setText(" ("+varIdentifier.getPrintedUnit()+")");
			}
			 */
			myOutputFields.put(varIdentifier,ll);
			outputContainer.addView(ll);
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
		myContext.registerEvent(new WF_Event_OnSave(this.getId()));
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












}