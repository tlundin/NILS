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
import android.widget.ListView;

import com.teraim.nils.ParameterSafe;
import com.teraim.nils.R;
import com.teraim.nils.dynamic.Executor;
import com.teraim.nils.dynamic.workflow_realizations.WF_Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_SimpleCounter;
import com.teraim.nils.utils.PersistenceHelper;


public class LinjePortalTemplate extends Executor  {
	List<WF_Container> myLayouts;


	ViewGroup myContainer = null;
	private PersistenceHelper ph;
	private ParameterSafe ps;
	private double[] cords;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ps = gs.getSafe();
		myContext.onResume();
		myLayouts = new ArrayList<WF_Container>();
		Log.d("nils","in onCreateView of provyta_template");
		myContainer = container;
		View v = inflater.inflate(R.layout.template_ruta_wf, container, false);	
		WF_Container root = new WF_Container("root", (LinearLayout)v.findViewById(R.id.root), null);
		LinearLayout aggregatePanel = (LinearLayout)v.findViewById(R.id.aggregates);
		myLayouts.add(root);
		myLayouts.add(new WF_Container("Field_List_panel_1", (FrameLayout)v.findViewById(R.id.fieldList), root));
		myLayouts.add(new WF_Container("Aggregation_panel_3", aggregatePanel, root));
		myLayouts.add(new WF_Container("Filter_panel_4", (LinearLayout)v.findViewById(R.id.filterPanel), root));
		myLayouts.add(new WF_Container("Field_List_panel_2", (FrameLayout)v.findViewById(R.id.Selected), root));
		myContext.addContainers(getContainers());

		
		//Gestures
		
		WF_SimpleCounter aggNo = new WF_SimpleCounter("avslutade_rutor", "Avslutade rutor", "Avslutade Rutor",
				myContext, true);
		
		//WF_ClickableField_Selection aggNo = new WF_ClickableField_Selection_OnSave("Avslutade Rutor:", "De rutor ni avslutat",
		//		myContext, "AvslRutor",true);
		aggregatePanel.addView(aggNo.getWidget());
	    final List<Integer> prevRutor = ps.getPrevRutor();

		return v;

	}



	/* (non-Javadoc)
	 * @see android.app.Fragment#onStart()
	 */
	@Override
	public void onResume() {
		super.onResume();

	}
	@Override
	public void onPause() {
		
		super.onPause();
	}



	


	@Override
	protected List<WF_Container> getContainers() {
		return myLayouts;
	}

	public void execute(String name, String target) {
		if (name.equals("template_function_export"))
			gs.getDb().export("ruta",gs.getArtLista().getVariableInstance("Current_Ruta").getValue());
	}



}