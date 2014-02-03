package com.teraim.nils.dynamic.templates;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.teraim.nils.R;
import com.teraim.nils.dynamic.Executor;
import com.teraim.nils.dynamic.blocks.ListSortingBlock;
import com.teraim.nils.dynamic.workflow_realizations.WF_Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_SorterWidget;


public class ListInputTemplate extends Executor {
	private LinearLayout sortPanel;
	List<WF_Container> myLayouts = new ArrayList<WF_Container>();
	private WF_SorterWidget a_o_widget,familj_widget;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.template_list_input_wf);
		//add the container for the field list.
		WF_Container root = new WF_Container("root", (LinearLayout)findViewById(R.id.root), null);
		sortPanel = (LinearLayout)findViewById(R.id.sortPanel);

		myLayouts.add(root);
		myLayouts.add(new WF_Container("Field_List_panel_1", (LinearLayout)findViewById(R.id.fieldList), root));
		myLayouts.add(new WF_Container("Sort_Panel_1", sortPanel, root));
		myLayouts.add(new WF_Container("Aggregation_panel_3", (LinearLayout)findViewById(R.id.aggregates), root));
		myLayouts.add(new WF_Container("Filter_panel_4", (LinearLayout)findViewById(R.id.filterPanel), root));
		myLayouts.add(new WF_Container("Field_List_panel_2", (LinearLayout)findViewById(R.id.Selected), root));
		myContext.addContainers(getContainers());

		//Create blocks for template functions.
		ListSortingBlock a_o = new ListSortingBlock("alphanumeric_sorting_function","Sort_Panel_1","Field_list_1");
		ListSortingBlock slakt = new ListSortingBlock("familje_sorting_function","Sort_Panel_1","Field_list_1");

		if (wf!=null) {
			run();

			a_o_widget = a_o.create(myContext);
			familj_widget = slakt.create(myContext);
		}
	}
	@Override
	protected List<WF_Container> getContainers() {
		return myLayouts;
	}

	public void execute(String name) {

		if (name.equals("template_function_show_sorter"))
			toggleSorter();
		else if (name.equals("template_function_show_familjer"))
			toggleFamiljer();

	}

	private boolean toggleStateF = true;
	private void toggleFamiljer() {
		if (toggleStateF)
			sortPanel.addView(familj_widget.getWidget());
		else {
			sortPanel.removeView(familj_widget.getWidget());
			familj_widget.removeExistingFilter();
		}
		toggleStateF = !toggleStateF;

	}
	boolean toggleStateS = true;
	private void toggleSorter() {
		if (toggleStateS) 
			sortPanel.addView(a_o_widget.getWidget());
			
		else {
			sortPanel.removeView(a_o_widget.getWidget());
			a_o_widget.removeExistingFilter();
		}
		toggleStateS=!toggleStateS;
	}









}