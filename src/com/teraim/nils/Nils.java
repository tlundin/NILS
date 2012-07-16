package com.teraim.nils;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;



public class Nils extends ListActivity {

	String tag = "Lifecycle";
	//Dbhelper db = new Dbhelper(this);
	
	ListView treeList = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//setContentView(R.layout.activity_nils);
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
		Intent myIntent = new Intent(getBaseContext(),FindAreaActivity.class);
		startActivity(myIntent);
	}

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

}
