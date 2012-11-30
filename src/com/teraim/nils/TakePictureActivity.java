package com.teraim.nils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class TakePictureActivity extends Activity implements SensorEventListener {
	private static int TAKE_PICTURE = 1;
	private Uri outputFileUri=null;
	SensorManager sensorManager;
	private Sensor sensorAccelerometer;
	private Sensor sensorMagneticField;

	private float[] valuesAccelerometer;
	private float[] valuesMagneticField;

	private float[] matrixR;
	private float[] matrixI;
	private float[] matrixValues;
	private ImageView oldPictureImageView,newPictureImageView; 
	private String arkivBildUrl = null;

	TextView readingAzimuth, readingPitch, readingRoll;
	Compass myCompass;
	private String dir = "";
	String[] imageNames = {
			"delyta",
			"ost",
			"vast",
			"norr",
			"syd"
	};
	String picPath = null;
	TextView riktningstxt;
	int compass;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.takepicture_singular);

		oldPictureImageView = (ImageView)findViewById(R.id.oldPic);
		newPictureImageView = (ImageView)findViewById(R.id.newPic);
		riktningstxt = 			(TextView)findViewById(R.id.riktningstxt);
		
		//get the selected picture
		compass = this.getIntent().getIntExtra("selectedpic", 0);
		//translate number into string
		
		dir = CommonVars.compassToPicName(compass);
		//TODO: Replace with getRutaId when more pics.
		picPath = CommonVars.cv().getCurrentPictureBasePath();
		//Check if there is an existing picture.
		arkivBildUrl = picPath+"/gamla/"+dir+".png";
		String nyBildUrl = picPath+"/nya/"+dir+".png";
		Log.d("NILS",picPath);
		Bitmap oldPic = 
			BitmapFactory.decodeFile(arkivBildUrl);
		
		Bitmap newPic = 
				BitmapFactory.decodeFile(nyBildUrl);
		//TODO: Replace with getrutaID when more pics.
		riktningstxt.setText("Ruta: "+CommonVars.cv().getRutaId()+" Provyta: "+CommonVars.cv().getProvytaId()+" Riktning: "+dir);
		oldPictureImageView.setImageBitmap(oldPic);

		//oldPictureImageView.setBackgroundResource(1);

		newPictureImageView.setImageBitmap(newPic);
		readingAzimuth = (TextView)findViewById(R.id.azimuth);
		readingPitch = (TextView)findViewById(R.id.pitch);
		readingRoll = (TextView)findViewById(R.id.roll);
		myCompass = (Compass)findViewById(R.id.mycompass);

		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		valuesAccelerometer = new float[3];
		valuesMagneticField = new float[3];

		matrixR = new float[9];
		matrixI = new float[9];
		matrixValues = new float[3];


	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){

		if (requestCode == TAKE_PICTURE){
			Log.d("PIC",outputFileUri.toString());

			//Save file in temporary storage.
			Bitmap bip = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+CommonVars.NILS_BASE_DIR+"/temp.png");		
			int w = bip.getWidth();
			int h = bip.getHeight();
			bip = Bitmap.createScaledBitmap(bip, w/6, h/6, false);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
			//			bip.compress(CompressFormat.JPEG, 0, outputStream); 
			byte[] bytes = outputStream.toByteArray(); 
			Log.d("NILS","Direction of cam: "+dir);

			//Bitmap bimp = BitmapFactory.decodeByteArray( bytes, 
			//		0,bytes.length);
			Log.d("NILS","The picture has a width of "+w+" and a height of "+h);
			//oldPic.setImageURI(outputFileUri);
			newPictureImageView.setImageBitmap(bip);
			OutputStream fOut = null;
			File file = new File(picPath+"/nya/", dir+".png");
			Log.d("NILS", "trying to save pic as: "+picPath+"/nya/"+dir+".png");
			try {
				fOut = new FileOutputStream(file);
			bip.compress(Bitmap.CompressFormat.PNG, 100, fOut);
				fOut.flush();
			fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}



			
			

		}

	}


	public void takePhoto(View v) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File file = new File(Environment.getExternalStorageDirectory()+CommonVars.NILS_BASE_DIR, "temp.png");

		outputFileUri = Uri.fromFile(file);
		//intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT,"500");
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,"portrait");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(intent, TAKE_PICTURE);

	}
	
	public void picZoom(View v) {
		Intent myIntent = new Intent(getBaseContext(),PictureZoom.class);
		if (arkivBildUrl != null)
			myIntent.putExtra("picpath", arkivBildUrl);
		startActivity(myIntent);
		
	}

	protected void onResume() {

		sensorManager.registerListener(this,
				sensorAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this,
				sensorMagneticField,
				SensorManager.SENSOR_DELAY_NORMAL);
		super.onResume();
	}

	@Override
	protected void onPause() {

		sensorManager.unregisterListener(this,
				sensorAccelerometer);
		sensorManager.unregisterListener(this,
				sensorMagneticField);
		super.onPause();
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

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

			double azimuth = Math.toDegrees(matrixValues[0]);
			double pitch = Math.toDegrees(matrixValues[1]);
			double roll = Math.toDegrees(matrixValues[2]);

			readingAzimuth.setText("Azimuth: " + String.valueOf(azimuth));
			readingPitch.setText("Pitch: " + String.valueOf(pitch));
			readingRoll.setText("Roll: " + String.valueOf(roll));

			myCompass.update(matrixValues[0]);
		}

	}


}
