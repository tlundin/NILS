package com.teraim.nils;

import android.content.Context;

public class CommonVars {

	
	
	//The current test surface
	//TODO: REMOVE
	private static int ytID=1;
	
	//The current "ruta"
	private static String rutaId=null;
	static PersistenceManager pm = null;
	//Constants
	
	public static String NILS_BASE_DIR = "/nils";

	private static String provytaId;

	public static int getCurrentYtID() {
		return ytID;
	}
	
	

	public static String getRutaId() {
		return rutaId;
	}



	public static void setRutaId(String rutaId) {
		CommonVars.rutaId = rutaId;
	}



	public static void setCurrentYtID(int ytID) {
		CommonVars.ytID = ytID;
	}
	public static String compassToString(int compass) {
		return (compass==0?"delyta":(compass==1?"ost":(compass==2?"vast":(compass==3?"norr":(compass==4?"syd":null)))));
	}

	//A singleton pointing to the persistencemanager. The PM will allow saving and retrieving parameters from the SD card.
	public static void startPersistenceManager(final Context ctx) {
		pm = PersistenceManager.getSingleton(ctx);
	}



	public static void setProvytaId(String ytID) {
		provytaId = ytID;
	}
	public static String getProvytaId() {
		return provytaId;
	}
	
	

}
