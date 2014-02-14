package com.teraim.nils;

import java.util.ArrayList;

import android.app.Fragment;
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
import android.widget.Button;
import android.widget.Toast;

public abstract class GestureFragment extends Fragment implements OnGesturePerformedListener  {

	  private GestureLibrary gestureLib;
	
	
	 @Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.gesture_fragment,
	        container, false);
	    Bundle b = this.getArguments();
	    String txt = b.getString("butt");
	    Button bu = (Button)view.findViewById(R.id.button1);
	    bu.setText(txt);
	    GestureOverlayView gestureOverlayView = (GestureOverlayView)view.findViewById(R.id.gesture_overlay);
	    gestureOverlayView.setGestureVisible(false);
	    gestureOverlayView.addOnGesturePerformedListener(this);
	    gestureLib = GestureLibraries.fromRawResource(this.getActivity(), R.raw.gestures);
	    if (!gestureLib.load()) {      	
	    	        Log.i("nils", "Load gesture libraries failed.");  
	    	    }  
	    return view;
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
				onLeft();
	  		} else 
	  			onRight();
	  			
	        Toast.makeText(this.getActivity(), prediction.name, Toast.LENGTH_SHORT)
	            .show();
	      }
	    }		
	}


	protected abstract void onRight();


	protected abstract void onLeft();

	
	
}
