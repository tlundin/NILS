package com.teraim.nils.dynamic.types;

public interface Variable {

	public enum Type {
		LITERAL,ARITMETIC,NUMERIC,FLOAT,BOOLEAN
	}
	
	public  String toString();
	 
	public Type getType();

	public  String getName(); 
	
	public  String getLabel();
	
	public  void setValue(String value);

	
	

	
}
