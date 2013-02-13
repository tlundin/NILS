package com.teraim.nils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.teraim.nils.DataTypes.Block;
import com.teraim.nils.exceptions.IllegalCallException;


/**
 * 
 * @author Terje
 *
 * Classes defining datatypes for ruta, provyta, delyta and tåg.
 * There are two Scan() functions reading data from two input files (found under the /raw project folder).
 */
public class DataTypes  {

	static DataTypes singleton;
	public static DataTypes getSingleton(Context c) {
		if (singleton == null) {
			singleton = new DataTypes();
			singleton.scanRutData(c.getResources().openRawResource(R.raw.rutdata));
			singleton.scanDelningsData(c.getResources().openRawResource(R.raw.delningsdata));
		}
		return singleton;
	}


	private ArrayList<Ruta> rutor = new ArrayList<Ruta>();
	
	
	//Workflow
	public static class Workflow {
		private List<Block> blocks;
		public String id,name;

		public List<Block> getBlocks() {
			return blocks;
		}
		public void addBlocks(List<Block> _blocks) {
			blocks = _blocks;
		}
		
	}
	
	/**
	 * Abstract base class Block
	 * @author Terje
	 *
	 */
	public abstract static class Block {

	}
	
	/**
	 * Startblock.
	 * @author Terje
	 *
	 */
	public static class StartBlock extends Block {
	
	}
	
	/**
	 * buttonblock
	 * @author Terje
	 *
	 */
	public static class ButtonBlock extends Block {
		String text="banarne";

		public ButtonBlock(String text) {
			Log.d("NILS","ButtonText is set to "+text);
			this.text = text;
		}

		public String getText() {
			return text;
		}
		
	}
	/**
	 * Layoutblock
	 * @author Terje
	 *
	 */
	public static class LayoutBlock extends Block {

		private String layoutDirection="", alignment="";

		public String getLayoutDirection() {
			return layoutDirection;
		}
		public String getAlignment() {
			return alignment;
		}
		public LayoutBlock(String layoutDirection, String alignment) {
			super();
			this.layoutDirection = layoutDirection;
			this.alignment = alignment;
		}
	}
	
	///ValuePair
	
	public class ValuePair {
		public String mkey,mval;
		public ValuePair(String key, String val) {
			mkey=key;
			mval=val;
		}
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
	public class Delyta extends ParameterCache {
		final int Max_Points = 10;
		private Train tr=null; 
		private final String myId;


		public Delyta(String id, String[] raw) {

			myId = id;

			setPoints(raw);

		}
		public int[][] getPoints() {
			if(tr!=null)
				return tr.getTag();
			else
				return null;
		}
		public String getId() {
			return myId;
		}
		public boolean setPoints(String[] tag) {
			int val = -1;
			boolean avst = true;

			//Put -999 to signal null value.
			if (tag!=null) {
				tr = new Train();
				for (String s:tag) {

					try {
						val = Integer.parseInt(s);
					} catch(NumberFormatException e) {
						//If error, break! 
						if (!s.equals("NA"))
							Log.e("NILS", "Not a number in delytedata: "+s);
						return false;
					}
					if (val<0) {
						return false;
					}

					//If avst is true, the AVSTÅND will be set and the arraypointer moved forward.
					if (avst) {

						avst = false;
						try {
							tr.setAvst(val);
						} catch (IllegalCallException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return false;
						}
					} else {
						try {
							tr.setRikt(val);
						} catch (IllegalCallException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return false;
						}
						avst = true;
					}

				}
			}
			return true;
		}


	}

	protected class Provyta {

		private String id;
		double N=0;
		double E=0;
		double lat=0;
		double longh=0;

		private ArrayList<Delyta>dy = new ArrayList<Delyta>();


		public Provyta(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public double[] getLatLong() {
			double[] ret = new double[2];
			ret[0]=lat;
			ret[1]=longh;
			return ret;
		}
		public void setSweRef(double n, double e) {
			N = n;
			E = e;
		}
		public void setGPS(double lat, double longh) {
			this.lat = lat;
			this.longh = longh;
		}

		//ADD will add the delyta if new. Otherwise it will update the current value.
		public void addDelyta(String delyteId, String[] raw) {

			dy.add(new Delyta(delyteId,raw));
		}

		public Delyta findDelyta(String delyteId) {
			for(Delyta d:dy)
				if(d.getId().equals(delyteId))
					return d;
			return null;
		}

		public ArrayList<Delyta>getDelytor() {
			return dy;
		}

		public void updateDelyta(int index, String[] tag) {
			Delyta d = dy.get(index);
			d.setPoints(tag);
		}
	}


	protected class Ruta {
		private String myId;

		private ArrayList<Provyta> provytor = new ArrayList<Provyta>();

		public Ruta(String id) {
			myId = id;
		}

		public String getId() {
			return myId;
		}

		private void addDelYta(String provYteId,String delyteId,String[] raw) {

			if (provYteId != null) {
				Provyta _py = findProvYta(provYteId);
				if (_py==null) {
					Log.e("NILS","Provyta with id "+provYteId+" not  found in rutdata but found in delningsdata");
					//_py = new ProvYta(provYteId);
					//py.add(_py);

				} else
					_py.addDelyta(delyteId, raw);
			}
		}

		public Provyta addProvYta_rutdata(String ytId, String north, String east, String lat, String longh) {
			Provyta yta = new Provyta(ytId);
			try {

				yta.setSweRef(Double.parseDouble(north),Double.parseDouble(east));
				Log.d("NILS","Adding Yta ID:  N E:"+ytId+" "+ Double.parseDouble(north)+" "+Double.parseDouble(east));
				yta.setGPS(Double.parseDouble(lat),Double.parseDouble(longh));
			} catch (NumberFormatException e) {
				Log.d("NILS","The center coordinates for yta "+ytId+" are not recognized as proper doubles");
				return null;
			}
			provytor.add(yta);
			//Add default 0 delyta.
			yta.addDelyta("0", null);
			return yta;
		}
		public ArrayList<Provyta> getAllProvYtor() {
			return provytor;
		}

		public Sorted sort() {
			Sorted s = new Sorted();
			return s;
		}

		public class Sorted {
			double[] N = new double[provytor.size()];
			double[] E = new double[provytor.size()];
			public Sorted() {
				int i = 0;
				for(Provyta y:provytor) {
					N[i]= y.N;
					E[i]= y.E;
					Log.d("NILS","SN: "+y.N+" SE: "+y.E);
					i++;
				}
				Arrays.sort(N);
				Arrays.sort(E);
			}
			//return minx,miny,maxx,maxy
			public double getMax_N_sweref_99() {
				return N[N.length-1];
			}
			public double getMax_E_sweref_99() {
				return E[E.length-1];
			}
			public double getMin_N_sweref_99() {
				return N[0];
			}
			public double getMin_E_sweref_99() {
				return E[0];
			}
		}



		public Provyta findProvYta(String ytId) {
			for(Provyta y:provytor) {
				if(y.id.equals(ytId)) {
					return y;
				}

			}
			return null;
		}


	}

	public Ruta findRuta(String id) {
		if (id == null)
			return null;
		for (Ruta r:rutor) 
			if (r.getId().equals(id))
				return r;
		return null;
	}

	public String[] getRutIds() {
		if (rutor != null) {
			String[] contents = new String[rutor.size()];		
			int i=0;
			for (Ruta r:rutor)
				contents[i++]=r.getId();
			return contents;
		}
		return null;
	}

	public ArrayList<Delyta> getDelytor(String rutId, String provyteId) {
		Ruta r = findRuta(rutId);
		if (r!=null) {
			Log.d("NILS","found ruta "+ rutId);
			Provyta p = r.findProvYta(provyteId);
			if (p!=null) {
				Log.d("NILS","Found provyta"+ provyteId);			
				return (p.getDelytor());
			} else {
				Log.e("NILS","DID NOT FIND Provyta for id "+provyteId);
				//TODO: Files must contains same provytor!
				//Fix for now: Generate default if missing.
				//p.addDelyta("1", null);
				//r.addProvYta(provyteId, "1", null);
				//return getDelytor(rutId,provyteId);
			}
		} else
			Log.e("NILS","DID NOT FIND RUTA "+ rutId);
		return null;
	}
	//scan csv file for Rutor. Create if needed.
	private void scanRutData(InputStream csvFile) {
		InputStreamReader is = new InputStreamReader(csvFile);
		BufferedReader br = new BufferedReader(is);
		String header;
		try {
			String row;
			header = br.readLine();
			Log.d("nils",header);
			//Find all RutIDs from csv. Create Ruta Class for each.
			while((row = br.readLine())!=null) {
				String  r[] = row.split(",");
				if (r!=null&&r.length>3) {
					Log.d("NILS",r[0]);
					Ruta ruta=findRuta(r[0]);
					if (ruta ==null) {
						ruta = new Ruta(r[0]);
						rutor.add(ruta);
					}
					int id = Integer.parseInt(r[1]);
					//Skip IDs belonging to inner ytor.
					if (id>12&&id<17)
						continue;
					if (ruta.addProvYta_rutdata(r[1],r[2],r[3],r[7],r[8])!=null)
						Log.d("NILS","added provyta with ID "+r[1]);
					else
						Log.d("NILS","discarded provyta with ID "+r[1]);

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Calculate the distance between smallest and biggest x,y values
		//This is done to be able to calculate the grid position.
		Log.d("NILS","checking minmax...");
		for (Ruta r:rutor) {
			Ruta.Sorted s = r.sort();
			Log.d("NILS","Ruta with id "+r.getId()+" has minxy: "+s.getMin_E_sweref_99()+" "+s.getMin_N_sweref_99()+
					" and maxXy: "+s.getMax_E_sweref_99()+" "+s.getMax_N_sweref_99());
		}
	}

	//scan csv file for Rutor. Create if needed.
	private void scanDelningsData(InputStream csvFile) {
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
					Ruta ruta = findRuta(r[2]);
					//if this is a new ruta, add it to the array
					if (ruta!=null) {
						//Extract the delningståg out from the data.
						String[] points = new String[noPo];
						System.arraycopy(r, 6, points, 0, noPo);
						ruta.addDelYta(r[4],r[5],points);
					}
					//TODO: Add this as ELSE when the files match. 
					//Currently only Rutor from Rutdata will matter.
					/* ruta = new Ruta(r[2]);
						rutor.add(ruta);
					 */

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Calculate the distance between smallest and biggest x,y values
		//This is done to be able to calculate the grid position.

	}





}
