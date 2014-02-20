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
	private String context;

	public StartBlock(String[] args,String wfn, String context) {
		workflowName = wfn;
		this.args = args;
		this.context = context;
	}

	public String getName() {
		return workflowName;
	}

	public String[] getArgs() {
		return args;
	}
	
	public String getWorkFlowContext() {
		return context;
	}
}