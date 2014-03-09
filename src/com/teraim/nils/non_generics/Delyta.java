package com.teraim.nils.non_generics;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;
import android.util.Log;

import com.teraim.nils.dynamic.types.Segment;
import com.teraim.nils.non_generics.DelyteManager.Coord;
import com.teraim.nils.non_generics.DelyteManager.ErrCode;

public class Delyta {

	List<Segment> tag;
	static final int Rad = 100;
	int delNr = -1;
	float area = -1;
	boolean background = false;
	float mySouth;
	float myWest;
	private static final Coord South = new Coord(100,180);
	private static final Coord West = new Coord(100,90);
	private static final float NO_VALUE = -9999999;
	private float myNumX=NO_VALUE,myNumY=NO_VALUE;

	public ErrCode create(List<Coord> raw) {	
		Log.d("nils","Got coordinates: ");
		String sue = "";
		if (raw == null||raw.size()==0)
			return null;
		for (Coord c:raw) {
			sue += "r: "+c.rikt+" a:"+c.avst+",";
		}
		Log.d("nils",sue);

		if (raw.get(0).avst!=Rad||raw.get(raw.size()-1).avst!=Rad) {
			Log.d("nils","Start eller slutpunkt ligger inte på radien");
			return ErrCode.EndOrStartNotOnRadius;
		}
		if (raw.size()<2) {
			Log.d("nils","För kort tåg! Tåg måste ha fler än två punkter.");
			return ErrCode.TooShort;
		}
		tag = new ArrayList<Segment>();
		//First is never an arc.
		Coord start,end;
		boolean isArc=false,previousWasArc = true;
		for (int i =0 ; i<raw.size()-1;i++) {	
			Log.d("nils","start: ("+raw.get(i).avst+","+raw.get(i).rikt+")");
			Log.d("nils","end: ("+raw.get(i+1).avst+","+raw.get(i+1).rikt+")");
			start = raw.get(i);
			end = raw.get(i+1);	
			isArc = (start.avst==Rad && end.avst==Rad && !previousWasArc);
			previousWasArc = isArc;					
			tag.add(new Segment(start,end,isArc));
		}
		//close the loop End -> Start. IsArc is always true.
		tag.add(new Segment(raw.get(raw.size()-1),raw.get(0),true));
		Log.d("nils","Added ending arc from "+tag.get(tag.size()-1).start.rikt+" to "+tag.get(tag.size()-1).end.rikt);
		//Calc area.
		calcStats();
		return ErrCode.ok;
	}

	private void calcStats() {
		area = calcArea();
		mySouth = distance(South);
		myWest = distance(West);
		
	}

	public void createFromSegments(List<Segment> ls) {
		//TODO:Should this rather be a copy?
		tag = ls;
		//This piece is background.
		background = true;
		calcStats();
	}

	public boolean isBackground() {
		return background;
	}

	//If segment is not an arc, the southermost point is on the line from south pole perpendicular to the segment
	//If segment is an arc, the arc is either covering the soutpole or not. If cover, done. If not, caluclate the line 
	//between the southmost coordinate and south pole. This is the distance.
	private float distance(Coord Pole) {
		float Dx,Dy,x1y2,x2y1;
		float ret=-1,max=10000;
		
		for (Segment s:tag) {
			if (s.isArc) {
				int endToPoleDist = pDist(s.end.rikt,Pole.rikt);
				int endToStartDist = pDist(s.end.rikt,s.start.rikt);
				if (endToPoleDist < endToStartDist) {				
					Log.d("nils","This arc goes through pole");
					max=0;
					break;
				}
				else {
					//d = sqrt [ (x2 - x1)^2 + (y2 - y1)^2 ]
					float dStart = (s.start.rikt-Pole.rikt);
					float dEnd = (s.end.rikt-Pole.rikt);
					if (dStart<0)
						dStart +=360;
					if (dEnd<0)
						dEnd +=360;
					Coord shortest = dStart<=dEnd?s.start:s.end;
					Dx = shortest.x-Pole.x;
					Dy = shortest.y-Pole.y;
					ret = (float)Math.sqrt(Dx*Dx+Dy*Dy);
					Log.d("nils","DISTANCE TO "+(Pole.equals(West)?"WEST":"SOUTH")+" POLAR: "+ret);
					
				}

			} else {
				Dx = s.start.x-s.end.x;
				Dy = s.start.y-s.end.y;
				x1y2 = s.start.x*s.end.y;
				x2y1 = s.end.x*s.start.y;
				float hyp = (float)Math.sqrt(Dx*Dx+Dy*Dy);
				float d = Math.abs(Dy*Pole.x-Dx*Pole.y + x1y2 - x2y1);
				ret = d/hyp;
				Log.d("nils","DISTANCE TO "+(Pole.equals(West)?"WEST":"SOUTH")+ret);
				float slope = Dy/Dx;
				
			}
			if (ret<max)
				max = ret;

		}
		return max;
	}

	public static int pDist(int from, int to) {
		if (to <= from)
			return from-to;
		else
			return from+360-to;
	}
	public static int rDist(int from, int to) {
		if (to >= from)
			return to-from;
		else
			return to+360-from;
	}

	public List<Segment> getSegments() {
		return tag;
	}

	public float calcArea() {
		List<Coord> areaC=new ArrayList<Coord>();
		//Area is calculated using Euler. 
		for (Segment s:tag) {
			//If not arc, add.
			if (!s.isArc) {
				areaC.add(s.start);
				areaC.add(s.end);
			}
			else
				addArcCoords(areaC,s.start,s.end);					
		}
		//Now use euler to calc area.
		float T=0; 
		int p,n;
		for (int i=0;i<areaC.size();i++) {
			p = i==0?areaC.size()-1:i-1;
			n = i==(areaC.size()-1)?0:i+1;
			T+= areaC.get(i).x*(areaC.get(n).y-areaC.get(p).y);
		}
		Log.d("nils","Area calculate to be "+T/2);
		return T/2;
	}

	//should be coordinates on the radius. with grad running 0..359. 
	private void addArcCoords(List<Coord> areaC, Coord start, Coord end) {
		int rikt;
		int i = start.rikt;
		Log.d("nils","Stratos: "+start.rikt+" endos: "+end.rikt);
		while (i!=end.rikt) {
			i=(i+1)%360;			
			rikt = start.rikt+i;
			areaC.add(new Coord(Rad,rikt));
			//Log.d("nils",""+i);
		}
	}

	public void setId(int delyteID) {
		delNr=delyteID;
	}

	public int getId() {
		return delNr;
	}

	public void setNumberPos(float mX, float mY) {
		myNumX = mX;
		myNumY = mY;
	}
	
	public Point getNumberPos() {
		if (myNumX==NO_VALUE||myNumX==NO_VALUE)
			return null;
		Point p = new Point();
		p.set((int)myNumX,(int)myNumY);
		return p;
	}
}

