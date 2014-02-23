package com.teraim.nils.dynamic.workflow_realizations;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.teraim.nils.GlobalState;
import com.teraim.nils.R;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Variable.DataType;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.dynamic.workflow_abstracts.Event;
import com.teraim.nils.dynamic.workflow_abstracts.Event.EventType;
import com.teraim.nils.dynamic.workflow_abstracts.EventListener;
import com.teraim.nils.expr.Expr;
import com.teraim.nils.expr.Parser;
import com.teraim.nils.expr.SyntaxException;
import com.teraim.nils.utils.Tools;

public class WF_DisplayValueField extends WF_Widget implements EventListener {

	private String formula;
	protected GlobalState gs;
	protected Unit unit;
	private Set<Entry<String,DataType>> myVariables;
	boolean fail = false;
	boolean stringT = false;

	public WF_DisplayValueField(String id, View v, String formula,WF_Context ctx, Unit unit, String label, boolean isVisible) {
		super(id, v, isVisible,ctx);
		((TextView)v.findViewById(R.id.header)).setText(label);
		gs = GlobalState.getInstance(ctx.getContext());
		o = gs.getLogger();
		this.formula = formula;
		Log.d("nils","In WF_DisplayValueField Create");	
		ctx.addEventListener(this, EventType.onSave);	
		this.unit=unit;
		Set<String> potVars = new HashSet<String>();

		//Try parsing the formula.
		String pattern = "+-*/()0123456789 ";
		String inPattern = "+-*/) ";
		boolean in = false;
		String curVar = null;
		if (formula !=null) {
			for (int i = 0; i < formula.length(); i++){
				char c = formula.charAt(i);  
				if (!in) {
					//assume its in & test
					in = true;   
					curVar = "";
					for(int j=0;j<pattern.length();j++)
						if (c == pattern.charAt(j)) {
							//System.out.println("found non-var char: "+pattern.charAt(j));
							//fail.
							in = false;
							break;
						}
				} else {
					//ok we are in. check if char is part of inPattern
					for(int j=0;j<inPattern.length();j++)
						if (c == pattern.charAt(j)) {
							//System.out.println("found non-var char inside: "+pattern.charAt(j));
							//fail.
							in = false;
							System.out.println("Found variable: "+curVar);
							potVars.add(curVar);
							curVar="";
							break;
						}
				}
				//Add if in.
				if (in)
					curVar += c;		    		    	
			}
			if (curVar.length()>0) {
				System.out.println("Found variable: "+curVar);
				potVars.add(curVar);
			}
			if (potVars == null || potVars.size()==0) {
				fail = true; 
				o.addRow("");
				o.addRedText("Found no variables in formula "+formula+". Variables starts with a..zA..z");
			}
			else {
				myVariables = new HashSet<Entry<String,DataType>>();
				for (String var:potVars) {
					List<String> row = gs.getArtLista().getCompleteVariableDefinition(var);
					
					if (row == null) {
						o.addRow("");
						o.addRedText("Couldn't find variable "+var+" referenced in formula "+formula);
						fail = true;
					} else {
						DataType type = gs.getArtLista().getnumType(row);					
						myVariables.add(new AbstractMap.SimpleEntry<String, DataType>(var.trim(),type));
					}
				}

				if (myVariables==null)
					fail = true;
				else {
					for (Entry<String, DataType>e:myVariables) {
						if (e.getValue()==DataType.text) {
							stringT = true;
							continue;
						} else
							if (stringT) {
								o.addRow("");
								o.addText("Text type mixed with non-text Type in formula: "+formula+". This is not allowed");
								fail = true;
							}
					}	

				}
			}
		}
		else {
			Log.d("nils","got null in formula");
			o.addRow("");
			o.addRedText("Formula evaluates to null in DisplayValueField");			
			fail = true;
		}

		if (fail) {
			o.addRow("");
			o.addRedText("Parsing of formula for DisplayValueBlock failed. Formula: "+formula);
		}
	}

	//update variable.
	@Override
	public void onEvent(Event e) {
		
		String strRes="";
		String subst=new String(formula);
		Log.d("nils","Got event in WF_DisplayValueField");	
		if (!fail) {
			Variable st;
			boolean substErr = false;
			for (Entry<String, DataType> entry:myVariables) {
				st = gs.getArtLista().getVariableInstance(entry.getKey());
				if (st==null||st.getValue()==null) {
					o.addRow("Couldn't find a value for variable "+entry.getKey()+". Formula cannot be calculated: "+formula);
					substErr=true;					
					break;
				} else {
					if (stringT) {
						
						strRes+=st.getValue();
					}
					else {
						subst = subst.replace(st.getId(), st.getValue());
						Log.d("nils","formula after subst: "+subst);
						if (st.getValue()==null||st.getValue().isEmpty()) {
							substErr=true;
							Log.d("nils","Variable has no value in substitution...");
						}
							
					}
				}
			}
			if (!substErr && !stringT ) {
				Parser p = gs.getParser();
				Expr exp=null;
				try {
					exp = p.parse(subst);
				} catch (SyntaxException e1) {
					o.addRow("");
					o.addRedText("Syntax error for formula "+formula+" after substitution to "+subst);
					e1.printStackTrace();
				}
				if (exp==null) 
				{
					o.addRow("");
					o.addRedText("Expr error for "+formula+" (after substitution) "+subst+". Expr is null");	
					return;
				} else
					strRes = Double.toString(exp.value());
			} else {
				
			}
		} else {
			o.addRow("");
			o.addYellowText("Formula "+formula+" is not being calculated because of parse errors");
			return;
		}
		((TextView)this.getWidget().findViewById(R.id.text)).setText(strRes+(unit==Unit.nd?"":Tools.getPrintedUnit(unit)));
	
	}




}
