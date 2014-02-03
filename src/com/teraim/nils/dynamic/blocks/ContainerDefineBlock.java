package com.teraim.nils.dynamic.blocks;

/**
 * Container Definition block
 * @author Terje
 *
 */
public  class ContainerDefineBlock extends Block {

	private String containerName="",containerType=null;

	public String getContainerName() {
		return containerName;
	}
	public String getContainerType() {
		return containerType;
	}

	public ContainerDefineBlock(String containerName, String containerType) {
		this.containerName =containerName;
		this.containerType = containerType;
	}
}