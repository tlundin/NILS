/**
 * 
 */
package com.teraim.nils;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * @author Terje
 *
 */
public class SelectRuta extends ListActivity {
	Rutdata rd=null;
	String[] values;
	
	  public void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    rd = new Rutdata(getResources().openRawResource(R.raw.rutdata));
		   
		    rd.scan();
		    values = rd.getRutIds();
		    if (values == null) {
		    	values = new String[1];
		    	values[0]="Oops...no data found";
		    }
		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		        R.layout.ruta_list_layout, values);
		    setListAdapter(adapter);
		    
		  }
		
	  protected void onListItemClick (ListView l, View v, int position, long id) {
		  Log.d("NILS", "I was clicked "+position+" should be "+values[position]);
			final Intent intent = new Intent(getBaseContext(),SelectYta.class);
			Bundle b = new Bundle();
			b.putString("ruta", values[position]);
			intent.putExtras(b);
			startActivity(intent);
	  }
	  
	  public void onStart()
		{
			super.onStart();
			Log.d("NILS", "In the onStart() event in SelectRuta");

		}
}