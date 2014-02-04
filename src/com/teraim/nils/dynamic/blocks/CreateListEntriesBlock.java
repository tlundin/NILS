package com.teraim.nils.dynamic.blocks;

import java.util.List;

import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.types.VariableConfiguration;
import com.teraim.nils.dynamic.workflow_abstracts.Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_Alphanumeric_Sorter;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.dynamic.workflow_realizations.WF_List;
import com.teraim.nils.dynamic.workflow_realizations.WF_List_UpdateOnSaveEvent;
import com.teraim.nils.dynamic.workflow_realizations.WF_OnlyWithValue_Filter;
import com.teraim.nils.dynamic.workflow_realizations.WF_TimeOrder_Sorter;

public  class CreateListEntriesBlock extends Block {
	String fileName=null;
	String containerId;
	String id;
	String type;
	private String selectionField;
	private String selectionPattern;
	private String filterName;

	public String getFileName() {
		return fileName;
	}
	public String getContainerId() {
		return containerId;
	}
	public CreateListEntriesBlock(String type,String fileName, String containerId, String id, String selectionField, String selectionPattern,
			String filterName) {
		this.fileName =fileName;
		this.containerId = containerId;
		this.id=id;
		this.selectionField=selectionField;
		this.selectionPattern=selectionPattern;
		this.filterName=filterName;
		this.type=type;
	}

	public void create(WF_Context myContext) {

		WF_List myList; 
		VariableConfiguration al = GlobalState.getInstance(myContext.getContext()).getArtLista();
		List<List<String>>rows = al.getTable().getRowsContaining(selectionField, selectionPattern);
		
		if (type.equals("selected_values_list")) {
			myList =  new WF_List_UpdateOnSaveEvent(id,myContext);
			myList.addSorter(new WF_TimeOrder_Sorter());	
		}
		else { 
			if (type.equals("selection_list")) {
				myList = new WF_List_UpdateOnSaveEvent(id,myContext);
				myList.addSorter(new WF_Alphanumeric_Sorter());
			} else
			{
				//TODO: Find other solution
				myList = new WF_List_UpdateOnSaveEvent(id,myContext);
				myList.addSorter(new WF_Alphanumeric_Sorter());
			}
		}

		Log.d("nils","about to add filter with name: "+filterName);
		if (filterName!=null) {
			if (filterName.equals("only_instantiated")) {
				Log.d("nils","Adding filter: only instantiated");
				myList.addFilter(new WF_OnlyWithValue_Filter());
			} else {
				Log.e("parser","filter of type: "+filterName+"is not yet supported");
			}
		}
		myList.createEntriesFromRows(rows);
		myList.draw();

		Container myContainer = myContext.getContainer(containerId);
		if (myContainer !=null) {
			myContainer.add(myList);
			myContext.addList(myList);		
		} else
			Log.e("nils","failed to parse listEntriesblock - could not find the container");

	}


}