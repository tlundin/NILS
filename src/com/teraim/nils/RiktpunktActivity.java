/**
 * 
 */
package com.teraim.nils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * @author Terje
 *
 */
public class RiktpunktActivity extends Activity {

	//int[] buttIds = {R.id.rikt1_button};
	int currButton = -1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.riktpunktscreen);
				
		
	}

	public void onSelectRiktPunkt(View v){
		Intent intent = new Intent(this,RiktpunktListViewActivity.class);
		currButton = v.getId();
		startActivityForResult(intent,0);
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		 Toast.makeText(getBaseContext(), "Getssszzzz", Toast.LENGTH_SHORT).show();
		if (data != null) {
			String selected = data.getExtras().getString("Selected");	
			Button b = (Button)findViewById(currButton);
			b.setText(selected);
		} else
			 Toast.makeText(getBaseContext(), "Upps null!!", Toast.LENGTH_SHORT).show();
	}

	
}
