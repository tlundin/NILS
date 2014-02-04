package com.teraim.nils.dynamic.workflow_realizations;

import android.util.Log;

import com.teraim.nils.dynamic.workflow_abstracts.Event;

public class WF_Event_OnRedraw extends Event {

	public WF_Event_OnRedraw(String id) {
		super(id,EventType.onRedraw);
		Log.d("nils","CREATED ONREDRAW FOR ID: "+id);
	}
	
	
}
