package com.teraim.nils.expr;

import com.teraim.nils.Variable;

public class Literal implements Variable {

	private String name,label,value;
	
	public Literal(String name,String label) {
		value=null;
		this.name=name;
		this.label=label;
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
	
	@Override
	public String getLabel() {
		return label;
	}
	

}
