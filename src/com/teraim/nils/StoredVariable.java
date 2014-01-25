package com.teraim.nils;

import java.io.Serializable;

public class StoredVariable implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6239650487891494128L;
	long rowId=-1;
	String rutId=null,provytaId=null,delytaId=null,smaytaId=null,value=null;
	public String getSmaytaId() {
		return smaytaId;
	}
	public void setSmaytaId(String smaytaId) {
		this.smaytaId = smaytaId;
	}
	String lag=null,author=null,varId=null,timeStamp=null;
	Type type=null;
	enum Type {
		ruta,provyta,delyta
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
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public StoredVariable(String rutId, String provytaId, String delytaId,
			String value, String lag, String author, String varId,
			String timeStamp, Type type) {
		super();
		this.rutId = rutId;
		this.provytaId = provytaId;
		this.delytaId = delytaId;
		this.value = value;
		this.lag = lag;
		this.author = author;
		this.varId = varId;
		this.timeStamp = timeStamp;
		this.type = type;
	}
	public void setId(long rId) {
		rowId = rId;
	}
	
	public long getId() {
		return rowId;
	}
	
	
	
	
}
