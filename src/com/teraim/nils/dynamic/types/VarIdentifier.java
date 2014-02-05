package com.teraim.nils.dynamic.types;
import android.content.Context;
import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.StoredVariable;
import com.teraim.nils.StoredVariable.Type;
import com.teraim.nils.Variable;
import com.teraim.nils.dynamic.types.Workflow.Unit;

//Class used to identify a variable, given that CURRENT Delyta/Provyta/Ruta (the context) is the parent.

public class VarIdentifier {

	public Variable.Type numType;
	public StoredVariable.Type varType;
	private String id,label;
	public Unit unit;
	private StoredVariable myStoredVar=null;		
	ParameterCache pc;
	private GlobalState gs;
	
	public VarIdentifier(Context ctx,String varLabel,String varId, Variable.Type numType, StoredVariable.Type varType, Unit unit) {
		gs = GlobalState.getInstance(ctx);
		id = varId;
		label = varLabel;
		this.numType=numType;
		this.varType=varType;
		this.unit=unit;
		if(varType == Type.ruta) {
			pc = gs.getCurrentRuta();
		} else {
			if(varType == Type.provyta) {
				pc = gs.getCurrentProvyta();
			} else {
				assert(varType == Type.delyta);
				pc = gs.getCurrentDelyta();
			} 
		}
		if (pc==null) 
			Log.e("nils","No parametercache identified for variable "+varId);
	}

	public String getPrintedValue() {
		if (pc==null) {
			Log.e("nils","Print: No parametercache identified for variable "+id);
			return "";
		}
		
		myStoredVar = pc.getVariable(id);
		if (myStoredVar!=null)
			return myStoredVar.getValue();		
		return "";
	}

	public void setValue(String value) {
		Log.d("nils","setvalue called for "+id+" with value "+value);
		//if we know the storedvar...
		if (myStoredVar!=null) {
			myStoredVar.setValue(value);
			pc.storeVariable(myStoredVar);
		}
		else
			pc.storeVariable(id,value);
	}

	public String getLabel() {
		return this.label;
	}
	
	public String getId() {
		return this.id;
	}
	public String getPrintedUnit() {
		if (unit == Unit.percentage)
			return "%";
		if (unit == Unit.nd)
			return "";
		else
			return unit.name();
	}

	public Long getValue() {
		Long ret=null;
		String sval = getPrintedValue();
		if (!sval.isEmpty()) {
			try {
				ret = Long.parseLong(sval);
			} catch (NumberFormatException e) {
				Log.e("nils","Not a number "+id+" had value "+sval);
				return null;
			}
		}
		return ret;
	}


}