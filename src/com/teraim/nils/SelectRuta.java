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

import com.teraim.nils.DataTypes.Ruta;

/**
 * @author Terje
 * If Ruta not set, ask user which ruta to use.
*	This could be replaced later.

 */
public class SelectRuta extends ListActivity {
	DataTypes rd=null;
	String[] values;
	
	  public void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    //Get the Singleton instance of RutData.
		    rd = DataTypes.getSingleton();	  
		    //Get the IDs
		    values = rd.getRutIds();
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
		  	Ruta r = rd.findRuta(values[position]);
		  	if (r!=null) {
		  		//CommonVars.cv().setRuta(r);
		  		//Persist this choice so that next time Ruta will not be queried from user.
		  		
		  		CommonVars.cv().ph.putR("ruta_id",values[position]);
		  	}
		  	 else
		  		Log.e("NILS", "Ruta not found in SelectRuta ID: "+values[position]);
		  	Intent intent = getIntent();
	  		setResult(Activity.RESULT_OK, intent);
		    finish(); 
			
	  }
	  
	  public void onStart()
		{
			super.onStart();
			Log.d("NILS", "In the onStart() event in SelectRuta");

		}
}