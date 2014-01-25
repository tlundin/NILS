package com.teraim.nils;

import java.util.ArrayList;

public class ParameterCache  {
	
	DbHelper db;
	
	public ParameterCache(DbHelper db){
		this.db = db;
	}

	//Generic function to get a specific String key from shared prefs.
	public String getVar(String key) {
		return db.;
	}
	//Global variable put.
	public void putVar(String key, String value) {
		sp.put(key,value);
	}
	
	public ArrayList<StoredVariable> getVars() {
		
	}
	
	public void freeze() {
		
	}
}
