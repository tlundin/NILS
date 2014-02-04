package com.teraim.nils.dynamic.workflow_realizations;

import com.teraim.nils.dynamic.workflow_abstracts.Event;
import com.teraim.nils.dynamic.workflow_abstracts.EventGenerator;

public class WF_Event_OnSave extends Event {


	public WF_Event_OnSave(String id) {
		super(id,EventType.onSave);
	}
	
	
}
