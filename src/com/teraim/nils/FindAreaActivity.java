package com.teraim.nils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

/** 
 * 	
 * @author Terje
 * Given a Test Area (Provyta), help the user find his way to it by displaying:
 * - Current Location
 * - Distance to target
 * - 5 previous pictures taken earlier time
 * 
 * Arriving at the spot, let the user take new pictures and mark the location given by GPS reader.
 * 
 */
public class FindAreaActivity extends Activity implements LocationListener, SensorEventListener {

	LocationManager lm;
	TextView ll = null,dt=null;
	Location destination = null;
	int provyteID = -1;
	SensorManager sensorManager;
	private Sensor sensorAccelerometer;
	private Sensor sensorMagneticField;
	private ImageView arrow;	
	private TextView fieldBearing;
	//private Compass myCompass;
	private float deg = -1; //the current bearing towards target in degrees east.
	private Location currentLocation=null;

	Integer[] imageIDs = {
			R.drawable.delyta,
			R.drawable.ost,
			R.drawable.vast,
			R.drawable.norr,
			R.drawable.syd,
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.findarea);
		ll = (TextView)findViewById(R.id.latlong);
		dt = (TextView)findViewById(R.id.distance);
		arrow = (ImageView)findViewById(R.id.arrow);

		fieldBearing = (TextView)findViewById(R.id.fieldBearing);
		lm = (LocationManager)
				getSystemService(Context.LOCATION_SERVICE);
		destination = new Location("");

		destination.setLatitude(59.308435);
		destination.setLongitude(17.978933);



		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(new ImageAdapter(this));
		gridView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView parent,
					View v, int position, long id)
			{
				Toast.makeText(getBaseContext(),
						"pic" + (position + 1) + " selected",
						Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(getBaseContext(),TakePictureActivity.class);
				intent.putExtra("selectedpic", position);
				startActivity(intent);
			}
		});

		/*		
		Bitmap delytaB = BitmapFactory.decodeFile("mnt/sdcard/nils/provytor/"+provyteID+"/old/delytaB.jpg");			
		Bitmap eastB = BitmapFactory.decodeFile("mnt/sdcard/nils/provytor/"+provyteID+"/old/east.jpg");		
		Bitmap westB = BitmapFactory.decodeFile("mnt/sdcard/nils/provytor/"+provyteID+"/old/west.jpg");		
		Bitmap northB = BitmapFactory.decodeFile("mnt/sdcard/nils/provytor/"+provyteID+"/old/north.jpg");		
		Bitmap southB = BitmapFactory.decodeFile("mnt/sdcard/nils/provytor/"+provyteID+"/old/south.jpg");		


		ImageView iv;

		iv = (ImageView)findViewById(R.id.delprovyta);
		if (delytaB==null)
			iv.setImageResource(R.drawable.delyta);
		else
			iv.setImageBitmap(delytaB);

		iv = (ImageView)findViewById(R.id.east);
		if (eastB==null)
			iv.setImageResource(R.drawable.ost);
		else
			iv.setImageBitmap(eastB);

		iv = (ImageView)findViewById(R.id.west);
		if (westB==null)
			iv.setImageResource(R.drawable.vast);
		else
			iv.setImageBitmap(westB);

		iv = (ImageView)findViewById(R.id.north);
		if (northB==null)
			iv.setImageResource(R.drawable.norr);
		else
			iv.setImageBitmap(northB);

		iv = (ImageView)findViewById(R.id.south);
		if (southB==null)
			iv.setImageResource(R.drawable.syd);
		else
			iv.setImageBitmap(southB);

		 */

		//Compass related..
		//myCompass = (Compass)findViewById(R.id.mycompass);

		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


	}


	public class ImageAdapter extends BaseAdapter
	{
		private Context context;
		public ImageAdapter(Context c)
		{
			context = c;
		}
		//---returns the number of images---
		public int getCount() {
			return imageIDs.length;
		}
		//---returns the item---
		public Object getItem(int position) {
			return position;
		}
		//---returns the ID of an item---
		public long getItemId(int position) {
			return position;
		}
		//---returns an ImageView view---
		public View getView(int position, View convertView,
				ViewGroup parent)
		{
			ImageView imageView;
			if (convertView == null) {
				imageView = new ImageView(context);
				imageView.setLayoutParams(new
						GridView.LayoutParams(195, 195));
				imageView.setScaleType(
						ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(5, 5, 5, 5);
			} else {
				imageView = (ImageView) convertView;
			}
			imageView.setImageResource(imageIDs[position]);
			//imageView.setImageBitmap(bm[position]);
			return imageView;
		}
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


		sensorManager.registerListener(this,
				sensorAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this,
				sensorMagneticField,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public void onPause() {
		super.onPause();
		//---remove the location listener---
		lm.removeUpdates(this);
		sensorManager.unregisterListener(this,
				sensorAccelerometer);
		sensorManager.unregisterListener(this,
				sensorMagneticField);
	}

	public void onLocationChanged(Location loc) {
		if (loc!=null) {
			ll.setText(loc.getLatitude()+","+loc.getLongitude());

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
	/*
	public void onSensorChanged(SensorEvent event) {
		switch(event.sensor.getType()){
		case Sensor.TYPE_ACCELEROMETER:
			for(int i =0; i < 3; i++){
				valuesAccelerometer[i] = event.values[i];
			}
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			for(int i =0; i < 3; i++){
				valuesMagneticField[i] = event.values[i];
			}
			break;
		}

		boolean success = SensorManager.getRotationMatrix(
				matrixR,
				matrixI,
				valuesAccelerometer,
				valuesMagneticField);

		if(success){
			SensorManager.getOrientation(matrixR, matrixValues);

			//			double azimuth = Math.toDegrees(matrixValues[0]);
			//			double pitch = Math.toDegrees(matrixValues[1]);
			//			double roll = Math.toDegrees(matrixValues[2]);
			if (currentLocation!=null) {
				deg = currentLocation.bearingTo(destination);
				if (deg<0)
					deg+=360;

			}
			Log.d("NILS","Current direction towards treasure: "+ deg);
			myCompass.update(matrixValues[0],deg);
		}

	}
	 */


	public void onSensorChanged( SensorEvent event ) {

		// If we don't have a Location, we break out
		if ( currentLocation == null ) return;

		float azimuth = event.values[0];
		float baseAzimuth = azimuth;

		GeomagneticField geoField = new GeomagneticField( Double
				.valueOf( currentLocation.getLatitude() ).floatValue(), Double
				.valueOf( currentLocation.getLongitude() ).floatValue(),
				Double.valueOf( currentLocation.getAltitude() ).floatValue(),
				System.currentTimeMillis() );

		azimuth -= geoField.getDeclination(); // converts magnetic north into true north

		// Store the bearingTo in the bearTo variable
		float bearTo = currentLocation.bearingTo( destination );

		// If the bearTo is smaller than 0, add 360 to get the rotation clockwise.
		if (bearTo < 0) {
			bearTo = bearTo + 360;
		}

		//This is where we choose to point it
		float direction = bearTo - azimuth;

		// If the direction is smaller than 0, add 360 to get the rotation clockwise.
		if (direction < 0) {
			direction = direction + 360;
		}

		rotateImageView( arrow, R.drawable.arrow, direction );

		//Set the field
		String bearingText = "N";

		if ( (360 >= baseAzimuth && baseAzimuth >= 337.5) || (0 <= baseAzimuth && baseAzimuth <= 22.5) ) bearingText = "N";
		else if (baseAzimuth > 22.5 && baseAzimuth < 67.5) bearingText = "NE";
		else if (baseAzimuth >= 67.5 && baseAzimuth <= 112.5) bearingText = "E";
		else if (baseAzimuth > 112.5 && baseAzimuth < 157.5) bearingText = "SE";
		else if (baseAzimuth >= 157.5 && baseAzimuth <= 202.5) bearingText = "S";
		else if (baseAzimuth > 202.5 && baseAzimuth < 247.5) bearingText = "SW";
		else if (baseAzimuth >= 247.5 && baseAzimuth <= 292.5) bearingText = "W";
		else if (baseAzimuth > 292.5 && baseAzimuth < 337.5) bearingText = "NW";
		else bearingText = "?";

		fieldBearing.setText(bearingText);

	}
	private void rotateImageView( ImageView imageView, int drawable, float rotate ) {

		// Decode the drawable into a bitmap
		Bitmap bitmapOrg = BitmapFactory.decodeResource( getResources(),
				drawable );

		// Get the width/height of the drawable
		DisplayMetrics dm = new DisplayMetrics(); getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = bitmapOrg.getWidth(), height = bitmapOrg.getHeight();

		// Initialize a new Matrix
		Matrix matrix = new Matrix();

		// Decide on how much to rotate
		rotate = rotate % 360;

		// Actually rotate the image
		matrix.postRotate( rotate, width, height );

		// recreate the new Bitmap via a couple conditions
		Bitmap rotatedBitmap = Bitmap.createBitmap( bitmapOrg, 0, 0, width, height, matrix, true );
		//BitmapDrawable bmd = new BitmapDrawable( rotatedBitmap );

		//imageView.setImageBitmap( rotatedBitmap );
		imageView.setImageDrawable(new BitmapDrawable(getResources(), rotatedBitmap));
		imageView.setScaleType( ScaleType.CENTER );
	}
}
