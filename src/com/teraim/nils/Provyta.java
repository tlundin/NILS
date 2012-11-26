/**
 * 
 */
package com.teraim.nils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * @author Terje
 * This class is used to draw a Provyta with all its parts (delytor)
 */
public class Provyta extends View {
	private ShapeDrawable mDrawable;
	private Paint p = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
	//private Path tag = new Path();
	private int width, height;

	public Provyta(Context context) {
		this(context,null);
	}
	
	public Provyta(Context context, AttributeSet attrs) {
		super(context,attrs);
		
		p.setColor(Color.BLACK);
		p.setStyle(Style.STROKE);
		
	}
 
	protected void onDraw(Canvas canvas) {
		   int w = getWidth();
		    int h = getHeight();
		    
		    double r;
		    int cy;
		    int cx;
		    r=(w>=h)?((h/2)-h*.2):((w/2)-w*.2);
		    cx = w/2;
		    cy = h/2;
		    //tag.lineTo(w-50,0);
		    Log.d("NILS","w h r"+w+" "+h+" "+r);
		    canvas.drawCircle(cx, cy,(int)r, p);
		    //canvas.drawLine(0, 0, w, h, p);
		   // canvas.drawPath(tag,p);
		//mDrawable.draw(canvas);
		//canvas.drawPath(tag, p);
		    drawTag(canvas,cx,cy,(int)r);
    }


	public void drawTag(Canvas c,int cx,int cy, int pixelRad) {
		
		final double realRadiusinDecimeter = 100;
		final double scaleF = pixelRad/realRadiusinDecimeter;
		int tst[][] = {{100,288},{100,48},{100,120},{100,263}};
//		int tst[][] = {{100,233},{000,360},{064,322},{100,047}};
		//int tst[][] = {{100,261},{100,36},{100,98},{100,200}};
		//int tst[][] = {{100,261},{100,36},{100,98},{100,200}};
		//int pst[][][] = {{100,200,300}};
		int xy[][] = new int[20][2];
		for (int i=0;i<tst.length;i++) {
			//avstånd från cirkelns mitt;
			int avst = tst[i][0];
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
			//om inte första punkten
			if( 
					i==1
					||
					i==(tst.length-1)
					||
					((i>1)&&!(tst[i][0]==tst[i-1][0]&&tst[i][0]==realRadiusinDecimeter)))
				//...och Om två delningspunkter mellan första och sista brytpunkt.. 
				//..INTE ligger på periferin..
				//...så ska vi dra en linje.

						c.drawLine(xy[i-1][0], xy[i-1][1],x,y, p);
			
			
			/*if(i==0)
				tag.moveTo(x, y);
				else
					tag.lineTo(x, y);
					*/
		}
		
	}

}


