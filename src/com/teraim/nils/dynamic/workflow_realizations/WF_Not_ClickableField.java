package com.teraim.nils.dynamic.workflow_realizations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teraim.nils.R;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Variable.DataType;

public abstract class WF_Not_ClickableField extends WF_ListEntry {
	protected WF_Context myContext;
	protected TextView myHeader;
	protected String myDescription;
	final LinearLayout outputContainer;
	protected Map<Variable,OutC> myOutputFields = new HashMap<Variable,OutC>();

	//Hack! Used to determine what is the master key for this type of element.
	//If DisplayOut & Virgin --> This is master key.
	boolean virgin=true;
	protected Variable myVar;
	public abstract LinearLayout getFieldLayout();



	//	public abstract String getFormattedText(Variable varId, String value);


	@Override
	public Set<Variable> getAssociatedVariables() {
		Set<Variable> s = new HashSet<Variable>();
		s.add(myVar);
		return s;
	}

	public class OutC {
		public OutC(LinearLayout ll, String f) {
			view = ll;
			format = f;
		}
		public LinearLayout view;
		public String format;
	}


	public WF_Not_ClickableField(final String label,final String descriptionT, WF_Context myContext, 
			View view,boolean isVisible) {
		super(view,myContext,isVisible);

		this.myContext = myContext;
		myHeader = (TextView)getWidget().findViewById(R.id.editfieldtext);
		outputContainer = (LinearLayout)getWidget().findViewById(R.id.outputContainer);
		//outputContainer.setLayoutParams(params);
		myHeader.setText(label);
		this.label = label;
		myDescription = descriptionT;


	}

	public void addVariable(Variable var, boolean displayOut, String format, boolean isVisible) {

		if (displayOut && virgin) {
			virgin = false;
			super.setKeyRow(var);
		}		
		if (displayOut) {
			LinearLayout ll = getFieldLayout();

			/*
			 TextView o = (TextView)ll.findViewById(R.id.outputValueField);
			TextView u = (TextView)ll.findViewById(R.id.outputUnitField);

			String value = Variable.getPrintedValue();
			if (!value.isEmpty()) {
				o.setText(varLabel+": "+value);	
				u.setText(" ("+Variable.getPrintedUnit()+")");
			}
			 */
			myOutputFields.put(var,new OutC(ll,format));
			outputContainer.addView(ll);
			myVar = var;
		}

	}


	@Override
	public void refreshOutputFields() {
		//Log.d("nils","refreshoutput called on "+myHeader);
		Iterator<Map.Entry<Variable,OutC>> it = myOutputFields.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Variable,OutC> pairs = (Map.Entry<Variable,OutC>)it.next();
			//Log.d("nils","Iterator has found "+pairs.getKey()+" "+pairs.getValue());
			Variable varId = pairs.getKey();
			LinearLayout ll = pairs.getValue().view;
			TextView o = (TextView)ll.findViewById(R.id.outputValueField);
			TextView u = (TextView)ll.findViewById(R.id.outputUnitField);			
			String value = varId.getValue();
			
			//Log.d("nils","In refreshoutputfield for variable "+varId.getId()+" with value "+varId.getValue());
			
			if (value!=null&&!value.isEmpty()) {
				o.setText(getFormattedText(varId,value,pairs.getValue().format));	
				u.setText(varId.getPrintedUnit());				
			}
			else {
				o.setText("");
				u.setText("");
			}
		}	
	}

	//TODO: This is of course  wrong.
	@Override
	public void refreshInputFields() {

	}

	public String getFormattedText(Variable varId, String value, String format) {
		int lf=0,rf=0;
		boolean hasFormat = false, hasDot = false;
		if (value!=null||value.length()>0) {
			if (format!=null) {
				hasFormat = true;
				if (format.contains(".")) {
					hasDot = true;
					String[] p = format.split("\\.");
					if (p!=null && p.length==2) {
						lf = p[0].length();
						rf = p[1].length();
					} 
				} else
					lf = format.length();
			}

			if (hasFormat) {
				int l = value.length();
				if (hasDot) {
					if (!value.contains(".")) {
						value += ".0";
					}
					String[] p = value.split("\\.");
					if (p!=null && p.length==2) {
						String Rf = p[1];
						if (Rf.length()>rf) 
							Rf = p[1].substring(0, rf);					
						if (Rf.length()<rf)
							Rf = addZeros(Rf,rf-Rf.length());
						String Lf = p[0];
						if (Lf.length()>lf) 
							Lf = p[0].substring(0,lf);					
						if (Lf.length()<lf)
							Lf = addSpaces(Lf,lf-Lf.length());
						value = Lf+"."+Rf;
					}		
				} else {
					if(value.contains(".")) {
						String p[]  = value.split("\\.");
						value = p[0];
					}
					if (value.length()<lf) 
						value = addSpaces(value,lf-value.length());

				}

			}
		}
		return value;
	}

	private String addZeros(String s,int i) {
		while (i-->0)
			s="0"+s;
		return s;
	}
	private String addSpaces(String s,int i) {
		while (i-->0)
			s=" "+s;
		return s;
	}


}
