package com.teraim.nils.dynamic.types;

import android.graphics.Bitmap;

import com.teraim.nils.utils.Geomatte;

public class Marker {
	public final static float Pic_H = 32;
	public int x,y;
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

	public double getMovementDirection() {
		return 0;
	}
}



