package com.teraim.nils;

/**
 * @author Terje 
 * 
 * This is the Common Vars class giving access to global state stored in the Persisted memory.
 * For now, persistence implemented via SharedPreferences only.
 */

import java.io.File;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.teraim.nils.DataTypes.Delyta;
import com.teraim.nils.DataTypes.Provyta;
import com.teraim.nils.DataTypes.Ruta;
import com.teraim.nils.exceptions.SharedPrefMissingException;

public class CommonVars {

	private static CommonVars singleton = null;
	
	//Shared Preferences
	private SharedPreferences sp = null;
	
	
	//NILS uid
	public static final UUID RED_UID = UUID.fromString("58500d27-6fd9-47c9-bf6b-d0969ce78bb3");
	public static final UUID BLUE_UID = UUID.fromString("ce8ec829-30e3-469b-886e-6cf8f1168e98");
		
	
	//Static methods
	public static String compassToPicName(int compass) {
		return (compass==0?"vast":(compass==1?"norr":(compass==2?"syd":(compass==3?"ost":null))));
	}
	
	

	//Static constants
	public final static String[] colors = {"Röd","Blå","Ofärgad"};

	public static final String TRUE = "true";
	public static final String FALSE= "false";

	public static String blue() {
		return colors[1];
	}
	public static String red() {
		return colors[0];
	}
	public static String nocolor() {
		return colors[2];
	}
	
	public UUID getmyUUID() {
		/*
		String myC = getDeviceColor();
		if (myC.equals(nocolor()))
			return null;
		else if (myC.equals(red()))
			return RED_UID;
		else
		*/
			return BLUE_UID;
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

	private Context ctx;

	private SharedPreferences rutaP;
	private SharedPreferences ytaP;
	private SharedPreferences delytaP;
	
	
	
	private CommonVars(Context ctx) throws SharedPrefMissingException  {
			
		this.ctx = ctx;
			sp=PreferenceManager.getDefaultSharedPreferences(ctx);
    		if (sp == null)
    			throw new SharedPrefMissingException();
 			
	}
	
	public static String NILS_BASE_DIR = "/nils";
		
	public static String UNDEFINED = "undefined";

	//Root object for the data structure.
	private Ruta myRuta = null;
	private Delyta myDelyta = null;
	private Provyta myProvyta = null;
	
	//The application context
	
	public Context getContext() {
		return ctx;
	}
	
	
	
	
	
	//getter & setter for current ruta,provyta,delyta..
	
	public void setRuta(Ruta r) {
		myRuta = r;
	}
	
	public Ruta getRuta() {
		return myRuta;
	}
	
	public void setDelyta(Delyta d) {
		myDelyta = d;
	}
	
	public Delyta getDelyta() {
		return myDelyta;
	}
	
	public void setProvyta(Provyta p) {
		myProvyta = p;
	}
	
	public Provyta getProvyta() {
		return myProvyta;
	}

	//Persisted variables.

	public void setDeviceColor(String color) {
		sp.edit().putString("deviceColor", color).commit();
		
	}
	
	public String getDeviceColor() {
		return sp.getString("deviceColor", UNDEFINED);		
	}

	public String getRemoteDeviceColor() {
		String myC = getDeviceColor();
		return (myC==null||myC.equals(UNDEFINED)||myC.equals(nocolor())?null:
			myC.equals(red())?blue():red());
	}
	//Generic function to get a specific String key from shared prefs.
	public String getG(String key) {
		return sp.getString(key,UNDEFINED);
	}
	//Global variable put.
	public void putG(String key, String value) {
		sp.edit().putString(key,value).commit();
	}
	
	//PUT for specific ruta & provyta & delyta.
	public void putD(String key, String value){
		delytaP.edit().putString(key, value);
	}
	
	//GET for specific ruta & provyta & delyta.
	public String getD(String key) {
		return delytaP.getString(key,UNDEFINED);
	}
	
	//PUT for specific ruta & provyta .
	public void putP(String key, String value){
		ytaP.edit().putString(key, value);
	}
	
	//GET for specific ruta & provyta .
	public String getP(String key) {
		return ytaP.getString(key,UNDEFINED);
	}
	
	//PUT for specific ruta 
	public void putR(String key, String value){
		rutaP.edit().putString(key, value);
	}
	
	//GET for specific ruta 
	public String getR(String key) {
		return rutaP.getString(key,UNDEFINED);
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
		NILS_BASE_DIR+"/ruta/"+
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
