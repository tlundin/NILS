package com.teraim.nils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import android.util.Log;
import com.teraim.nils.DataTypes.Workflow.Unit;
import com.teraim.nils.StoredVariable.Type;

public class VarToListConfigRow {

	public String getListEntryName() {
		return listEntryName;
	}

	public String getVarName() {
		return varName;
	}

	public String getEntryLabel() {
		return entryLabel;
	}

	public String getAction() {
		return action;
	}

	public String getVarLabel() {
		return varLabel;
	}

	public Variable.Type getnumType() {
		return numType;
	}

	public StoredVariable.Type getVarType() {
		return Type.delyta;
	}
	
	public boolean isDisplayInList() {
		return displayInList;
	}

	public Unit getUnit() {
		return unit;
	}

	static final int VAR_TO_LIST_CONFIG_ROW_LENGTH = 9;

	

	String listEntryName;
	String varName;
	String entryLabel;
	String action;
	String varLabel;
	//StoredVariable.Type varType;
	Variable.Type numType;
	boolean displayInList=false;
	Unit unit;

	private VarToListConfigRow(String[] row,boolean display,Unit unit) {
		int c=0;
		listEntryName=row[c++];
		varName=row[c++];
		entryLabel=row[c++];
		action=row[c++];
		varLabel=row[c++];
		String type = row[c++];
		numType= (type.equals("number"))?Variable.Type.NUMERIC:Variable.Type.LITERAL;
		
		displayInList=display;
		this.unit=unit;
	}

	public static VarToListConfigRow createRow(String[] row) {
		if(row.length!=VAR_TO_LIST_CONFIG_ROW_LENGTH) {
			Log.e("NILS","Row is either too short or too long: "+row.length);
			return null;
		}
		String t = row[6];
		String u = row[7];	
		Unit unit;
		if (u.equals("dm"))
			unit = Unit.dm;
		else if(u.equals("%"))
			unit = Unit.percentage;
		else {
			unit = Unit.undefined;
			Log.d("nils","Could not recognize unit: "+u+". Supported: % and dm");
		}
		return new VarToListConfigRow(row,!(t==null||t.equalsIgnoreCase("FALSE")),
				unit);

	}






}