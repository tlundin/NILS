package com.teraim.nils;

public interface Variable {

	
	public final static String LITERAL = "literal";
	public final static String ARITMETIC= "aritmetic";
	public final static String NUMERIC = "numeric";
	public final static String BOOLEAN = "boolean";

	public abstract String toString();
	 
	public abstract String getType();

	public String getName(); 
	
	public String getLabel();
	
	public void setValue(String value);
	

	
}
