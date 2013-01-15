package com.teraim.nils;

import android.os.Bundle;


public class MarkslagTab_Glaciar_Activity extends RB_Activity {


	int[] myIds = {R.id.radioGlaciar,R.id.radioPermaSnow};
	String[] myNames = {"Glaciär","Permasnö"};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.markslag_glaciar_tab);
		ids = myIds;

	}

	@Override
	protected String genName(int id) {
		return myNames[id];
	}
}