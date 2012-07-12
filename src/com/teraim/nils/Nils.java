package com.teraim.nils;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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
		Intent myIntent = new Intent(getBaseContext(),TakePictureActivity.class);
		startActivity(myIntent);
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
