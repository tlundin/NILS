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
 * If Color of Device not set, ask user which color to use.
*	This could be replaced later.

 */

public class SelectColor extends ListActivity {

	Rutdata rd=null;	
	  public void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    //Get the Singleton instance of RutData.
		    
		    //Send Ids ro Adapter. Display.
		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		        R.layout.ruta_list_layout, CommonVars.colors);
		    setListAdapter(adapter);
		    
		  }
		
	  protected void onListItemClick (ListView l, View v, int position, long id) {
		  Log.d("NILS", "I was clicked "+position+" should be "+CommonVars.colors[position]);
		  	//Persist the Current Color ID in storage.
			CommonVars.cv().setDeviceColor(CommonVars.colors[position]);
			Intent intent = getIntent();
			setResult(Activity.RESULT_OK, intent);  //now you can use Activity.RESULT_OK, its irrelevant whats the resultCode    
		    finish(); //finish the startNewOne activity
			
	  }
	  
	  public void onStart()
		{
			super.onStart();
			Log.d("NILS", "In the onStart() event in SelectColor");

		}
}
