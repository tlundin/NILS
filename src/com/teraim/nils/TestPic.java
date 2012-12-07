package com.teraim.nils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.teraim.nils.CommonVars;


public class TestPic extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		//intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT,"500");
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		//intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,"landscape");
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File file = new File(Environment.getExternalStorageDirectory()+CommonVars.NILS_BASE_DIR, "temp.png");
		Uri outputFileUri = Uri.fromFile(file);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(intent, TAKE_PICTURE);
		
	}	
	final int TAKE_PICTURE = 133;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){

		if (requestCode == TAKE_PICTURE){
			Toast.makeText(this, "Got back!", Toast.LENGTH_LONG).show();
			//Save file in temporary storage.
			Bitmap bip = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+CommonVars.NILS_BASE_DIR+"/temp.png");		
			int w = bip.getWidth();
			int h = bip.getHeight();
			bip = Bitmap.createScaledBitmap(bip, w/6, h/6, false);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
			//			bip.compress(CompressFormat.JPEG, 0, outputStream); 
			byte[] bytes = outputStream.toByteArray(); 
				Log.d("NILS","The picture has a width of "+w+" and a height of "+h);
			//oldPic.setImageURI(outputFileUri);
			//newPictureImageView.setImageBitmap(bip);
			OutputStream fOut = null;
			String folder = CommonVars.compassToPicName(this.getIntent().getIntExtra("selectedpic", 0))+".png";
			File file = new File(CommonVars.cv().getCurrentPictureBasePath()+"/nya/", 
					folder);
			Log.d("NILS", "trying to save pic as: "+CommonVars.cv().getCurrentPictureBasePath()+"/nya/"+folder+".png");
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
}
