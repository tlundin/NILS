package com.teraim.nils;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**
 * Parent class for Activities having a menu row.
 * @author Terje
 *
 */
public class MenuActivity extends Activity {

	
	private BroadcastReceiver brr;
	
		
		
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		final MenuActivity me = this;

		brr = new BroadcastReceiver() {
			@Override
			public void onReceive(Context ctx, Intent intent) {
					me.refreshStatusRow();
				}
				
				
			};
		//Listen for bluetooth events.
			IntentFilter filter = new IntentFilter();
			filter.addAction(BluetoothRemoteDevice.SYNK_SERVICE_STARTED);
			filter.addAction(BluetoothRemoteDevice.SYNK_SERVICE_STOPPED);
			filter.addAction(BluetoothRemoteDevice.SYNK_SERVICE_CONNECTED);
			filter.addAction(BluetoothRemoteDevice.SYNK_SERVICE_CONNECTED);
			
		this.registerReceiver(brr, filter);
		//Listen for Service started/stopped event.
	
	}


	

	@Override
	protected void onResume() {
		super.onResume();
		refreshStatusRow();
	}

	public void onDestroy()
	{
		super.onDestroy();
		Log.d("NILS", "In the onDestroy() event");
		
		//Stop listening for bluetooth events.
		this.unregisterReceiver(brr);

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

	MenuItem mnu2 = null,mnu3=null,mnu4=null;
	private void CreateMenu(Menu menu)
	{
		mnu2 = menu.add(0, 1, 1, "");
		mnu2.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		mnu3 = menu.add(0, 2, 2, "");
		mnu3.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		mnu4 = menu.add(0, 3, 3,"");
		mnu4.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		MenuItem mnu5 = menu.add(0, 4, 4, "Item 5");
		mnu5.setIcon(android.R.drawable.ic_menu_preferences);
		mnu5.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		refreshStatusRow();
	}

	protected void refreshStatusRow() {
		Log.d("NILS","Refreshing status row");
		if (mnu2!=null)
			mnu2.setTitle("Synkning: "+CommonVars.cv().getSyncStatusS());
		if (mnu3!=null)
			mnu3.setTitle("Användare: "+CommonVars.cv().getUserName());
		if (mnu4!=null)
			mnu4.setTitle("Färg: "+CommonVars.cv().getDeviceColor());
		
	}

	
	private boolean MenuChoice(MenuItem item) {

	
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					//Turn off bluetooth if running
					//This will also turn off the server as a side effect.
					//Intent intent = new Intent();
					//intent.setAction(BluetoothAdapter.ACTION_STATE_CHANGED);
					Intent intent = new Intent(getBaseContext(),BluetoothRemoteDevice.class);
					if (CommonVars.cv().getSyncStatus()==BluetoothRemoteDevice.SYNK_STOPPED) {
						startService(intent);
						//Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				        //startActivity(enableBtIntent);
						//intent.putExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
					}

					else {
						stopService(intent);
						//intent.putExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_ON);
					}
					//getBaseContext().sendBroadcast(intent);
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					break;
				}
			}
		};
		
		switch (item.getItemId()) {
		case 0:
			//Toast.makeText(this, "You clicked on Item 1",
			//		Toast.LENGTH_LONG).show();
		case 1:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Synkronisering")
			.setMessage("Vill du "+(CommonVars.cv().getSyncStatus()==BluetoothRemoteDevice.SYNK_STOPPED?"slå på ":"stänga av ")+"synkroniseringen?").setPositiveButton("Ja", dialogClickListener)
			.setNegativeButton("Nej", dialogClickListener).show();
			break;
		case 2:
			Toast.makeText(this, "Ändra användare",
					Toast.LENGTH_LONG).show();
		case 3:
			Toast.makeText(this, "Ändra färg",
					Toast.LENGTH_LONG).show();
		case 4:
			Intent intent = new Intent(getBaseContext(),ConfigMenu.class);
			startActivity(intent);
			return true;
		}
		return false;
	}

	
}
