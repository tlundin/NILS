package com.teraim.nils.expr;

import com.teraim.nils.Variable;
import com.teraim.nils.Variable.Type;

public class Numeric extends Aritmetic {

	public Numeric(String name, String label) {
		super(name,label);
	}

	@Override
	public Type getType() {
		return Type.NUMERIC;
	}
	
}
