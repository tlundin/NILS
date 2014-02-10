package com.teraim.nils.dynamic.types;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.teraim.nils.GlobalState.ErrorCode;
import com.teraim.nils.GlobalState;
import com.teraim.nils.StoredVariable;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.utils.Tools;


public class VariableConfiguration {

	public static String Col_Variable_Name = "Variable Name";
	public static String Col_Entry_Label = "Entry Label";
	public static String Col_Variable_Scope = "Variable Scope";
	static List<String>requiredColumns=Arrays.asList("List Entry Name",Col_Variable_Name,Col_Entry_Label,"Action","Variable Label","Num Type",Col_Variable_Scope,"Displayed","Unit","Description");

	
	static int LIST_ENTRY = 0,VARIABLE_NAME=1,ENTRY_LABEL=2,ACTION=3,VARIABLE_LABEL=4,NUM_TYPE=5,VARIABLE_TYPE=6,DISPLAY_IN_LIST=7,UNIT=8,Description=9;
	
	Map<String,Integer>fromNameToColumn = new HashMap<String,Integer>();

	
	Table myTable;
	
	public VariableConfiguration(Table t) {
		myTable = t;
	}
	
	public ErrorCode validateAndInit() {
		for (String c:requiredColumns) {
			if (myTable==null)
				return ErrorCode.file_not_found;
			int tableIndex = myTable.getColumnIndex(c);
			if (tableIndex==-1) {
				Log.e("nils","Missing column: "+c);
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
	
	
	public String getListEntryName(List<String> row) {
		return row.get(fromNameToColumn.get(requiredColumns.get(LIST_ENTRY)));
	}

	public String getVarName(List<String> row) {
		return row.get(fromNameToColumn.get(requiredColumns.get(VARIABLE_NAME)));
	}

	public String getEntryLabel(List<String> row) {
		return row.get(fromNameToColumn.get(requiredColumns.get(ENTRY_LABEL)));
	}

	public String getAction(List<String> row) {
		return row.get(fromNameToColumn.get(requiredColumns.get(ACTION)));
	}

	public String getVarLabel(List<String> row) {
		return row.get(fromNameToColumn.get(requiredColumns.get(VARIABLE_LABEL)));
	}

	public String getDescription(List<String> row) {
		return row.get(fromNameToColumn.get(requiredColumns.get(Description)));
	}

	public Variable.Type getnumType(List<String> row) {
		String type = row.get(fromNameToColumn.get(requiredColumns.get(NUM_TYPE)));
		return (type.equals("number"))?Variable.Type.NUMERIC:Variable.Type.LITERAL;
	}

	public StoredVariable.Type getVarType(List<String> row) {
		String type = row.get(fromNameToColumn.get(requiredColumns.get(VARIABLE_TYPE)));
		return type.equals("delyta")? StoredVariable.Type.delyta:(type.equals("provyta")?
				StoredVariable.Type.provyta:StoredVariable.Type.ruta);
	}
	
	public boolean isDisplayInList(List<String> row) {
		return row.get(fromNameToColumn.get(requiredColumns.get(DISPLAY_IN_LIST))).equalsIgnoreCase("TRUE");
	}

	public Unit getUnit(List<String> row) {
		return Tools.convertToUnit(row.get(fromNameToColumn.get(requiredColumns.get(UNIT))));
	}
	
	
}

