package com.teraim.nils.utils;

import java.util.ArrayList;

import android.content.SharedPreferences;

public class PersistenceHelper {
	public static final String UNDEFINED = "";
	public static final String CURRENT_RUTA_ID_KEY = "ruta_id";
	public static final String CURRENT_PROVYTA_ID_KEY = "provyta_id";
	public static final String CURRENT_DELYTA_ID_KEY = "delyta_id";
	public static final String USER_ID_KEY = "user_id";
	public static final String LAG_ID_KEY = "lag_id";
	public static final String MITTPUNKT_KEY = "mittpunkt";
	public static final String DEVICE_COLOR_KEY = "deviceColor";

	SharedPreferences sp;
	
	ArrayList<String> delta = new ArrayList<String>();
	
	public PersistenceHelper(SharedPreferences sp) {
		this.sp = sp;
	}
	

	public String get(String key) {
		return sp.getString(key,UNDEFINED);
	}

	public void put(String key, String value) {
		sp.edit().putString(key,value).commit();
	}
	

	/*
	public void setR(String varId, String value) {
		assert(currentRuta!=null);
		String fullId = currentRuta+"|"+varId;
		put(fullId,value);
		delta.add(fullId);
	}

	public void setP(String varId, String value) {
		assert(currentRuta!=null);
		assert(currentProvyta!=null);
		String fullId = currentRuta+"|"+currentProvyta+"|"+varId;
		put(fullId,value);
		delta.add(fullId);
	}
	public void setD(String varId, String value) {
		assert(currentRuta!=null);
		assert(currentProvyta!=null);
		assert(currentDelyta!=null);
		String fullId = currentRuta+"|"+currentProvyta+"|"+currentDelyta+varId;
		put(fullId,value);
		delta.add(fullId);
	}
	
	*/
	/*
	public StoredVariable getVar(String varId) {
		if (varId == null)
			return null;
		String[] s = varId.split("|");
		if (s==null || s.length==1) {
			Log.e("nils","This does not seem to be a Variable: "+varId);
			return null;
		}
		StoredVariable sv = new StoredVariable();
		//Ruta
		if (s.length==2) {
			sv.rutId = s[0];
			sv.value = s[1];
			sv.type = Type.ruta;
		} else if (s.length==3) {
			sv.rutId = s[0];
			sv.provytaId = s[1];
			sv.value = s[2];
			sv.type = Type.provyta;
		} else if (s.length==4) {
			sv.rutId = s[0];
			sv.provytaId = s[1];
			sv.delytaId = s[3];
			sv.value = s[4];
			sv.type = Type.delyta;
		} else {
			Log.e("nils","This Variable has too many parts: "+varId);
			return null;		
		}
		return sv;
	
	}
	*/
}
