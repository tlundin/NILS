package com.teraim.nils.dynamic.workflow_realizations;

import java.util.Map;
import java.util.Set;

import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.workflow_abstracts.Event;

public class WF_Event_OnSave extends Event {

	Map<Variable, String> varsAffected=null;
	
	public WF_Event_OnSave(String id) {
		super(id,EventType.onSave);
	}
	
	public WF_Event_OnSave(String id, Map<Variable, String> changes) {
		super(id,EventType.onSave);
		varsAffected=changes;
	}
	
	
}
