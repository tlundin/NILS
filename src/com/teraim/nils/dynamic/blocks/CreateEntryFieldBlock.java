package com.teraim.nils.dynamic.blocks;

import java.util.List;

import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.StoredVariable;
import com.teraim.nils.Variable;
import com.teraim.nils.dynamic.types.VariableConfiguration;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.dynamic.workflow_abstracts.Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_ClickableField_Selection;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.dynamic.workflow_realizations.WF_List_UpdateOnSaveEvent;

public class CreateEntryFieldBlock extends Block {

	private static final String selectionField = VariableConfiguration.Col_Variable_Name;
	String name,type,label,purpose,containerId;
	Unit unit;
	private String id;
	private WF_List_UpdateOnSaveEvent myList;

	public CreateEntryFieldBlock(String name, String type, String label,
			String purpose, Unit unit,String containerId) {
		super();
		this.name = name;
		this.type = type;
		this.label = label;
		this.purpose = purpose;
		this.unit = unit;
		this.containerId=containerId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the purpose
	 */
	public String getPurpose() {
		return purpose;
	}

	/**
	 * @return the unit
	 */
	public Unit getUnit() {
		return unit;
	}

	

	public void create(WF_Context myContext) {
		boolean success = false;
		VariableConfiguration al = GlobalState.getInstance(myContext.getContext()).getArtLista();
		List<List<String>>rows = al.getTable().getRowsContaining(selectionField, name);
		if (rows!=null) {
			Container myContainer = myContext.getContainer(containerId);

/*
				myList = new WF_List_UpdateOnSaveEvent(name,myContext);
				myList.createEntriesFromRows(rows);
				myList.draw();
				
				if (myContainer !=null) {
					myContainer.add(myList);
					myContext.addList(myList);		
				} else
					Log.e("nils","failed to parse listEntriesblock - could not find the container");
*/
				
			List<String> r = rows.get(0);

			if (r!=null) {
 
				WF_ClickableField_Selection myField = new WF_ClickableField_Selection(al.getEntryLabel(r),al.getDescription(r),myContext,name);
				
				if (myField !=null) {
					myField.addVariable(label,name, unit, Variable.Type.NUMERIC, StoredVariable.Type.delyta, true);
					if(myContainer !=null) {
						myContainer.add(myField);
						myField.refreshInputFields();
						success = true;
					}
				}
			}
		}
		if (!success)
			Log.e("nils","CreateEntryFieldBlock: Could not add EntryInputField "+name+".");
	}



				
		}
	

