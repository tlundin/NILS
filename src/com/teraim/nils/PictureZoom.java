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
		if (pic==null)
			
			try {
			
			int position = this.getIntent().getIntExtra("pos", -1);
			if (position != -1)
		        tmp.setImageBitmap(
		        	    CommonVars.decodeSampledBitmapFromResource(getResources(), 
		        	    		R.drawable.class.getField(CommonVars.compassToPicName(position)+"_demo").getInt(null), 250,250));
	
			} catch (Exception e) {
				
			}
		else 
			tmp.setImageBitmap(pic);
		setContentView(tmp);
		}
		

}
