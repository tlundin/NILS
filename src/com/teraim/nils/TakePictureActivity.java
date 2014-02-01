package com.teraim.nils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class TakePictureActivity extends Activity {
	private static int TAKE_PICTURE = 1;
	private Uri outputFileUri=null;

	private ImageView oldPictureImageView,newPictureImageView; 
	private String arkivBildUrl = null;

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
		//takePhoto(null);
		newPictureImageView = (ImageView)findViewById(R.id.newPic);
		//TODO: Replace with getRutaId when more pics.
		picPath =  GlobalState.getInstance(this).getCurrentPictureBasePath();

		String nyBildUrl = picPath+"/nya/"+dir+".png";
	
		Bitmap newPic = 
				BitmapFactory.decodeFile(nyBildUrl);
		if(newPic == null) {
			Log.d("NILS", "newpic null");
			newPic = 
					BitmapFactory.decodeResource(getResources(), R.drawable.noimg);
		}
			

		//oldPictureImageView.setBackgroundResource(1);

		newPictureImageView.setImageBitmap(newPic);
		
		takePhoto(null);
	}

	/*
		TextView textExisting = (TextView) findViewById(R.id.textExisting);
		oldPictureImageView = (ImageView)findViewById(R.id.oldPic);
		riktningstxt = 			(TextView)findViewById(R.id.riktningstxt);
		riktningstxt.setText("Ruta: "+CommonVars.cv().getRutaId()+" Provyta: "+CommonVars.cv().getProvytaId()+" Riktning: "+dir);
		
	
		//get the selected picture
		compass = this.getIntent().getIntExtra("selectedpic", 0);
		//translate number into string
		
		dir = CommonVars.compassToPicName(compass);
		//Check if there is an existing picture.
		arkivBildUrl = picPath+"/gamla/"+dir+".png";
		Log.d("NILS",picPath);
		Bitmap oldPic = 
			BitmapFactory.decodeFile(arkivBildUrl);
		if(oldPic == null) {
			textExisting.setVisibility(View.GONE);
			oldPictureImageView.setVisibility(View.GONE);
		}
		else {
			textExisting.setVisibility(View.VISIBLE);
			oldPictureImageView.setVisibility(View.VISIBLE);
			oldPictureImageView.setImageBitmap(oldPic);
		}
			
		
		Bitmap newPic = 
				BitmapFactory.decodeFile(nyBildUrl);
		if(newPic == null) {
			Log.d("NILS", "newpic null");
			newPic = 
					BitmapFactory.decodeResource(getResources(), R.drawable.noimg);
		}
			
		riktningstxt.setText("Ruta: "+CommonVars.cv().getRutaId()+" Provyta: "+CommonVars.cv().getProvytaId()+" Riktning: "+dir);
		

		//oldPictureImageView.setBackgroundResource(1);

		newPictureImageView.setImageBitmap(newPic);


	}

*/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == TAKE_PICTURE){
			Toast.makeText(this, "Got back!", Toast.LENGTH_LONG).show();
			Log.d("PIC",outputFileUri.toString());
		}
	}
	
	
	/*
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

*/

	public void takePhoto(View v) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, TAKE_PICTURE);
		//File file = new File(Environment.getExternalStorageDirectory()+CommonVars.NILS_BASE_DIR, "temp.png");

		//outputFileUri = Uri.fromFile(file);
		//intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT,"500");
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,"portrait");
		//intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

	}
	
	public void picZoom(View v) {
		Intent myIntent = new Intent(getBaseContext(),PictureZoom.class);
		if (arkivBildUrl != null)
			myIntent.putExtra("picpath", arkivBildUrl);
		startActivity(myIntent);
		
	}
	
	

	protected void onResume() {

			super.onResume();
	}

	@Override
	protected void onPause() {

		super.onPause();
	}
}


