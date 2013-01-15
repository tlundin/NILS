package com.teraim.nils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

public class MarkslagTab_Akvatisk_Activity extends RB_Activity {

	int[] myIds = {R.id.radio_utom_mosaik,R.id.radio_mosaik};
	String[] myNames = {"Akvatisk_utom_mosaik","Akvatisk_mosaik"};
		    public void onCreate(Bundle savedInstanceState) {
		        super.onCreate(savedInstanceState);
		        setContentView(R.layout.markslag_akvatisk_tab);
		        ids = myIds;
		        
	        
		    }

			@Override
			protected String genName(int id) {
				return myNames[id];
			}


}

