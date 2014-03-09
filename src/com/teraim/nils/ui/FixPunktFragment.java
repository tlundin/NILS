package com.teraim.nils.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.Fragment;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
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
import com.teraim.nils.dynamic.types.Marker;
import com.teraim.nils.dynamic.types.Variable;

public class FixPunktFragment extends Fragment implements OnGesturePerformedListener {

	final String[] variables = new String[] {"fixpunkter_FixPunkt1_avstand",
		"fixpunkter_FixPunkt1_riktning","fixpunkter_FixPunkt2_avstand",
		"fixpunkter_FixPunkt2_riktning","fixpunkter_FixPunkt3_avstand",
		"fixpunkter_FixPunkt3_riktning",
	};
	final Set<FixPunkt>fixPunkter=new HashSet<FixPunkt>();
	GlobalState gs;
	private GestureLibrary gestureLib;
	protected Marker[] markers;
	
	private class FixPunkt {
		public FixPunkt(Variable avst, Variable rikt) {
			this.avst=avst;
			this.rikt=rikt;
		}
		Variable avst;
		Variable rikt;
	}
	
	final int png[] = new int[] {R.drawable.fixpunkt,R.drawable.fixpunkt,R.drawable.fixpunkt};
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		gs = GlobalState.getInstance(getActivity());
		Log.d("nils","in onCreateView of fixpunkt_fragment");
		View v = inflater.inflate(R.layout.template_fixpunkt_right, container, false);	
		
	
		final FrameLayout fl = (FrameLayout)v.findViewById(R.id.circle);
		
		FixytaView fyv = new FixytaView(getActivity(),null);		
		
		//Create markers
		Bitmap bm;
		Bitmap scaled;		
		int h = 48; // height in pixels
		int w = 48; // width in pixels  
		markers = new Marker[3];
		
		for(int i=0;i<3;i++) {
			bm = BitmapFactory.decodeResource(getResources(), png[i]);
			scaled = Bitmap.createScaledBitmap(bm, h, w, true);
			markers[i] = new Marker(scaled);
			Variable avst,rikt;
			String avstKey,riktKey;
			avstKey = variables[i*2];
			riktKey = variables[i*2+1];
			avst = gs.getArtLista().getVariableInstance(avstKey);
			rikt = gs.getArtLista().getVariableInstance(riktKey);
			if (avst!=null && rikt !=null)
				fixPunkter.add(new FixPunkt(avst,rikt));			
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
		for (FixPunkt f:fixPunkter)
			markers[j++].setValue(f.avst.getValue(),f.rikt.getValue());		
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
