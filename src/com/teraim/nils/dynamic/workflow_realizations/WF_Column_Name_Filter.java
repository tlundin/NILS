package com.teraim.nils.dynamic.workflow_realizations;

import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.teraim.nils.dynamic.workflow_abstracts.Listable;

//Specialized filter. Will filter a list on Prefix.
public class WF_Column_Name_Filter extends WF_Filter {

	String myPrefix = "";
	String filterColumn;
	private String columnToMatch;

	@Override
	public List<? extends Listable> filter(List<? extends Listable> list) {
		String key;
		Iterator<? extends Listable> it = list.iterator();
		while(it.hasNext()) {
			Listable l = it.next();
			key = l.getSortableField(columnToMatch);
			if (key==null || key.length()==0) {
				Log.e("nils","Key was null or 0 length in filter");
				continue;
			}				
			boolean match = false;
			for (int i=0;i<myPrefix.length();i++) {
				if (key.charAt(0)==myPrefix.charAt(i)) {
					match=true;
					break;
				}
			}
			if (!match) {
					it.remove();
					Log.d("nils","filter removes element "+key+" because "+key.charAt(0)+" doesn't match "+myPrefix);
			}
				
		}
		return list;
	}


	public WF_Column_Name_Filter(String id,String filterCh,String columnToMatch) {
		myPrefix = filterCh;
		this.columnToMatch=columnToMatch;
	}


}