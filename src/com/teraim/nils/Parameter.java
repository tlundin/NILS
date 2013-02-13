package com.teraim.nils;

import java.io.Serializable;

public class Parameter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String mkey,mvalue;
	
	Parameter(String key, String value) {
		mkey  = key;
		mvalue = value;
	}
}

