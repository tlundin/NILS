package com.teraim.nils.dynamic.blocks;

import android.view.LayoutInflater;

import com.teraim.nils.GlobalState;
import com.teraim.nils.R;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.workflow_abstracts.Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.dynamic.workflow_realizations.WF_Not_ClickableField_SumAndCountOfVariables;

/**Blocks that so far implements only signal
 * 
 * @author Terje
 *
 */
public  class AddSumOrCountBlock extends Block {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4139158043307360229L;
	String containerId, label, postLabel,myPattern, target,result;
	WF_Not_ClickableField_SumAndCountOfVariables.Type type;
	String format;
	boolean isVisible = true;
	private VariableConfiguration al;
	public AddSumOrCountBlock(String id,String containerId, String label,String postLabel,
			String filter, String target,
			WF_Not_ClickableField_SumAndCountOfVariables.Type sumOrCount,String result,
			boolean isVisible, String format) {
		this.containerId=containerId;
		this.label=label;
		this.myPattern=filter;
		this.target=target;
		type = sumOrCount;
		this.result = result;
		this.postLabel = postLabel;
		this.isVisible = isVisible;
		this.format = format;
		this.blockId=id;
		
	}
	
	//TODO: CHECK ON POSTLABEL
	
	public void create(WF_Context myContext) {
		o = GlobalState.getInstance(myContext.getContext()).getLogger();
		al = GlobalState.getInstance(myContext.getContext()).getArtLista();

		Container myContainer = myContext.getContainer(containerId);
		WF_Not_ClickableField_SumAndCountOfVariables field = new WF_Not_ClickableField_SumAndCountOfVariables(
				label,"", myContext, 
				target, myPattern,
				type,isVisible);
		if (result == null) {
			o.addRow("");
			o.addRedText("Error in XML: block_add_sum_of_selected_variables_display is missing a result parameter for:"+label);
		} else {
			Variable v = al.getVariableInstance(result);
			if (v==null) {
				o.addRow("");
				o.addRedText("Error in block_add_sum_of_selected_variables_display: missing variable for result parameter: "+result);
			} else 
				field.addVariable(v, true,format,true);			
		}
		/*
		
		if (type==WF_Not_ClickableField_SumAndCountOfVariables.Type.count)
			field.addVariable(label, "AntalArter", Unit.nd, Variable.DataType.numeric, Variable.StorageType.delyta, true);
		else
			field.addVariable(label, "SumTackning", Unit.percentage, Variable.DataType.numeric, Variable.StorageType.delyta, true);
		*/
			
		
		field.matchAndRecalculateMe();
		field.refreshOutputFields();
		myContainer.add(field);
	}





}


