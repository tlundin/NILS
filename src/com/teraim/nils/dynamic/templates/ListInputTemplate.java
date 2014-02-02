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
import com.teraim.nils.dynamic.workflow_realizations.WF_ListSorter;


public class ListInputTemplate extends Executor {
	private LinearLayout sortPanel;
	List<WF_Container> myLayouts = new ArrayList<WF_Container>();
	private WF_ListSorter a_o_widget,familj_widget;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.template_list_input_wf);
		//add the container for the field list.
		WF_Container root = new WF_Container("root", (LinearLayout)findViewById(R.id.root), null);
		sortPanel = (LinearLayout)findViewById(R.id.sortPanel);

		myLayouts.add(root);
		myLayouts.add(new WF_Container("Field_List_panel_1", (LinearLayout)findViewById(R.id.fieldList), root));
		myLayouts.add(new WF_Container("Sort_Panel_1", sortPanel, root));
		myLayouts.add(new WF_Container("Aggregation_Panel", (LinearLayout)findViewById(R.id.aggregates), root));
		myLayouts.add(new WF_Container("Filter_Panel_1", (LinearLayout)findViewById(R.id.filterPanel), root));
		myContext.addContainers(getContainers());

		//Create blocks for template functions.
		ListSortingBlock a_o = new ListSortingBlock("alphanumeric_sorting_function","Sort_Panel_1","Field_list_1");
		ListSortingBlock slakt = new ListSortingBlock("familje_sorting_function","Sort_Panel_1","Field_list_1");
	
		if (wf!=null)
			execute();
		
		a_o_widget = a_o.create(myContext);
		familj_widget = slakt.create(myContext);
	}
	@Override
	protected List<WF_Container> getContainers() {
		return myLayouts;
	}
	
	public void execute(String name) {
		
		if (name.equals("template_function_show_sorter"))
			toggleSorter(true);
		else if (name.equals("template_function_hide_sorter"))
			toggleSorter(false);
		else if (name.equals("template_function_show_familjer"))
			toggleFamiljer(true);
		else if (name.equals("template_function_hide_familjer"))
			toggleFamiljer(false);
		else if (name.equals("template_function_hide_edited"))
			toggleHideEdited(true);
		else if (name.equals("template_function_show_edited"))
			toggleHideEdited(false);

	}
	
	private void toggleHideEdited(boolean b) {
	}
	
	private void toggleFamiljer(boolean b) {
		if (b)
			sortPanel.addView(familj_widget.getWidget());
		else {
			sortPanel.removeView(familj_widget.getWidget());
			familj_widget.removeExistingFilter();
		}
			
		
	}
	private void toggleSorter(boolean b) {
		if (b)
			sortPanel.addView(a_o_widget.getWidget());
		else {
			sortPanel.removeView(a_o_widget.getWidget());
			a_o_widget.removeExistingFilter();
			
		}
	}









}