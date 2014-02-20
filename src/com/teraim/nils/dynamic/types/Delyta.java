package com.teraim.nils.dynamic.types;

import android.content.Context;
import android.util.Log;

import com.teraim.nils.exceptions.IllegalCallException;

public class Delyta  {
	final int Max_Points = 10;
	private Train tr=null; 
	private final String myId;
	private Provyta myParent;

	public Delyta(String id, Provyta parent, String[] raw) {
		
		myId = id;
		myParent = parent;
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
						e.printStackTrace();
						return false;
					}
				} else {
					try {
						tr.setRikt(val);
					} catch (IllegalCallException e) {
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