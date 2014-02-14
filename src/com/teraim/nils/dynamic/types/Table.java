package com.teraim.nils.dynamic.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

public class Table implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1183209171210448313L;
	//The table is a Map of key=Header,value=List of Data.
	private final Map<String,List<String>> colTable=new HashMap<String,List<String>>();
	private final Map<Integer,List<String>> rowTable=new HashMap<Integer,List<String>>();
	//Immutable list of Required columns.
	private int columnCount=0,rowCount=0;
	private String[] myColumns;



	public Table (String[] columnNames) {
		assert(columnNames!=null);
		for(String key:columnNames) 
			colTable.put(key, new ArrayList<String>());		
		columnCount = columnNames.length;
		myColumns = columnNames;
	}

	public enum ErrCode {
		tooManyColumns,
		tooFewColumns,
		ok
	};
	
	public ErrCode addRow(List<String> rowEntries) {
		int index=0;
		int size = rowEntries.size();

		if (size > myColumns.length)
			return ErrCode.tooManyColumns;
		for(String entry:rowEntries) 
			colTable.get(myColumns[index++]).add(entry);
		rowTable.put(rowCount++, rowEntries);
//		if (size < myColumns.length) {
//			Log.d("nils","Number of columns: "+size+" Required number: "+myColumns.length);
//			return ErrCode.tooFewColumns;
		return ErrCode.ok;
	}


	public List<String> getColumn(String columnName) {
		return colTable.get(columnName);
	}

	public List<String> getRow(Integer row) {
		return rowTable.get(row);
	}

	public List<List<String>> getRowsContaining(String columnName, String pattern) {
		Log.d("nils","Trying to find rows matching column "+columnName+" and pattern "+pattern);
		List<List<String>> ret = null;
		List<String> column = colTable.get(columnName);
		if(column!=null) {
			for(int i = 0;i<column.size();i++) {
				Log.d("nils","i: "+i+" col: "+column.get(i));
				if (column.get(i).equalsIgnoreCase(pattern)||column.get(i).matches(pattern)) {
					if (ret == null)
						ret = new ArrayList<List<String>>();
					ret.add(rowTable.get(i));
				}
			}
			if (ret!=null)
				Log.d("nils","Returning "+ret.size()+" rows in getRows(Table)");
		} 
		return ret;
	}

	public int getColumnIndex(String c) {
		for (int i=0;i<myColumns.length;i++)
			if (c.equals(myColumns[i]))
				return i;
		return -1;
	}

	public List<String> getRowContaining(String columnName,
			String key) {
		List<String> column = colTable.get(columnName);
		for(int i = 0;i<column.size();i++) 
			if (column.get(i).equalsIgnoreCase(key)) {
				//Log.d("nils","found master variable "+key+" in Artlista");
				return rowTable.get(i);
			}
		Log.d("nils","Did not find master variable "+key+" in Artlista column: "+columnName);
		return null;
	}

	public String getElement(String columnName,List<String> row) {
		String result = null;
		int index = getColumnIndex(columnName);
		if (index !=-1) {	
			result = row.get(index);
			//Log.d("nils","found field "+columnName+": "+result+" in class Table");
		} else
			Log.d("nils","Did NOT find field "+columnName+" in class Table");
		return result;
	}


}
