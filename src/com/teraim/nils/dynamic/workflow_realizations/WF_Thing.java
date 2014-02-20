package com.teraim.nils.dynamic.workflow_realizations;

import android.widget.LinearLayout;

import com.teraim.nils.LoggerI;


public abstract class WF_Thing {

	private String myId;
	LinearLayout myWidget;
	protected LoggerI o;

	public WF_Thing(String id) {
		myId = id;
	}
	
	public String getId() {
		return myId;
	}


};