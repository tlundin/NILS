package com.teraim.nils.dynamic.workflow_abstracts;

public abstract class Event {

	public enum EventType {
		onSave,
		onClick,
		onRedraw
	}
	
	private  String generatorId;
	private  EventType myType;
	

	public Event (String fromId, EventType et) {
		this.generatorId = fromId;
		myType = et;
	}
	public EventType getType() {
		return myType;
	}
	
	public String getProvider() {
		return generatorId;
	}
	
	
}
