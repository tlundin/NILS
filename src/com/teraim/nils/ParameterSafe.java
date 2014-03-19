package com.teraim.nils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


//Object used to save state of stuff if things go booboo

public class ParameterSafe implements Serializable {
	
	
	private static final long serialVersionUID = 176168233268854817L;

	List<Integer> prevRutor,prevProvytor;
	
	Set<Integer> avslutadeRutor;
	
	String cR,cP,cD;

	
	public ParameterSafe() {
		prevRutor = new ArrayList<Integer>();
		prevProvytor = new ArrayList<Integer>();
		avslutadeRutor = new HashSet<Integer>();
	}
	
	
	/**
	 * @return the prevRutor
	 */
	public List<Integer> getPrevRutor() {
		return prevRutor;
	}
	
	public int getNumberOfAvslutadeRutor() {
		return avslutadeRutor.size();
	}

	/**
	 * @param prevRutor the prevRutor to set
	 */
	
	//Alla currents..current ruta etc.
	public void setCurrents(String r,String p, String d) {
		cR=r;
		cP=p;
		cD=d;
	}


	public List<Integer> getPrevYtor() {
		return prevProvytor;
	}

}
