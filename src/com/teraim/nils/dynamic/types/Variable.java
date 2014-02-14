package com.teraim.nils.dynamic.types;

import java.io.Serializable;

public class Variable implements Serializable {

	
	
	//A Stored Variable has a key, value + timestamp/team/userId
	//It has a scope 
	
	//The key is a String[] of up to X separate values.
	
	//Current columnIds for keypart.
		
	private static final long serialVersionUID = 6239650487891494128L;
	
	String smaytaId,rutId,provytaId,delytaId;
	//Default for 
	
	long rowId=-1;
	String value=null;
	public String getSmaytaId() {
		return smaytaId;
	}
	public void setSmaytaId(String smaytaId) {
		this.smaytaId = smaytaId;
	}
	String lag=null,author=null,varId=null,timeStamp=null;
	StorageType type=null;
	public enum StorageType {
		ruta,provyta,delyta
	}
	public enum DataType {
		numeric,bool,list,text
	}
	public String getRutId() {
		return rutId;
	}
	public void setRutId(String rutId) {
		this.rutId = rutId;
	}
	public String getProvytaId() {
		return provytaId;
	}
	public void setProvytaId(String provytaId) {
		this.provytaId = provytaId;
	}
	public String getDelytaId() {
		return delytaId;
	}
	public void setDelytaId(String delytaId) {
		this.delytaId = delytaId;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getLag() {
		return lag;
	}
	public void setLag(String lag) {
		this.lag = lag;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getVarId() {
		return varId;
	}
	public void setVarId(String varId) {
		this.varId = varId;
	}
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public StorageType getType() {
		return type;
	}
	public void setType(StorageType type) {
		this.type = type;
	}
	public Variable(String rutId, String provytaId, String delytaId,
			String value, String varId, StorageType type) {
		this.rutId = rutId;
		this.provytaId = provytaId;
		this.delytaId = delytaId;
		this.value=value;
		this.varId = varId;
		this.type = type;
		
	}
	public void setDatabaseId(long rId) {
		rowId = rId;
	}
	
	public long getId() {
		return rowId;
	}
	public boolean existsInDB() {
		return !(rowId == -1);
	}
	
	
	
	
}
