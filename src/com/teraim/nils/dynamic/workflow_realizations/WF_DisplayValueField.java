package com.teraim.nils.dynamic.workflow_realizations;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.types.ParameterCache;
import com.teraim.nils.dynamic.types.VarIdentifier;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.dynamic.workflow_abstracts.Event;
import com.teraim.nils.dynamic.workflow_abstracts.Event.EventType;
import com.teraim.nils.dynamic.workflow_abstracts.EventListener;

public class WF_DisplayValueField extends WF_Widget implements EventListener {

	String myVar;
	Unit unit;
	ParameterCache pc;
	public WF_DisplayValueField(String id, View v, String varKey,WF_Context ctx, Unit unit) {
		super(id, v);
		this.myVar=varKey;
		Log.d("nils","In WF_DisplayValueField Create");	
		ctx.addEventListener(this, EventType.onSave);	
		pc = GlobalState.getInstance(ctx.getContext()).getCurrentDelyta();
		this.unit=unit;
	}

	//update variable.
	@Override
	public void onEvent(Event e) {
		Log.d("nils","Got event in WF_DisplayValueField");	
		//TODO: CHANGE WHEN MORE THAN ONE DELYTA
		Variable sv = pc.getVariable(myVar);
		if (sv==null) 
			Log.e("nils","Storedvariable null for variable "+myVar+" in WF_DisplayValueField");
		else
			((TextView)this.getWidget()).setText(sv.getValue()+(unit==Unit.nd?"":VarIdentifier.getPrintedUnit(unit)));
	}
	
	
	

}
