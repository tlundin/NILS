package com.teraim.nils.dynamic.workflow_realizations;

import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.teraim.nils.dynamic.workflow_abstracts.Listable;

//Specialized filter. Will remove elements with a value.
public class WF_OnlyWithoutValue_Filter extends WF_Filter {

	@Override
	public List<? extends Listable> filter(List<? extends Listable> list) {
		Iterator<? extends Listable> it = list.iterator();
		while(it.hasNext()) {
			Listable l = it.next();
			String value = l.getValue();
			if (value != null) {
				it.remove();
				Log.d("nils","filter removes element "+l.getKey()+" because its value is null");
			}
		}
		return list;
	}

	
}