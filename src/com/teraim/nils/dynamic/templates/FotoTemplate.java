package com.teraim.nils.dynamic.templates;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.teraim.nils.R;
import com.teraim.nils.dynamic.Executor;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.workflow_realizations.WF_Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_Event_OnSave;
import com.teraim.nils.non_generics.Constants;
import com.teraim.nils.utils.Geomatte;
import com.teraim.nils.utils.ImageHandler;


public class FotoTemplate extends Executor implements OnGesturePerformedListener, LocationListener {

	List<WF_Container> myLayouts;
	
	private GestureLibrary gestureLib;
	private ToggleButton gpsB;
	private TextView gpsT,GPS_X,GPS_Y;
	private boolean fixed = false;
	private TextView GPS_Acc;
	private ImageButton norr;
	private ImageButton syd;
	private ImageButton ost;
	private ImageButton vast;
	private ImageButton sp;
	private ImageHandler imgHandler;
	private LocationManager lm;
	private HashMap<String, ImageButton> buttonM = new HashMap<String,ImageButton>();
	private double[] cords;
	private ToggleButton avstandB;
	private TextView norrSken;
	private TextView vastT;
	private TextView ostT;
	private TextView spT;
	private TextView sydT;

	private View v;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		myContext.emptyContianers();
		Log.d("nils","in onCreateView of foto template");
		
		v = inflater.inflate(R.layout.template_foto, container, false);	

		//		myLayouts.add(new WF_Container("Field_List_panel_1", (LinearLayout)v.findViewById(R.id.fieldList), root));
		//		myLayouts.add(new WF_Container("Aggregation_panel_3", (LinearLayout)v.findViewById(R.id.aggregates), root));
		//		myLayouts.add(new WF_Container("Description_panel_1", (FrameLayout)v.findViewById(R.id.Description), root));
		myContext.addContainers(getContainers());


		//Gestures
		GestureOverlayView gestureOverlayView = (GestureOverlayView)v.findViewById(R.id.gesture_overlay);
		gestureOverlayView.setGestureVisible(false);
		gestureOverlayView.addOnGesturePerformedListener(this);
		gestureLib = GestureLibraries.fromRawResource(this.getActivity(), R.raw.gestures);
		if (!gestureLib.load()) {      	
			Log.i("nils", "Load gesture libraries failed.");  
		}  



		if (wf!=null) {
			run();
		}		
		avstandB = (ToggleButton)v.findViewById(R.id.avstandB);
		gpsB = (ToggleButton)v.findViewById(R.id.gpsBtn);
		gpsT = (TextView)v.findViewById(R.id.gpsText);
		GPS_X = (TextView)v.findViewById(R.id.GPS_X);
		GPS_Y = (TextView)v.findViewById(R.id.GPS_Y);
		GPS_Acc = (TextView)v.findViewById(R.id.Accuracy);
		norr = (ImageButton)v.findViewById(R.id.pic_norr);
		syd = (ImageButton)v.findViewById(R.id.pic_soder);
		ost= (ImageButton)v.findViewById(R.id.pic_ost);
		vast = (ImageButton)v.findViewById(R.id.pic_vast);
		sp= (ImageButton)v.findViewById(R.id.sp);
		norrSken = (TextView)v.findViewById(R.id.norrSken);
		
		vastT= (TextView)v.findViewById(R.id.vastT);
		ostT= (TextView)v.findViewById(R.id.ostT);
		spT= (TextView)v.findViewById(R.id.spT);
		sydT= (TextView)v.findViewById(R.id.sydT);
		buttonM.clear();
		gpsT.setText("");
		avstand = false;
		//Init pic handler
		imgHandler = new ImageHandler(gs,this);		
		//Init geoupdate
		lm = (LocationManager)this.getActivity().getSystemService(Context.LOCATION_SERVICE);
		//Create buttons.
		initPic(norr,"NORR");
		initPic(syd,"SYD");
		initPic(ost,"OST");
		initPic(vast,"VAST");
		initPic(sp,"SMA");
		File folder = new File(Constants.PIC_ROOT_DIR);
		if(!folder.mkdirs())
			Log.e("NILS","Failed to create pic root folder");

		gpsB.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (gpsB.isChecked()) {
					if (!setStartPoint())
						gpsB.setChecked(false);
				} else 
					fixed = false;					
				
			}
		});

		avstandB.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggleAvstand();
			}
		});


		return v;

	}
	
	boolean avstand=false;
	private void toggleAvstand() {
		avstand = !avstand;
		int status = avstand?View.INVISIBLE:View.VISIBLE;
		norrSken.setText(avstand?"AVSTÅNDSBILD":"NORR");
		syd.setVisibility(status);
		ost.setVisibility(status);
		vast.setVisibility(status);
		sp.setVisibility(status);
		gpsB.setVisibility(status);
		gpsT.setVisibility(status);
		GPS_X.setVisibility(status);
		GPS_Y.setVisibility(status);
		GPS_Acc.setVisibility(status);
		vastT.setVisibility(status);
		ostT.setVisibility(status);
		spT.setVisibility(status);
		sydT.setVisibility(status);
		
	}
	

	private void initPic(final ImageButton b, final String name) {
		buttonM.put(name, b);
		imgHandler.drawButton(b,name);
		imgHandler.addListener(b,name);
	}

	@Override
	public void onPause() {
		super.onPause();
		lm.removeUpdates(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		//---request for location updates---
		lm.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				0,
				1,
				this);

	}




	@Override
	public void onStart() {
		super.onStart();
		if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
			startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
		Log.d("nils","in onStart foto");

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent myI){
		Log.d("nils","Gets to onActivityResult");

		if (requestCode == ImageHandler.TAKE_PICTURE){
			if (resultCode == Activity.RESULT_OK) 
			{
				Log.d("Strand","picture was taken, result ok");
				//				String name = myI.getStringExtra(Strand.KEY_PIC_NAME);
				String currSaving = imgHandler.getCurrentlySaving();
				if (currSaving!=null) {
					ImageButton b = buttonM.get(currSaving);
					imgHandler.drawButton(b,currSaving);
					Log.d("Strand","Drew button!");
				} else
					Log.e("Strand","Did not find pic with name "+currSaving+" in onActRes in TakePic Activity");
			} else {
				Log.d("Strand","picture was NOT taken, result NOT ok");
			}

		}

	}



	@Override
	protected List<WF_Container> getContainers() {
		myLayouts = new ArrayList<WF_Container>();
		myLayouts.add(new WF_Container("root", (RelativeLayout)v.findViewById(R.id.root), null));
		return myLayouts;
	}

	public void execute(String name, String target) {

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
					OldPhotosFragment gs = new OldPhotosFragment();  			
					ft.replace(R.id.content_frame, gs);
					ft.addToBackStack(null);
					ft.commit(); 
				} else 
					Toast.makeText(getActivity(), "bättre kan du", Toast.LENGTH_SHORT).show();

			}
		}		
	}


	@Override
	public void onLocationChanged(Location location) {
		//If no startpunkt set, update button
		if (!fixed) {
			cords = Geomatte.convertToSweRef(location.getLatitude(),location.getLongitude());
			gpsB.setVisibility(View.VISIBLE);
			GPS_X.setText(location.getLatitude()+"");
			GPS_Y.setText(location.getLongitude()+"");
			GPS_Acc.setText(location.getAccuracy()+"");
			//gpsB.setText("Sätt startpunkt\n(N: "+cords[0]+" \nÖ: "+cords[1]+")");
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		gpsB.setVisibility(View.GONE);
		gpsT.setText("GPS avslagen.");
	}
	@Override
	public void onProviderEnabled(String arg0) {
		gpsT.setText("GPS påslagen. Söker..");
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle arg2) {
		if (status == LocationProvider.AVAILABLE) {
			gpsT.setText("GPS signal ok");
		} else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			gpsB.setVisibility(View.GONE);
			gpsT.setText("Söker GPS..");


		} else if (status == LocationProvider.OUT_OF_SERVICE) {
			gpsB.setVisibility(View.GONE);
			gpsT.setText("GPS tjänst förlorad");

		}


	}

	public boolean setStartPoint() {	



		if(cords!=null) {
			VariableConfiguration al = gs.getArtLista();
			Variable e = al.getVariableInstance("CenGPSNo");
			Variable w = al.getVariableInstance("CenGPSOs");
			e.setValue(cords[1]+"");
			w.setValue(cords[0]+"");
			gpsT.setText("Startpunkt satt till:\nN: "+cords[0]+" Ö: "+cords[1]);
			fixed=true;
			myContext.registerEvent(new WF_Event_OnSave("fototemplate"));
			return true;
		} else {
			gpsT.setText("Kan inte sätta ett värde! Ingen GPS signal.");
			return false;
		}
	}



}