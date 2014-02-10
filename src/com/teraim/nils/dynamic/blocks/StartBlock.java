package com.teraim.nils.dynamic.blocks;

/**
 * Startblock.
 * @author Terje
 *
 */
public  class StartBlock extends Block {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6860379561108690650L;
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