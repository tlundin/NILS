package com.teraim.nils.dynamic.blocks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout.LayoutParams;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.types.VariableConfiguration;
import com.teraim.nils.dynamic.workflow_abstracts.Container;
import com.teraim.nils.dynamic.workflow_abstracts.Filterable;
import com.teraim.nils.dynamic.workflow_realizations.WF_Column_Name_Filter;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.dynamic.workflow_realizations.WF_Filter;
import com.teraim.nils.dynamic.workflow_realizations.WF_List;
import com.teraim.nils.dynamic.workflow_realizations.WF_SorterWidget;
import com.teraim.nils.dynamic.workflow_realizations.WF_Widget;

public class ListSortingBlock extends Block {

	
	String containerId,type,target;
	Filterable targetList;
	
	

	public ListSortingBlock(String type,String containerId, String targetId) {
		this.type = type;
		this.containerId = containerId;
		this.target = targetId;
	}


	public WF_SorterWidget create(WF_Context ctx) {

		
		//Identify targetList. If no list, no game.
		Container myContainer = ctx.getContainer(containerId);
		if (myContainer == null) 
			Log.e("parser","Warning: No container defined for component ListSortingBlock");
		
		Log.d("nils","Sort target is "+target);
		targetList = ctx.getFilterable(target);
		if (targetList == null) {
			Log.e("parser","couldn't create sortwidget - could not find target list");
			return null;
		}
		else {
			
			return new WF_SorterWidget(ctx,type,((WF_List)targetList));
			//myContainer.add(new WF_Widget(buttonPanel));
		}

	}

	public void draw(Context ctx, ViewGroup container) {

	}
}