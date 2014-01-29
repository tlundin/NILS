package com.teraim.nils.flowtemplates;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.teraim.nils.DataTypes.WF_Container;
import com.teraim.nils.DataTypes;
import com.teraim.nils.R;


public class ListInputTemplate extends Executor {
	LinearLayout fieldBg,sortBg;
	List<WF_Container> myLayouts = new ArrayList<WF_Container>();
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.template_list_input_wf);
		//add the container for the field list.
		WF_Container root = DataTypes.getSingleton().new WF_Container("root", (LinearLayout)findViewById(R.id.root), null);
		myLayouts.add(root);
		myLayouts.add(DataTypes.getSingleton().new WF_Container("Field_List_panel_1", (LinearLayout)findViewById(R.id.fieldList), root));
		myLayouts.add(DataTypes.getSingleton().new WF_Container("Sort_Panel_1", (LinearLayout)findViewById(R.id.sortPanel), root));
		myLayouts.add(DataTypes.getSingleton().new WF_Container("Aggregation_Panel", (LinearLayout)findViewById(R.id.aggregates), root));
		myLayouts.add(DataTypes.getSingleton().new WF_Container("Filter_Panel_1", (LinearLayout)findViewById(R.id.filterPanel), root));
		
		
		if (wf!=null)
			execute();
	}
	@Override
	protected List<WF_Container> getContainers() {
		return myLayouts;
	}
	









}