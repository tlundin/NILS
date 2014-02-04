package com.teraim.nils.dynamic.workflow_realizations;

import java.util.List;

import com.teraim.nils.dynamic.types.Workflow.Type;
import com.teraim.nils.dynamic.workflow_abstracts.Event.EventType;

public class WF_Event {

	EventType t;
	List<String> parameters;
	

	
	public WF_Event(EventType t, List<String> parameters) {
		this.t=t;
		this.parameters=parameters;
	}
}
