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
import com.teraim.nils.dynamic.blocks.ListSortingBlock;
import com.teraim.nils.dynamic.workflow_abstracts.Filter;
import com.teraim.nils.dynamic.workflow_realizations.WF_Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_List;
import com.teraim.nils.dynamic.workflow_realizations.WF_OnlyWithoutValue_Filter;
import com.teraim.nils.dynamic.workflow_realizations.WF_SorterWidget;


public class ListInputTemplate extends Executor {
	public static String FIELD_LIST = "Field_list_1";
	private LinearLayout sortPanel;
	List<WF_Container> myLayouts;
	private WF_SorterWidget a_o_widget,familj_widget;

	
	
	
	/* (non-Javadoc)
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	ViewGroup myContainer = null;
	ListSortingBlock a_o,slakt;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		myContext.onResume();
		myLayouts = new ArrayList<WF_Container>();
		Log.d("nils","in onCreateView");
		myContainer = container;
		View v = inflater.inflate(R.layout.template_list_input_wf, container, false);	
		WF_Container root = new WF_Container("root", (LinearLayout)v.findViewById(R.id.root), null);
		sortPanel = (LinearLayout)v.findViewById(R.id.sortPanel);

		myLayouts.add(root);
		myLayouts.add(new WF_Container("Field_List_panel_1", (LinearLayout)v.findViewById(R.id.fieldList), root));
		myLayouts.add(new WF_Container("Sort_Panel_1", sortPanel, root));
		myLayouts.add(new WF_Container("Aggregation_panel_3", (LinearLayout)v.findViewById(R.id.aggregates), root));
		myLayouts.add(new WF_Container("Filter_panel_4", (LinearLayout)v.findViewById(R.id.filterPanel), root));
		myLayouts.add(new WF_Container("Field_List_panel_2", (LinearLayout)v.findViewById(R.id.Selected), root));
		myContext.addContainers(getContainers());
		a_o = new ListSortingBlock("alphanumeric_sorting_function","Sort_Panel_1",FIELD_LIST);
		slakt = new ListSortingBlock("familje_sorting_function","Sort_Panel_1",FIELD_LIST);

		if (wf!=null) {
			run();
		}
		a_o_widget = a_o.create(myContext);
		familj_widget = slakt.create(myContext);
		
		return v;

	}



	/* (non-Javadoc)
	 * @see android.app.Fragment#onStart()
	 */
	@Override
	public void onStart() {
		super.onStart();

		Log.d("nils","in onStart");
		//myContainer.removeAllViews();
		//Create blocks for template functions.
	
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
		else if (name.equals("template_function_hide_edited"))
			hideEdited();

	}

	Filter f = new WF_OnlyWithoutValue_Filter();
	private boolean toggleStateH = true;
	private void hideEdited() {
		final WF_List fieldList = (WF_List)myContext.getFilterable(FIELD_LIST);
		if (toggleStateH) {
			fieldList.addFilter(f);
		} else
			fieldList.removeFilter(f);
		fieldList.draw();
		toggleStateH = !toggleStateH;
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