package com.teraim.nils;

import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Main extends Activity {

	private static final long INITIAL_DELAY = 2000; //pause for 2 secs to show logo.
	String tag = "Lifecycle";
	//ListView treeList = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Layer between application and persistent storage.
		//The persistent storage used is Shared Preferences
		//http://developer.android.com/guide/topics/data/data-storage.html#pref
		CommonVars.startPersistenceManager(this);
		
		//Load JSON configuration 
		/*
		String jsonStr = null;
		InputStreamReader is = new InputStreamReader(getResources().openRawResource(R.raw.nils_json));
		try {
	       jsonStr = new java.util.Scanner(is).useDelimiter("\\A").next();
	    } catch (java.util.NoSuchElementException e) {
	        jsonStr = "";
	    }
		
		JSONObject json=null;
		JSONArray jsa = null;
		try {
			String tst = jsonStr.substring(70800,70908);
			
			json = new JSONObject(jsonStr);
			//jsa = new JSONArray(buffer.toString());
			//if (jsa != null)
			//	json = jsa.getJSONObject(0);

	
		if (json!=null) {
			String name = (String)json.get("name");
			System.out.println(name);
		}
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		*/
		setContentView(R.layout.loadscreen);
		//treeList = (ListView) findViewById(R.id.treelist);
		///db.copyDBIfNeeded();
		//db.open();
		//Log.d("NILS","gets!");
		//SimpleCursorAdapter mAdapter = db.getAdapter();
		//Log.d("NILS","nogets!");
		//setListAdapter(mAdapter);
		//treeList.setAdapter(mAdapter);
		//treeList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
  
        // capture touches on the listview
		/*
		treeList.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		// store the selected contact for later
        		TextView textView = (TextView) view;
//        		contactToDelete = textView.getText().toString();
        	}
        });
        */
		
		//db.close();
		
		
		//Show start screen
		//
		//XmlPullParser parser = Xml.newPullParser();
	
		
		final Intent myIntent = new Intent(getBaseContext(),SelectRuta.class);
		Handler mHandler = new Handler();
		mHandler.postDelayed(new Runnable() {
            public void run() {
            	startActivity(myIntent);
            	finish();
            }
        }, INITIAL_DELAY);
    }
		
	

	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_nils, menu);
		return true;
	}
    */
	public void onStart()
	{
		super.onStart();
		Log.d(tag, "In the onStart() event");
		/*
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
		db.close();
		
		}
		*/
	}
	
	
	
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
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	CreateMenu(menu);
	return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	return MenuChoice(item);
	}
	
	private void CreateMenu(Menu menu)
	{
	MenuItem mnu1 = menu.add(0, 0, 0, "Item 1");
	{
	mnu1.setIcon(R.drawable.ic_launcher);
	mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}
	MenuItem mnu2 = menu.add(0, 1, 1, "Item 2");
	{
	mnu2.setIcon(R.drawable.ic_launcher);
	mnu2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}
	MenuItem mnu3 = menu.add(0, 2, 2, "Item 3");
	{
	mnu3.setIcon(R.drawable.ic_launcher);
	mnu3.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}
	MenuItem mnu4 = menu.add(0, 3, 3, "Item 4");
	{
	mnu4.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}
	MenuItem mnu5 = menu.add(0, 4, 4, "Item 5");
	{
	mnu5.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}
	}
	private boolean MenuChoice(MenuItem item)
	{
	switch (item.getItemId()) {
	case 0:
	Toast.makeText(this, "You clicked on Item 1",
	Toast.LENGTH_LONG).show();
	return true;
	case 1:
	Toast.makeText(this, "You clicked on Item 2",
	Toast.LENGTH_LONG).show();
	return true;
	case 2:
	Toast.makeText(this, "You clicked on Item 3",
	Toast.LENGTH_LONG).show();
	return true;
	case 3:
	Toast.makeText(this, "You clicked on Item 4",
	Toast.LENGTH_LONG).show();
	return true;
	case 4:
	Toast.makeText(this, "You clicked on Item 5",
	Toast.LENGTH_LONG).show();
	return true;
	}
	return false;
	}
	*/

}
