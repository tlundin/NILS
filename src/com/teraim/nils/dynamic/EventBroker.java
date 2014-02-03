package com.teraim.nils.dynamic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.teraim.nils.dynamic.workflow_abstracts.Event;
import com.teraim.nils.dynamic.workflow_abstracts.Event.EventType;
import com.teraim.nils.dynamic.workflow_abstracts.EventListener;

public class EventBroker {

	Map<EventType,List<EventListener>> eventListeners= new HashMap<EventType,List<EventListener>>();
	
	

	public void registerEventListener(EventType et,EventListener el) {
		
		List<EventListener> els = eventListeners.get(et);
		if (els==null) {
			els = new LinkedList<EventListener>();
			eventListeners.put(et, els);
		}
		
		els.add(el);
	
	}
	
	public void onEvent(Event e) {
		List<EventListener> els = eventListeners.get(e.getType());
		if (els==null) {
			Log.d("nils","No eventlistener exists for event "+e.getType().name());
		} else {
		for(EventListener el:els)
			el.onEvent(e);
		}
	}
	
}
