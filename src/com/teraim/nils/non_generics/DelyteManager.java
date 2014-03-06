package com.teraim.nils.non_generics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.utils.Tools;


//SLU specific class.


public class DelyteManager {

	GlobalState gs;
	VariableConfiguration al;
	private List<Delyta> myDelytor = new ArrayList<Delyta>();
	private static final int Rad = 100;
	private static final int MAX_DELYTEID = 5;


	//helper classes
	public class Coord {
		public float x;
		public float y;	
		public final int avst,rikt;
		public Coord(int avst,int rikt) {
			int riktC = rikt-90;
			if (riktC<0)
				riktC +=360;
			double phi = 0.0174532925*(riktC);
			x = (float)(avst * Math.cos(phi));
			y = (float)(avst * Math.sin(phi));	
			//
			this.avst=avst;
			this.rikt=rikt;
		}
	}

	public class Segment {
		public Coord start,end;
		public boolean isArc;
		public Segment(Coord start, Coord end, boolean isArc) {
			super();
			this.start = start;
			this.end = end;
			this.isArc = isArc;
		}

	}


	public enum ErrCode {
		TooShort,
		EndOrStartNotOnRadius,
		ok
	}

	public class Delyta {
		List<Segment> tag;
		int delNr = -1;
		float area = -1;
		Coord mittpunkt;

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
			//Calc area.
			area = calcArea();

			mittpunkt = calcMittPunkt();

			return ErrCode.ok;
		}

		private Coord calcMittPunkt() {
			//take midpoint of all segments. 
			/*
			for (Segment s:tag) {
				float mid = s.end.rikt - s.start.rikt / 2;
				Coord start = new Coord(100,mid);
				Coord end = new Coord(100,mid+180);
				end = mid + 180
			}
			 */
			return null;
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
	}




	public DelyteManager(GlobalState gs) {
		this.gs = gs;
		al = gs.getArtLista();
	}


	public void generateFromCurrentContext() {
		
		loadKnownDelytor(al.getVariableValue(null,"Current_Ruta"),al.getVariableValue(null,"Current_Provyta"));
		
	}


	public void clear() {
		myDelytor.clear();
	}

	public void loadKnownDelytor(String ruta,String provyta) {

		if (ruta==null||provyta==null)
			return;
		Log.d("nils","loadKnownDelytor with RutaID "+ruta+" and provytaID "+provyta);
		for (int delyteID=1;delyteID<=MAX_DELYTEID;delyteID++) {

			String rawTag = al.getVariableValue(Tools.createKeyMap("ruta",ruta,"provyta",provyta,"delyta",delyteID+""), "TAG");					 
			if (rawTag != null && rawTag.length()>0) {
				Log.d("nils","TÅG has value "+rawTag);
				String[] tagElems = rawTag.split("\\|");
				Delyta delyta = createDelyta(tagElems);
				delyta.setId(delyteID);
				myDelytor.add(delyta);
			} 
		}
	
		Log.d("nils","found "+myDelytor.size()+" tåg");
			
		
	}
	
	public ErrCode addUnknownTag(List<Coord> tagCoordinateList) {
		Delyta delyta = new Delyta();
		ErrCode ec = delyta.create(tagCoordinateList);
		if (ec==ErrCode.ok)
			myDelytor.add(delyta);
		return ec;
	}


	public Delyta createDelyta(String[] tagElems) {								
		if (tagElems!=null) {
			for (String s:tagElems) {
				Log.d("nils","tagElem: "+s);
			}
			int avst=-1,rikt=-1;
			List<Coord> tagCoordinateList = new ArrayList<Coord>();
			for (int j=0;j<tagElems.length-1;j+=2) {
				avst =Integer.parseInt(tagElems[j]);
				rikt =Integer.parseInt(tagElems[j+1]);
				tagCoordinateList.add(new Coord(avst,rikt));
				
			}
			Delyta delyta = new Delyta();
			ErrCode ec = delyta.create(tagCoordinateList);
			if (ec==ErrCode.ok)
				return delyta;
			else 
				Log.e("nils","Failed to create delyta. error code: "+ec);
		}
		return null;
	}


	public List<Delyta> getDelytor() {
		return myDelytor;
	}
}
