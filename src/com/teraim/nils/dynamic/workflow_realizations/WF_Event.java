package com.teraim.nils.dynamic.workflow_realizations;

import java.util.List;

public class WF_Event {

	Type t;
	List<String> parameters;
	
	enum Type {
		onSave,
		onClick
	}
	
	public WF_Event(Type t, List<String> parameters) {
		this.t=t;
		this.parameters=parameters;
	}
}
