package com.teraim.nils.dynamic.workflow_realizations;

import java.util.Set;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.teraim.nils.R;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.workflow_abstracts.Event;
import com.teraim.nils.dynamic.workflow_abstracts.Event.EventType;
import com.teraim.nils.dynamic.workflow_abstracts.EventListener;
import com.teraim.nils.dynamic.workflow_abstracts.Listable;

public class WF_Not_ClickableField_SumAndCountOfVariables extends
WF_Not_ClickableField implements EventListener {

	private WF_List targetList;

	String myPattern;
	//Detta är en id för widgeten - inte variabeln!!
	//private static final String MY_WIDGET_ID = "Antal Arter";
	
	public enum Type {
		sum,
		count
	}
	Type myType;

	public WF_Not_ClickableField_SumAndCountOfVariables(String header,String descriptionT, WF_Context myContext, 
			View view, String myTarget, String pattern,Type sumOrCount) {
		super(header, descriptionT, myContext, view);

		targetList = myContext.getList(myTarget);
		if (targetList == null) {
			Log.e("parser","couldn't create sortwidget - could not find target list");
		}

		myPattern = pattern;
		myContext.addEventListener(this,EventType.onRedraw);
		myType = sumOrCount;
		
	}

	@Override
	public LinearLayout getFieldLayout() {
		return (LinearLayout)LayoutInflater.from(ctx).inflate(R.layout.output_field_selection_element,null);
	}

	@Override
	public String getFormattedText(Variable varId, String value) {
		return value;
	}


	@Override
	public void onEvent(Event e) {
		Log.d("nils","In ADDNUMBER event targetListId: "+targetList.getId()+" e.getProvider: "+e.getProvider()+
				"type of event: "+e.getType().name());
		if (e.getProvider().equals(targetList.getId())) {
			matchAndRecalculateMe();
			this.refreshValues();
		} else
			Log.d("nils","event discarded - from wrong list");

	}

	public void matchAndRecalculateMe() {
		Long sum=Long.valueOf(0);
		for (Listable l:targetList.getList()) {
			Set<Variable> vars = l.getAssociatedVariables();
			if (vars!=null && !vars.isEmpty()) {
				for (Variable v:vars) {
					if (v.getId().matches(myPattern)) {
//						Log.d("nils","ADD_NUMBER_OF_SELECTION: Found match! "+v.getId());				
						if (v.getValue()!=null) {
							if (myType == Type.count)
								sum++;
							else {
								String val=v.getValue();
								if (val!=null && !val.isEmpty())
									sum+=Long.parseLong(v.getValue());
							}
						}
					}

				}
			} else {
				Log.d("nils ","Vars for "+l.getLabel()+" empty");
			}
		}
		if (myVar !=null)
			myVar.setValue(sum.toString());

	}

}
