package com.teraim.nils.expr;

import com.teraim.nils.Variable;

public class Literal implements Variable {

	private String name,value;
	
	public Literal(String _name) {
		value=null;name=_name;
	}

	

	public void setValue(String val) {
		value = val;
	}

	@Override
	public String getType() {
		return Variable.LITERAL;
	}

	@Override
	public String getName() {
		return name;
	}

}
