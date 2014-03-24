package com.teraim.nils.dynamic.templates;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.teraim.nils.R;
import com.teraim.nils.dynamic.Executor;
import com.teraim.nils.dynamic.workflow_realizations.WF_Container;

public class PageWithAggregationTemplate extends Executor {

	@Override
	protected List<WF_Container> getContainers() {
		myLayouts = new ArrayList<WF_Container>();
		WF_Container root = new WF_Container("root", (LinearLayout)v.findViewById(R.id.root), null);
		myLayouts.add(root);
		myLayouts.add(new WF_Container("Field_panel_1", (LinearLayout)v.findViewById(R.id.fieldList), root));
		myLayouts.add(new WF_Container("Aggregation_panel_3", (LinearLayout)v.findViewById(R.id.aggregates), root));
		myLayouts.add(new WF_Container("Description_panel_2", (LinearLayout)v.findViewById(R.id.Description), root));
		return myLayouts;
	}

	@Override
	public void execute(String function, String target) {
		// TODO Auto-generated method stub

	}

	List<WF_Container> myLayouts;
	ViewGroup myContainer = null;
	View v;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.d("nils","in onCreateView of Template PAGE with AGGregation");
		v = inflater.inflate(R.layout.template_page_with_agg_wf, container, false);	
		
		myContext.onCreateView();
		myContext.addContainers(getContainers());
		if (wf!=null) {
			run();
		}		
		return v;
	}
}
