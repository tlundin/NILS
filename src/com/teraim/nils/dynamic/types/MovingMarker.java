package com.teraim.nils.dynamic.types;

import android.graphics.Bitmap;

import com.teraim.nils.utils.Geomatte;

public class MovingMarker extends Marker {
	private int prevx,prevy;

	public MovingMarker(Bitmap bmp) {
		super(bmp);
	}

	@Override
	public void set(int x,int y,int dist) {
		prevx=this.x; prevy=this.y;
		this.x=x;this.y=y;this.dist = dist;
	}

	@Override
	public double getMovementDirection() {
		return Geomatte.getRikt2(prevy, prevx, y, x);
	}
}