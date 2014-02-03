package com.teraim.nils.dynamic.workflow_realizations;

import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.teraim.nils.GlobalState;
import com.teraim.nils.StoredVariable;
import com.teraim.nils.dynamic.types.Delyta;
import com.teraim.nils.dynamic.types.VariableConfiguration;
import com.teraim.nils.dynamic.workflow_abstracts.Listable;


public abstract class WF_ListEntry extends WF_Widget implements Listable,Comparable<Listable> {

	Context ctx=null;
	//String keyVariable=null;
	List<String> keyRow =null;
	VariableConfiguration al;
	String label = "";
	
	public abstract void refreshValues();
	public abstract void refreshInputFields();

	public WF_ListEntry(View v,Context ctx) {
		super(v);
		this.ctx=ctx;
		al = GlobalState.getInstance(ctx).getArtLista();
	}

	public void setKeyRow(String key) {
		keyRow = al.getTable().getRowContaining(VariableConfiguration.Col_Variable_Name, key);
		if (keyRow != null)
			label = al.getTable().getElement(VariableConfiguration.Col_Entry_Label, keyRow);
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
		StoredVariable stv = fetchVariableFromDb(varId);
		
		if(stv!=null)
			return stv.getValue();
		else
			return null;
	}
	
	@Override
	public long getTimeStamp() {
		String varId = getKey();
		StoredVariable stv = fetchVariableFromDb(varId);
		
		if(stv!=null)
			return Long.parseLong(stv.getTimeStamp());		
		else {
			Log.e("nils","no timestamp found in WF_ListEntry for "+varId);
			return -1;
		}
			
	}
		
	private StoredVariable fetchVariableFromDb(String varId) {
		if (varId==null) 
			Log.e("nils","Variable with NULL ID in WF_Listelement");
		else {
			Delyta d = GlobalState.getInstance(ctx).getCurrentDelyta();
			if (d == null) 
				Log.e("nils","Delyta NULL in WF_Listelement");
			else {
				StoredVariable var = d.getVariable(varId);
				if (var== null) 
					Log.e("nils","Variable"+varId+" has no valuein WF_ListEntry");
				else 
					return var;
			}
		}
		return null;
	}
	
	public String getLabel() {
		return label;
	}
	
	@Override
	public int compareTo(Listable other) {
		return this.getLabel().compareTo(other.getLabel());
	}

	
	 public static class Comparators {

	        public static Comparator<Listable> Alphabetic = new Comparator<Listable>() {
	            @Override
	            public int compare(Listable o1,Listable o2) {
	                return o1.getLabel().compareTo(o2.getLabel());
	            }
	        };
	        public static Comparator<Listable> Time = new Comparator<Listable>() {
	            @Override
	            public int compare(Listable o1, Listable o2) {
	                return (int)(o2.getTimeStamp() - o1.getTimeStamp());
	            }
	        };
	     
	   }
	

}

