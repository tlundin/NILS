package com.teraim.nils;

/**
 * @author Terje 
 * 
 * This is the Common Vars class giving access to global state stored in the Persisted memory.
 * For now, persistence implemented via SharedPreferences only.
 */

import java.io.File;

import com.teraim.nils.exceptions.SharedPrefMissingException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class CommonVars {

	private static CommonVars singleton = null;
	
	//private static Context ctx;
	private SharedPreferences sp = null;
	
	
	//Static methods
	public static String compassToPicName(int compass) {
		return (compass==0?"vast":(compass==1?"norr":(compass==2?"syd":(compass==3?"ost":null))));
	}

	//Static constants
	public final static String[] colors = {"Röd","Blå","Ofärgad"};

	public static String blue() {
		return colors[1];
	}
	public static String red() {
		return colors[0];
	}
	public static String nocolor() {
		return colors[2];
	}

	public static void init(Context ctx) {
    		try {
				singleton = new CommonVars(ctx);
			} catch (SharedPrefMissingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
    	
		Log.d("NILS","exit init. singleton is "+singleton);
	}
	
	public static CommonVars cv() {
		if (singleton == null)
			Log.d("NILS","Singleton is null");
		return singleton;			 
	}

	private CommonVars(Context ctx) throws SharedPrefMissingException  {
			
			sp=PreferenceManager.getDefaultSharedPreferences(ctx);
    		if (sp == null)
    			throw new SharedPrefMissingException();
 			
	}
	
	public static String NILS_BASE_DIR = "/nils";
		
	public static String UNDEFINED = "undefined";

	//getter & setter for key values.
	
	public String getRutaId() {
		
		return sp.getString("rutId", UNDEFINED);
	}

	public void setRutaId(String rutaId) {
		sp.edit().putString("rutId", rutaId).commit();
	}

	public String getProvytaId() {
		return sp.getString("provytaId", UNDEFINED);
	}
	public void setProvytaId(String ytID) {
		sp.edit().putString("provytaId", ytID).commit();
	}



	public void setDeviceColor(String color) {
		sp.edit().putString("deviceColor", color).commit();
		
		
	}
	
	public String getDeviceColor() {
		return sp.getString("deviceColor", UNDEFINED);
		
	}

	//Generic function to get a specific String key from shared prefs.
	//default val set to null
	public String get(String key) {
		return sp.getString(key,null);
	}
	public void put(String key, String value) {
		sp.edit().putString(key,value).commit();
	}
	public String getUserName() {
		final int MAX_NAME_LENGTH = 16;
		String un = sp.getString("username", "?");
		if (un.length()>MAX_NAME_LENGTH)
			un = un.substring(0, MAX_NAME_LENGTH);
		return un;
	}
	public String getCurrentPictureBasePath() {
		return Environment.getExternalStorageDirectory()+
		NILS_BASE_DIR+"/delyta/"+
		"1"+"/bilder";
	}
	
	public static void createFoldersIfMissing(File file) {
		final File parent_directory = file.getParentFile();

		if (null != parent_directory)
		{
		    parent_directory.mkdirs();
		}
	}
	

}
