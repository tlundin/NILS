package com.teraim.nils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.teraim.nils.DataTypes.Delyta;
import com.teraim.nils.DataTypes.Provyta;
import com.teraim.nils.DataTypes.Ruta;

public abstract class RB_Activity extends Activity {

	protected int[] ids;
	protected Delyta delyta=null;
	private int c = 0;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Global parameter transfer of delyta.
		delyta = CommonVars.cv().getDelyta();

	}

	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked
		int id = (view.getId());

		for (int i = 0; i< ids.length;i++)
			if (ids[i]==id) {

				final String value = genName(i);
				Log.d("NILS","Generated name "+value);
				//Save the parameter.
				delyta.put("markslag", value);   
				CommonVars.cv().sendParameter(this,"markslag",value,-1);
				//BluetoothRemoteDevice.getSingleton().sendParameter("markslag", value,-1);
				/*ServiceConnection serviceConnection = new ServiceConnection() {

					@Override
					public void onServiceDisconnected(ComponentName name) {
						Log.d("NILS","Disconeected"  );
					}

					@Override
					public void onServiceConnected(ComponentName name, IBinder service) {
						final BluetoothRemoteDevice localservice;
						localservice = ((BluetoothRemoteDevice.LocalBinder) service).getBinder();
						try {
							Log.d("NILS", "trying to send message to service: "+value);
							localservice.sendParameter("markslag", value,-1);

						} catch (Exception e) {
						}
					}
				};
				if (getApplicationContext().bindService(new Intent("com.teraim.nils.StartService"), serviceConnection, BIND_AUTO_CREATE))
					Toast.makeText(this, "BOUND", Toast.LENGTH_LONG);
				else
					Log.d("BOUND","NOT BOUND");
					
				*/
			}
		finish();
	}



	public void add(int x) {
		ids[c++]=x;
	}

	protected abstract String genName(int id);


}
