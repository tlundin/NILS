package com.teraim.nils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
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
		double x;
		double y;
	}
	
	
	protected class Ruta {
		String id;
		ArrayList<Yta> ytor = new ArrayList<Yta>();
		public Yta addYta(String ytId, String x, String y) {
			Yta yta = new Yta();
			try {
				yta.x = Double.parseDouble(x);
				yta.y = Double.parseDouble(y);
			} catch (NumberFormatException e) {
				Log.d("NILS","The yta coordinates 1."+x+" 2."+y+" are not recognized as proper doubles");
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
				v=y.x;
				if (v>ret[2])
					ret[2]=v;
				if(v<ret[0])
					ret[0]=v;
				v=y.y;
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
						if (ruta.addYta(r[1],r[3],r[2])!=null)
							Log.d("NILS","added yta with ID "+r[1]);
				
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
