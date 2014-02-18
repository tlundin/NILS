package com.teraim.nils.dynamic.blocks;

import java.util.List;

import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Numerable;
import com.teraim.nils.dynamic.types.VariableConfiguration;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.dynamic.workflow_abstracts.Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_ClickableField_Selection;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.dynamic.workflow_realizations.WF_List_UpdateOnSaveEvent;

public class CreateEntryFieldBlock extends Block {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2013870148670474248L;
	String name,type,label,containerId,postLabel;
	Unit unit;
	GlobalState gs;
	
	public CreateEntryFieldBlock(String name, String label,
			String postLabel,String containerId) {
		super();
		this.name = name;
		this.label = label;
		this.postLabel=postLabel;
		this.containerId=containerId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}



	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}


	

	

	public void create(WF_Context myContext) {
		gs = GlobalState.getInstance(myContext.getContext());
		Container myContainer = myContext.getContainer(containerId);
		o = gs.getLogger();
		VariableConfiguration al = gs.getArtLista();
		WF_ClickableField_Selection myField = new WF_ClickableField_Selection(label,"This description has no tag in the xml for block_create_entry_field",myContext,name);
		Log.d("nils","NAME: "+name);
		List<String> row = al.getCompleteVariableDefinition(name);
		if (row == null) {
			o.addRow("");
			o.addRedText("Variable "+name+" not found in definition file for CreateEntryBlock");
		} else	{	
			for (String s:row)
				Log.d("nils","s: "+s);
			myField.addVariable(label,postLabel,name, al.getUnit(row),al.getnumType(row), al.getVarType(row), true);
		}
			if(myContainer !=null) {
			myContainer.add(myField);
			myField.refreshInputFields();				
		}
			
		
	}



				
		}
	

