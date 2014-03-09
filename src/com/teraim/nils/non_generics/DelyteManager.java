package com.teraim.nils.non_generics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import android.graphics.Point;
import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.Segment;
import com.teraim.nils.utils.Tools;


//SLU specific class.


public class DelyteManager {

	GlobalState gs;
	VariableConfiguration al;
	private List<Delyta> myDelytor = new ArrayList<Delyta>();
	private static final int MAX_DELYTEID = 5;



	//helper classes
	public static class Coord {
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

	

	public enum ErrCode {
		TooShort,
		EndOrStartNotOnRadius,
		ok
	}


	public void analyze() {
		//Find the background
		calcRemainingYta();
		//Sort south - west and give each a delyteID
		assignDelyteId();
		//Find coordinates where to put the numbers.
		findNumberCoord();
	}

	private void findNumberCoord() {
		//Find where to put the number.
		
		List<Segment> cTag;
		for (Delyta d:myDelytor) {
			
			
			cTag = d.getSegments();
			//Just a simple arc?
			if (cTag.size()==2) {
				//Put in middle of pyramid.
				Segment arc,line;
				if (cTag.get(0).isArc) {
					arc = cTag.get(0);
					line = cTag.get(1);
				} else {
					arc = cTag.get(1);
					line = cTag.get(0);
				}
				int dist;
				int arcMid,nyR;
				if (arc.start.rikt>arc.end.rikt) {
					dist = Delyta.rDist(arc.end.rikt,arc.start.rikt);
					arcMid =  (dist/2);
					nyR = arc.start.rikt-arcMid;
					if (nyR<0)
						nyR=360+nyR;
					Log.d("nils","Start more: "+nyR);
				}
				else {
					dist = Delyta.rDist(arc.end.rikt,arc.start.rikt);
					arcMid =  (dist/2);
					nyR = arc.start.rikt-arcMid;
					if (nyR<0)
						nyR=360+nyR;
					Log.d("nils","Start less: "+nyR);
				}
				
				
					
				
				Coord m = new Coord(85,nyR);
			    
			    d.setNumberPos(m.x,m.y);
			}
			
			
		}
		
		
		//If its a normal arc, put it in the center of the Pyramid.
		
	}
	private float midP(float s,float e) {
		return (s+e)/2;
	}

	private void assignDelyteId() {
		int delyteIdC = 1;
		int backId = -1;
		SortedSet<Delyta> s = new TreeSet<Delyta>(new Comparator<Delyta>() {

			@Override
			public int compare(Delyta lhs, Delyta rhs) {
				return (int)(lhs.mySouth==rhs.mySouth?lhs.myWest-rhs.myWest:lhs.mySouth-rhs.mySouth);
			}});
		
		Log.d("nils","IN SORTOS");
		printDelytor();
		s.addAll(myDelytor);
		
		
		Log.d("nils","Mydelyor has "+myDelytor.size()+" delytor");
		Log.d("nils","Sorted ytor according to south/west has "+s.size()+" delytor");
		for (Delyta d:s) {
			d.setId(delyteIdC);
			if (d.isBackground()) {
				Log.d("nils","found background piece...");
				if (backId!=-1)
					d.setId(backId);
				else
					backId = delyteIdC;
			}
			Log.d("nils",
					"Assigned ID "+d.getId()+" to delyta with first segment S:"+d.getSegments().get(0).start.rikt+" E:"+d.getSegments().get(0).end.rikt+" isArc: "+d.getSegments().get(0).isArc);

			delyteIdC++;
		}
	}

	private final List<Segment> freeArcs = new ArrayList<Segment>();

	private void calcRemainingYta() {
		freeArcs.clear();
		SortedSet<Segment> sortedArcs = new TreeSet<Segment>(new Comparator<Segment>(){
			@Override
			public int compare(Segment lhs, Segment rhs) {
				return lhs.start.rikt-rhs.start.rikt;
			}});
		//Sort arcs. Save in set.
		for(Delyta d:myDelytor)
			for (Segment s:d.tag) {
				if (!s.isArc)
					continue;
				else {
					Log.d("nils","In free arc, adding arc piece S:"+s.start.rikt+" E:"+s.end.rikt);
					sortedArcs.add(s);
				}
			}
		if (sortedArcs.isEmpty()) {
			Log.d("nils","NO FREE ARC!");
		} else {
			//A free arc is an arc that stretches between the end of an existing arc, and the beginning of the next.
			Segment x = sortedArcs.last();
			Log.d("nils","number of arcs: "+sortedArcs.size());
			for (Segment s:sortedArcs) {
				freeArcs.add(new Segment(x.end,s.start,true));
				x=s;
			}
		}
		List<List<Segment>>missingPieces = new ArrayList<List<Segment>>();
		List<Segment>bgPoly;
		Set<Segment>noArcs = new HashSet<Segment>();
		//Extract all segments that are not arcs.
		for(Delyta d:myDelytor) {
			for (Segment s:d.tag) {
				if (s.isArc)
					continue;
				else
					noArcs.add(s);
			}
		}


		Log.d("nils","Free arcs: ");
		for(Segment s:freeArcs) {
			Log.d("nils","S: "+s.start.rikt+" E:"+s.end.rikt);
			bgPoly = new ArrayList<Segment>();
			//Add the free arc but reversed! 
			bgPoly.add(new Segment(s.start,s.end,true));
			//Begin from the end piece (in reverse = start)
			Coord currentCoord = s.end;
			//we want to find the line that  ends here and goes to our start. when we reach there it is done.
			Coord end = s.start;
			boolean notDone = true;
			while (notDone) {
				//Find corresponding "real" Segment.
				boolean noLuck = true;
				Log.d("nils","Tåg has "+sortedArcs.size()+" elements");
				for (Segment se:noArcs) {
					Log.d("nils","Comparing current: "+currentCoord.rikt+" with "+se.end.rikt);
					if (currentCoord.rikt == se.end.rikt) {
						Log.d("nils","Found END MATCH!");
						//Add the reversed segment.
						bgPoly.add(new Segment(se.end,se.start,se.isArc));

						if (se.isArc)
							Log.e("nils","In DelyteManager,calcRemYta..segment seems to be arc...should not happen");
						currentCoord = se.start;
						if (currentCoord.rikt==end.rikt) {
							Log.d("nils","Done, found end");
							missingPieces.add(bgPoly);
							notDone = false;
							noLuck = false;
							break;
						} else {							
							noLuck = false;
							break;
						}

					} 

				}
				if (noLuck) {
					Log.e("nils","went thorough all without finding coord...should not happen.");
					notDone=false;
				}
			}
		}
			//Here we should have all missing polygons.
			Log.d("nils","found "+missingPieces.size()+" polygons");
			//Build delytor.
			for (List<Segment> ls:missingPieces) {
				Delyta d = new Delyta();
				d.createFromSegments(ls);
				myDelytor.add(d);
			}
			Log.d("nils","myDelytor now contains "+myDelytor.size()+" delytor.");
			printDelytor();
		

		//Using the free arcs as starting point, build the missing delyta.

	}





	private void printDelytor() {
		for (Delyta d:myDelytor) {
			Log.d("nils","DELYTA ID: "+d.getId()+" isbg: "+d.isBackground()+" WEST: "+d.myWest+" SOUTH: "+d.mySouth);
			for(Segment s:d.getSegments()) {
				Log.d("nils","S: "+s.start.rikt+" E:"+s.end.rikt+" isArc: "+s.isArc);
			}
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
		Log.d("nils","Calling mydelytor.clear()");
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
