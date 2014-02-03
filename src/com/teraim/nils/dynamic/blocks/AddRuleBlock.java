package com.teraim.nils.dynamic.blocks;

import android.content.Context;

import com.teraim.nils.dynamic.types.Rule;

public  class AddRuleBlock extends Block {

	private Rule r;

	public AddRuleBlock(Context ctx, String lbl, String ruleName,String target, String condition, String action, String errorMsg) {
		this.r = new Rule(ctx, ruleName,target,condition,action,errorMsg);

	}

	public Rule getRule() {
		return r;
	}


}