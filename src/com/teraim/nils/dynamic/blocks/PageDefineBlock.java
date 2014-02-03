package com.teraim.nils.dynamic.blocks;

/**
 * Page Definition block
 * @author Terje
 *
 */
public  class PageDefineBlock extends Block {

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
	public PageDefineBlock(String pageName,String pageType,String pageLabel) {
		this.pageName =pageName;
		this.pageType = pageType;
		this.pageLabel=pageLabel;
	}
}
