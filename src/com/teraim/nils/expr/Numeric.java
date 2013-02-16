package com.teraim.nils.expr;

import com.teraim.nils.Variable;

public class Numeric extends Aritmetic {

	public Numeric(String name) {
		super(name);
	}

	@Override
	public String getType() {
		return Variable.NUMERIC;
	}
	
}
