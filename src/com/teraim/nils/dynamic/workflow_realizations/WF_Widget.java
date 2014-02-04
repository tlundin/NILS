package com.teraim.nils.dynamic.workflow_realizations;

import android.view.View;

import com.teraim.nils.dynamic.workflow_abstracts.Drawable;

public class WF_Widget extends WF_Thing implements Drawable {

	private View myView;
	
	public WF_Widget(String id,View v) {
		super(id);
		myView = v;
	}


	@Override
	public View getWidget() {
		return myView;
	}


};