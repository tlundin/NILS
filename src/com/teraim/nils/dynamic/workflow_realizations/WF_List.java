package com.teraim.nils.dynamic.workflow_realizations;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.widget.LinearLayout;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.types.VariableConfiguration;
import com.teraim.nils.dynamic.workflow_abstracts.Filter;
import com.teraim.nils.dynamic.workflow_abstracts.Filterable;
import com.teraim.nils.dynamic.workflow_abstracts.Listable;
import com.teraim.nils.dynamic.workflow_abstracts.Sortable;
import com.teraim.nils.dynamic.workflow_abstracts.Sorter;

public abstract class WF_List extends WF_Widget implements Sortable,Filterable {

	protected final List<Listable> list = new  ArrayList<Listable>(); //Instantiated in constructor
	protected final List<Filter> myFilters=new ArrayList<Filter>();
	protected final List<Sorter> mySorters=new ArrayList<Sorter>();
	protected WF_Context myContext;
	protected VariableConfiguration al;
	private List<? extends Listable> filteredList;
	//How about using the Container's panel?? TODO
	public WF_List(String id, WF_Context ctx) {
		super(id,new LinearLayout(ctx.getContext()));	
		myWidget = (LinearLayout)getWidget();
		myWidget.setOrientation(LinearLayout.VERTICAL);
		myContext = ctx;
		al = GlobalState.getInstance(ctx.getContext()).getArtLista();
	}

	@Override
	public void addSorter(Sorter s) {
		mySorters.add(s);
	}
	@Override
	public void removeSorter(Sorter s) {
		mySorters.remove(s);
	}
	@Override
	public void addFilter(Filter f) {
		myFilters.add(f);
	}

	@Override
	public void removeFilter(Filter f) {
		myFilters.remove(f);
	}
	
	public List<Listable> getList() {
		return list;
	}

	public void createEntriesFromRows(List<List<String>> rows) {
		myWidget.removeAllViews();
		addEntriesFromRows(rows);
	}
	public abstract void addEntriesFromRows(List<List<String>> rows);


	public void draw() {
		filteredList = list;
		if (myFilters != null) {			
			List<Listable> listx = new ArrayList<Listable>(list);
			for (Filter f:myFilters) {
				f.filter(listx);
			}
			filteredList = listx;
		}
		if (mySorters != null) {
			for (Sorter s:mySorters) {
				filteredList = s.sort(filteredList);
			}
		}
		Log.d("nils","in redraw...");
		myWidget.removeAllViews();
		for (Listable l:filteredList) {
			l.refreshInputFields();
			//Everything is WF_Widgets, so this is safe!
			
			myWidget.addView(((WF_Widget)l).getWidget());
		} 

	}






}
