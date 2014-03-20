package com.teraim.nils.dynamic.blocks;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.workflow_realizations.WF_ClickableField_Selection;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;


public class AddVariableToEntryFieldBlock extends Block {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7978000865030730562L;
	boolean displayOut,isVisible;
	String target,namn,format;
	GlobalState gs;
	
	public AddVariableToEntryFieldBlock(String id,String target,String namn,boolean displayOut,String format,boolean isVisible) {
		this.blockId=id;
		this.target=target;
		this.namn=namn;
		this.displayOut=displayOut;
		this.format = format;
		this.isVisible=isVisible;
	}
	
	public Variable create(WF_Context myContext) {
		gs = GlobalState.getInstance(myContext.getContext());
		o = gs.getLogger();
		VariableConfiguration al = gs.getArtLista();

		WF_ClickableField_Selection myField = (WF_ClickableField_Selection)myContext.getDrawable(target);
		if (myField == null) {
			o.addRow("");
			o.addRedText("Couldn't find Entry Field with name "+target+" in AddVariableToEntryBlock" );
			
		} else {
			Variable var = al.getVariableInstance(namn);
			if (var!=null) {
				myField.addVariable(var, displayOut, format,isVisible);
				return var;
			}
		}
		return null;
	}
}
