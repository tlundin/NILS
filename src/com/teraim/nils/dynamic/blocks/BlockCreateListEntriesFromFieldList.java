package com.teraim.nils.dynamic.blocks;

import java.util.List;

import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.workflow_abstracts.Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_Alphanumeric_Sorter;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.dynamic.workflow_realizations.WF_List;
import com.teraim.nils.dynamic.workflow_realizations.WF_List_UpdateOnSaveEvent;
import com.teraim.nils.dynamic.workflow_realizations.WF_OnlyWithValue_Filter;
import com.teraim.nils.dynamic.workflow_realizations.WF_TimeOrder_Sorter;

public class BlockCreateListEntriesFromFieldList extends Block {

	String id,type,containerId,selectionPattern,selectionField;
	boolean isVisible = true;
	public BlockCreateListEntriesFromFieldList(String namn, String type,
			String containerId, String selectionPattern, String selectionField) {
		super();
		this.id = namn;
		this.type = type;
		this.containerId = containerId;
		this.selectionPattern = selectionPattern;
		this.selectionField = selectionField;

	}

	private static final long serialVersionUID = -5618217142115636960L;


	public void create(WF_Context myContext) {
		o = GlobalState.getInstance(myContext.getContext()).getLogger();
		WF_List myList; 
		VariableConfiguration al = GlobalState.getInstance(myContext.getContext()).getArtLista();
		List<List<String>>rows = al.getTable().getRowsContaining(selectionField, selectionPattern);
		if (rows==null||rows.size()==0) {
			o.addRow("");
			o.addRedText("Selectionfield: "+selectionField+" selectionPattern: "+selectionPattern+" returns zero rows! List cannot be created");
		} else {
			if (type.equals("selected_values_list")) {
				o.addRow("This is a selected values type list. Adding Time Order sorter.");
				myList =  new WF_List_UpdateOnSaveEvent(id,myContext,rows,isVisible);
				myList.addSorter(new WF_TimeOrder_Sorter());	
				o.addRow("Adding Filter Type: only instantiated");
				myList.addFilter(new WF_OnlyWithValue_Filter());
			}
			else { 
				if (type.equals("selection_list")) {
					o.addRow("This is a selection list. Adding Alphanumeric sorter.");
					myList = new WF_List_UpdateOnSaveEvent(id,myContext,rows,isVisible);
					myList.addSorter(new WF_Alphanumeric_Sorter());
				} else
				{
					//TODO: Find other solution
					myList = new WF_List_UpdateOnSaveEvent(id,myContext,rows,isVisible);
					myList.addSorter(new WF_Alphanumeric_Sorter());
				}
			}


			//myList.createEntriesFromRows(rows);
			//myList.draw();

			Container myContainer = myContext.getContainer(containerId);
			if (myContainer !=null) {
				myContainer.add(myList);
				myContext.addList(myList);		
				Log.d("nils","QQQ ADDED LIST "+myList.getId());
			} else {
				o.addRow("");
				o.addRedText("Failed to add listEntriesblock - could not find the container "+containerId);
			}

		}
	}
}
