package com.teraim.nils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.location.Location;
import android.util.Log;


/**
 * 
 * @author Terje
 *
 * Helper class for wrapping CSV files containing Ruta coordinates and IDs..
 */
public class Rutdata  {

	static Rutdata singleton;
	public static Rutdata getSingleton(Context c) {
		if (singleton == null) {
			singleton = new Rutdata(c.getResources().openRawResource(R.raw.rutdata));
			singleton.scan();
		}
		return singleton;
	}

	protected class Yta {
		String id;
		double sweLat=0;
		double sweLong=0;
		double lat=0;
		double longh=0;

		public double[] getSweRefCoords() {
			double[] ret = new double[2];
			ret[0]=sweLat;
			ret[1]=sweLong;
			return ret;
		}
		public double[] getLatLong() {
			double[] ret = new double[2];
			ret[0]=lat;
			ret[1]=longh;
			return ret;
		}
		public void setSweRef(double lat, double longh) {
			sweLat = lat;
			sweLong = longh;
		}
		public void setGPS(double lat, double longh) {
			this.lat = lat;
			this.longh = longh;
		}
	}


	protected class Ruta {
		String id;
		ArrayList<Yta> ytor = new ArrayList<Yta>();
		public Yta addYta(String ytId, String swelat, String swelong, String lat, String longh) {
			Yta yta = new Yta();
			try {
				yta.setSweRef(Double.parseDouble(swelat),Double.parseDouble(swelong));
				yta.setGPS(Double.parseDouble(lat),Double.parseDouble(longh));
			} catch (NumberFormatException e) {
				Log.d("NILS","The center coordinates for yta "+ytId+" are not recognized as proper doubles");
				return null;
			}
			yta.id = ytId;
			ytor.add(yta);
			return yta;
		}
		public ArrayList<Yta> getYtor() {
			return ytor;
		}
		//return minx,miny,maxx,maxy
		double[] getMinMaxValues() {
			double[] ret = {999999999,999999999,-1,-1};
			//compare current values. Replace if lower/higher
			for (Yta y:ytor) {
				double v;			
				v=y.sweLong;
				if (v>ret[2])
					ret[2]=v;
				if(v<ret[0])
					ret[0]=v;
				v=y.sweLat;
				if (v>ret[3])
					ret[3]=v;
				if(v<ret[1])
					ret[1]=v;

			}
			return ret;
		}
		public Yta findYta(String ytId) {
			for(Yta y:ytor) {
				if(y.id.equals(ytId)) {
					return y;
				}

			}
			return null;
		}

	}


	InputStream csvFile;
	ArrayList<Ruta> rutor = new ArrayList<Ruta>();

	private Rutdata(InputStream inputStream) {
		csvFile = inputStream;

	}

	public Ruta findRuta(String id) {
		for (Ruta r:rutor) 
			if (r.id.equals(id))
				return r;
		Ruta r = new Ruta();
		r.id = id;
		rutor.add(r);
		return r;
	}

	public String[] getRutIds() {
		if (rutor != null) {
			String[] contents = new String[rutor.size()];		
			int i=0;
			for (Ruta r:rutor)
				contents[i++]=r.id;
			return contents;
		}
		return null;
	}
	//scan csv file for Rutor. Create if needed.
	private void scan() {
		InputStreamReader is = new InputStreamReader(csvFile);
		BufferedReader br = new BufferedReader(is);
		String header="boo";
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
					int id = Integer.parseInt(r[1]);
					//Skip IDs belonging to inner ytor.
					if (id>12&&id<17)
						continue;
					if (ruta.addYta(r[1],r[2],r[3],r[7],r[8])!=null)
						Log.d("NILS","added yta with ID "+r[1]);
					else
						Log.d("NILS","discarded yta with ID "+r[1]);

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Calculate the distance between smallest and biggest x,y values
		//This is done to be able to calculate the grid position.
		Log.d("NILS","checking minmax...");
		for (Ruta r:rutor) {
			double f[] = r.getMinMaxValues();
			Log.d("NILS","Ruta with id "+r.id+" has minxy: "+f[0]+" "+f[1]+
					" and maxXy: "+f[2]+" "+f[3]);
		}
	}





}
