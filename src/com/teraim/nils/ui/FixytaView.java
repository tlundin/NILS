package com.teraim.nils.ui;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.teraim.nils.dynamic.types.Delyta;
import com.teraim.nils.dynamic.types.Marker;
import com.teraim.nils.dynamic.types.MovingMarker;
import com.teraim.nils.utils.Geomatte;

/**
 * @author Terje
 * 
 * This class is used to draw a Provyta with all its parts (delytor)
 */
public class FixytaView extends View {




	private Paint px = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
	private Paint pl = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);

	private Paint p = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
	private Paint p20 = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
	private Paint p50 = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
	
	private MovingMarker user = null;
	private Marker[] fixpunkter;
	private String msg = "";

	//private Path tag = new Path();

	public void setFixedMarkers (Marker[] fixpunkter) {
		this.fixpunkter=fixpunkter;
	}

	public FixytaView(Context context, AttributeSet attrs) {
		super(context,attrs);


		px.setColor(Color.DKGRAY);
		px.setTypeface(Typeface.SANS_SERIF);


		pl.setColor(Color.BLACK);
		pl.setStyle(Style.STROKE);
		pl.setTypeface(Typeface.DEFAULT_BOLD); 
		pl.setTextSize(25);


		p.setColor(Color.BLACK);
		p.setStyle(Style.STROKE);

		p20.setColor(Color.BLUE);
		p20.setStrokeWidth(2);
		p20.setStyle(Style.STROKE);		
		p20.setTypeface(Typeface.SANS_SERIF); 


		p50.setColor(Color.RED);
		p50.setStrokeWidth(3);
		p50.setStyle(Style.STROKE);
		p50.setTypeface(Typeface.SANS_SERIF); 



	}


	final double innerRealRadiusInMeter = 10;
	final double midRealRadiusInMeter = 20;
	final double realRadiusinMeter = 50;
	double rScaleF=0,oScaleF=0;

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);			
		int w = getWidth();
		int h = getHeight();
		double r;
		int cy;
		int cx;
		r=(w>=h)?((h/2)-h*.1):((w/2)-w*.1);
		cx = w/2;
		cy = h/2;
		oScaleF = r/realRadiusinMeter;
		//A dot in the middle!
		canvas.drawPoint(cx, cy, p);

		canvas.drawCircle(cx, cy,(int)r, p50);
		canvas.drawCircle(cx, cy,(float)(20.0*oScaleF), p20);
		canvas.drawCircle(cx, cy,(float)(10.0*oScaleF), p);
		rScaleF = oScaleF;
		canvas.drawText("50",(int)(cx+r)-25, cy, p);
		canvas.drawText("20",(int)(cx+(20.0*oScaleF))-20, cy, p);
		canvas.drawText("10",(int)(cx+(10.0*oScaleF))-15, cy, p);
		canvas.drawText("N",cx,(float)(h*.1), pl);

		for(Marker focusMarker:fixpunkter) {
			if (focusMarker.hasPosition()) {
				double alfa;
				//Log.d("NILS","Blue has position");
				if(focusMarker.getDistance()<realRadiusinMeter) {
					alfa = focusMarker.getMovementDirection();
					int ux = (int) (cx-focusMarker.x*rScaleF);
					int iconx = (int)(Marker.Pic_H/2+Marker.Pic_H *  Math.sin(alfa));
					ux = ux - iconx;
					int uy = (int) (cy+focusMarker.y*rScaleF);
					int icony = (int)(MovingMarker.Pic_H/2+MovingMarker.Pic_H *  Math.cos(alfa));
					uy = uy + icony;
//					canvas.save();
					canvas.drawBitmap(focusMarker.bmp, ux, uy, null);
//					canvas.restore();
					msg = "X: "+ux+" Y: "+uy+" icX: "+(-iconx)+" icY: "+icony;
				} else {
					//Log.d("NILS","Blue is outside radius");
					//Given that blue is outside current Max Radius, draw an arrow to indicate where..
					alfa = Geomatte.getRikt2(focusMarker.y, focusMarker.x,0,0);
					float x = (float)(cx + r * Math.sin(alfa));
					float y =  (float)(cy - r * Math.cos(alfa));
					canvas.save();
					canvas.rotate((float)(180+(180*alfa/Math.PI)), x, y);
					canvas.drawBitmap(focusMarker.bmp, x, y, null);
					canvas.restore();
					//TODO:
					//If last value closer than this value, draw arrow pointing towards middle
				} 
			}
		}

		//Msg in top.
		if (msg!=null && msg.length()>0)
			canvas.drawText(msg, cx-msg.length()*3, (float)(h*.1+30), px);

		//update other fixpoints.


	}


	public void showDistance(int dist) {
		msg = "Avst: "+String.valueOf(dist)+"m";
	}

	ArrayList<Delyta> delar = null;
	public void setDelytor(ArrayList<Delyta> dy) {
		delar = dy;
	}


	public void drawTag(Canvas c,int cx,int cy) {

		//		int tst[][] = {{100,288},{100,48},{100,120},{100,263}};
		int tst[][] = {{100,233},{000,360},{064,322},{100,047}};
		//int tst[][] = {{100,261},{100,36},{100,98},{100,200}};
		//int tst[][] = {{100,261},{100,36},{100,98},{100,200}};
		//int pst[][][] = {{100,200,300}};
		float xy[][] = new float[20][2];
		tst = null;
		//Are there any divisions of this Yta?
		if(delar!=null&&delar.size()>0) {
			//Log.d("NILS", "DELAR size is "+delar.size());
			for (Delyta del:delar) {

				//draw each 
				if (del !=null) {
					tst = del.getPoints();
					//Log.d("NILS", "Tågets size is "+tst.length);

					if (tst!=null && tst.length>1) {
						for (int i=0;i<tst.length;i++) {
							//avstånd från cirkelns mitt;
							int avst = tst[i][0];
							//Quit if null value
							if (avst==-999)
								break;
							//Grad-riktning. 360 är för avstånd 0
							int rikt = tst[i][1];
							double rr = (rikt-90) * Math.PI/180;
							int x = (int) (cx+ avst*oScaleF*(Math.cos(rr)));
							int y = (int) (cy+ avst*oScaleF*(Math.sin(rr)));
							//Log.d("NILS","avst "+avst+" rScaleF "+rScaleF+" rikt "+rikt+" radRikt "+rr+" cos: "+Math.cos(rr)+" sin:"+Math.sin(rr));
							//Log.d("NILS","X: "+x+" Y:"+y);

							//y = 2*r-y;
							//x = x - 2*(x-r);
							xy[i][0]=x;
							xy[i][1]=y;
							c.drawText(Integer.toString(i), x, y, p);
							boolean isArc = (i>1)&&(tst[i][0]==tst[i-1][0]&&tst[i][0]==realRadiusinMeter);
							//Log.d("NILS","I: "+i);
							if( 
									//om andra punkten
									i==1
									||
									//...eller sista
									((tst.length>1)&&(i==(tst.length-1)))
									||
									//..eller nästa är brytpunkt
									tst[i+1][0]==-999
									||
									//...och det är inte en arc mellan...
									((i>1)&&(isArc==false))
									) {
								//då ritar vi.
								c.drawLine(xy[i-1][0], xy[i-1][1],x,y, p);
							}

						}

					}
				}

			}
		}else {
			Log.e("NILS","no delyta in pos 0 for delar");
			tst = null;
		}

	}





	public void showUser(String deviceColor, int wx, int wy,int dist) {

		user.set(wx,wy,dist);

	}




	public void showWaiting() {
		msg = "Väntar på GPS";
	}



	



}


