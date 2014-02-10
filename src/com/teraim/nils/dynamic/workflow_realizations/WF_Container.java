package com.teraim.nils.dynamic.workflow_realizations;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.teraim.nils.dynamic.workflow_abstracts.Container;

public class WF_Container extends WF_Thing implements Container {

	private ViewGroup me;
	Container parent;
	List<WF_Widget> myItems;

	public WF_Container(String id, ViewGroup container, Container parent) {
		super(id);
		this.parent=parent;
		me = container;
		myItems = new ArrayList<WF_Widget>();
	}

	@Override
	public Container getParent() {
		return parent;
	}


	@Override
	public Container getRoot() {
		Container parent = this;
		Container child = null;
		while (parent!=null) {
			child = parent;
			parent = parent.getParent();			
		}
		return child;
	}



	@Override
	public void draw() {

		Log.d("nils","in WF_Container draw with ID: "+this.getId()+". I have  "+myItems.size()+" widgets.");
		View v;

		for(WF_Widget d:myItems) {
			v = d.getWidget();
			if (v.getParent()!=null && v.getParent().equals(me)) {
				Log.d("nils","Parent of this object is me. Skip draw!!!");
				continue;
			}
			if (v!=null) {
				me.addView(v);
			}
		} 

	}

	@Override
	public void add(WF_Widget d) {
		myItems.add(d);
	}

	@Override
	public void remove(WF_Widget d) {
		myItems.remove(d);
	}

	@Override
	public List<WF_Widget> getWidgets() {
		return myItems;
	}

	@Override
	public void removeAll() {
		Log.d("nils","cleaning up container...");
		if (myItems!=null) {
			myItems = new ArrayList<WF_Widget>();
			me.removeAllViews();
		}

	}



}
