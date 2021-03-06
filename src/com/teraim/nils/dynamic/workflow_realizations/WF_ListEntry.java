package com.teraim.nils.dynamic.workflow_realizations;

import java.util.Comparator;
import java.util.List;

import android.util.Log;
import android.view.View;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.workflow_abstracts.Listable;
import com.teraim.nils.utils.DbHelper.StoredVariableData;


public abstract class WF_ListEntry extends WF_Widget implements Listable,Comparable<Listable> {

	//String keyVariable=null;
	List<String> keyRow =null;
	String label = "";
	Variable myVar = null;
	
	public abstract void refreshOutputFields();
	public abstract void refreshInputFields();

	public WF_ListEntry(View v,WF_Context ctx,boolean isVisible) {
		super("LIST_ID",v,isVisible,ctx);
	}

	public void setKeyRow(Variable var) {
			myVar = var;
			if (myVar!=null) {
				keyRow = myVar.getBackingDataSet();		
				//Log.d("nils","Calling setKeyRow for "+keyRow.toString());
				label = myVar.getLabel();
			}
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
		if (myVar == null)
			return null;
		else return myVar.getId();
	}

	
	@Override
	public String getValue() {
		if (myVar == null)
			return null;
		return myVar.getValue();
	}
	
	@Override
	public long getTimeStamp() {
		if (myVar == null)
			return -1;
		else {
			StoredVariableData sv = myVar.getAllFields();
			if (sv == null)
				return -1;
			else 
				return Long.parseLong(sv.timeStamp);		
		}
		
			
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

