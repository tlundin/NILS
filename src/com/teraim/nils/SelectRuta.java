/**
 * 
 */
package com.teraim.nils;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * @author Terje
 * If Ruta not set, ask user which ruta to use.
*	This could be replaced later.

 */
public class SelectRuta extends ListActivity {
	Rutdata rd=null;
	String[] values;
	
	  public void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    //Get the Singleton instance of RutData.
		    rd = Rutdata.getSingleton(this);	  
		    //Get the IDs
		    values = rd.getRutIds();
		    for(String s:values) 
		    	Log.d("NILS","SELECTRUTA: "+s);
		    if (values == null) {
		    	values = new String[1];
		    	values[0]="Oops...no data found";
		    }
		    //Send Ids ro Adapter. Display.
		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		        R.layout.ruta_list_layout, values);
		    setListAdapter(adapter);
		    
		  }
		
	  protected void onListItemClick (ListView l, View v, int position, long id) {
		  Log.d("NILS", "I was clicked "+position+" should be "+values[position]);
		  	//Persist the Current Ruta ID in storage.
			CommonVars.cv().setRutaId(values[position]);
			Intent intent = getIntent();
			setResult(Activity.RESULT_OK, intent);  //now you can use Activity.RESULT_OK, its irrelevant whats the resultCode    
		    finish(); //finish the startNewOne activity
			
	  }
	  
	  public void onStart()
		{
			super.onStart();
			Log.d("NILS", "In the onStart() event in SelectRuta");

		}
}