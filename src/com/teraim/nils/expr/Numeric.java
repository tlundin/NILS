package com.teraim.nils.expr;

import com.teraim.nils.dynamic.types.Numerable;
import com.teraim.nils.dynamic.types.Numerable.Type;

public class Numeric extends Aritmetic {

	public Numeric(String name, String label) {
		super(name,label);
	}

	@Override
	public Type getType() {
		return Type.NUMERIC;
	}
	
}
