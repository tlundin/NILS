package com.teraim.nils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ParameterCache implements Serializable {
	
	Map<String,String> sp = new HashMap<String,String>();

	//Generic function to get a specific String key from shared prefs.
	public String get(String key) {
		return sp.get(key);
	}
	//Global variable put.
	public void put(String key, String value) {
		sp.put(key,value);
	}
	
	public Map<String,String> getParameters() {
		return sp;
	}
	
	public void freeze() {
		
	}
}
