package com.teraim.nils;

/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.teraim.nils.DataTypes.Provyta;
import com.teraim.nils.DataTypes.Ruta;


/**
 * This shows how to place markers on a map.
 */
public class MapSelect extends android.support.v4.app.FragmentActivity
implements OnMarkerClickListener, OnInfoWindowClickListener, OnMarkerDragListener {


	private GoogleMap mMap;
	private ArrayList<Provyta> ytor;
	private Map<Marker,Provyta> markers = new HashMap<Marker,Provyta>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selectyta);
		Ruta ruta = CommonVars.cv().getRuta();
		ytor = ruta.getAllProvYtor();


		setUpMapIfNeeded();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
					.getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	private void setUpMap() {
		// Hide the zoom controls as the button panel will cover it.
		mMap.getUiSettings().setZoomControlsEnabled(false);

		// Add lots of markers to the map.
		addMarkersToMap();

		// Setting an info window adapter allows us to change the both the contents and look of the
		// info window.
		//mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

		// Set listeners for marker events.  See the bottom of this class for their behavior.
		mMap.setOnMarkerClickListener(this);
		mMap.setOnInfoWindowClickListener(this);
		mMap.setOnMarkerDragListener(this);
		mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

		// Pan to see all markers in view.
		// Cannot zoom to bounds until the map has a size.
		final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
		if (mapView.getViewTreeObserver().isAlive()) {
			mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@SuppressLint("NewApi") // We check which build version we are using.
				@Override
				public void onGlobalLayout() {
					LatLng[] corners = CommonVars.cv().getRuta().getCorners();
					Log.d("NILS","SW: "+corners[0]+" NE: "+corners[1]);
					LatLngBounds bounds = new LatLngBounds(corners[0], corners[1]);

					mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);                    
					mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
				}
			});
		}
	}

	private void addMarkersToMap() {
		final int[] ids = {R.drawable.ytcirklar_s_init,R.drawable.ytcirklar_s_ready, 
				R.drawable.ytcirklar_s_problem, R.drawable.ytcirklar_s_aktiv};
		int r=0;
		LatLng latlong = new LatLng(-27.47093, 153.0235);
		double[] latlon;
		if (mMap == null)
			Toast.makeText(this, "UPPPPPSSS", Toast.LENGTH_LONG).show();
		else {
			int j=1;
			for(final Provyta yta:ytor) {
				latlon = yta.getLatLong();
				latlong = new LatLng(latlon[0],latlon[1]);
				Log.d("NILS","yta "+j++);
				Log.d("NILS","latlong "+latlong.latitude+" "+latlong.longitude);
				Log.d("NILS","yta id"+yta.getId());
				markers.put(mMap.addMarker(new MarkerOptions()

				.position(latlong)
				.title(yta.getId())
				.snippet("E: "+yta.E+" N: "+yta.N)
				.icon(BitmapDescriptorFactory.fromResource(ids[r++%ids.length]))),yta);

			}   	
		}

	}

	private boolean checkReady() {
		if (mMap == null) {
			Toast.makeText(this, "Kartan är inte klar", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	/** Called when the Clear button is clicked. */
	public void onClearMap(View view) {
		if (!checkReady()) {
			return;
		}
		mMap.clear();
	}

	/** Called when the Reset button is clicked. */
	public void onResetMap(View view) {
		if (!checkReady()) {
			return;
		}
		// Clear the map because we don't want duplicates of the markers.
		mMap.clear();
		addMarkersToMap();
	}

	//
	// Marker related listeners.
	//
	Provyta currentSelected = null;

	@Override
	public boolean onMarkerClick(final Marker marker) {
		if (currentSelected !=null && currentSelected.equals(markers.get(marker)))
			//Pressed second time the same
			onProvytaClick(currentSelected);

		// This causes the marker at Perth to bounce into position when it is clicked.
		final LatLng PERTH = marker.getPosition();
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		Projection proj = mMap.getProjection();
		Point startPoint = proj.toScreenLocation(PERTH);
		startPoint.offset(0, -100);
		final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		final long duration = 1500;

		final Interpolator interpolator = new BounceInterpolator();
		currentSelected = markers.get(marker);
		handler.post(new Runnable() {
			@Override
			public void run() {
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = interpolator.getInterpolation((float) elapsed / duration);
				double lng = t * PERTH.longitude + (1 - t) * startLatLng.longitude;
				double lat = t * PERTH.latitude + (1 - t) * startLatLng.latitude;
				marker.setPosition(new LatLng(lat, lng));

				if (t < 1.0) {
					// Post again 16ms later.
					handler.postDelayed(this, 16);
				} 
			}
		});

		// We return false to indicate that we have not consumed the event and that we wish
		// for the default behavior to occur (which is for the camera to move such that the
		// marker is centered and for the marker's info window to open, if it has one).
		return false;
	}

	private void onProvytaClick(Provyta yta) {
		CommonVars cv = CommonVars.cv();
		cv.setProvyta(yta);
		AlertDialog.Builder alert = new AlertDialog.Builder(this);


		alert.setMessage("Ska ytan inventeras?");

		alert.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				final Intent takePictureIntent = new Intent(getBaseContext(),TakePicture.class);
				final Intent hittaYtaIntent = new Intent(getBaseContext(),HittaYta.class);


				if(CommonVars.cv().getDeviceColor().equals(CommonVars.blue())) {
					Log.d("NILS","dosa blå!"+CommonVars.blue());				
					startActivity(takePictureIntent);
				}
				else {
					startActivity(hittaYtaIntent);     

				}
				//finish();
				// Intent intent = getIntent();
				//setResult(Activity.RESULT_OK, intent);

			}
		});

		alert.setNegativeButton("Nej", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		alert.show();


	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		Toast.makeText(getBaseContext(), "Click Info Window", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
	}

	@Override
	public void onMarkerDrag(Marker marker) {
	}


}
