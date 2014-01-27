package com.teraim.nils;
import android.util.Log;

import com.teraim.nils.DataTypes.Unit;
import com.teraim.nils.StoredVariable.Type;

//Class used to identify a variable, given that CURRENT Delyta/Provyta/Ruta (the context) is the parent.

public class VarIdentifier {

	public Variable.Type numType;
	public StoredVariable.Type varType;
	public String id,label;
	public Unit unit;
	ParameterCache pc;
	
	public VarIdentifier(String varLabel,String varId, Variable.Type numType, StoredVariable.Type varType, Unit unit) {
		id = varId;
		label = varLabel;
		this.numType=numType;
		this.varType=varType;
		this.unit=unit;
		if(varType == Type.ruta) {
			pc = CommonVars.cv().getCurrentRuta();
		} else {
			if(varType == Type.provyta) {
				pc = CommonVars.cv().getCurrentProvyta();
			} else {
				assert(varType == Type.delyta);
				pc = CommonVars.cv().getCurrentDelyta();
			} 
		}
		if (pc==null) 
			Log.e("nils","No parametercache identified for variable "+varId);
	}

	public String getPrintedValue() {
		if (pc==null) {
			Log.e("nils","No parametercache identified for variable "+id);
			return "";
		}
		StoredVariable sv;		
		sv = pc.getVariable(id);
		if (sv!=null)
			return sv.getValue();		
		return "";
	}

	public void setValue(String value) {
		pc.storeVariable(id, value);
	}

}