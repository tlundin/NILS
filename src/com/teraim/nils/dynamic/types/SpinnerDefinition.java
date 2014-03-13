package com.teraim.nils.dynamic.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpinnerDefinition implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3915632410406656481L;
	public class SpinnerElement implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 9162426573700197032L;
		public String value,opt,descr; 
		public List<String> varMapping = new ArrayList<String>();	
		public SpinnerElement(String val,String opt,String vars,String descr) {
			value = val;
			this.opt = opt;
			this.descr=descr;
			if (vars!=null&&!vars.isEmpty()) {
				String[] v = vars.split("\\|");
				for (String s:v) 
					varMapping.add(s);
			}
		}
	}
	
	String spinnerId;
	Map<String,List<SpinnerElement>> myElements = new HashMap<String,List<SpinnerElement>>();
	
	public List<SpinnerElement> get(String spinnerId){
		return myElements.get(spinnerId);
	}
	public void add(String id,List<SpinnerElement> l) {
		myElements.put(id, l);
	}
	public int size() {
		return myElements.size();
	}
}
