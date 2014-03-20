package com.teraim.nils.dynamic.blocks;

import android.content.Context;

import com.teraim.nils.dynamic.types.Rule;

public  class AddRuleBlock extends Block {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2045031005203874390L;
	private Rule r;


	public AddRuleBlock(String id,Context ctx, String lbl, String ruleName,String target, String condition, String action, String errorMsg) {
		this.r = new Rule(ctx, ruleName,target,condition,action,errorMsg);
		this.blockId=id;
	}

	public Rule getRule() {
		return r;
	}


}