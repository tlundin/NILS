package com.teraim.nils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class RiktpunktListViewActivity extends Activity {
	
	
	String[] texts = new String[] {
			"Björk",
			"Asp",
			"Gran",
			"Ädellöv"
	};
	
	int[] pics = new int[]{
	        R.drawable.bjork,
	        R.drawable.asp,
	        R.drawable.gran,
	        R.drawable.adellov,
	       
	    };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.riktpunktlistview);
		
		// Each row in the list stores country name, currency and flag
        List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();
 
        for(int i=0;i<texts.length;i++){
            HashMap<String, String> hm = new HashMap<String,String>();
            hm.put("txt", texts[i]);
            hm.put("pic", Integer.toString(pics[i]) );
            aList.add(hm);
        }
		
		
         // Keys used in Hashmap
         String[] from = { "txt","pic"};
         
         // Ids of views in listview_layout
         int[] to = {R.id.txt,R.id.pic};
         
         // Instantiating an adapter to store each items
         // R.layout.listview_layout defines the layout of each item
         SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.riktpunklist, from, to);
  
         // Getting a reference to listview of main.xml layout file
         ListView listView = ( ListView ) findViewById(R.id.listview);
  
         // Setting the adapter to the listView
         listView.setAdapter(adapter);
         
         // Defining the item click listener for listView
         OnItemClickListener itemClickListener = new OnItemClickListener() {
        	 public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                 Toast.makeText(getBaseContext(), "You selected :" + texts[position], Toast.LENGTH_SHORT).show();
                 Intent intent = getIntent();
                 intent.putExtra("Selected", texts[position]);
                 setResult(Activity.RESULT_OK,intent);
                 finish();
                 
              }
         };
  
         // Setting the item click listener for listView
         listView.setOnItemClickListener(itemClickListener);
         
         
		
	}
}
