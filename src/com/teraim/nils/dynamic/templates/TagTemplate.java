package com.teraim.nils.dynamic.templates;

import java.util.ArrayList;
import java.util.List;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.teraim.nils.R;
import com.teraim.nils.dynamic.Executor;
import com.teraim.nils.dynamic.types.Marker;
import com.teraim.nils.dynamic.workflow_realizations.WF_Container;
import com.teraim.nils.ui.ProvytaView;




public class TagTemplate extends Executor {

	private static final int MAX_PUNKTER = 8;
	//Real radius in meter.
	private static final int Rad = 100;


	public class Coord {
		public float x;
		public float y;	
		public int avst,rikt;
		public Coord(int avst,int rikt) {
			rikt = (rikt-90);
			if (rikt<0)
				rikt +=360;
			double phi = 0.0174532925*(rikt);
			x = (float)(avst * Math.cos(phi));
			y = (float)(avst * Math.sin(phi));	
			//
			this.avst=avst;
			this.rikt=rikt;
		}
	}

	enum ErrCode {
		TooShort,
		EndOrStartNotOnRadius,
		ok
	}

	//List<Segment> mySegments = new ArrayList<Segment>();	
	/*
	 * 		public class Segment {
			List<Coord> myCoords = new ArrayList<Coord>();
			public void add(Coord c) {
				myCoords.add(c);
			}
			public List<Coord> get() {
				return myCoords;
			}
		}
		public  List<Segment> getPolySegments() {
			return mySegments;
		}
	 */



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

	public class Delyta {


		List<Segment> tag;

		int delNr = -1;
		float area = -1;
		Coord mittpunkt;

		public ErrCode create(List<Coord> raw) {			
			if (raw == null)
				return null;

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
	}



	private ArrayList<WF_Container> myLayouts;
	private ViewGroup myContainer;
	private ProvytaView pyv;

	@Override
	protected List<WF_Container> getContainers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void execute(String function, String target) {
		// TODO Auto-generated method stub

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		int[] buttonIds = new int[] {R.id.button1,R.id.button2,R.id.button3,R.id.button4,R.id.button5};
		View v = inflater.inflate(R.layout.template_tag, container, false);	

		final FrameLayout py = (FrameLayout)v.findViewById(R.id.circle);
		
		Marker man = new Marker(BitmapFactory.decodeResource(getResources(),R.drawable.icon_man));

		pyv = new ProvytaView(activity, null, man);		

		py.addView(pyv);
		
		
		int[][] t = { 

				{100,233,0,360,64,322,100,47},
				{100,288,100,48,100,120,100,263},
				{100,48,100,288},
				{100,233,57,180,100,143},
				{100,29,75,336,100,320},
				{100,261,100,36,100,98,100,200},
				{100,219,100,116},
				{100,116,100,30},
				{100,30,100,270}	
		 		
		};
		List<Delyta> delytor=new ArrayList<Delyta>();

		for (int d = 0;d<t.length;d++) {
			int[]u = t[d];
			Delyta delyta = new Delyta();
			List<Coord> coords = new ArrayList<Coord>();
			for (int i=0;i<u.length-1;i+=2) 
				coords.add(new Coord(u[i],u[i+1]));
			ErrCode e = delyta.create(coords);
			if (e == ErrCode.ok) {
				delytor.add(delyta);
			} else 
				Log.e("nils","Couldnt create delyta: "+e.name());
		}		

		final List<List <Delyta>> dy = new ArrayList<List<Delyta>>();
		
		List <Delyta> d1 = new ArrayList<Delyta>();
		d1.add(delytor.get(0));
		dy.add(d1);
		List <Delyta> d2 = new ArrayList<Delyta>();
		d2.add(delytor.get(1));
		d2.add(delytor.get(2));
		dy.add(d2);
		List <Delyta> d3= new ArrayList<Delyta>();
		d3.add(delytor.get(3));
		d3.add(delytor.get(4));
		dy.add(d3);
		List <Delyta> d4= new ArrayList<Delyta>();
		d4.add(delytor.get(5));
		dy.add(d4);		
		List <Delyta> d5= new ArrayList<Delyta>();
		d5.add(delytor.get(6));
		d5.add(delytor.get(7));
		d5.add(delytor.get(8));
		dy.add(d5);		
		Button[] button = new Button[5];
		for (int i=0;i<5;i++) {
			button[i]=(Button)v.findViewById(buttonIds[i]);
			final int c = i;
			button[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					pyv.showDelytor(dy.get(c));
				}
			});
		}
		
		return v;

	}

	/* (non-Javadoc)
	 * @see android.app.Fragment#onStart()
	 */
	@Override
	public void onStart() {


		

		
		
		super.onStart();
	}




	/*

		• Första och sista punkten måste ligga på cirkelprovytans periferi.	 
		• Delningspunkterna måste beskrivas medurs. 
		• Första linjen i tåget får ej vara en cirkelbåge. 
		• Om två delningspunkter mellan första och sista brytpunkt ligger på periferin måste 
		  linjen mellan dem vara en cirkelbåge. I annat fall måste en av punkterna flyttas in 
		  mot centrum 1 dm, så att avståndet till punkten ej är lika med ytradien. 
		• Antalet delningspunkter får vara högst 6 per delningståg. 
		• Provytan får delas i högst 5 delar
	 */






}
