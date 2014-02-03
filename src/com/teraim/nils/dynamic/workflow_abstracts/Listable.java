package com.teraim.nils.dynamic.workflow_abstracts;

//Listable represents a row of data with columns. 
//TODO: Weaknesses : Cannot sort on value, only columns in Configuration Time data.
public interface Listable {
	public String getSortableField(String columnId);
	//null if no value in database.
	public String getValue();
	public String getKey();
}