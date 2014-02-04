package com.teraim.nils.dynamic.workflow_realizations;

import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.teraim.nils.dynamic.workflow_abstracts.Filter;
import com.teraim.nils.dynamic.workflow_abstracts.Listable;

public class WF_OnlyWithValue_Filter extends WF_Filter implements Filter {

	@Override
	public List<? extends Listable> filter(List<? extends Listable> list) {
		
		Iterator<? extends Listable> it = list.iterator();
		while(it.hasNext()) {
			Listable l = it.next();
			String value = l.getValue();
			if (value == null||value.length()==0) {
				it.remove();
				Log.d("nils","filter removes element "+l.getKey()+" because its value is null");
			}
		}
		return list;
	}

	

}

