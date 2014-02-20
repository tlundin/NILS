package com.teraim.nils.dynamic.types;

import android.graphics.Bitmap;
import android.util.Log;

public class Marker {
	public final static float Pic_H = 32;
	public int x,y;
	public int riktning;
	public int dist;
	public Bitmap bmp;

	public Marker(Bitmap bmp) {
		dist=0;
		x=y=dist=0;
		this.bmp = Bitmap.createScaledBitmap(bmp, 32, (int)Pic_H, false);				

	}

	public void set(int x, int y, int dist) {
		this.x=x;this.y=y;this.dist = dist;		
	}

	public int getDistance() {
		return dist;
	}
	public boolean hasPosition() {
		return dist>0;
	}

	public void setValue(String avst, String rikt) {
		if(avst==null||rikt==null||avst.length()==0||rikt.length()==0) {
			Log.d("nils","null or empty in setValue Marker class");
			return;
		}
		dist = Integer.parseInt(avst);
		riktning = Integer.parseInt(rikt);
		
		x=(int)(dist*Math.cos(riktning*0.0174532925));
		y=(int)(dist*Math.sin(riktning*0.0174532925));
	}
}



