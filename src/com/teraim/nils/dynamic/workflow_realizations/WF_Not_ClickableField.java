package com.teraim.nils.dynamic.workflow_realizations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teraim.nils.R;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.utils.Tools;

public abstract class WF_Not_ClickableField extends WF_ListEntry {
	protected WF_Context myContext;
	protected TextView myHeader;
	protected String myDescription;
	final LinearLayout outputContainer;
	protected Map<Variable,LinearLayout> myOutputFields = new HashMap<Variable,LinearLayout>();

	//Hack! Used to determine what is the master key for this type of element.
	//If DisplayOut & Virgin --> This is master key.
	boolean virgin=true;
	protected Variable myVar;
	private Unit myUnit;
	public abstract LinearLayout getFieldLayout();
	public abstract String getFormattedText(Variable varId, String value);


	@Override
	public Set<Variable> getAssociatedVariables() {
		Set<Variable> s = new HashSet<Variable>();
		s.add(myVar);
		return s;
	}
	
	
	public WF_Not_ClickableField(final String myId,final String descriptionT, WF_Context myContext, View view,boolean isVisible) {
		super(view,myContext,isVisible);
	
		this.myContext = myContext;
		myHeader = (TextView)getWidget().findViewById(R.id.editfieldtext);
		outputContainer = (LinearLayout)getWidget().findViewById(R.id.outputContainer);

		myHeader.setText(myId);
		
		myDescription = descriptionT;
	
	
	}
	
	public void addVariable(Variable var, boolean displayOut) {
		
		String varId = var.getId();
		if (displayOut && virgin) {
			virgin = false;
			super.setKeyRow(varId);
		}
		
	    myUnit = var.getUnit();
		if (displayOut) {
			LinearLayout ll = getFieldLayout();

			/*
			 TextView o = (TextView)ll.findViewById(R.id.outputValueField);
			TextView u = (TextView)ll.findViewById(R.id.outputUnitField);

			String value = Variable.getPrintedValue();
			if (!value.isEmpty()) {
				o.setText(varLabel+": "+value);	
				u.setText(" ("+Variable.getPrintedUnit()+")");
			}
			 */
			myOutputFields.put(var,ll);
			outputContainer.addView(ll);
			myVar = var;
		}

	}
	
	
	@Override
	public void refreshOutputFields() {
		//Log.d("nils","refreshoutput called on "+myHeader);
		Iterator<Map.Entry<Variable,LinearLayout>> it = myOutputFields.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Variable,LinearLayout> pairs = (Map.Entry<Variable,LinearLayout>)it.next();
			//Log.d("nils","Iterator has found "+pairs.getKey()+" "+pairs.getValue());
			Variable varId = pairs.getKey();
			LinearLayout ll = pairs.getValue();
			TextView o = (TextView)ll.findViewById(R.id.outputValueField);
			TextView u = (TextView)ll.findViewById(R.id.outputUnitField);			
			String value = varId.getValue();
			if (value!=null&&!value.isEmpty()) {
				o.setText(getFormattedText(varId,value));	
				u.setText(Tools.getPrintedUnit(myUnit));
			}
			else {
				o.setText("");
				u.setText("");
			}
		}	
	}

	//TODO: This is of course  wrong.
	@Override
	public void refreshInputFields() {
		
	}

}
