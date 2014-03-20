package com.teraim.nils.dynamic.blocks;


/**
 * Layoutblock
 * @author Terje
 *
 */
public  class LayoutBlock extends Block {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5739546668415459049L;
	private String layoutDirection="", alignment="";

	public String getLayoutDirection() {
		return layoutDirection;
	}
	public String getAlignment() {
		return alignment;
	}
	public LayoutBlock(String id,String lbl, String layoutDirection, String alignment) {
		this.layoutDirection = layoutDirection;
		this.alignment = alignment;
		this.blockId=id;
	}
}