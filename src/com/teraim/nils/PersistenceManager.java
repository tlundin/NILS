package com.teraim.nils;

import com.teraim.nils.utils.DbHelper;

import android.content.Context;
import android.content.SharedPreferences;

public class PersistenceManager {
	
	private DbHelper myDb;
	private SharedPreferences tDb;
	
	private PersistenceManager(Context ctx) {
		myDb = new DbHelper(ctx);
		tDb = ctx.getSharedPreferences("NILS_prefs", 0);
		
	}
	public static PersistenceManager getSingleton(Context ctx) {
		return new PersistenceManager(ctx);
	}

	public void persist(String key, String value) {
		SharedPreferences.Editor editor = tDb.edit();
		editor.putString(key, value);
		editor.commit();
	}
	public String get(String key) {
		return tDb.getString(key, null);
	}
}
