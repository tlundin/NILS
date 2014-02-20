package com.teraim.nils.dynamic.types;

import java.util.ArrayList;

import com.teraim.nils.GlobalState;

public class Provyta  {

	private String id;
	double N=0;
	double E=0;
	double lat=0;
	double longh=0;

	private ArrayList<Delyta>dy = new ArrayList<Delyta>();
	private Ruta myParent;

	public Provyta(String id, Ruta parent) {
		this.id = id;
		myParent = parent;
	}

	public String getId() {
		return id;
	}

	public Ruta getParent() {
		return myParent;
	}

	public double[] getLatLong() {
		double[] ret = new double[2];
		ret[0]=lat;
		ret[1]=longh;
		return ret;
	}
	
	public double[] getSweRef() {
		double[] ret = new double[2];
		ret[0]=N;
		ret[1]=E;
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

		dy.add(new Delyta(delyteId,this,raw));
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



	public GlobalState getContext() {
		return myParent.getContext();
	}
}