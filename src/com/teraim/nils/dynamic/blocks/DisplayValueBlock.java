package com.teraim.nils.dynamic.blocks;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

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
	String namn,  label,formula, containerId,format;
	boolean isVisible = false;
	Unit unit;
	GlobalState gs;
	
	public DisplayValueBlock(String namn, String label,Unit unit,
			String formula, String containerId,boolean isVisible,String format) {
		this.unit=unit;
		this.namn=namn;;
		this.label=label;
		this.formula=formula;
		this.containerId=containerId;
		this.isVisible=isVisible;
		this.format=format;
	}

	public void create(final WF_Context myContext) {
		gs = GlobalState.getInstance(myContext.getContext());
		o=gs.getLogger();
		Container myContainer = myContext.getContainer(containerId);
		if (myContainer != null) {
		final Context ctx = myContext.getContext();
		LinearLayout tv = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.display_value_textview,null);
		WF_DisplayValueField vf = new WF_DisplayValueField(namn,tv,formula,myContext,unit,label,isVisible,format);
		myContainer.add(vf);
		vf.onEvent(new WF_Event_OnSave(null));
		} else {
			Log.d("nils","ContainerID: "+containerId);
			o.addRow("");
			o.addRedText("Could not find container for DisplayValueBlcok with name (container): "+containerId+" (block): "+namn);
		}
			
	}
}
