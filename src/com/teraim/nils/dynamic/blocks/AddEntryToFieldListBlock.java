package com.teraim.nils.dynamic.blocks;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.dynamic.workflow_realizations.WF_Static_List;




public class AddEntryToFieldListBlock extends Block {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8389535736187888854L;
	String target= null,namn= null,label=null,description=null;

	
	public AddEntryToFieldListBlock(String id,String namn,String target,
			String label, String description) {
		super();
		this.target = target;
		this.namn = namn;	
		this.label = label;
		this.description = description;
		this.blockId=id;
	}
	
	
	public void create(WF_Context myContext) {
		o = GlobalState.getInstance(myContext.getContext()).getLogger();
		WF_Static_List myList = myContext.getList(target);

		if (myList==null) {
			o.addRow("");
			o.addRedText("List with name "+target+" was not found in AddEntryToFieldListBlock. Skipping");
		} else 
			
			myList.addFieldListEntry(namn,label,description);
	
	}
}
