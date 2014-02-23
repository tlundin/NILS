package com.teraim.nils.dynamic.workflow_realizations;

import java.util.List;

import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.Logger;
import com.teraim.nils.dynamic.workflow_abstracts.Event;
import com.teraim.nils.dynamic.workflow_abstracts.Event.EventType;
import com.teraim.nils.dynamic.workflow_abstracts.EventGenerator;
import com.teraim.nils.dynamic.workflow_abstracts.EventListener;

public class WF_List_UpdateOnSaveEvent extends WF_List implements EventListener,EventGenerator{

	
	public WF_List_UpdateOnSaveEvent(String id, WF_Context ctx,boolean isVisible) {
		super(id, ctx,isVisible);
		ctx.addEventListener(this, EventType.onSave);
		o = GlobalState.getInstance(ctx.getContext()).getLogger();
	}

	@Override
	public void addEntriesFromRows(List<List<String>> rows) 	{
		if (rows!=null) {
			o.addRow("Adding "+rows.size()+" list entries (variables)");
			int index = 0;
			WF_ClickableField listRow=null;
			for(List<String> r:rows) {
				if (r==null) {
					o.addRow("found null value in config file row "+index);
				} else {
					if (al.getAction(r).equals("create")) {
						Log.d("nils","create...");
						//C_F_+index is the ID for the element.
						//TODO: ID is a bit hacked here..
						
						listRow = new WF_ClickableField_Selection(al.getEntryLabel(r),al.getDescription(r),myContext,"C_F_"+index,true);
						list.add(listRow);	
					} 
					if (!al.getAction(r).equals("add")&&!al.getAction(r).equals("create"))
						o.addRow("something is wrong...action is neither Create or Add: "+al.getAction(r));
					else {
						Log.d("nils","add...");
						if (listRow!=null) {
							Log.d("nils","var added "+al.getVarLabel(r));
							listRow.addVariable(al.getVarLabel(r),"", al.getVarName(r),al.isDisplayInList(r));
						}
					}
				}
				index++;
			}
		}
	}

	@Override
	public void onEvent(Event e) {
		if (e.getProvider().equals(this))
			Log.d("nils","Throwing event that originated from me");
		else {
			Log.d("nils","GOT EVENT!!");
			draw();
		}
		myContext.registerEvent(new WF_Event_OnRedraw(this.getId()));
	}

	

	

}
