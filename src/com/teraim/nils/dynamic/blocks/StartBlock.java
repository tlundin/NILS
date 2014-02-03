package com.teraim.nils.dynamic.blocks;

/**
 * Startblock.
 * @author Terje
 *
 */
public  class StartBlock extends Block {
	final private String workflowName;
	final private String[] args;

	public StartBlock(String[] args,String wfn) {
		workflowName = wfn;
		this.args = args;
	}

	public String getName() {
		return workflowName;
	}

	public String[] getArgs() {
		return args;
	}
}