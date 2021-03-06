package com.teraim.nils.dynamic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.GlobalState.ErrorCode;
import com.teraim.nils.dynamic.types.Table;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.utils.Tools;


public class VariableConfiguration {

	public static String Col_Variable_Name = "Variable Name";
	public static String Col_Variable_Label = "Variable Label";
	public static String Col_Variable_Keys = "Key Chain";
	public static String Type = "Type";
	public static String Col_Functional_Group = "Funktionell grupp";
	public static String Col_Variable_Local = "Local";

	public final static String KEY_YEAR = "�r";




	public static List<String>requiredColumns=Arrays.asList(Col_Variable_Keys,Col_Functional_Group,Col_Variable_Name,Col_Variable_Label,Type,"Unit","List Values","Description",Col_Variable_Local);


	private static int KEY_CHAIN=0,FUNCTIONAL_GROUP=1,VARIABLE_NAME=2,VARIABLE_LABEL=3,TYPE=4,UNIT=5,LIST_VALUES=6,DESCRIPTION=7,LOCAL=8;

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

	public List<String> getListElements(List<String> row) {
		List<String> el = null;
		String listS = row.get(fromNameToColumn.get(requiredColumns.get(LIST_VALUES)));
		if (listS!=null&&listS.trim().length()>0) {
			String[] x = listS.trim().split("\\|");
			if (x!=null&&x.length>0)
				el = new ArrayList<String>(Arrays.asList(x));
		}
		return el;
	}
	public String getVarName(List<String> row) {
		return row.get(fromNameToColumn.get(requiredColumns.get(VARIABLE_NAME)));
	}

	public String getVarLabel(List<String> row) {
		return row.get(fromNameToColumn.get(requiredColumns.get(VARIABLE_LABEL)));
	}

	public String getVariableDescription(List<String> row) {		
		return row.get(fromNameToColumn.get(requiredColumns.get(DESCRIPTION)));
	}

	public boolean getVarIsLocal(List<String> row) {
		String s= row.get(fromNameToColumn.get(requiredColumns.get(LOCAL)));
		Log.d("nils","getvarislocal uses string "+s);
		if (s==null||s.length()==0||!s.equalsIgnoreCase("x"))
			return false;
		else
			return true;
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
			if (type.equals("number")||type.equals("numeric"))
				return Variable.DataType.numeric;
			else if (type.equals("boolean"))
				return Variable.DataType.bool;
			else if (type.equals("list"))
				return Variable.DataType.list;
			else if (type.equals("text"))
				return Variable.DataType.text;
			else
				Log.e("nils","TYPE NOT KNOWN: "+type);
		}
		Log.e("nils","TYPE NULL?: "+type);


		return null;
	}


	public String getUnit(List<String> row) {
		return row.get(fromNameToColumn.get(requiredColumns.get(UNIT)));
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
		if (res == null)
			Log.e("nils","getEntryLabel failed to find a Label for row: "+row.toString());
		return res;
	}

	public String getBeskrivning(List<String> row) {
		String b = myTable.getElement("Beskrivning", row);
		if(b==null) 
			b = this.getVariableDescription(row);

		return (b==null?"":b);
	}



	public String getUrl(List<String> row) {
		return myTable.getElement("Internet link", row);	
	}

	public boolean isDisplayInList(List<String> row) {
		return false;
	}

	Map<String,Variable>varCache = new ConcurrentHashMap<String,Variable>();

	public String getVariableValue(Map<String, String> keyChain, String varId) {
		return new Variable(varId,null,null,keyChain,gs,"value").getValue();
		
	}

	public Variable getVariableInstance(String varId) {
		String varLabel =null;
		String keyChain = null;
		Variable v = varCache.get(varId);
		if (v!=null) {
			Log.d("nils","found cached var: "+varId+" backing: "+this.getCompleteVariableDefinition(varId));
			return v;
		}
		else {
			List<String> row = this.getCompleteVariableDefinition(varId);
			if (row!=null) {
				keyChain = this.getKeyChain(row);
				varLabel = this.getVarLabel(row);
				v = getVariableInstance(keyChain,varId,varLabel,row,gs.getCurrentKeyHash(),"value");		
				if (v!=null)
					varCache.put(varId, v);
				
				else
					Log.e("nils","getVariableInstance: Could not CREATE: "+varId);	
			} else 
				Log.e("nils","getVariableInstance: Cannot find variable: "+varId);		
			
			return v;
		}
	}


	//Create a variable with the current context and the variable's keychain.
	public Variable getVariableInstance(String keyChain,String varId,String varLabel,List<String> row,Map<String, String> cMap,String valueColumn) {	
		//find my keys in the current context.
		//Use a cache for faster access.
		return new Variable(varId,varLabel,row,buildDbKey(keyChain,cMap),gs,valueColumn);
	} 


	private Map<String, String> buildDbKey(String keyChain,
			Map<String, String> cMap) {
		if (keyChain==null||keyChain.isEmpty()) 
			return null;

		String[] keys = keyChain.split("\\|");
		Map<String, String> vMap = new HashMap<String,String>();
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
		return vMap;
	}


	public Map<String,String> createStandardKeyMap() {
		String currentYear = getVariableValue(null,"Current_Year");
		String currentRuta = getVariableValue(null,"Current_Ruta");
		String currentProvyta = getVariableValue(null,"Current_Provyta");		
		String currentDelyta = getVariableValue(null,"Current_Delyta");		
		if (currentRuta == null||currentProvyta==null||currentDelyta==null)
			return null;
		return Tools.createKeyMap(KEY_YEAR,currentYear,"ruta",currentRuta,"provyta",currentProvyta,"delyta",currentDelyta);
	}

	public String getCurrentRuta() {
		return getVariableValue(null,"Current_Ruta");
	}

	public String getCurrentProvyta() {
		return getVariableValue(null,"Current_Provyta");
	}

	public void invalidateCache() {
		for (Variable v:varCache.values())
			v.invalidate();
	}

	public void destroyCache() {
		varCache.clear();
	}








}

