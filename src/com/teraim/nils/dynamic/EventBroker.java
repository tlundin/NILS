package com.teraim.nils.dynamic;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.workflow_abstracts.Event;
import com.teraim.nils.dynamic.workflow_abstracts.Event.EventType;
import com.teraim.nils.dynamic.workflow_abstracts.EventListener;
import com.teraim.nils.dynamic.workflow_realizations.WF_Event_OnSave;
import com.teraim.nils.non_generics.Constants;

public class EventBroker {

	Map<EventType,List<EventListener>> eventListeners= new ConcurrentHashMap<EventType,List<EventListener>>();
	private Context ctx;
	
	public EventBroker(Context ctx) {
		this.ctx=ctx;
	}

	public void registerEventListener(EventType et,EventListener el) {
		
		List<EventListener> els = eventListeners.get(et);
		if (els==null) {
			els = new LinkedList<EventListener>();
			eventListeners.put(et, els);
		}
		Log.d("nils","Added eventlistener for event "+et.name());
		els.add(el);
	
	}
	
	public void onEvent(Event e) {
		List<EventListener> els = eventListeners.get(e.getType());
		if (els==null) {
			Log.d("nils","No eventlistener exists for event "+e.getType().name());
		} else {
			Log.d("nils","sending event to "+els.size()+" listeners");

		for(EventListener el:els)
			el.onEvent(e);
		}
		
		if (e instanceof WF_Event_OnSave && e.getProvider()!=Constants.SYNC_ID) {
			Log.d("nils","Save event...sending delayed sync request");
			new Handler().postDelayed(new Runnable() {
				public void run() {
					GlobalState.getInstance(ctx).triggerTransfer();
				}
			}, 2000);
		} 
	}
	
	public void removeAllListeners() {
		eventListeners.clear();
	}
	
}
