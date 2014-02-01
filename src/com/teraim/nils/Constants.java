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
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

public class Constants {

	
	
	//String constants
	//The root folder for the SD card is in the global Environment.
	private final static String path = Environment.getExternalStorageDirectory().getPath();
		//Remember to always add system root path before any app specific path!

	//Root for NILS
	public final static String NILS_ROOT_DIR = path+"/nils/";
	public final static String CONFIG_FILES_DIR = NILS_ROOT_DIR + "config/";
	//public static String NILS_BASE_DIR = "/nils";
	public static String UNDEFINED = "undefined";

	
	//NILS uid
	public static final UUID RED_UID = UUID.fromString("58500d27-6fd9-47c9-bf6b-d0969ce78bb3");
	public static final UUID BLUE_UID = UUID.fromString("ce8ec829-30e3-469b-886e-6cf8f1168e98");
		
	
	//Static methods
	public static String compassToPicName(int compass) {
		return (compass==0?"vast":(compass==1?"norr":(compass==2?"syd":(compass==3?"ost":null))));
	}


	public static final String TRUE = "true";
	public static final String FALSE= "false";

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
	
	public static UUID getmyUUID() {
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

 	//Persisted variables.




	
	
	

	





	

	


}
