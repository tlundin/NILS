package com.teraim.nils.utils;

import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.blocks.Block;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.workflow_realizations.WF_ClickableField_Selection;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;

public class AddVariableToEntryFieldBlock extends Block {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2748537558779469614L;
	boolean isVisible = true;
	String target,name,format;
	GlobalState gs;
	
	public AddVariableToEntryFieldBlock(String target,
			String namn, boolean isVisible,String format) {
		super();
		this.isVisible = isVisible;
		this.target = target;
		this.name = namn;
		this.format = format;
	} 


	public void create(WF_Context myContext) {
		gs = GlobalState.getInstance(myContext.getContext());
		o = gs.getLogger();
		VariableConfiguration al = gs.getArtLista();
		Log.d("nils","NAME: "+name);
		Variable v = al.getVariableInstance(name);
		if (v == null) {
			o.addRow("");
			o.addRedText("Variable "+name+" not found in definition file for CreateEntryBlock");
		} else {
			WF_ClickableField_Selection myField = (WF_ClickableField_Selection)myContext.getDrawable(target);
			if (myField==null) {
				o.addRow("");
				o.addRedText("Adding variable "+name+" to EntryField "+target+" failed. Could not find "+target);
			} else {
				Log.d("nils","Calling addVariable for variable "+v.getId());
				myField.addVariable(v, isVisible, format);
			}
		}
	}
}
