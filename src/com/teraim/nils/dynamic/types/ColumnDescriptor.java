package com.teraim.nils.dynamic.types;

public class ColumnDescriptor {

	public String colName;
	public boolean isEditable;
	public boolean isHeader;
	public boolean isOutput;
	
	public ColumnDescriptor(String name, boolean canEdit, boolean isHeader, boolean isOutput) {
		this.colName=name;
		this.isEditable=canEdit;
		this.isHeader=isHeader;
		this.isOutput=isOutput;		
	}
}
