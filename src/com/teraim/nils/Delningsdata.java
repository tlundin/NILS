package com.teraim.nils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

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

	public class Delyta {
		final int Max_Points = 10;
		private int[][] ar=new int[Max_Points][2];
		private final String myId;

		public Delyta(String id, String[] raw) {
			
			int i = -1;
			int val = -1;
			boolean avst = true;
			myId = id;
			//Put -999 to signal null value.
			for (int j =0;j<Max_Points;j++) {
				ar[j][0]=-999;
			}
			String deb="";
			for (String s:raw) {
				deb+=s+" ";
			}
			//Log.d("NILS","Raw: "+deb);
			
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
					i++;
					ar[i][0]=val;
				} else {
					ar[i][1]=val;
					avst = true;
				}
				
			}
			//Log.d("NILS","Tåg skapat med "+i+" element");
		}
		public int[][] getPoints() {
			return ar;
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
			if(this.getId().equals("416")&&provYteId.equals("9")) {
				Log.e("NILS", "NINE GETTING ADDEEEEDDD");
				String dd="";
				for(String s:raw)
					dd+=s;
				Log.e("NILS","DD "+dd);
			}
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
}


