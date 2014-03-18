package com.teraim.nils.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GeoListener implements LocationListener {

	
	private LocationManager lm;
	private double[] cords;
	
	public GeoListener(Context ctx) {
		lm = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);

	}
	
	public void onResume() {
		lm.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				0,
				1,
				this);
	}
	
	public void onPause() {
		lm.removeUpdates(this);
	}
	
	
	@Override
	public void onLocationChanged(Location location) {
		cords = Geomatte.convertToSweRef(location.getLatitude(),location.getLongitude());
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
