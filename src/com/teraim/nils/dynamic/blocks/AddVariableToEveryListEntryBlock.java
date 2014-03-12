package com.teraim.nils.dynamic.blocks;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.dynamic.workflow_realizations.WF_List;

public class AddVariableToEveryListEntryBlock extends Block {

	String target,variableSuffix,format;
	boolean displayOut,isVisible;
	private static final long serialVersionUID = 3621078864866872867L;
	
	
	
	public AddVariableToEveryListEntryBlock(String target,
			String variableSuffix, boolean displayOut, String format,boolean isVisible) {
		super();

		this.target = target;
		this.variableSuffix = variableSuffix;
		this.displayOut = displayOut;
		this.format = format;
		this.isVisible=isVisible;
	}



	//addVariableToEveryListEntry(String varSuffix,boolean displayOut)
	//addVariable(String varLabel,Unit unit,String varId,boolean displayOut)
	
	public void create(WF_Context myContext) {
		
		WF_List l = myContext.getList(target);
		o = GlobalState.getInstance(myContext.getContext()).getLogger();
		if (l==null) {
			o.addRow("");
			o.addRedText("Couldn't find list with ID "+target+" in AddVariableToEveryListEntryBlock");
		} else {
			l.addVariableToEveryListEntry(variableSuffix, displayOut,format,isVisible);
		}
	}
}
