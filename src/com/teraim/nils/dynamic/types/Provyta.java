package com.teraim.nils.dynamic.types;

import java.util.ArrayList;

import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.StoredVariable;
import com.teraim.nils.StoredVariable.Type;

public class Provyta extends ParameterCache {

	private String id;
	double N=0;
	double E=0;
	double lat=0;
	double longh=0;

	private ArrayList<Delyta>dy = new ArrayList<Delyta>();
	private Ruta myParent;

	public Provyta(String id, Ruta parent) {
		super(parent.getContext());
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

	@Override
	public StoredVariable getVariable(String varId) {
		if (myParent == null) {
			Log.e("nils","Getvariable called on provyta without parent..?");
			return null;
		} else
			return getProvyteVariable(myParent.getId(),id,varId);
	}

	@Override
	public StoredVariable storeVariable(String varId, String value) {
		return this.storeVariable(new StoredVariable(myParent.getId(), this.getId(), null,
				value, 	varId,
				Type.provyta));

	}

	public GlobalState getContext() {
		return myParent.getContext();
	}
}