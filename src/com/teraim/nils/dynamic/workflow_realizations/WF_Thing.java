package com.teraim.nils.dynamic.workflow_realizations;

import android.widget.LinearLayout;

import com.teraim.nils.Logger;


public abstract class WF_Thing {

	private String myId;
	LinearLayout myWidget;
	protected Logger o;

	public WF_Thing(String id) {
		myId = id;
	}
	
	public String getId() {
		return myId;
	}


};