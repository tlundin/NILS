package com.teraim.nils.dynamic.workflow_realizations;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.widget.LinearLayout;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.types.VariableConfiguration;
import com.teraim.nils.dynamic.workflow_abstracts.Filterable;
import com.teraim.nils.dynamic.workflow_abstracts.Sortable;

public abstract class WF_List extends WF_Widget implements Sortable,Filterable {

	protected final List<WF_VariableEntryField> list = new  ArrayList<WF_VariableEntryField>(); //Instantiated in constructor
	protected final List<WF_Filter> myFilters=new ArrayList<WF_Filter>();
	protected WF_Context myContext;
	protected VariableConfiguration al;
	//How about using the Container's panel?? TODO
	public WF_List(String id, WF_Context ctx) {
		super(new LinearLayout(ctx.getContext()));	
		myWidget = (LinearLayout)getWidget();
		myWidget.setOrientation(LinearLayout.VERTICAL);
		myId = id;
		myContext = ctx;
		al = GlobalState.getInstance(ctx.getContext()).getArtLista();
	}


	@Override
	public void sort() {
		// TODO Auto-generated method stub			
	}

	@Override
	public void addFilter(WF_Filter f) {
		myFilters.add(f);
	}

	@Override
	public void removeFilter(WF_Filter f) {
		if (f!=null)
			Log.d("nils","removing filter "+f.getId());
		if(myFilters.remove(f))
			Log.d("nils","...succesfully");
	}

	@Override
	public String getId() {
		return myId;
	}

	public void redraw(List<WF_VariableEntryField> list) {
		myWidget.removeAllViews();
		for (WF_VariableEntryField l:list) {
			l.refreshValues();
			myWidget.addView(l.getWidget());
		}
	}


	@Override
	public void runFilters() {
		List<WF_VariableEntryField> listx = new ArrayList<WF_VariableEntryField>(list);
		for (WF_Filter f:myFilters) {
			f.filter(listx);
		}
		Log.d("nils","in redraw...");
		redraw(listx);
	}



	public void createEntriesFromRows(List<List<String>> rows) {
		myWidget.removeAllViews();
		addEntriesFromRows(rows);
	}
	public abstract void addEntriesFromRows(List<List<String>> rows);
	
	
	



}
