package com.teraim.nils.dynamic.blocks;


/**
 * Layoutblock
 * @author Terje
 *
 */
public  class LayoutBlock extends Block {

	private String layoutDirection="", alignment="";

	public String getLayoutDirection() {
		return layoutDirection;
	}
	public String getAlignment() {
		return alignment;
	}
	public LayoutBlock(String lbl, String layoutDirection, String alignment) {
		this.layoutDirection = layoutDirection;
		this.alignment = alignment;
	}
}