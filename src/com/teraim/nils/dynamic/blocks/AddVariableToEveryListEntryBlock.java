package com.teraim.nils.dynamic.blocks;

import java.util.Set;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.dynamic.workflow_realizations.WF_Static_List;

public class AddVariableToEveryListEntryBlock extends Block {

	String target,variableSuffix,format;
	boolean displayOut,isVisible;
	private static final long serialVersionUID = 3621078864866872867L;
	
	
	
	public AddVariableToEveryListEntryBlock(String id,String target,
			String variableSuffix, boolean displayOut, String format,boolean isVisible) {
		super();

		this.target = target;
		this.variableSuffix = variableSuffix;
		this.displayOut = displayOut;
		this.format = format;
		this.isVisible=isVisible;
		this.blockId=id;
	}



	//addVariableToEveryListEntry(String varSuffix,boolean displayOut)
	//addVariable(String varLabel,Unit unit,String varId,boolean displayOut)
	
	public Set<Variable> create(WF_Context myContext) {
		
		WF_Static_List l = myContext.getList(target);
		o = GlobalState.getInstance(myContext.getContext()).getLogger();
		if (l==null) {
			o.addRow("");
			o.addRedText("Couldn't find list with ID "+target+" in AddVariableToEveryListEntryBlock");
		} else {
			return l.addVariableToEveryListEntry(variableSuffix, displayOut,format,isVisible);
		}
		return null;
	}
}
