package com.teraim.nils.dynamic.blocks;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.teraim.nils.GlobalState;
import com.teraim.nils.R;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.dynamic.workflow_abstracts.Container;
import com.teraim.nils.dynamic.workflow_abstracts.EventGenerator;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.dynamic.workflow_realizations.WF_DisplayValueField;
import com.teraim.nils.dynamic.workflow_realizations.WF_Event_OnSave;

public class DisplayValueBlock extends Block implements EventGenerator {

	private static final long serialVersionUID = 9151756426062334462L;
	String namn, type, label,variable, containerId;
	Unit unit;
	GlobalState gs;
	
	public DisplayValueBlock(String namn, String type, String label,Unit unit,
			String variable, String containerId) {
		this.unit=unit;
		this.namn=namn;
		this.type=type;
		this.label=label;
		this.variable=variable;
		this.containerId=containerId;
	}

	public void create(final WF_Context myContext) {
		gs = GlobalState.getInstance(myContext.getContext());
		o=gs.getLogger();
		Container myContainer = myContext.getContainer(containerId);
		final Context ctx = myContext.getContext();
		TextView tv = (TextView) LayoutInflater.from(ctx).inflate(R.layout.display_value_textview,null);
		WF_DisplayValueField vf = new WF_DisplayValueField(namn,tv,variable,myContext,unit);
		myContainer.add(vf);
		vf.onEvent(new WF_Event_OnSave(null));
	}
}
