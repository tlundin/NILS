package com.teraim.nils.dynamic.blocks;

/**
 * Page Definition block
 * @author Terje
 *
 */
public  class PageDefineBlock extends Block {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5680503647867451264L;
	private String pageName="",pageType=null,pageLabel="";

	public String getPageName() {
		return pageName;
	}
	public String getPageType() {
		return pageType;
	}
	public String getPageLabel() {
		return pageLabel;
	}
	public PageDefineBlock(String id,String pageName,String pageType,String pageLabel) {
		this.pageName =pageName;
		this.pageType = pageType;
		this.pageLabel=pageLabel;
		this.blockId=id;
	}
}
