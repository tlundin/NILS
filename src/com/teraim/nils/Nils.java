package com.teraim.nils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;



public class Nils extends Activity {

	String tag = "Lifecycle";
	Dbhelper db = new Dbhelper(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nils);
		db.copyDBIfNeeded();


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_nils, menu);
		return true;
	}

	public void onStart()
	{
		super.onStart();
		Log.d(tag, "In the onStart() event");
		//---get all params---
		db.open();
		Log.d("NILS","db opened!");
		Cursor c = db.getAllParams();
		if (c.moveToFirst())
		{
			Log.d("NILS","Move to first ok");
			do {
				displayParams(c);
			} while (c.moveToNext());
		}
		db.close();}
	
	
	
	
	public void onRestart() {
		super.onRestart();
		Log.d(tag, "In the onRestart() event");
	}
	
	public void onResume()
	{
		super.onResume();
		Log.d(tag, "In the onResume() event");
	}
	public void onPause()
	{
		super.onPause();
		Log.d(tag, "In the onPause() event");
	}
	public void onStop()
	{
		super.onStop();
		Log.d(tag, "In the onStop() event");
	}
	public void onDestroy()
	{
		super.onDestroy();
		Log.d(tag, "In the onDestroy() event");
	}

	public void displayParams(Cursor c)
	{
		Toast.makeText(this,
				"id: " + c.getString(1) + "\n" +
						"Name: " + c.getString(0) + "\n" +
						"Value: " + c.getString(2),
						Toast.LENGTH_LONG).show();
	}

}
