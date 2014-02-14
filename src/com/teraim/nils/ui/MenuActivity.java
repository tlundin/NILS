package com.teraim.nils.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.teraim.nils.GlobalState;
import com.teraim.nils.Logger;
import com.teraim.nils.R;
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
					Log.d("nils","Broadcastreceiver refresh statusrow!");
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
	public boolean onPrepareOptionsMenu(Menu menu) {
	    super.onPrepareOptionsMenu(menu);
	   refreshStatusRow();
	    return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return MenuChoice(item);
	}

	private final static int NO_OF_MENU_ITEMS = 4;
	MenuItem mnu[] = new MenuItem[NO_OF_MENU_ITEMS];
	private void CreateMenu(Menu menu)
	{

		for(int c=0;c<mnu.length-1;c++) {
			mnu[c]=menu.add(0,c,c,"");
			mnu[c].setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);	

		}
		mnu[mnu.length-1]=menu.add(0,mnu.length-1,mnu.length-1,"");
		mnu[mnu.length-1].setIcon(android.R.drawable.ic_menu_preferences);
		mnu[mnu.length-1].setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		//mnu5.setIcon(android.R.drawable.ic_menu_preferences);
		//mnu5.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		//refreshStatusRow();
	}

	protected void refreshStatusRow() {
		Log.d("NILS","Refreshing status row ");
		int c=0;
		String pid = ph.get(PersistenceHelper.CURRENT_PROVYTA_ID_KEY);
		String rid = ph.get(PersistenceHelper.CURRENT_RUTA_ID_KEY);
		mnu[c++].setTitle("Ruta/Provyta: "+rid+"/"+pid);
		mnu[c++].setTitle("LOG");
		mnu[c++].setTitle("Synkning: "+gs.getSyncStatusS());
		
		//mnu[c++].setTitle("Användare: "+gs.getPersistence().get(PersistenceHelper.USER_ID_KEY));
		//mnu[c++].setTitle("Typ: "+gs.getDeviceType());
		if(!ph.getB(PersistenceHelper.DEVELOPER_SWITCH)) {
			Log.d("nils","devswitch off");
			mnu[1].setVisible(false);
		}
		else {
			Log.d("nils","devswitch on");
			mnu[1].setVisible(true);
		}
		

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
			final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.log_dialog_popup);
            dialog.setTitle("Session Log");
            final TextView tv=(TextView)dialog.findViewById(R.id.logger);
            Typeface type=Typeface.createFromAsset(getAssets(),
    		        "clacon.ttf");
    		tv.setTypeface(type);
            final Logger log = gs.getLogger();
            log.setOutputView(tv);
            //trigger redraw.
            log.draw();
            Button close=(Button)dialog.findViewById(R.id.log_close);
            dialog.show();
            close.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
            Button clear = (Button)dialog.findViewById(R.id.log_clear);
            clear.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					log.clear();
				}
			});
			
		break;
		case 2:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Synkronisering")
			.setMessage("Vill du "+(gs.getSyncStatus()==BluetoothRemoteDevice.SYNK_STOPPED?"slå på ":"stänga av ")+"synkroniseringen?").setPositiveButton("Ja", dialogClickListener)
			.setNegativeButton("Nej", dialogClickListener).show();
			break;

		case 0:
		case 3:
			Intent intent = new Intent(getBaseContext(),ConfigMenu.class);
			startActivity(intent);
			return true;
		}
		return false;
	}

	
}
