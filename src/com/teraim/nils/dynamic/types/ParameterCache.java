package com.teraim.nils.dynamic.types;

import android.content.Context;

import com.teraim.nils.GlobalState;
import com.teraim.nils.StoredVariable;
import com.teraim.nils.utils.DbHelper;

public abstract class ParameterCache  {
	
	DbHelper db;
	
	public ParameterCache(GlobalState gs){		
		db = gs.getDb();
	}

	//Abstracts that implement crud for variables on a database implementation.
	
	public abstract StoredVariable getVariable(String varId);
	public abstract StoredVariable storeVariable(String varId, String value);
	
	public StoredVariable storeVariable(StoredVariable var) {
		db.insertVariable(var);
		return var;
	}
	
	protected void deleteVariable(StoredVariable var) {
		db.deleteVariable(var);
	}
	
	protected StoredVariable getDelyteVariable(String rutId, String provyteId, String delyteId, String varId) {
		return db.getVariable(StoredVariable.Type.delyta,rutId, provyteId, delyteId, varId);
	}
	protected StoredVariable getProvyteVariable(String rutId, String provyteId, String varId) {
		return db.getVariable(StoredVariable.Type.provyta,rutId, provyteId, null, varId);
	}
	
	protected StoredVariable getRutVariable(String rutId,String varId) {
		return db.getVariable(StoredVariable.Type.ruta,rutId, null, null, varId);
	}
		
	
	
	
	
	
}
