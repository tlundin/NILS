/**
 * 
 */
package com.teraim.nils;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.teraim.nils.Delningsdata.Delyta;

/**
 * @author Terje
 * 
 * This class is used to draw a Provyta with all its parts (delytor)
 */
public class ProvytaView extends View {



	private Paint p = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);

	private Paint px = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);

	private Paint pl = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);

	private User red,blue,uncolored;
	private String msg = "";
	
	//private Path tag = new Path();

	
	private Bitmap needle; 
	public ProvytaView(Context context, AttributeSet attrs) {
		super(context,attrs);
		

		p.setColor(Color.BLACK);
		p.setStyle(Style.STROKE);
		px.setColor(Color.DKGRAY);
		px.setTypeface(Typeface.SANS_SERIF);
		

		pl.setColor(Color.BLACK);
		pl.setStyle(Style.STROKE);
		pl.setTypeface(Typeface.DEFAULT_BOLD); 
		pl.setTextSize(25);




		needle= 
				BitmapFactory.decodeResource(getResources(), R.drawable.arrow);
		needle = Bitmap.createScaledBitmap(needle, 32, 32, false);
		
		blue = new User(BitmapFactory.decodeResource(context.getResources(),
				R.drawable.blue_pin_48));
		red = new User(BitmapFactory.decodeResource(context.getResources(),R.drawable.red_pin_48));
		
		uncolored = red = new User(BitmapFactory.decodeResource(context.getResources(),R.drawable.red_pin_48));
	}




	private class User {
		final static int Pic_H = 32;
		public int x,y;
		private int dist;
		protected Bitmap bmp;
		
		public User(Bitmap bmp) {
			x=y=dist=-1;
			this.bmp = Bitmap.createScaledBitmap(bmp, 32, Pic_H, false);		
		}
		public void set(int x,int y,int dist) {
			this.x=x;this.y=y;this.dist = dist;
		}
	
		public int getDistance() {
			return dist;
		}
		public boolean hasPosition() {
			return dist>0;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);			
		int w = getWidth();
		int h = getHeight();

		double r;
		int cy;
		int cx;
		r=(w>=h)?((h/2)-h*.1):((w/2)-w*.1);
		scaleF = r/realRadiusinMeter;
		cx = w/2;
		cy = h/2;
		//tag.lineTo(w-50,0);
		Log.d("NILS","w h r"+w+" "+h+" "+r);
		//draw 100 meter circle
		canvas.drawCircle(cx, cy,(int)r, p);

		//draw 10 meter circle
		canvas.drawCircle(cx, cy,(float)(10.0*scaleF), p);

		//draw a "N" for north.
		//30 is fontsize + padding.
		canvas.drawText("N",cx,(float)(h*.1), pl);

		//canvas.drawLine(0, 0, w, h, p);
		// canvas.drawPath(tag,p);
		//mDrawable.draw(canvas);
		//canvas.drawPath(tag, p);
		if (delar !=null)
			drawTag(canvas,cx,cy);
		if (red.hasPosition()) {
			if(red.getDistance()<realRadiusinMeter) {
			int ux = (int) (cx-red.x*scaleF);
			//inverted north/south
			//Subtract picture height. "Needle is in leftdown corner.
			int uy = (int) (cy+red.y*scaleF)-User.Pic_H;
			Log.d("NILS","drawing red at "+ux+" "+uy);
			canvas.drawBitmap(red.bmp, ux, uy, null);
			} else {
				//Given that blue is outside current Max Radius, draw an arrow to indicate where..
				double alfa = Geomatte.getRikt(red.getDistance(), cy, cx, red.y, red.x);
				float x = (float)(cx + r * Math.sin(alfa));
				float y =  (float)(cy - r * Math.cos(alfa));
				canvas.save();
				canvas.rotate((float)-alfa, x, y);
				canvas.drawBitmap(needle, x, y, null);
				canvas.restore();
			} 
		}
		if (blue.hasPosition()) {
			Log.d("NILS","Blue has position");
			if(blue.getDistance()<realRadiusinMeter) {
			int ux = (int) (cx-blue.x*scaleF)-User.Pic_H;;
			//inverted north/south
			//Subtract picture height. "Needle is in leftdown corner.
			int uy = (int) (cy+blue.y*scaleF)-User.Pic_H;
			Log.d("NILS","drawing blue at "+ux+" "+uy);
			canvas.drawBitmap(blue.bmp, ux, uy, null);
			} else {
				Log.d("NILS","Blue is outside radius");
			//Given that blue is outside current Max Radius, draw an arrow to indicate where..
			double alfa = Geomatte.getRikt(blue.getDistance(), 0, 0, blue.y, blue.x);
			float x = (float)(cx + r * Math.sin(alfa));
			float y =  (float)(cy - r * Math.cos(alfa));
			canvas.save();
			canvas.rotate((float)(180+(180*alfa/Math.PI)), x, y);
			canvas.drawBitmap(needle, x, y, null);
			canvas.restore();
			//TODO:
			//If last value closer than this value, draw arrow pointing towards middle
			} 
		}

		//Msg in top.
		if (msg!=null && msg.length()>0)
			canvas.drawText(msg, cx-msg.length()*3, (float)(h*.1+30), px);

		//update compass.
		//Rotate arrow symbol to point to real "current" north
		
		//int x = (int) (cx+ r*(Math.cos(oldNorth)));
		//int y = (int) (cy+ r*(Math.sin(oldNorth)));




	}


	public void showDistance(int dist) {
		msg = "Avst: "+String.valueOf(dist)+"m";
	}

	ArrayList<Delyta> delar = null;
	public void setDelytor(ArrayList<Delyta> dy) {
		delar = dy;
	}


	final double realRadiusinMeter = 100;
	double scaleF=0;

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
			Log.d("NILS", "DELAR size is "+delar.size());
			for (Delyta del:delar) {

				//draw each 
				if (del !=null) {
					tst = del.getPoints();

					if (tst!=null) {
						for (int i=0;i<tst.length;i++) {
							//avstånd från cirkelns mitt;
							int avst = tst[i][0];
							//Quit if null value
							if (avst==-999)
								break;
							//Grad-riktning. 360 är för avstånd 0
							int rikt = tst[i][1];
							double rr = (rikt-90) * Math.PI/180;
							int x = (int) (cx+ avst*scaleF*(Math.cos(rr)));
							int y = (int) (cy+ avst*scaleF*(Math.sin(rr)));
							Log.d("NILS","avst "+avst+" scaleF "+scaleF+" rikt "+rikt+" radRikt "+rr+" cos: "+Math.cos(rr)+" sin:"+Math.sin(rr));
							Log.d("NILS","X: "+x+" Y:"+y);

							//y = 2*r-y;
							//x = x - 2*(x-r);
							xy[i][0]=x;
							xy[i][1]=y;
							c.drawText(Integer.toString(i), x, y, p);
							//om inte första punkten
							boolean isArc = (i>1)&&(tst[i][0]==tst[i-1][0]&&tst[i][0]==realRadiusinMeter);
							if( 
									i==1
									||
									i==(tst.length-1)
									||
									tst[i+1][0]==-999
									||
									//...och Om två delningspunkter mellan första och sista brytpunkt.. 
									//..INTE ligger på periferin..
									//...så ska vi dra en linje.
									((i>1)&&(isArc==false))) {
								c.drawLine(xy[i-1][0], xy[i-1][1],x,y, p);
							}

							/*if (isArc) {
						float avgX = Math.abs((xy[i][0]+xy[i-1][0])/2);
						float avgY = Math.abs((xy[i][1]+xy[i-1][1])/2);
						Log.d("NILS","isArch i"+isArc+" "+i);
						c.drawText(Integer.toString(i), avgX, avgY, p);
					}
							 */


							/*if(i==0)
						tag.moveTo(x, y);
						else
							tag.lineTo(x, y);
							 */
						}

					}
				}

			}
		}else {
			Log.e("NILS","no delyta in pos 0 for delar");
			tst = null;
		}

	}




	public void showUser(String deviceColor, Location arg0, double alfa,
			double dist) {
		// TODO Auto-generated method stub

	}




	public void showUser(String deviceColor, int wx, int wy,int dist) {
		
		Log.d("INIT","DIST "+dist);
		if(deviceColor.equals(CommonVars.red()))  
				red.set(wx, wy,dist);
			
		else 	
			if(deviceColor.equals(CommonVars.blue()))
				blue.set(wx, wy,dist);
			else
				uncolored.set(wx,wy,dist);
	
	}




	public void showWaiting() {
		msg = "Väntar på GPS";
	}

	




}


