package com.teraim.nils;


import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class Main extends Activity {

	private static final long INITIAL_DELAY = 2000; //pause for 2 secs to show logo.
	private String tag = "Lifecycle";
	//ListView treeList = null;
	private Activity me;
	private CommonVars cv;
	private DataTypes T;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this,R.xml.myprefs, false);
		CommonVars.init(this);
		//Get the instance.
		cv = CommonVars.cv();

		if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
				== ConnectionResult.SUCCESS)
			Toast.makeText(this, "Google Services found", Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, "Google Services not found: "+
					GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
		, Toast.LENGTH_LONG).show();
		
		oonCreate(savedInstanceState);
			
	}
	
	
	//@Override
	public void oonCreate(Bundle savedInstanceState) {
		//super.onCreate(savedInstanceState);

		//show start picture
		setContentView(R.layout.loadscreen);

		//broadcastreceiver that will listen for bluetooth on/off.
		//When bluetooth is started, the sync service will be started as well.

		//Layer between application and persistent storage.
		//The persistent storage used is Shared Preferences
		//http://developer.android.com/guide/topics/data/data-storage.html#pref
		PreferenceManager.setDefaultValues(this,
				R.xml.myprefs, false);

		me = this;

		//Initialize common vars.
		CommonVars.init(this);
		//Get the instance.
		cv = CommonVars.cv();

		//Load workflow bundle
		//cv.setWorkflows(WorkflowParser.parse(this));

		//Load datatypes
		T = DataTypes.getSingleton(this);

		//check bluetooth
		if (mBluetoothAdapter == null) {
			new AlertDialog.Builder(this).setTitle("Ups!")
			.setMessage("Din platta verkar inte stödja Blåtand. Utan blåtand fungerar inte den här versionen.")
			.setNeutralButton("Jag förstår!", new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					checkConditions();
				}})
				.show();
			//check that there is a bonded device 
		} else {	
			if (mBluetoothAdapter.isEnabled() && mBluetoothAdapter.getBondedDevices().isEmpty()) {
				new AlertDialog.Builder(this).setTitle("Ups!")
				.setMessage("Din datainsamlare är inte kopplad (bondad) till en annan datainsamlare! Måste göras i systemets blåtandsmeny. Annars fungerar inte synkroniseringen!")
				.setNeutralButton("Jag förstår!", new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						checkConditions();
					}})
					.show();				
			} else {
				//mBluetoothAdapter.getBondedDevices().iterator().next().getBluetoothClass().
				Intent in = new Intent(this,BluetoothRemoteDevice.class);
				//If bluetooth supported, start the communication server.
				startService(in);				
				//Delay a little while so that the start pic is visible.			
				Handler mHandler = new Handler();
				mHandler.postDelayed(new Runnable() {
					public void run() {
						checkConditions();
					}
				}, INITIAL_DELAY);
			}
		}
	}

	final BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
	final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

	// askForColor,askForRuta;
	final static int ASK_RUTA_RC = 1;
	final static int ASK_COLOR_RC = 2;
	final static int REQUEST_ENABLE_BT = 3;
	final static int START_LISTENING_FOR_OTHER_DEVICE = 4;
	//final static int ASK_YTA_RC = 3; 

	/*private void checkConditions2() {
		final Intent testGPS = new Intent(getBaseContext(),TestGpsActivity.class);
		startActivity(testGPS);

	}
	 */

	private void checkConditions() {
		//final Intent testGPS = new Intent(getBaseContext(),TestGpsActivity.class);
		final Intent selectRutaIntent = new Intent(getBaseContext(),SelectRuta.class);
		final Intent selectColorIntent = new Intent(getBaseContext(),SelectColor.class);
		final Intent startMenuIntent = new Intent(getBaseContext(),StartMenuActivity.class);

		//If Color not set, check.
		String deviceColor = cv.getDeviceColor();		
		boolean askForColor = (deviceColor.equals(CommonVars.UNDEFINED));
		//Start the server listening thread..

		if (askForColor) {
			startActivityForResult(selectColorIntent,ASK_COLOR_RC);	
		}
		else {
			//if Ruta is not known, check.
			String currentRuta = cv.getG("ruta_id");
			boolean askForRuta = (currentRuta.equals(CommonVars.UNDEFINED));
			if (askForRuta) {
				startActivityForResult(selectRutaIntent,ASK_RUTA_RC);
			}
			else {
				//If Rutaid known, create the Ruta from the input files.
				//TODO: Some error checking needed here...
				cv.setRuta(T.findRuta(cv.getG("ruta_id")));
				//startActivity(startMenuIntent); 
				WorkflowParser wfp = new WorkflowParser();
				wfp.execute(this);
				finish();
			}
		}

	}




	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_ENABLE_BT) 
		{
			if (resultCode == Activity.RESULT_OK) 
			{
				Log.d("NILS","Bluetooth is now set");

			} else {
				Log.e("NILS","Bluetooth was NOT turned on");

			}
			checkConditions();
		}


		if (requestCode == ASK_COLOR_RC) //check if the request code is the one you've sent
		{
			if (resultCode == Activity.RESULT_OK) 
			{
				Log.d("NILS","Color is now set");
				checkConditions();
			} else 
				Log.e("NILS","Color dialog was not executed properly.");
		}
		else if (requestCode == ASK_RUTA_RC) //check if the request code is the one you've sent
		{
			if (resultCode == Activity.RESULT_OK) 
			{
				Log.d("NILS","Ruta is now set");
				checkConditions();
			}else 
				Log.e("NILS","Ruta dialog was not executed properly.");
		}

		/*else if (requestCode == ASK_YTA_RC)
	{
		if (resultCode == Activity.RESULT_OK) 
			Log.d("NILS","Yta is now set");
		finish();

	}
		 */



	}


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
