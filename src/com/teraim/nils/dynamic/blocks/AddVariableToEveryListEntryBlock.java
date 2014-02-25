package com.teraim.nils.dynamic.blocks;

import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.dynamic.workflow_realizations.WF_List;

public class AddVariableToEveryListEntryBlock extends Block {

	String target,variableSuffix;
	boolean displayOut;
	private static final long serialVersionUID = 3621078864866872867L;

	
	
	public AddVariableToEveryListEntryBlock(String target,
			String variableSuffix, boolean displayOut) {
		super();
		this.target = target;
		this.variableSuffix = variableSuffix;
		this.displayOut = displayOut;
	}



	//addVariableToEveryListEntry(String varSuffix,boolean displayOut)
	//addVariable(String varLabel,Unit unit,String varId,boolean displayOut)
	
	public void create(WF_Context myContext) {
		
		WF_List l = myContext.getList(target);
		if (l==null) {
			o.addRow("");
			o.addRedText("Couldn't find list with ID "+target+" in AddVariableToEveryListEntryBlock");
		} else {
			l.addVariableToEveryListEntry(variableSuffix, displayOut);
		}
	}
}