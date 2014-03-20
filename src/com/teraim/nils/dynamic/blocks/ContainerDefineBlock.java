package com.teraim.nils.dynamic.blocks;

/**
 * Container Definition block
 * @author Terje
 *
 */
public  class ContainerDefineBlock extends Block {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5110181631546326249L;
	private String containerName="",containerType=null;

	public String getContainerName() {
		return containerName;
	}
	public String getContainerType() {
		return containerType;
	}

	public ContainerDefineBlock(String id,String containerName, String containerType) {
		this.containerName =containerName;
		this.containerType = containerType;
		this.blockId=id;
	}
	//TODO: Container definition not used!!
}