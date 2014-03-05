package com.teraim.nils.dynamic.templates;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teraim.nils.R;
import com.teraim.nils.dynamic.Executor;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.Marker;
import com.teraim.nils.dynamic.workflow_realizations.WF_Container;
import com.teraim.nils.ui.ProvytaView;




public class TagTemplate extends Executor {

	private static final int MAX_PUNKTER = 8;
	//Real radius in meter.
	private static final int Rad = 100;

	GridLayout gl;
	Set<EditText> glContent = new HashSet<EditText>();

	private static final int MAX_COL =10,MAX_ROWS=6;


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
			Log.d("nils","Got coordinates: ");
			String sue = "";
			for (Coord c:raw) {
				sue += "r: "+c.rikt+" a:"+c.avst+",";
			}
			Log.d("nils",sue);
			if (raw == null||raw.size()==0)
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
	private int numberOfColumns;

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

		View v = inflater.inflate(R.layout.template_tag, container, false);	

		final FrameLayout py = (FrameLayout)v.findViewById(R.id.circle);
		gl = (GridLayout)v.findViewById(R.id.gridLayout);
		Marker man = new Marker(BitmapFactory.decodeResource(getResources(),R.drawable.icon_man));

		pyv = new ProvytaView(activity, null, man);		

		py.addView(pyv);

		VariableConfiguration al = gs.getArtLista();
		//Get all variables from group "delningstag".
		List<List<String>> rows = al.getTable().getRowsContaining(VariableConfiguration.Col_Functional_Group,"delningstag");		

		if (rows!=null) {
			Log.d("nils","Found "+rows.size()+" variables in group delningståg");
			//Pivot table so that each Tåg is a column.
			numberOfColumns = rows.size();
			String[][] tableElements = new String[MAX_COL][MAX_ROWS];
			for (int i=0;i<numberOfColumns;i++) {
				String tag = al.getVariableValue(al.getKeyChain(rows.get(i)), al.getVarName(rows.get(i)));
				if (tag!=null&&tag.length()>0) {
					String[] tagElems = tag.split("\\|");						
					if (tagElems!=null) {
						int index = 0;
						boolean avst = true;
						String ar = "";
						for (String s:tagElems) {
							if (avst) {
								ar=s;
								avst=false;
							} else {
								tableElements[i][index]=ar+","+s;
								avst=true;
								index++;
							}								
						}
					}
				}
			}
			//Add empty top corner at index 0.
			gl.addView(new TextView(gs.getContext()),0);
			//Add headers.
			TextView h;
			for (int i=0;i<numberOfColumns;i++)	{
				h = (TextView)inflater.inflate(R.layout.header_tag_textview, null);
				h.setText(al.getVarLabel(rows.get(i)));
				gl.addView(h,i+1);
			}
			//while there are still some tåg with values, continue.
			for (int i=0;i<MAX_ROWS;i++) {
				//Add row header.
				h = (TextView)inflater.inflate(R.layout.header_tag_textview, null);
				h.setText((i+1)+"");
				gl.addView(h,(i+1)*(numberOfColumns+1));
				LinearLayout l; 
				for (int j=0;j<numberOfColumns;j++) {
					l = (LinearLayout)inflater.inflate(R.layout.edit_fields_tag, null);
					gl.addView(l,j+1+(i+1)*(numberOfColumns+1));
					final EditText avst = (EditText)l.findViewById(R.id.avst);
					final EditText rikt = (EditText)l.findViewById(R.id.rikt);	
					glContent.add(avst);
					glContent.add(rikt);
					String te = tableElements[j][i];
					if (te!=null&&te.length()>0) {
						String[] ar = te.split(",");				
						avst.setText(ar[0]);
						rikt.setText(ar[1]);
						

					}					
				}
			}
			for (int i=1;i<(numberOfColumns+1);i++) {
				for (int j=1;j<MAX_ROWS+1;j++) {
					final int ii = i; final int jj=j;
					final EditText et = ((EditText)((gl.getChildAt(j*(numberOfColumns+1)+i)).findViewById(R.id.rikt)));
					et.setOnLongClickListener(new OnLongClickListener() {
						
						@Override
						public boolean onLongClick(View v) {
							et.setText("");
							return true;
						}
					});
					et.setOnKeyListener(new OnKeyListener() {
						
						@Override
						public boolean onKey(View v, int keyCode, KeyEvent event) {
							if (keyCode == KeyEvent.KEYCODE_TAB || keyCode == KeyEvent.KEYCODE_ENTER)
								if (jj<MAX_ROWS) {
									LinearLayout l2 = (LinearLayout)(gl.getChildAt((jj+1)*(numberOfColumns+1)+ii));
									EditText avst = ((EditText)(l2).findViewById(R.id.avst));
									avst.requestFocus();
									return true;
								}
							return false;
						}
					});
					et.addTextChangedListener(new TextWatcher() {
						
						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {
							if (count==3) {
								if (jj<MAX_ROWS) {
									LinearLayout l2 = (LinearLayout)(gl.getChildAt((jj+1)*(numberOfColumns+1)+ii));
									EditText avst = ((EditText)(l2).findViewById(R.id.avst));
									avst.requestFocus();									
								}
							}
						}
						
						@Override
						public void beforeTextChanged(CharSequence s, int start, int count,
								int after) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void afterTextChanged(Editable s) {
							// TODO Auto-generated method stub
							
						}
					});
					
				}
			}
			gl.getChildAt(numberOfColumns+2).requestFocus();
		}

		/*
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
		 */
		/*	
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

		 */
		
		Button ritaom = (Button)v.findViewById(R.id.redraw);
		Button rensa = (Button)v.findViewById(R.id.rensa);
		Button spara = (Button)v.findViewById(R.id.spara);

		
		ritaom.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				redrawTrains();
			}
		});
		
		rensa.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(TagTemplate.this.getActivity())
			    .setTitle("Nyutlägg")
			    .setMessage("Det här tar bort alla inmatade värden. Är du säker?")
			    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			        	for(EditText et:glContent)
							et.setText("");
			        	redrawTrains();
			        }
			     })
			    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			            // do nothing
			        }
			     })
			    .setIcon(android.R.drawable.ic_dialog_alert)
			     .show();
				
			}
		});
		spara.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(TagTemplate.this.getActivity())
			    .setTitle("Spara")
			    .setMessage("Det här sparar alla förändringar permanent. Är du säker?")
			    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			        	
			        }
			     })
			    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			            // do nothing
			        }
			     })
			    .setIcon(android.R.drawable.ic_dialog_alert)
			     .show();
			}
		});
		return v;

	}

	private void redrawTrains() {
		List<Delyta> delytor=new ArrayList<Delyta>();
		//Create train array.
		for (int i=1;i<numberOfColumns+1;i++) {
			int index = 0;
			List<Coord> coords = new ArrayList<Coord>();
			for (int j=1;j<MAX_ROWS+1;j++) {
				LinearLayout ll = (LinearLayout)(gl.getChildAt(j*(numberOfColumns+1)+i));
				EditText avst = ((EditText)(ll).findViewById(R.id.avst));
				EditText rikt = ((EditText)(ll).findViewById(R.id.rikt));
				String avS = avst.getText().toString();
				String riS = rikt.getText().toString();
				//If one of the values are empty - tag ends.
				if (empty(avS)||empty(riS)) {
					if (coords.size()>1) {
						Delyta delyta = new Delyta();
						ErrCode e = delyta.create(coords);
						if (e == ErrCode.ok) {
							delytor.add(delyta);

						} else 
							Log.e("nils","Couldnt create delyta: "+e.name());
					}
					break;
				}
				else {	
					coords.add(new Coord(Integer.parseInt(avS),Integer.parseInt(riS)));
					//t[i-1][index++] = Integer.parseInt(avS);
					//t[i-1][index++] = Integer.parseInt(riS);
				}
			}
		}


		//Draw.



		pyv.showDelytor(delytor);		

	}

	private boolean empty(String s) {
		return s==null||s.length()==0;
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
