package com.teraim.nils.expr;

import com.teraim.nils.Variable;

public class Bool extends Aritmetic {

	public Bool(String name, String label) {
		super(name, label);
	}
	
	@Override
	public String getType() {
		return Variable.BOOLEAN;
	}

	 public void setValue(boolean b) { 
			setValue(b?1:0); 
		 }

	 public Boolean getValue() {
		if( value()==1.0)
			return true;
		if (value()==0)
			return false;
		return null;
	 }
}
