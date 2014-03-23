package com.teraim.nils.dynamic.workflow_realizations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.ColumnDescriptor;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Variable.DataType;
import com.teraim.nils.dynamic.workflow_abstracts.Event;
import com.teraim.nils.dynamic.workflow_abstracts.Event.EventType;
import com.teraim.nils.dynamic.workflow_abstracts.EventListener;
import com.teraim.nils.utils.Tools;
import com.teraim.nils.utils.DbHelper.Selection;


public class WF_Instance_List extends WF_List implements EventListener {
	//final String variableName;
	final Selection s;
	final String[] columnNames;
	final List<ColumnDescriptor> cd;
	final String varId;
	int myHeaderCol = 0;
	Map<String,String> listElemSelector;

	//Variablename should always be first element of columns. Will be used as header.
	public WF_Instance_List(String id, boolean isVisible, WF_Context ctx, List<ColumnDescriptor> columns, Selection selection, String varId, Map<String, String> keySet) {
		super(id, isVisible, ctx);

		//this.variableName= variableName; 
		this.s = selection;
		columnNames = new String[columns.size()];
		int i=0;
		for (ColumnDescriptor cd:columns) {
			columnNames[i]=cd.colName;
			if (cd.isHeader)
				myHeaderCol = i;
			i++;
		}
		
		
		cd=columns;
		this.varId = varId;
		this.listElemSelector = keySet;
		ctx.addEventListener(this, EventType.onSave);
	}
	
	

	
	
	@Override
	public void onEvent(Event e) {

		Log.d("nils","GOT EVENT!!");
		refreshList();
		draw();
		myContext.registerEvent(new WF_Event_OnRedraw(this.getId()));
	}

	//Need: 
	// - header label
	// - editfields. 
	//[TransportLed				2 meter]

	private void refreshList() {
		Log.d("nils","In refereshlist..");
		list.clear();		
		List<String[]> rows = gs.getDb().getValues(columnNames,s);
		if (rows!=null) {
			Log.d("nils","Got "+rows.size()+" results in refreshList, WF_Instance");
			int rowC=0;
			
			for (String[] colVals:rows) {
				if (colVals!=null) {	
					Map<String,String> bonnlapp = new HashMap<String,String>(listElemSelector);				
					for (int colC=0;colC<colVals.length;colC++) {
						bonnlapp.put(cd.get(colC).colName, colVals[colC]);
					Log.d("nils","Adding key"+cd.get(colC).colName+" with value "+colVals[colC]);
					}
					WF_ClickableField_Selection entryF = new WF_ClickableField_Selection(colVals[myHeaderCol],"",myContext,this.getId()+rowC,true);										
					Variable v = new Variable(varId,"Meter",al.getCompleteVariableDefinition(varId),bonnlapp,gs,"meter");
					v.setType(DataType.numeric);
					entryF.addVariable(v, true, null, true);
					list.add(entryF);	
				}
				rowC++;
			}
		}
	}
}
