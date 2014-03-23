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
	protected List<? extends Listable> filteredList;

	protected WF_Context myContext;
	protected GlobalState gs;
	protected VariableConfiguration al;

	//How about using the Container's panel?? TODO
	public WF_List(String id,boolean isVisible,WF_Context ctx) {
		super(id,new LinearLayout(ctx.getContext()),isVisible,ctx);	
		myWidget = (LinearLayout)getWidget();
		myWidget.setOrientation(LinearLayout.VERTICAL);
		myWidget.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		myContext = ctx;
		gs = GlobalState.getInstance(ctx.getContext());
		al = gs.getArtLista();
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
