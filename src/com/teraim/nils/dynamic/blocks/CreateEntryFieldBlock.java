package com.teraim.nils.dynamic.blocks;

import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.dynamic.workflow_abstracts.Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_ClickableField_Selection;
import com.teraim.nils.dynamic.workflow_realizations.WF_ClickableField_Selection_OnSave;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;

public class CreateEntryFieldBlock extends Block {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2013870148670474248L;
	String name,type,label,containerId,postLabel;
	Unit unit;
	GlobalState gs;
	boolean isVisible = false;
	String format;
	
	public CreateEntryFieldBlock(String name, 
			String containerId,boolean isVisible,String format) {
		super();
		this.name = name;
		this.containerId=containerId;
		this.isVisible=isVisible;
		this.format = format;
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
		Log.d("nils","NAME: "+name);
		Variable v = al.getVariableInstance(name);
		if (v == null) {
			o.addRow("");
			o.addRedText("Variable "+name+" referenced in block_create_entry_field not found.");
		} else	{	
			WF_ClickableField_Selection myField = new WF_ClickableField_Selection_OnSave(v.getLabel(),al.getBeskrivning(v.getBackingDataSet()),myContext,name,isVisible);
			Log.d("nils", "In CreateEntryField.Description: "+al.getBeskrivning(v.getBackingDataSet()));
			Log.d("nils","Backing data: "+v.getBackingDataSet().toString());
			myField.addVariable(v, true,format,true);
			myContext.addDrawable(v.getId(), myField);
			if(myContainer !=null) {
				myContainer.add(myField);
				myField.refreshInputFields();	
				myField.refreshOutputFields();
		}
			
		}
			
		
	}



				
		}
	

