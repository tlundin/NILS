package com.teraim.nils.dynamic.blocks;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout.LayoutParams;
import android.widget.ToggleButton;

import com.teraim.nils.dynamic.workflow_abstracts.Container;
import com.teraim.nils.dynamic.workflow_abstracts.Filterable;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.dynamic.workflow_realizations.WF_OnlyWithoutValue_Filter;
import com.teraim.nils.dynamic.workflow_realizations.WF_Widget;

public class ListFilterBlock extends Block {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6044282434929671356L;
	String containerId,type,target,label,function;

	public ListFilterBlock(String containerId, String type, String target,
			String label, String function) {
		super();
		this.containerId = containerId;
		this.type = type;
		this.target = target;
		this.label = label;
		this.function = function;
	}

	public void create(final WF_Context myContext) {
		Container myContainer = myContext.getContainer(containerId);
		if (myContainer == null) 
			return;
		final Context ctx = myContext.getContext();
		ToggleButton toggleB = new ToggleButton(ctx);
		toggleB.setTextOn(label+" på");
		toggleB.setTextOff(label+" av");
		toggleB.setChecked(false);
		LayoutParams params = new LayoutParams();
		params.width = LayoutParams.MATCH_PARENT;
		params.height = LayoutParams.WRAP_CONTENT;
		toggleB.setTextSize(15);
		toggleB.setLayoutParams(params);
		
		toggleB.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean on = ((ToggleButton) v).isChecked();
				if (on) {
					Filterable f = myContext.getFilterable(target);
					//WF_Kvar_Filter f = ;
					f.addFilter(new WF_OnlyWithoutValue_Filter());
				}
			}
		});
		
		
		myContainer.add(new WF_Widget(label,toggleB));

		
	}
	
	
}
