package com.teraim.nils.dynamic.workflow_realizations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teraim.nils.R;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.VarIdentifier;
import com.teraim.nils.dynamic.types.Numerable;
import com.teraim.nils.dynamic.types.Workflow.Unit;

public abstract class WF_Not_ClickableField extends WF_ListEntry {
	protected WF_Context myContext;
	protected TextView myHeader;
	final LinearLayout outputContainer;
	protected Map<VarIdentifier,LinearLayout> myOutputFields = new HashMap<VarIdentifier,LinearLayout>();

	//Hack! Used to determine what is the master key for this type of element.
	//If DisplayOut & Virgin --> This is master key.
	boolean virgin=true;
	protected VarIdentifier myVar;
	
	public abstract LinearLayout getFieldLayout();
	public abstract String getFormattedText(VarIdentifier varId, String value);
	public abstract String getFormattedUnit(VarIdentifier varId);

	@Override
	public Set<VarIdentifier> getAssociatedVariables() {
		Set<VarIdentifier> s = new HashSet<VarIdentifier>();
		s.add(myVar);
		return s;
	}
	
	
	public WF_Not_ClickableField(final String myId,final String descriptionT, WF_Context myContext, View view) {
		super(view,myContext.getContext());
	
		this.myContext = myContext;
		myHeader = (TextView)getWidget().findViewById(R.id.editfieldtext);
		outputContainer = (LinearLayout)getWidget().findViewById(R.id.outputContainer);

		myHeader.setText(myId);
	
	
	}
	
	public void addVariable(String varLabel,String postLabel, String varId, Unit unit, Variable.DataType numType, Variable.StorageType varType, boolean displayOut) {
		
		if (displayOut && virgin) {
			virgin = false;
			super.setKeyRow(varId);
		}

		// Set an EditText view to get user input 
		VarIdentifier varIdentifier = new VarIdentifier(ctx,varLabel,varId,numType,varType,unit);
	
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
			myVar = varIdentifier;
		}

	}
	
	
	@Override
	public void refreshValues() {
		//Log.d("nils","refreshoutput called on "+myHeader);
		Iterator<Map.Entry<VarIdentifier,LinearLayout>> it = myOutputFields.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<VarIdentifier,LinearLayout> pairs = (Map.Entry<VarIdentifier,LinearLayout>)it.next();
			//Log.d("nils","Iterator has found "+pairs.getKey()+" "+pairs.getValue());
			VarIdentifier varId = pairs.getKey();
			LinearLayout ll = pairs.getValue();
			TextView o = (TextView)ll.findViewById(R.id.outputValueField);
			TextView u = (TextView)ll.findViewById(R.id.outputUnitField);			
			String value = varId.getPrintedValue();
			if (!value.isEmpty()) {
				o.setText(getFormattedText(varId,value));	
				u.setText(getFormattedUnit(varId));
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
