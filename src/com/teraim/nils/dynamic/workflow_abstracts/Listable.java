package com.teraim.nils.dynamic.workflow_abstracts;

import java.util.Set;

import com.teraim.nils.dynamic.types.VarIdentifier;

//Listable represents a row of data with columns. 
//TODO: Weaknesses : Cannot sort on value, only columns in Configuration Time data.
public interface Listable {
	public String getSortableField(String columnId);
	public String getKey();
	//TODO: Must separate into Comparable class or similar?
	public long getTimeStamp();
	public String getValue();
	public String getLabel();
	public void refreshValues();
	public void refreshInputFields();
	Set<VarIdentifier> getAssociatedVariables();


}