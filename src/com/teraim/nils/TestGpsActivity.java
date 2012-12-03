package com.teraim.nils;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class TestGpsActivity extends Activity implements LocationListener {

	LocationManager lm;
	TextView lat,longh, dt =null;
	Location destination, currentLocation = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.testgps);
		lat = (TextView)findViewById(R.id.lath);
		longh = (TextView)findViewById(R.id.longh);
		dt = (TextView)findViewById(R.id.distanceh);
		
		destination = new Location("");

		destination.setLatitude(59.302623);
		destination.setLongitude(17.980497);

		lm = (LocationManager)
				getSystemService(Context.LOCATION_SERVICE);

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
	public void onPause() {
		super.onPause();
		//---remove the location listener---
		lm.removeUpdates(this);

	}


	public void onLocationChanged(Location loc) {
		if (loc!=null) {
			lat.setText(loc.getLatitude()+"");
			longh.setText(loc.getLongitude()+"");

			dt.setText(Math.round(loc.distanceTo(destination))+" meter(s)");
			currentLocation = loc;

		}


	}

	public void onProviderDisabled(String arg0) {
		Log.e("NILS", "no GPS Signal!");
	}

	public void onProviderEnabled(String arg0) {
		Log.e("NILS", "GPS Signal!");

	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}
}
