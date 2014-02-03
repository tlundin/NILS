package com.teraim.nils.dynamic.workflow_abstracts;

public abstract class Event {

	public enum EventType {
		onSave,onClick
	}
	
	private  EventGenerator ep;
	private  EventType myType;
	

	public Event (EventGenerator ep, EventType et) {
		this.ep = ep;
		myType = et;
	}
	public EventType getType() {
		return myType;
	}
	
	public EventGenerator getProvider() {
		return ep;
	}
	
	
}
