package com.teraim.nils.dynamic.workflow_realizations;

import android.widget.LinearLayout;


public abstract class WF_Thing {

	private String myId;
	LinearLayout myWidget;

	public WF_Thing(String id) {
		myId = id;
	}
	
	public String getId() {
		return myId;
	}


};