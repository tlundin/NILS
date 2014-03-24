package com.teraim.nils.dynamic.templates;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.teraim.nils.ParameterSafe;
import com.teraim.nils.R;
import com.teraim.nils.dynamic.Executor;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.workflow_realizations.WF_Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_SimpleCounter;
import com.teraim.nils.ui.MenuActivity;
import com.teraim.nils.ui.RutaAdapter;
import com.teraim.nils.utils.DbHelper;
import com.teraim.nils.utils.DbHelper.Selection;
import com.teraim.nils.utils.PersistenceHelper;


public class RutaTemplate extends Executor implements OnGesturePerformedListener {
	List<WF_Container> myLayouts;


	
	
	
	/* (non-Javadoc)
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	ViewGroup myContainer = null;
	private GestureLibrary gestureLib;
	private PersistenceHelper ph;
	private ParameterSafe ps;



	private double[] cords;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ps = gs.getSafe();
		myContext.emptyContianers();
		myLayouts = new ArrayList<WF_Container>();
		Log.d("nils","in onCreateView of provyta_template");
		myContainer = container;
		View v = inflater.inflate(R.layout.template_ruta_wf, container, false);	
		WF_Container root = new WF_Container("root", (LinearLayout)v.findViewById(R.id.root), null);
		ListView fieldList = (ListView)v.findViewById(R.id.fieldListL);
		ListView selectedList = (ListView)v.findViewById(R.id.SelectedL);
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
	    
	    GestureOverlayView gestureOverlayView = (GestureOverlayView)v.findViewById(R.id.gesture_overlay);
	     
	    gestureOverlayView.setGestureVisible(false);
	    gestureOverlayView.addOnGesturePerformedListener(this);
	    gestureLib = GestureLibraries.fromRawResource(this.getActivity(), R.raw.gestures);
	    if (!gestureLib.load()) {      	
	    	        Log.i("nils", "Load gesture libraries failed.");  
	    	    }  
	    
	    
	    final List<Integer> prevRutor = ps.getPrevRutor();
	    
	    List<Integer> rutor = new ArrayList<Integer>();
	    TreeSet<Integer> rutorS = new TreeSet<Integer>();
	    DbHelper db = gs.getDb();
		List<String[]> values = db.getValues(new String[] {db.getColumnName("ruta")}, new Selection());
	    for (String[] val:values)
	    	rutorS.add(Integer.parseInt(val[0]));
	    rutor.addAll(rutorS);
	    
	    RutaAdapter customAdapter = new RutaAdapter(this.getActivity(), R.layout.ruta_list_row, rutor);
	    final RutaAdapter selectedListA = new RutaAdapter(this.getActivity(), R.layout.ruta_list_row, prevRutor);
	    fieldList.setAdapter(customAdapter);
	    selectedList.setAdapter(selectedListA);
	    fieldList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				
				final String pi = ((TextView)view.findViewById(R.id.header)).getText().toString();
				 final Variable var = gs.getArtLista().getVariableInstance("Current_Ruta");
				 if (pi.equals(var.getValue()))
							Log.d("nils","Samma ruta vald - ingen ändring");
				 else {
					 new AlertDialog.Builder(RutaTemplate.this.getActivity())
						.setTitle("Byta Ruta")
						.setMessage("Är du säker att du vill byta till ruta "+pi+"?") 
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setCancelable(false)
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which)  {
								var.setValue(pi);
								prevRutor.add(0, Integer.parseInt(pi));
								selectedListA.notifyDataSetChanged();
								gs.sendEvent(MenuActivity.REDRAW);
							}

						})
						.setNegativeButton(android.R.string.no,new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {}} ) 
							.show();
					
					
					
				 }
						
						
					}
				});
			
				
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



	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
	    Log.d("nils","Number of gestures available: "+gestureLib.getGestureEntries().size());
	    ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
	    Log.d("nils","Number of predictions: "+predictions.size());
	    for (Prediction prediction : predictions) {
	      if (prediction.score > .5) {
	  		Log.d("nils","MATCH!!");
	  		if (prediction.name.equals("left")) {
	  			final FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction(); 
	  			Fragment gs = new Fragment();  			
	  			ft.replace(R.id.content_frame, gs);
	  			ft.addToBackStack(null);
	  			ft.commit(); 
	  		} else 
				Toast.makeText(getActivity(), "Fel håll", Toast.LENGTH_SHORT).show();
	  			
	      }
	    }		
	}




	






}