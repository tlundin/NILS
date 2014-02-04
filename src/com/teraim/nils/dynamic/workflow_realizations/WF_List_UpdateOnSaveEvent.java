package com.teraim.nils.dynamic.workflow_realizations;

import java.util.List;

import android.util.Log;

import com.teraim.nils.dynamic.workflow_abstracts.Event;
import com.teraim.nils.dynamic.workflow_abstracts.Event.EventType;
import com.teraim.nils.dynamic.workflow_abstracts.EventListener;
import com.teraim.nils.dynamic.workflow_abstracts.EventGenerator;

public class WF_List_UpdateOnSaveEvent extends WF_List implements EventListener,EventGenerator{

	public WF_List_UpdateOnSaveEvent(String id, WF_Context ctx) {
		super(id, ctx);
		//myContext.addEventProvider(this,EventType.onSave);
		myContext.addEventListener(this,EventType.onSave);
	}

	@Override
	public void addEntriesFromRows(List<List<String>> rows) 	{
		if (rows!=null) {
			Log.d("nils","Config file had "+rows.size()+" entries");
			int index = 0;
			WF_ClickableField listRow=null;
			for(List<String> r:rows) {
				if (r==null) {
					Log.e("nils","found null value in config file row "+index);
				} else {
					if (al.getAction(r).equals("create")) {
						//C_F_+index is the ID for the element.

						listRow = new WF_ClickableField_Selection(al.getEntryLabel(r),al.getDescription(r),myContext,"C_F_"+index);
						list.add(listRow);	
					} 
					if (!al.getAction(r).equals("add")&&!al.getAction(r).equals("create"))
						Log.e("nils","something is wrong...action is neither Create or Add: "+al.getAction(r));
					else {
						Log.d("nils","add...");
						if (listRow!=null) {
							Log.d("nils","var added "+al.getVarLabel(r));
							listRow.addVariable(al.getVarLabel(r), al.getVarName(r), al.getUnit(r), al.getnumType(r),al.getVarType(r), al.isDisplayInList(r));
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
	}

	

	

}
