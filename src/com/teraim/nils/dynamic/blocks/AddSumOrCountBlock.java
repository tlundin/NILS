package com.teraim.nils.dynamic.blocks;

import android.view.LayoutInflater;

import com.teraim.nils.R;
import com.teraim.nils.StoredVariable;
import com.teraim.nils.dynamic.types.Variable.Type;
import com.teraim.nils.dynamic.types.Workflow.Unit;
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
	String containerId, label, myPattern, target;
	WF_Not_ClickableField_SumAndCountOfVariables.Type type;
	
	public AddSumOrCountBlock(String containerId, String label,
			String filter, String target,WF_Not_ClickableField_SumAndCountOfVariables.Type sumOrCount) {
		this.containerId=containerId;
		this.label=label;
		this.myPattern=filter;
		this.target=target;
		type = sumOrCount;
	}
	
	
	public void create(WF_Context myContext) {
		Container myContainer = myContext.getContainer(containerId);
		WF_Not_ClickableField_SumAndCountOfVariables field = new WF_Not_ClickableField_SumAndCountOfVariables(
				label,"", myContext, LayoutInflater.from(myContext.getContext()).inflate(R.layout.selection_field_normal,null), 
				target, myPattern,
				type);
		if (type==WF_Not_ClickableField_SumAndCountOfVariables.Type.count)
			field.addVariable(label, "AntalArter", Unit.nd, Type.NUMERIC, StoredVariable.Type.delyta, true);
		else
			field.addVariable(label, "SumTackning", Unit.percentage, Type.NUMERIC, StoredVariable.Type.delyta, true);
			
		//refresh value.
		field.matchAndRecalculateMe();
		field.refreshValues();
		myContainer.add(field);
	}





}


