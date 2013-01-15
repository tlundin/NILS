	package com.teraim.nils;
	import android.app.Activity;
import android.os.Bundle;


public class MarkslagTab_Anlagd_Activity extends RB_Activity {

	int[] myIds = {R.id.radio_anlagd1,R.id.radio_anlagd2,R.id.radio_anlagd3,R.id.radio_anlagd4,R.id.radio_anlagd5};
	String[] myNames = {"Transportområde","Bebyggelseområde","Industriområde","Rekreationsområde","Jordbruksområde"};
		    public void onCreate(Bundle savedInstanceState) {
		        super.onCreate(savedInstanceState);
		        setContentView(R.layout.markslag_anlagd_tab);
		        ids = myIds;
	        
		    }

			@Override
			protected String genName(int id) {
				return myNames[id];
			}


}
