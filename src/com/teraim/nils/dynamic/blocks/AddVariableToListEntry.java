package com.teraim.nils.dynamic.blocks;

import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.workflow_realizations.WF_ClickableField_Selection;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.dynamic.workflow_realizations.WF_List;

public class AddVariableToListEntry extends Block {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2748537558779469614L;
	boolean isVisible = true;
	String targetField,targetList,format,varNameSuffix;
	GlobalState gs;
	
	public AddVariableToListEntry(String varNameSuffix,
			String targetList,String targetField, boolean isVisible,String format) {
		super();
		this.isVisible = isVisible;
		this.targetField = targetField;
		this.targetList = targetList;
		this.format = format;
		this.varNameSuffix=varNameSuffix;
	} 


	public void create(WF_Context myContext) {
		gs = GlobalState.getInstance(myContext.getContext());
		o = gs.getLogger();
		VariableConfiguration al = gs.getArtLista();
		
		WF_List l= myContext.getList(targetList);
			if (l!=null) {
				Log.d("nils","Found entry field in AddVariableToListEntry");
				boolean added = l.addVariableToListEntry(varNameSuffix,targetField,format,isVisible);
				if (!added) {
					Log.e("nils","Didn't find list entry"+targetField+ " in AddVariableToListEntry");
				}
			} else
				Log.e("nils","Didn't find list in AddVariableToListEntry");
				
		}
		
		
	
}
