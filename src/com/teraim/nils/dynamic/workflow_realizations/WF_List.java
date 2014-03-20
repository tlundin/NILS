package com.teraim.nils.dynamic.workflow_realizations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TableLayout.LayoutParams;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.workflow_abstracts.Filter;
import com.teraim.nils.dynamic.workflow_abstracts.Filterable;
import com.teraim.nils.dynamic.workflow_abstracts.Listable;
import com.teraim.nils.dynamic.workflow_abstracts.Sortable;
import com.teraim.nils.dynamic.workflow_abstracts.Sorter;

public abstract class WF_List extends WF_Widget implements Sortable,Filterable {

	protected final List<Listable> list = new  ArrayList<Listable>(); //Instantiated in constructor
	protected final List<Filter> myFilters=new ArrayList<Filter>();
	protected final List<Sorter> mySorters=new ArrayList<Sorter>();
	private List<? extends Listable> filteredList;
	protected final List<List<String>> myRows;
	protected WF_Context myContext;
	protected VariableConfiguration al;
	protected String group;
	//How about using the Container's panel?? TODO
	public WF_List(String id, WF_Context ctx,List<List<String>> rows,boolean isVisible) {
		super(id,new LinearLayout(ctx.getContext()),isVisible,ctx);	
		myWidget = (LinearLayout)getWidget();
		myWidget.setOrientation(LinearLayout.VERTICAL);
		myWidget.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		myContext = ctx;
		al = GlobalState.getInstance(ctx.getContext()).getArtLista();
		myRows = rows;
		group = al.getFunctionalGroup(myRows.get(0));
		
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
	
	public abstract Set<Variable> addVariableToEveryListEntry(String varSuffix,boolean displayOut,String format,boolean isVisible);
	public abstract void addFieldListEntry(String listEntryID, 
			String label, String description);
	public abstract Variable addVariableToListEntry(String varNameSuffix,boolean displayOut,String targetField,
			String format, boolean isVisible);


	/*
	public void createEntriesFromRows(List<List<String>> rows) {
		myWidget.removeAllViews();
		addEntriesFromRows(rows);
	}
	
	
	public abstract void addEntriesFromRows(List<List<String>> rows);
*/
	int intC=0;
	public void draw() {
		Log.e("draw","DRAW CALLED "+ (++intC)+" times from list"+this.getId());
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
			l.refreshOutputFields();
			//Everything is WF_Widgets, so this is safe!
			
			myWidget.addView(((WF_Widget)l).getWidget());
		} 

	}
	


}
