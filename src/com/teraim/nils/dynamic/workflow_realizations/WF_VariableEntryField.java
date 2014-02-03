package com.teraim.nils.dynamic.workflow_realizations;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.teraim.nils.GlobalState;
import com.teraim.nils.StoredVariable;
import com.teraim.nils.dynamic.types.VariableConfiguration;
import com.teraim.nils.dynamic.types.Delyta;
import com.teraim.nils.dynamic.workflow_abstracts.Listable;


public abstract class WF_VariableEntryField extends WF_Widget implements Listable {

	Context ctx=null;
	//String keyVariable=null;
	List<String> keyRow =null;
	VariableConfiguration al;
	
	public abstract void refreshValues();

	public WF_VariableEntryField(View v,Context ctx) {
		super(v);
		this.ctx=ctx;
		al = GlobalState.getInstance(ctx).getArtLista();
	}

	public void setKeyRow(String key) {
		keyRow = al.getTable().getRowContaining(VariableConfiguration.Col_Variable_Name, key);
	}

	@Override
	public String getSortableField(String columnId) {
		if (keyRow!=null && columnId!=null)
			return al.getTable().getElement(columnId, keyRow);
		else 
			return null;
	}

	@Override
	public String getKey() { 
		if (keyRow != null) 
			return al.getTable().getElement(VariableConfiguration.Col_Variable_Name, keyRow);
		return null;
	}

	@Override
	public String getValue() {
		//find the variable ID.
		String varId = getKey();
		if (varId==null) 
			Log.e("nils","Variable with NULL ID in WF_Listelement");
		else {
			Delyta d = GlobalState.getInstance(ctx).getCurrentDelyta();
			if (d == null) 
				Log.e("nils","Delyta NULL in WF_Listelement");
			else {
				StoredVariable var = d.getVariable(varId);
				if (var== null) 
					Log.e("nils","Variable"+varId+" has no valuein WF_Listelement");
				else {
					return var.getValue();
				}
			}
		}

		return null;
	}

}

