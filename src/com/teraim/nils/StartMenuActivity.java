package com.teraim.nils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class StartMenuActivity extends Activity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	
	Spinner f_spinner,r_spinner;
	DataTypes T;
	Activity me;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.startmenu);
		
		me = this;
		f_spinner = (Spinner) findViewById(R.id.farg_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.deviceColors, R.layout.spinneritem);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		f_spinner.setAdapter(adapter);
		f_spinner.setSelection(adapter.getPosition(CommonVars.cv().getDeviceColor()));
		
		
		r_spinner = (Spinner) findViewById(R.id.rut_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		T = DataTypes.getSingleton(this);	  
		    //Get the IDs
		String[]  values = T.getRutIds();
		if (values == null) {
		    	values = new String[1];
		    	values[0]="Oops...no data found";
		    }

		adapter = new ArrayAdapter<CharSequence>(this,
		        R.layout.spinneritem, values);
	
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		r_spinner.setAdapter(adapter);
		r_spinner.setSelection(adapter.getPosition(CommonVars.cv().getRuta().getId()));
		
		
	}

	public void onButton(View view) {
	     String rutId = (String)r_spinner.getSelectedItem();
	     String deviceCol = (String)f_spinner.getSelectedItem();
	     
	     //Persist this selection
	     CommonVars.cv().putG("ruta_id", rutId);
	     CommonVars.cv().setRuta(T.findRuta(rutId));
	     
	     //Set device color
	     CommonVars.cv().setDeviceColor(deviceCol);
	     
	     //Jump to provyteselection.
	     Intent intent = new Intent(getBaseContext(),SelectYta.class);
	     startActivity(intent);
	 }
	
	/**
	 * Ask user before closing the application.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						me.finish();
						//kill the synkservice if running
						Intent intent = new Intent(getBaseContext(),BluetoothRemoteDevice.class);
						stopService(intent);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						break;
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Avsluta program")
			.setMessage("Vill du verkligen avsluta?").setPositiveButton("Ja", dialogClickListener)
			.setNegativeButton("Nej", dialogClickListener).show();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}