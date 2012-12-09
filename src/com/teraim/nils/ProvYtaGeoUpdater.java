package com.teraim.nils;


import java.util.ArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.teraim.nils.Delningsdata.Delyta;
import com.teraim.nils.Rutdata.Ruta;
import com.teraim.nils.Rutdata.Yta;

public class ProvYtaGeoUpdater implements LocationListener
 {

	ProvytaView myView;
	LocationManager lm;

	//private double myMx=6577621,myMy=669933;
	//private double myCenterLat = 59.303203;
	//private double myCenterLong = 17.984738;
	private Location myLocation, center,swerefC;
	private GeoUpdaterCb geoCb;
	SensorManager sensorManager;
	private Sensor sensorAccelerometer;
	private Sensor sensorMagneticField;




	public ProvYtaGeoUpdater(Context c, ProvytaView w,GeoUpdaterCb cb) {

		lm = (LocationManager)c.getSystemService(Context.LOCATION_SERVICE);
		
		if (lm==null) {
			Log.e("NILS","Startup of GPS tracking failed in ProvYtaGeoUpdater");
		}
		//Sensor stuff
		sensorManager = (SensorManager)c.getSystemService(Context.SENSOR_SERVICE);

		if (sensorManager==null) {
			Log.e("NILS","Startup of magnetic/tilt tracking failed in ProvYtaGeoUpdater");
		}
		geoCb = cb;
		//Initialize provyta with center coordinates.
		Rutdata rd = Rutdata.getSingleton(c);
		Ruta r = rd.findRuta(CommonVars.cv().getRutaId());
		Yta y = r.findYta(CommonVars.cv().getProvytaId());
		double[] cc = y.getLatLong();
		center = new Location("");
		//This is in sweref.
		center.setLatitude(cc[0]);
		center.setLongitude(cc[1]);
		double[] dd = y.getSweRefCoords();
		swerefC = new Location("");
		swerefC.setLatitude(dd[0]);
		swerefC.setLongitude(dd[1]);
		myView = w;
		myView.showWaiting();
		




	}


	public void onPause() {
		lm.removeUpdates(this);
	}

	public void onResume() {
		//---request for location updates---
		lm.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				0,
				1,
				this);
 
	}

	final static int ProvYtaRadiusInMeters = 100;
	//Has the callback been called already when user within 5 meter circle?
	boolean cbCalled = false;
	//Inner radius - if within, allow prressing the button for setMittPunkt
	int InnerRadiusInMeters = 10;
	private Location currentLocation=null;
	
	public void onLocationChanged(Location arg0) {
		currentLocation = arg0;
		//TODO: Remove unnecessary math code.
		Log.d("NILS","myLat myLong "+arg0.getLatitude()+" "+arg0.getLongitude());
		Log.d("NILS","centerLat centerLong "+center.getLatitude()+" "+center.getLongitude());
		double dist = arg0.distanceTo(center);
		double[] xy = Geomatte.convertToSweRef(arg0.getLatitude(),arg0.getLongitude());
		Log.d("NILS","Sweref avstånd: "+Geomatte.sweDist(swerefC.getLatitude(), swerefC.getLongitude(), xy[0], xy[1]));
		Log.d("NILS","Latlong avstånd: "+Geomatte.dist(center.getLatitude(), center.getLongitude(), xy[0], xy[1]));		
		Log.d("NILS","Platformsavstånd: "+arg0.distanceTo(center));		
		//double alfa = Geomatte.getRikt(dist, center.getLatitude(), center.getLongitude(), xy[0], xy[1]);
			//myView.showUser(CommonVars.cv().getDeviceColor(),arg0,alfa,dist);
			//distance in meters between target and center
			int wy = (int)(swerefC.getLatitude() - xy[0]);
			int wx = (int)(swerefC.getLongitude() - xy[1]);
			//show user distance from middle point of circle
			Log.d("NILS","user x y "+wx+" "+wy);
			myView.showUser(CommonVars.cv().getDeviceColor(), wx,wy,(int)dist);
			if (geoCb!=null && 
					dist < InnerRadiusInMeters &&
					!cbCalled) {
				geoCb.onWithinFiveMeters();
				cbCalled = true;
			} else 
				if(cbCalled) {
					cbCalled=false;
					geoCb.onOutsideFiveMeters();
				}
		
		myView.showDistance((int)dist);
		myView.invalidate();
	}

	public void onProviderDisabled(String arg0) {
		Log.d("NILS","Provider disabled in PROVYTA!");
	}

	public void onProviderEnabled(String provider) {
		Log.d("NILS","Provider enabled in PROVYTA!");

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d("NILS","Status changed! in PROVYTA!");

	}


	public void setDelytor(ArrayList<Delyta> dy) {


	}


	public Location getCurrentPosition() {
		return currentLocation;
	}

	


}
