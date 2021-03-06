package com.teraim.nils.dynamic.workflow_realizations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.EventBroker;
import com.teraim.nils.dynamic.Executor;
import com.teraim.nils.dynamic.workflow_abstracts.Container;
import com.teraim.nils.dynamic.workflow_abstracts.Drawable;
import com.teraim.nils.dynamic.workflow_abstracts.Event;
import com.teraim.nils.dynamic.workflow_abstracts.Event.EventType;
import com.teraim.nils.dynamic.workflow_abstracts.EventListener;
import com.teraim.nils.dynamic.workflow_abstracts.Filterable;
import com.teraim.nils.dynamic.workflow_abstracts.Listable;

public class WF_Context {

	private Context ctx;
	private final List<WF_Static_List> lists=new ArrayList<WF_Static_List>();
	private Map<String,Drawable> drawables;
	private List<WF_Container> containers;
	private final Executor myTemplate;
	private EventBroker eventBroker;
	//ID for the container containing the template itself
	private final int rootContainerId;

	public WF_Context(Context ctx,Executor e,int rootContainerId) {
		this.ctx=ctx;
		myTemplate = e;
		eventBroker = new EventBroker(ctx);
		this.rootContainerId=rootContainerId;
		this.drawables=new HashMap<String,Drawable>();
	}
	public Context getContext() {
		return ctx;
	}

	public Activity getActivity() {
		return (Activity)ctx;
	}
	
	public List<WF_Static_List> getLists() {
		return lists;
	}

	public  WF_Static_List getList(String id) {
		for (WF_Static_List wfl:lists) {
			Log.d("nils","filterable list: "+wfl.getId());
			String myId = wfl.getId();				
			if(myId!=null && myId.equalsIgnoreCase(id))
				return wfl;
		}
		return null;
	}	


	public List<Listable> getListable(String id) {
		for (WF_Static_List wfl:lists) {
			Log.d("nils","filterable list: "+wfl.getId());
			String myId = wfl.getId();				
			if(myId!=null && myId.equalsIgnoreCase(id))
				return wfl.getList();
		}
		return null;
	}	

	//for now it is assumed that all lists implements filterable.
	public Filterable getFilterable(String id) {
		Log.d("nils","Getfilterable called with id "+id);
		if (id==null||lists==null)
			return null;
		for (WF_Static_List wfl:lists) {
			Log.d("nils","filterable list: "+wfl.getId());
			String myId = wfl.getId();				
			if(myId!=null && myId.equalsIgnoreCase(id))
				return wfl;
		}
		return null;
	}
	public void addContainers(List<WF_Container> containers) {
		this.containers = containers; 
	}

	public void addList(WF_Static_List l) {
		lists.add(l);
	}
	
	public void addDrawable(String key,Drawable d) {	
		drawables.put(key,d);
	}
	
	public Drawable getDrawable(String name) {
		return drawables.get(name);
	}


	public Container getContainer(String id) {
		//Log.d("nils","GetContainer. looking for container "+id);
		if (id==null || id.length()==0) {
			Log.d("nils","Container: null. Defaulting to root.");
			id = "root";
		}
		for (WF_Container c:containers) {
			String myId =c.getId();				
			if(myId!=null && myId.equalsIgnoreCase(id))
				return c;
		}
		Log.e("nils","Failed to find container "+id);
		return null;
	}

	
	
	public void emptyContianers() {
		if (containers!=null)
			for (Container c:containers) 
					c.removeAll();
		if (lists.size()!=0) {
			lists.clear();
			drawables.clear();
			eventBroker.removeAllListeners();
		}
	}

	//draws all containers traversing the tree.
	public void drawRecursively(Container c) {
		if (c==null) {
			Log.e("nils","This container is null");
			return;
		}

		c.draw();
		List<Container> cs = getChildren(c);
		for(Container child:cs)
			drawRecursively(child);

	}
	private List<Container> getChildren(Container key) {
		List<Container>ret = new ArrayList<Container>();
		if (key!=null) {
			Container parent;
			for(Container c:containers) {
				parent = c.getParent();
				if (parent == null) {
					//Log.e("nils","Parent is null in getChildren");
					continue;
				}
				if (parent.equals(key))
					ret.add(c);
			}
		}
		return ret;
	}

	public Executor getTemplate() {
		return myTemplate;
	}

	public void addEventListener(EventListener el,
			EventType et) {
		
		eventBroker.registerEventListener(et, el);
	}

	public void onEvent(Event ev) {
		eventBroker.onEvent(ev);
	}
	public void registerEvent(Event event) {
		eventBroker.onEvent(event);
	}
	public int getRootContainer() {
		return rootContainerId;
	}
	public void onCreateView() {
		this.emptyContianers();
		if (containers!=null)
			containers.clear();
	}
	
	
	

}
