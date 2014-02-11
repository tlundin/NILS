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
	public static final String CONFIG_LOCATION = "config_name";
	public static final String BUNDLE_LOCATION = "bundle_name";
	public static final String SERVER_URL = "server_location";
	public static final String CURRENT_VERSION_OF_WF_BUNDLE = "current_version_wf";
	public static final String CURRENT_VERSION_OF_CONFIG_FILE = "current_version_config";
	public static final String POWER_USER_KEY = "Powar Uzaarrr";
	public static final String FIRST_TIME_KEY = "firzzt";

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
	


}
