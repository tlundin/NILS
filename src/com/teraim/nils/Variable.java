package com.teraim.nils;

public interface Variable {

	public final static String LITERAL = "literal";
	public final static String ARITMETIC= "aritmetic";
	public final static String NUMERIC = "numeric";
	public final static String BOOLEAN = "boolean";

	public String toString();
	 
	public String getType();

	public String getName();
	
	public void setValue(String value);
	
}
