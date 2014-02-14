package com.teraim.nils.dynamic.types;

import android.content.Context;

import com.teraim.nils.GlobalState;
import com.teraim.nils.utils.DbHelper;

public abstract class ParameterCache  {
	
	DbHelper db;
	
	public ParameterCache(GlobalState gs){		
		db = gs.getDb();
	}

	//Abstracts that implement crud for variables on a database implementation.
	
	public abstract Variable getVariable(String varId);
	public abstract Variable storeVariable(String varId, String value);
	
	public Variable storeVariable(Variable var) {
		db.insertVariable(var);
		return var;
	}
	
	protected void deleteVariable(Variable var) {
		db.deleteVariable(var);
	}
	
	protected Variable getDelyteVariable(String rutId, String provyteId, String delyteId, String varId) {
		return db.getVariable(Variable.StorageType.delyta,rutId, provyteId, delyteId, varId);
	}
	protected Variable getProvyteVariable(String rutId, String provyteId, String varId) {
		return db.getVariable(Variable.StorageType.provyta,rutId, provyteId, null, varId);
	}
	
	protected Variable getRutVariable(String rutId,String varId) {
		return db.getVariable(Variable.StorageType.ruta,rutId, null, null, varId);
	}
		
	
	
	
	
	
}
