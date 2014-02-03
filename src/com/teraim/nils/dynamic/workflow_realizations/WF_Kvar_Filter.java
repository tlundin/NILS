package com.teraim.nils.dynamic.workflow_realizations;

import java.util.List;
import java.util.Set;

import android.util.Log;

import com.teraim.nils.VarIdentifier;
import com.teraim.nils.dynamic.workflow_abstracts.Listable;

//Specialized filter. Will remove elements with a value.
public class WF_Kvar_Filter extends WF_Filter {

	@Override
	public List<? extends Listable> filter(List<? extends Listable> list) {
		WF_ClickableField cb = ((WF_ClickableField)list.get(0));
		Set<VarIdentifier> varids = cb.myVars.keySet();
		Log.d("nils","vars for first: "+varids.size());
		return list;
	}

	
}