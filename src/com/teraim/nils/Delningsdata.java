package com.teraim.nils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.teraim.nils.exceptions.IllegalCallException;

import android.content.Context;
import android.util.Log;

public class Delningsdata {



	static Delningsdata singleton;
	public static Delningsdata getSingleton(Context c) {
		if (singleton == null) {
			singleton = new Delningsdata(c.getResources().openRawResource(R.raw.delningsdata));
			singleton.scan();
		}
		return singleton;
	}

	//Train class stores the "TÅG" in swedish, i.e. the dividing lines crossing the Provyta (TestArea).
	//Train defined by points in a circle. Each point is described as an angle (rikt) and a distance (dist).
	//There can be up to 8 points per Train but there must be an equal number of Avst/Rikt, so
	//setAvst and setRikt needs be called equal number of times. 
	
	
	public class Train  {
		static final int Max_Points = 10;
		final int[] avst;
		final int[] rikt;
		private int current;
		
		boolean nick;
		boolean carter;
		
		public Train() {
			nick = carter = false;
			avst=new int[Max_Points];
			rikt=new int[Max_Points];
			current=0;
		}
		public void setAvst(int avs) throws IllegalCallException {
			if(!nick) {
				avst[current]=avs;
			
				nick = true;
				checkIfNext();
			} else
				throw new IllegalCallException();
			
		}
		public void setRikt(int rik) throws IllegalCallException {
			if(!carter) {
			rikt[current]=rik;
			carter = true;
			checkIfNext();
			} else
				throw new IllegalCallException();

		}
		private void checkIfNext() {
			if (nick&carter) {
				current++;
				nick = carter = false;
			}
		}
		
		public int getSize() {
			return current;
		}
		
		public int[][] getTag() {
			if (current==0)
				return null;
			int ret[][]= new int[current][2];
			for(int i=0;i<current;i++) {
				ret[i][0]=avst[i];
				ret[i][1]=rikt[i];
			}
			return ret;
		}
	}
	public class Delyta {
		final int Max_Points = 10;
		private Train tr = new Train();
		private final String myId;

		public Delyta(String id, String[] raw) {
			
			int i = -1;
			int val = -1;
			boolean avst = true;
			myId = id;
			//Put -999 to signal null value.
			
			for (String s:raw) {
				
				try {
					val = Integer.parseInt(s);
				} catch(NumberFormatException e) {
					//If error, break! 
					if (!s.equals("NA"))
						Log.e("NILS", "Not a number in delytedata: "+s);
					break;
				}
				if (val<0) {
					break;
				}
				
				//If avst is true, the AVSTÅND will be set and the arraypointer moved forward.
				if (avst) {
					
					avst = false;
					try {
						tr.setAvst(val);
					} catch (IllegalCallException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						tr.setRikt(val);
					} catch (IllegalCallException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					avst = true;
				}
				
			}
			//Log.d("NILS","Tåg skapat med "+i+" element");
		}
		public int[][] getPoints() {
			return tr.getTag();
		}
		public String getId() {
			return myId;
		}
	}
	private InputStream csvFile;

	private Delningsdata(InputStream inputStream) {
		csvFile = inputStream;
	}
	
	public class ProvYta {

		private String myId;	
		private ArrayList<Delyta>dy = new ArrayList<Delyta>();

		

		public ProvYta(String id) {
			myId = id;
		}

		public String getId() {
			return myId;
		}
		public void addDelyta(String delyteId, String[] raw) {
			dy.add(new Delyta(delyteId,raw));
		}

		public ArrayList<Delyta>getDelytor() {
			return dy;
		}

	}

	private class Ruta {

		private String myId;
		private ArrayList<ProvYta> py = new ArrayList<ProvYta>();

		public Ruta(String id) {
			myId = id;
		}

		public String getId() {
			return myId;
		}

		private void addProvYta(String provYteId,String delyteId,String[] raw) {

			if (provYteId != null) {
			ProvYta _py = getProvYta(provYteId);
			if (_py==null) {
				_py = new ProvYta(provYteId);
				py.add(_py);
			} 
			_py.addDelyta(delyteId, raw);
		}
		}
		
		private ProvYta getProvYta(String provyteId) {
			for (ProvYta p:py) {
				if (p.getId().equals(provyteId)) {
					return p;
				}

			}
			return null;
		}
	}

	ArrayList<Ruta> ar = new ArrayList<Ruta>();

	private Ruta getRuta(String rutId) {
		if (rutId == null)
			return null;
		for (Ruta r: ar ) {
			if (r.getId().equals(rutId))
				return r;
		}
		return null;
	}
	
	public ArrayList<Delyta> getDelytor(String rutId, String provyteId) {
		Ruta r = getRuta(rutId);
		if (r!=null) {
			Log.d("NILS","found ruta "+ rutId);
		ProvYta p = r.getProvYta(provyteId);
		if (p!=null) {
			Log.d("NILS","Found provyta"+ provyteId);			
			return (p.getDelytor());
		} 
		} else
			Log.e("NILS","DID NOT FIND RUTA "+ rutId);
		return null;
	}
	//scan csv file for Rutor. Create if needed.
	private void scan() {
		InputStreamReader is = new InputStreamReader(csvFile);
		BufferedReader br = new BufferedReader(is);
		final int noPo = 16;
		try {
			String row;
			String header = br.readLine();
			Log.d("NILS",header);
			//Find rutId etc
			while((row = br.readLine())!=null) {
				String  r[] = row.split("\t");
				if (r!=null) {	
					if (r[2]==null)
						continue;
					Ruta ruta = getRuta(r[2]);
					//if this is a new ruta, add it to the array
					if (ruta==null && r[2]!=null) {
						ruta = new Ruta(r[2]);
						ar.add(ruta);
					}					
					String[] points = new String[noPo];
					System.arraycopy(r, 6, points, 0, noPo);
					ruta.addProvYta(r[4],r[5],points);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Calculate the distance between smallest and biggest x,y values
		//This is done to be able to calculate the grid position.

	}
	
	public void calcStuff() {
		
		
		
		
	}
}


