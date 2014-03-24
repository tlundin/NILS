package com.teraim.nils.dynamic.workflow_realizations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

		Log.d("nils","Got event from "+(e==null?"null":e.getProvider()));
		if (e instanceof WF_Event_OnSave) {
			boolean insert = true;
			WF_Event_OnSave onS = (WF_Event_OnSave)e;
			Map<Variable, String> x = onS.varsAffected;
			if (x!=null) {
				Entry<Variable, String> eset = x.entrySet().iterator().next();
				Variable v = eset.getKey();
				String oldValue = eset.getValue();
				String[] sel = v.getSelection().selectionArgs;
				Log.d("nils","Variable has selector Args"+sel.toString());
				for (int i=0;i<sel.length;i++)
					Log.d("nils",sel[i]);
				Log.d("nils","VALUE: "+v.getValue());
				Log.d("nils","OLD VALUE:"+oldValue);
				String meter = oldValue;
				if (!meter.equals(v.getValue())) {
					if (v.getValue()==null) {
						Log.d("nils","This is a DELETE event");
						insert = false;
					} else
						//if not delete, replace meter with old meter to remove existing values.
						v.getKeyChain().put("meter", meter);

					
					String name = v.getKeyChain().get("value");

					Log.d("nils","Name is"+name);
					v.getKeyChain().remove("value");
					
					Set<Entry<String, String>> xx = v.getKeyChain().entrySet();
					for (Entry<String, String> ee:xx) 
						Log.d("nils","key, value: "+ee.getKey()+","+ee.getValue());

					//put the old meter key back.
					
					
					//keys
					gs.setKeyHash(v.getKeyChain());
					List<List<String>>rows =gs.getArtLista().getTable().getRowsContaining(VariableConfiguration.Col_Functional_Group, name);
					if (rows!=null) {
						Log.d("nils","Got "+rows.size()+" results");
						Map<String,String> deletedVariables = 
								deleteAllDependants(rows);
						if (insert && deletedVariables!=null) {
							Log.d("nils","reinserting variables with new key.");
							v.getKeyChain().put("meter", v.getValue());
							Log.d("nils","meter set to "+v.getValue());
							gs.setKeyHash(v.getKeyChain());
							//Delete any existing values on same meter.
							deleteAllDependants(rows);
							Set<Entry<String, String>> es = deletedVariables.entrySet();
							Variable var;
							for (Entry<String, String> en:es) {
								var = al.getVariableInstance(en.getKey());
								if (var!=null) {
								if (var.getValue()!=null) 
									Log.e("nils","This variable already has a value...should not happen!");
								var.setValue(en.getValue());
								} else
									Log.e("nils","Variable null...should not happen. VarID: "+en.getKey());
							}
						}
					}

				} else
					Log.e("nils","Do nothing, value didn't change");
			}
		}

		refreshList();
		draw();
		myContext.registerEvent(new WF_Event_OnRedraw(this.getId()));
	}



	private Map<String, String> deleteAllDependants(List<List<String>> rows) {
		Map<String,String> ret = null;
		Variable v;
		for (List<String>row:rows) {

			v = al.getVariableInstance(al.getVarName(row));
			if (v!=null && v.getValue()!=null) {
				Log.d("nils","Deleting: "+v.getId()+"with value "+v.getValue());
				if (ret==null)
					ret = new HashMap<String,String>();
				ret.put(v.getId(), v.getValue());
				v.deleteValue();

			}
		}
		return ret;
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
