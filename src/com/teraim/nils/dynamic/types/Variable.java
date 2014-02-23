package com.teraim.nils.dynamic.types;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.utils.DbHelper;
import com.teraim.nils.utils.DbHelper.Selection;
import com.teraim.nils.utils.DbHelper.StoredVariableData;

public class Variable implements Serializable {

	
	
	//A Stored Variable has a key, value + timestamp/team/userId
	//It has a scope 
	
	//The key is a String[] of up to X separate values.
	
	//Current columnIds for keypart.
		
	private static final long serialVersionUID = 6239650487891494128L;
	
	Map<String, String> keyChain = new HashMap <String,String>(); 
	//String value=null;
	String name=null;
	DataType myType=null;
	String myValue=null;

	Selection mySelection=null;
	
	private DbHelper myDb;

	private List<String> myRow;

	private Unit myUnit;
	
	public enum DataType {
		numeric,bool,list,text
	}
	
	public String getValue() {
		if (myValue==null)
			myValue = myDb.getValue(name,mySelection);
		return myValue;
	}
	
	public StoredVariableData getAllFields() {
		return myDb.getVariable(name,mySelection);
	}
	
	public void setValue(String value) {
		myValue = value;
		myDb.insertVariable(this,value);
	}
	
	
	public String getId() {
		return name;
	}
	public void setId(String name) {
		this.name = name;
	}
	
	public Map<String, String> getKeyChain() {
		return keyChain;
	}
	
	public Unit getUnit() {
		return myUnit;
	}
	
	public DataType getType() {
		return myType;
	}
	
	public List<String> getBackingDataSet() {
		return myRow;
	}
	
	public Selection getSelection() {
		return mySelection;
	}
	
	
	public Variable(String name,List<String> row,Map<String,String>keyChain, GlobalState gs) {
		this.name = name;
		myType = gs.getArtLista().getnumType(row);
		myUnit = gs.getArtLista().getUnit(row);
		myRow = row;
		this.keyChain=keyChain;		
		myDb = gs.getDb();
		mySelection = myDb.createSelection(keyChain,name);
	}




	
	
}
