package com.teraim.nils.dynamic.templates;

import java.util.ArrayList;
import java.util.List;

import android.app.FragmentTransaction;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.teraim.nils.R;
import com.teraim.nils.dynamic.Executor;
import com.teraim.nils.dynamic.workflow_realizations.WF_Container;
import com.teraim.nils.ui.FixPunktFragment;
import com.teraim.nils.utils.PersistenceHelper;


public class ProvytaTemplate extends Executor implements OnGesturePerformedListener {
	List<WF_Container> myLayouts;


	
	
	
	/* (non-Javadoc)
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	ViewGroup myContainer = null;
	private GestureLibrary gestureLib;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		myContext.onResume();
		myLayouts = new ArrayList<WF_Container>();
		Log.d("nils","in onCreateView of provyta_template");
		myContainer = container;
		View v = inflater.inflate(R.layout.template_list_input_wf, container, false);	
		WF_Container root = new WF_Container("root", (LinearLayout)v.findViewById(R.id.root), null);
		myLayouts.add(root);
		myLayouts.add(new WF_Container("Field_List_panel_1", (LinearLayout)v.findViewById(R.id.fieldList), root));
		myLayouts.add(new WF_Container("Aggregation_panel_3", (LinearLayout)v.findViewById(R.id.aggregates), root));
		myLayouts.add(new WF_Container("Filter_panel_4", (LinearLayout)v.findViewById(R.id.filterPanel), root));
		myLayouts.add(new WF_Container("Field_List_panel_2", (LinearLayout)v.findViewById(R.id.Selected), root));
		myContext.addContainers(getContainers());

		
		//Gestures
	    /*
	    GestureOverlayView gestureOverlayView = (GestureOverlayView)v.findViewById(R.id.gesture_overlay);
	     
	    gestureOverlayView.setGestureVisible(false);
	    gestureOverlayView.addOnGesturePerformedListener(this);
	    gestureLib = GestureLibraries.fromRawResource(this.getActivity(), R.raw.gestures);
	    if (!gestureLib.load()) {      	
	    	        Log.i("nils", "Load gesture libraries failed.");  
	    	    }  
		*/
		
		
		if (wf!=null) {
			run();
		}		
		return v;

	}



	/* (non-Javadoc)
	 * @see android.app.Fragment#onStart()
	 */
	@Override
	public void onStart() {
		super.onStart();

		Log.d("nils","in onStart provyta_template");
	
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
	  			FixPunktFragment gs = new FixPunktFragment();  			
	  			ft.replace(R.id.content_frame, gs);
	  			ft.addToBackStack(null);
	  			ft.commit(); 
	  		} else 
				Toast.makeText(getActivity(), "bättre kan du", Toast.LENGTH_SHORT).show();
	  			
	      }
	    }		
	}






}