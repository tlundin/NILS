package com.teraim.nils;

import android.content.Context;

public class CommonVars {

	
	
	//The current test surface
	static int ytID=1;
	static PersistenceManager pm = null;
	//Constants
	
	public static String NILS_BASE_DIR = "/nils";

	public static int getCurrentYtID() {
		return ytID;
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

}
