package com.teraim.nils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.teraim.nils.bluetooth.BluetoothRemoteDevice;
import com.teraim.nils.utils.PersistenceHelper;

/**
 * Parent class for Activities having a menu row.
 * @author Terje
 *
 */
public class MenuActivity extends Activity {

	
	private BroadcastReceiver brr;
	private GlobalState gs;
	private PersistenceHelper ph;
	
		
		
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		final MenuActivity me = this;

		gs = GlobalState.getInstance(this);
		ph = gs.getPersistence();
		
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

	MenuItem mnu1 = null,mnu2 = null,mnu3=null,mnu4=null;
	private void CreateMenu(Menu menu)
	{
		mnu1 = menu.add(0, 0, 0, "");
		mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		mnu2 = menu.add(0, 1, 1, "");
		mnu2.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		mnu3 = menu.add(0, 2, 2, "");
		mnu3.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		mnu4 = menu.add(0, 3, 3,"");
		mnu4.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		MenuItem mnu5 = menu.add(0, 4, 4, "Item 5");
		mnu5.setIcon(android.R.drawable.ic_menu_preferences);
		mnu5.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		refreshStatusRow();
	}

	protected void refreshStatusRow() {
		Log.d("NILS","Refreshing status row");
		if (mnu1!=null) {
			
			String pid = ph.get(PersistenceHelper.CURRENT_PROVYTA_ID_KEY);
			String rid = ph.get(PersistenceHelper.CURRENT_RUTA_ID_KEY);
			
			mnu1.setTitle("Ruta/Provyta: "+rid+"/"+pid);
		}
		if (mnu2!=null)
			mnu2.setTitle("Synkning: "+gs.getSyncStatusS());
		if (mnu3!=null)
			mnu3.setTitle("Användare: "+gs.getPersistence().get(PersistenceHelper.USER_ID_KEY));
		if (mnu4!=null)
			mnu4.setTitle("Typ: "+gs.getDeviceType());
		
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
					if (gs.getSyncStatus()==BluetoothRemoteDevice.SYNK_STOPPED) {
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
			
		case 1:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Synkronisering")
			.setMessage("Vill du "+(gs.getSyncStatus()==BluetoothRemoteDevice.SYNK_STOPPED?"slå på ":"stänga av ")+"synkroniseringen?").setPositiveButton("Ja", dialogClickListener)
			.setNegativeButton("Nej", dialogClickListener).show();
			break;
		case 0:
			Toast.makeText(this,"ändra ruta eller provyta",Toast.LENGTH_LONG).show();
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
