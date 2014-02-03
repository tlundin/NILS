package com.teraim.nils.dynamic.types;

import com.teraim.nils.exceptions.IllegalCallException;

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