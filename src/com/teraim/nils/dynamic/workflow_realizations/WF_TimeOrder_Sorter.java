package com.teraim.nils.dynamic.workflow_realizations;

import java.util.Collections;
import java.util.List;

import android.util.Log;

import com.teraim.nils.dynamic.workflow_abstracts.Listable;
import com.teraim.nils.dynamic.workflow_abstracts.Sorter;


public class WF_TimeOrder_Sorter implements Sorter {

	@Override
	public List<? extends Listable> sort(List<? extends Listable> list) {
		Log.d("nils","Before TIME Sort: ");
		for(Listable l:list)
			Log.d("nils",l.getLabel()+",");
		Collections.sort(list, WF_ListEntry.Comparators.Time);
		Log.d("nils","After TIME Sort: ");
		for(Listable l:list)
			Log.d("nils",l.getLabel()+",");
		return list;
	}

}
