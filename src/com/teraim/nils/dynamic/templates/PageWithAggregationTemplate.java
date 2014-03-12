package com.teraim.nils.dynamic.templates;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.teraim.nils.R;
import com.teraim.nils.dynamic.Executor;
import com.teraim.nils.dynamic.workflow_realizations.WF_Container;

public class PageWithAggregationTemplate extends Executor {

	@Override
	protected List<WF_Container> getContainers() {
		return myLayouts;
	}

	@Override
	public void execute(String function, String target) {
		// TODO Auto-generated method stub

	}

	List<WF_Container> myLayouts;
	ViewGroup myContainer = null;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		myContext.onResume();
		myLayouts = new ArrayList<WF_Container>();
		Log.d("nils","in onCreateView of fixpunkt_template");
		myContainer = container;
		View v = inflater.inflate(R.layout.template_fixpunkt_wf, container, false);	
		WF_Container root = new WF_Container("root", (LinearLayout)v.findViewById(R.id.root), null);
		myLayouts.add(root);
		myLayouts.add(new WF_Container("Field_panel_1", (LinearLayout)v.findViewById(R.id.fieldList), root));
		myLayouts.add(new WF_Container("Aggregation_panel_3", (LinearLayout)v.findViewById(R.id.aggregates), root));
		myLayouts.add(new WF_Container("Description_panel_2", (FrameLayout)v.findViewById(R.id.Description), root));
		myContext.addContainers(getContainers());
		if (wf!=null) {
			run();
		}		
		return v;
	}
}
