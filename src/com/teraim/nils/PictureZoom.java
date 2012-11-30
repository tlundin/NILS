package com.teraim.nils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;


public class PictureZoom extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImageView tmp = new ImageView(this);
		Bitmap pic = 
				BitmapFactory.decodeFile(this.getIntent().getStringExtra("picpath"));

		tmp.setImageBitmap(pic);
		setContentView(tmp);
		}
		

}
