package com.teraim.nils.dynamic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.GlobalState.ErrorCode;
import com.teraim.nils.dynamic.types.Table;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.utils.Tools;


public class VariableConfiguration {

	public static String Col_Variable_Name = "Variable Name";
	public static String Col_Variable_Label = "Variable Label";
	public static String Col_Variable_Keys = "Key Chain";
	public static String Type = "Type";
	public static String Col_Functional_Group = "Funktionell grupp";
	
		
	
	
	
	
	public static List<String>requiredColumns=Arrays.asList(Col_Variable_Keys,Col_Functional_Group,Col_Variable_Name,Col_Variable_Label,Type,"Unit","List Values");

	
	private static int KEY_CHAIN=0,FUNCTIONAL_GROUP=1,VARIABLE_NAME=2,VARIABLE_LABEL=3,TYPE=4,UNIT=5,LIST_VALUES=6;
	
	Map<String,Integer>fromNameToColumn = new HashMap<String,Integer>();

	
	Table myTable;
	GlobalState gs;
	
	public VariableConfiguration(GlobalState gs) {
		this.gs = gs;
		myTable = gs.thawTable();
		
	}
	
	public ErrorCode validateAndInit() {
		if (myTable==null)
			return ErrorCode.file_not_found;
		for (String c:requiredColumns) {
			int tableIndex = myTable.getColumnIndex(c);
			if (tableIndex==-1) {
				Log.e("nils","Missing column: "+c);
				Log.e("nils","Tabe has "+myTable.getColumnHeaders().toString());
				return ErrorCode.missing_required_column;
			}
			else
				//Now we can map a call to a column to the actual implementation.
				//Actual column index is decoupled.
				fromNameToColumn.put(c, tableIndex);
		}
		
		return ErrorCode.ok;
	}
	
	public Table getTable() {
		return myTable;
	}
	
	/*
	public String getListEntryName(List<String> row) {
		return row.get(fromNameToColumn.get(requiredColumns.get(LIST_ENTRY)));
	}
	*/
	
	public String getVarName(List<String> row) {
		return row.get(fromNameToColumn.get(requiredColumns.get(VARIABLE_NAME)));
	}

	public String getVarLabel(List<String> row) {
		return row.get(fromNameToColumn.get(requiredColumns.get(VARIABLE_LABEL)));
	}
	
	public String getKeyChain(List<String> row) {
		return row.get(fromNameToColumn.get(requiredColumns.get(KEY_CHAIN)));		
	}

	public String getFunctionalGroup(List<String> row) {
		return row.get(fromNameToColumn.get(requiredColumns.get(FUNCTIONAL_GROUP)));
	}
	public Variable.DataType getnumType(List<String> row) {
		String type = row.get(fromNameToColumn.get(requiredColumns.get(TYPE)));
		if (type!=null) {
		type.trim();
		return type.equals("number")?
				Variable.DataType.numeric:(type.equals("boolean")?
				Variable.DataType.bool:(type.equals("list")?
				Variable.DataType.list:(type.equals("numeric")?
				Variable.DataType.numeric:
				Variable.DataType.text)));
		}
		return null;
	}


	public Unit getUnit(List<String> row) {
		return Tools.convertToUnit(row.get(fromNameToColumn.get(requiredColumns.get(UNIT))));
	}

	public List<String> getCompleteVariableDefinition(String varName) {
		return myTable.getRowFromKey(varName);
	}
	
	public String getAction(List<String> row) {
		return null;
	}
	
	public String getEntryLabel(List<String> row) {
		String res= myTable.getElement("Svenskt Namn", row);
		//If this is a non-art variable, use varlabel instead.
		if (res==null) 
			res =this.getVarLabel(row);
		return res;
	}
	
	public String getDescription(List<String> row) {
		return myTable.getElement("Beskrivning", row);
	}
	
	public boolean isDisplayInList(List<String> row) {
		return false;
	}

	Map<String,Variable>varCache = new HashMap<String,Variable>();
	
	//Create a variable with the current context and the variable's keychain.
	public Variable getVariableInstance(String varId) {	
		Variable v = varCache.get(varId);
		if (v!=null) 
			return v;
		String varLabel =null;
		List<String> row = this.getCompleteVariableDefinition(varId);
		if (row!=null) {
		String keyChain = this.getKeyChain(row);
		varLabel = this.getVarLabel(row);
		//Log.d("nils","getVariableInstance for "+varId+" with keychain "+keyChain);
		//Log.d("nils","KeyChain is empty?"+keyChain.isEmpty());
		Map<String, String> vMap;
		if (!keyChain.isEmpty()) {
		String[] keys = keyChain.split("\\|");
		//find my keys in the current context.
		vMap = new HashMap<String,String>();
		Map<String, String> cMap = gs.getCurrentContext().getKeyHash();
		for (String key:keys) {
			String value = cMap.get(key);
			if (value!=null) {
				vMap.put(key, value);
				//Log.d("nils","Adding keychain key:"+key+" value: "+value);
			}
			else {
				Log.e("nils","Couldn't find key "+key+" in current context");
				
			}
		}
		} else
			vMap=null;
		//Use a cache for faster access.
		v = new Variable(varId,varLabel,row,vMap,gs);
		varCache.put(varId, v);
		return v;
		} 
		Log.e("nils","Couldn't find variable "+varId+" in getVariableInstance");
		return null;
	}
	
	
}
