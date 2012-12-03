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
	private User red,blue;
	private String msg = "";
	//private Path tag = new Path();

	private Context context;

	public ProvytaView(Context context, AttributeSet attrs) {
		super(context,attrs);
		this.context = context;
		//lm.addTestProvider(LocationManager.GPS_PROVIDER, false, false,
		//        false, false, true, true, true, 0, 5);
		/*Location mockLocation = new Location(LocationManager.GPS_PROVIDER); // a string
		mockLocation.setLatitude(59.303189);  // double 
		mockLocation.setLongitude(17.984716); 
		mockLocation.setAltitude(19); 
		mockLocation.setTime(System.currentTimeMillis()); 
		 */
		//lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation);
		//lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

		p.setColor(Color.BLACK);
		p.setStyle(Style.STROKE);
		px.setColor(Color.DKGRAY);
		px.setTypeface(Typeface.SANS_SERIF);
		blue = new BlueUser();
		red = new RedUser();

	}
	
	private class RedUser extends User {
		public RedUser() {
			bmp = BitmapFactory.decodeResource(context.getResources(),R.drawable.red_pin_48);
			bmp = Bitmap.createScaledBitmap(bmp, 32, 32, false);
		}


	}
	private class BlueUser extends User {
		public BlueUser() {
			super();
			bmp = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.blue_pin_48);
			bmp = Bitmap.createScaledBitmap(bmp, 32, 32, false);
		}

	}
	private class User {
		int x,y;
		protected Bitmap bmp;
		protected boolean isVisible=false;
		public User() {

		}
		public void set(int x,int y) {
			this.x=x;this.y=y;
		}
		public boolean isVisible() {
			return isVisible;
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
			scaleF = r/realRadiusinDecimeter;
			cx = w/2;
			cy = h/2;
			//tag.lineTo(w-50,0);
			Log.d("NILS","w h r"+w+" "+h+" "+r);
			canvas.drawCircle(cx, cy,(int)r, p);
			//canvas.drawLine(0, 0, w, h, p);
			// canvas.drawPath(tag,p);
			//mDrawable.draw(canvas);
			//canvas.drawPath(tag, p);
			if (delar !=null)
				drawTag(canvas,cx,cy);
			if (red.isVisible()) {
				int ux = (int) (cx-red.x*scaleF);
				//inverted north/south
				int uy = (int) (cy+red.y*scaleF);
				Log.d("NILS","drawing red at "+ux+" "+uy);
				canvas.drawBitmap(red.bmp, ux, uy, null);
			}
			if (blue.isVisible()) {
				int ux = (int) (cx-blue.x*scaleF);
				//inverted north/south
				int uy = (int) (cy+blue.y*scaleF);
				Log.d("NILS","drawing blue at "+ux+" "+uy);
				canvas.drawBitmap(blue.bmp, ux, uy, null);
			}

			//Msg in center.
			if (msg!=null && msg.length()>0)
				canvas.drawText(msg, cx-msg.length()*3, cy, px);

		}

		
	public void showDistance(int dist) {
		msg = "Dist: "+String.valueOf(dist);
	}

	ArrayList<Delyta> delar = null;
	public void setDelytor(ArrayList<Delyta> dy) {
		delar = dy;
	}


	final double realRadiusinDecimeter = 100;
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
						boolean isArc = (i>1)&&(tst[i][0]==tst[i-1][0]&&tst[i][0]==realRadiusinDecimeter);
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




	public void showUser(String deviceColor, int wx, int wy) {
		int notVisible = -1000;
		if(deviceColor.equals(CommonVars.red()))  {
			if(wx!=notVisible) {
				red.set(wx, wy);
				red.isVisible= true;
			} else
				red.isVisible = false;

		}		
		else {
			if(wx!=notVisible) {
				blue.set(wx, wy);
				blue.isVisible = true;
			}
			else
				blue.isVisible = false;
		}
	}




	public void showWaiting() {
		msg = "Väntar på GPS";
	}




}


