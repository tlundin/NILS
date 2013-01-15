package com.teraim.nils;

import android.os.Bundle;


public class MarkslagTab_SA_Activity extends RB_Activity {

	int[] myIds = {R.id.radio_sa1,R.id.radio_sa2,R.id.radio_sa3,R.id.radio_sa4};
	String[] myNames = {"Torvbildande_utom_stränder","Torvbildande_vid_stränder",
			"Icke_torvbildande_utom_stränder","icke_torvbildande_vid_stränder"};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.markslag_sa_utom_skog_tab);
		ids = myIds;

	}

	@Override
	protected String genName(int id) {
		return myNames[id];
	}

}



