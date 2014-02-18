package com.teraim.nils.statics;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.teraim.nils.GlobalState;
import com.teraim.nils.R;
import com.teraim.nils.dynamic.types.Delyta;
import com.teraim.nils.dynamic.types.Marker;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.ui.FixytaView;

public class FixPunktFragment extends Fragment implements OnGesturePerformedListener {

	final String[] variables = new String[] {"FixPunkt1.avstand",
		"FixPunkt1.riktning","FixPunkt2.avstand",
		"FixPunkt2.riktning","FixPunkt3.avstand",
		"FixPunkt3.riktning"};
	GlobalState gs;
	Delyta dy;
	Marker[] markers = new Marker[3];
	private GestureLibrary gestureLib;

	
	final int png[] = new int[] {R.drawable.fixpunkt,R.drawable.fixpunkt,R.drawable.fixpunkt};
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		gs = GlobalState.getInstance(getActivity());
		//TODO : Change later on.
		dy = gs.getCurrentDelyta();
		Log.d("nils","in onCreateView of fixpunkt_fragment");
		View v = inflater.inflate(R.layout.template_fixpunkt_right, container, false);	
		
	
		final FrameLayout fl = (FrameLayout)v.findViewById(R.id.circle);
		
		FixytaView fyv = new FixytaView(getActivity(),null);		
		
		//Create markers
		Bitmap bm;
		Bitmap scaled;		
		int h = 48; // height in pixels
		int w = 48; // width in pixels    
		for(int i=0;i<3;i++) {
			bm = BitmapFactory.decodeResource(getResources(), png[i]);
			scaled = Bitmap.createScaledBitmap(bm, h, w, true);
			markers[i] = new Marker(scaled);
			
		}
		
		
		fyv.setFixedMarkers(markers);
		fl.addView(fyv);
		
	    GestureOverlayView gestureOverlayView = (GestureOverlayView)v.findViewById(R.id.gesture_overlay);
	    gestureOverlayView.setGestureVisible(false);
	    gestureOverlayView.addOnGesturePerformedListener(this);
	    gestureLib = GestureLibraries.fromRawResource(this.getActivity(), R.raw.gestures);
	    if (!gestureLib.load()) {      	
	    	        Log.i("nils", "Load gesture libraries failed.");  
	    	    }  
	
	    
		
		return v;
	}
	/* (non-Javadoc)
	 * @see android.app.Fragment#onStart()
	 */
	@Override
	public void onStart() {
		int j=0;
		Variable a,r;
		for(int i = 0; i<variables.length;i+=2) {
			a = dy.getVariable(variables[i]);
			r = dy.getVariable(variables[i+1]);
			if (a!=null&&r!=null)
				markers[j++].setValue(a.getValue(),r.getValue());
		}			
		super.onStart();
	}
	
	
	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
	    Log.d("nils","Number of gestures available: "+gestureLib.getGestureEntries().size());
	    ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
	    Log.d("nils","Number of predictions: "+predictions.size());
	    for (Prediction prediction : predictions) {
	      if (prediction.score > .5) {
	  		Log.d("nils","MATCH!!");
	  		if (prediction.name.equals("right")) {
	  			getFragmentManager().popBackStackImmediate();

	  		} else 
				Toast.makeText(getActivity(), "vänster till höger", Toast.LENGTH_SHORT).show();
	  			
	      }
	    }		
	}
	
}
