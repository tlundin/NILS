package com.teraim.nils.dynamic.types;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.teraim.nils.GlobalState;
import com.teraim.nils.StoredVariable;
import com.teraim.nils.StoredVariable.Type;

public class Ruta extends ParameterCache {
	private String myId;
	//private Context ctx;
	GlobalState gs;
	private ArrayList<Provyta> provytor = new ArrayList<Provyta>();

	public Ruta(GlobalState gs,String id) {
		super(gs);
		this.gs = gs;
		myId = id;
	}

	public String getId() {
		return myId;
	}

	public void addDelYta(String provYteId,String delyteId,String[] raw) {

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
		Provyta yta = new Provyta(ytId,this);
		try {

			yta.setSweRef(Double.parseDouble(north),Double.parseDouble(east));
			//Log.d("NILS","Adding Yta ID:  N E:"+ytId+" "+ Double.parseDouble(north)+" "+Double.parseDouble(east));
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

	public LatLng[] getCorners() {
		//North south
		double[] lat = new double[provytor.size()];
		//East west
		double[] lon = new double[provytor.size()];
		int i = 0;

		for(Provyta y:provytor) {
			lat[i]= y.lat;
			lon[i]= y.longh;
			//Log.d("NILS","SN: "+y.N+" SE: "+y.E);
			i++;
		}
		Arrays.sort(lat);
		Arrays.sort(lon);
		LatLng[] ret = new LatLng[2];
		//sw
		ret[0] = new LatLng(lat[0],lon[0]);
		//ne
		ret[1]= new LatLng(lat[lat.length-1],  lon[lon.length-1]);
		return ret;
	}

	public class Sorted {
		double[] N = new double[provytor.size()];
		double[] E = new double[provytor.size()];
		public Sorted() {
			int i = 0;
			for(Provyta y:provytor) {
				N[i]= y.N;
				E[i]= y.E;
				//Log.d("NILS","SN: "+y.N+" SE: "+y.E);
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
			if(y.getId().equals(ytId)) {
				return y;
			}

		}
		Log.d("nils","Couldn't find provyta with ID "+ytId);
		return null;
	}

	@Override
	public StoredVariable getVariable(String varId) {
		if (myId == null) {
			Log.e("nils","My ID was null in getVariable Ruta for variable: "+varId);
			return null;
		} else

			return getRutVariable(myId, varId);
	}

	@Override
	public StoredVariable storeVariable(String varId, String value) {
		return this.storeVariable(new StoredVariable(this.getId(), null, null,
				value, 	varId,
				Type.ruta));

	}

	public GlobalState getContext() {
		return gs;
	}
	
	


}