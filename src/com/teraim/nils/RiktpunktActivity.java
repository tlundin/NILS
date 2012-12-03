/**
 * 
 */
package com.teraim.nils;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TableLayout;

/**
 * @author Terje
 *
 */
public class RiktpunktActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.riktpunktscreen);
		TableLayout tv = (TableLayout)findViewById(R.id.riktpunkttable);
		
	}

	
}
