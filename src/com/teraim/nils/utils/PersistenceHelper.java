package com.teraim.nils.utils;

import java.util.ArrayList;

import android.content.SharedPreferences;

public class PersistenceHelper {
	public static final String UNDEFINED = "";
//	public static final String CURRENT_RUTA_ID_KEY = "ruta_id";
//	public static final String CURRENT_PROVYTA_ID_KEY = "provyta_id";
//	public static final String CURRENT_DELYTA_ID_KEY = "delyta_id";
//	public static final String CURRENT_YEAR_ID_KEY = "current_year_id";
	public static final String USER_ID_KEY = "user_id";
	public static final String LAG_ID_KEY = "lag_id";
	public static final String MITTPUNKT_KEY = "mittpunkt";
	public static final String DEVICE_COLOR_KEY = "device_type";
	public static final String CONFIG_LOCATION = "config_name";
	public static final String BUNDLE_LOCATION = "bundle_name";
	public static final String SERVER_URL = "server_location";
	public static final String CURRENT_VERSION_OF_WF_BUNDLE = "current_version_wf";
	public static final String CURRENT_VERSION_OF_CONFIG_FILE = "current_version_config";
	public static final String CURRENT_VERSION_OF_PROGRAM = "prog_version";
	public static final String FIRST_TIME_KEY = "firzzt";
	public static final String DEVELOPER_SWITCH = "dev_switch";
	public static final String VERSION_CONTROL_SWITCH_OFF = "no_version_control";
	public static final String CURRENT_VERSION_OF_VARPATTERN_FILE = "current_version_varpattern";
	public static final String TAG_DATA_HAS_BEEN_READ = "tagdata_read";
	public static final String TIME_OF_LAST_CHANGE = "kakkadua";

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
	public void put(String key, boolean value) {
		sp.edit().putBoolean(key,value).commit();
	}
	
	
	public boolean getB(String key) {
		return sp.getBoolean(key, false);
	}
	
	
	


}
