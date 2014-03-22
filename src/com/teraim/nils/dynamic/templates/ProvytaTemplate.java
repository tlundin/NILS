package com.teraim.nils.dynamic.templates;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.teraim.nils.ParameterSafe;
import com.teraim.nils.R;
import com.teraim.nils.dynamic.Executor;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.workflow_realizations.WF_Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_DisplayValueField;
import com.teraim.nils.dynamic.workflow_realizations.WF_SimpleCounter;
import com.teraim.nils.ui.MenuActivity;
import com.teraim.nils.ui.RutaAdapter;
import com.teraim.nils.utils.PersistenceHelper;
import com.teraim.nils.utils.Tools;


public class ProvytaTemplate extends Executor implements OnGesturePerformedListener {
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
		myContext.onResume();
		myLayouts = new ArrayList<WF_Container>();
		Log.d("nils","in onCreateView of provyta_template");
		myContainer = container;
		View v = inflater.inflate(R.layout.template_provyta_wf, container, false);	
		WF_Container root = new WF_Container("root", (LinearLayout)v.findViewById(R.id.root), null);
		ListView selectedList = (ListView)v.findViewById(R.id.SelectedL);
		ViewGroup aggregatePanel = (LinearLayout)v.findViewById(R.id.aggregates);
		ViewGroup fieldListPanel = (LinearLayout)v.findViewById(R.id.fieldList);
		
		final Spinner pySpinner = (Spinner)fieldListPanel.findViewById(R.id.pySpinner);
		myLayouts.add(root);
		myLayouts.add(new WF_Container("Field_List_panel_1", fieldListPanel , root));
		myLayouts.add(new WF_Container("Aggregation_panel_3", aggregatePanel, root));
		myLayouts.add(new WF_Container("Filter_panel_4", (LinearLayout)v.findViewById(R.id.filterPanel), root));
		myLayouts.add(new WF_Container("Field_List_panel_2", (FrameLayout)v.findViewById(R.id.Selected), root));
		myContext.addContainers(getContainers());

		
		WF_DisplayValueField rSel = new WF_DisplayValueField("whatevar", "Current_Ruta",myContext, null, 
				"Vald Ruta", true,null);
		
		WF_SimpleCounter aggNo = new WF_SimpleCounter("avslutade_rutor", "Avslutade provytor", "Avslutade provytor",
				myContext, true);
		
		//WF_ClickableField_Selection aggNo = new WF_ClickableField_Selection_OnSave("Avslutade Rutor:", "De rutor ni avslutat",
		//		myContext, "AvslRutor",true);
		Button navi = (Button)fieldListPanel.findViewById(R.id.naviButton);
		Button gron = (Button)fieldListPanel.findViewById(R.id.gronB);
		navi.setOnClickListener(new OnClickListener() {
			//TODO: CHANGE TO CORRECT GPS
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+50.782727+","+(-2.994937)));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
				
		aggregatePanel.addView(rSel.getWidget());
		aggregatePanel.addView(aggNo.getWidget());
	    
	    GestureOverlayView gestureOverlayView = (GestureOverlayView)v.findViewById(R.id.gesture_overlay);
	     
	    gestureOverlayView.setGestureVisible(false);
	    gestureOverlayView.addOnGesturePerformedListener(this);
	    gestureLib = GestureLibraries.fromRawResource(this.getActivity(), R.raw.gestures);
	    if (!gestureLib.load()) {      	
	    	        Log.i("nils", "Load gesture libraries failed.");  
	    	    }  
	    
	    
	    final List<Integer> prevProvytor = ps.getPrevYtor();
	    
	    List<String> provytor = new ArrayList<String>();
	    final Variable pyv = gs.getArtLista().getVariableInstance("Current_Provyta");	    
	    String[] opt = Tools.generateList(gs, pyv);	    
	    for (String s:opt) 
	    	provytor.add(s);
	    
	    ArrayAdapter<String> adp1=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,provytor);
	    adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    pySpinner.setAdapter(adp1);
	    final RutaAdapter selectedListA = new RutaAdapter(this.getActivity(), R.layout.ruta_list_row, prevProvytor);
	    selectedList.setAdapter(selectedListA);	
	    
	    
	    pySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                String pi=pySpinner.getSelectedItem().toString();
                if (pi.equals(pyv.getValue()))
					Log.d("nils","Samma provyta vald - ingen ändring");
                else {
			 
						pyv.setValue(pi);
						prevProvytor.add(0, Integer.parseInt(pi));
						selectedListA.notifyDataSetChanged();
						gs.sendEvent(MenuActivity.REDRAW);
					}	
				
			}
            

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
	    
	   final EditText vg = (EditText)inflater.inflate(R.layout.gron_lapp, null);
	    
	    gron.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
				alert.setTitle("Grön Lapp");
				alert.setMessage("Berätta om den här provytan!");
				alert.setPositiveButton("Spara", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {				  
						
					}
				});
				alert.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						
					}
				});	
			
				Dialog d = alert.setView(vg).create();
				d.setCancelable(false);
				d.show();
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