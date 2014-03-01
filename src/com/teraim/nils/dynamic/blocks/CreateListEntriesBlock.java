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

public  class CreateListEntriesBlock extends Block {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6745006895748449015L;
	String fileName=null;
	String containerId;
	String id;
	String type;
	private String selectionField;
	private String selectionPattern;
	private String filterName;
	private boolean isVisible = true;

	public String getFileName() {
		return fileName;
	}
	public String getContainerId() {
		return containerId;
	}
	public CreateListEntriesBlock(String type,String fileName, String containerId, String id, String selectionField, String selectionPattern,
			String filterName,boolean isVisible) {
		this.fileName =fileName;
		this.containerId = containerId;
		this.id=id;
		this.selectionField=selectionField;
		this.selectionPattern=selectionPattern;
		this.filterName=filterName;
		this.type=type;
		this.isVisible=isVisible;

	}

	public void create(WF_Context myContext) {
		o = GlobalState.getInstance(myContext.getContext()).getLogger();
		WF_List myList; 
		VariableConfiguration al = GlobalState.getInstance(myContext.getContext()).getArtLista();
		List<List<String>>rows = al.getTable().getRowsContaining(selectionField, selectionPattern);
		
		if (type.equals("selected_values_list")) {
			o.addRow("This is a selected values type list. Adding Time Order sorter.");
			myList =  new WF_List_UpdateOnSaveEvent(id,myContext,rows,isVisible);
			myList.addSorter(new WF_TimeOrder_Sorter());	
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

		o.addRow("Adding filter with name: "+filterName);
		if (filterName!=null) {
			if (filterName.equals("only_instantiated")) {
				o.addRow("Filter Type: only instantiated");
				myList.addFilter(new WF_OnlyWithValue_Filter());
			} else {
				o.addRow("");
				o.addRedText("Filter Type: "+filterName+" is not yet supported");
			}
			
		}
		myList.createEntriesFromRows(rows);
		myList.draw();

		Container myContainer = myContext.getContainer(containerId);
		if (myContainer !=null) {
			myContainer.add(myList);
			myContext.addList(myList);		
		} else {
			o.addRow("");
			o.addRedText("Failed to add listEntriesblock - could not find the container "+containerId);
		}

	}


}