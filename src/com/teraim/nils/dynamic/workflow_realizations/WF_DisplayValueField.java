package com.teraim.nils.dynamic.workflow_realizations;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import android.util.Log;
import android.view.LayoutInflater;
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

	private String formula,format;
	protected GlobalState gs;
	protected Unit unit;
	private Set<Entry<String,DataType>> myVariables;
	boolean fail = false;
	boolean stringT = false;

	public WF_DisplayValueField(String id, String formula,WF_Context ctx, Unit unit, 
			String label, boolean isVisible,String format) {
		super(id, LayoutInflater.from(ctx.getContext()).inflate(R.layout.display_value_textview,null), isVisible,ctx);
		((TextView)getWidget().findViewById(R.id.header)).setText(label);
		gs = GlobalState.getInstance(ctx.getContext());
		o = gs.getLogger();
		this.formula = formula;
		Log.d("nils","In WF_DisplayValueField Create");	
		ctx.addEventListener(this, EventType.onSave);	
		this.unit=unit;
		myVariables = Tools.parseFormula(gs,formula);
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


		if (fail) {
			o.addRow("");
			o.addRedText("Parsing of formula for DisplayValueBlock failed. Formula: "+formula);
		}
		this.onEvent(new WF_Event_OnSave("fuckme"));
	}

	//update variable.
	@Override
	public void onEvent(Event e) {

		String strRes="";
		String subst;
		Log.d("nils","Got event in WF_DisplayValueField");	
		if (!fail) {

			subst = Tools.substituteVariables(gs,myVariables,formula,stringT);
			if (subst!=null ) {
				if (!stringT ) {
					if (Tools.isNumeric(subst)) 
						strRes = subst;
					else {
						strRes = Tools.parseExpression(gs,formula,subst);
						if (strRes==null) {
							o.addRow("");
							o.addRedText("Formula "+formula+" is not being calculated because of RT parse error");	
							return;
						}
					}
				} else
					strRes = subst;
			} else {
				o.addRow("");
				o.addYellowText("Formula "+formula+" is not being calculated because substitution failed");				
			}
		} else {
			o.addRow("");
			o.addYellowText("Formula "+formula+" is not being calculated because of parse errors");
			return;
		}
		((TextView)this.getWidget().findViewById(R.id.outputValueField)).setText(strRes);
		((TextView)this.getWidget().findViewById(R.id.outputUnitField)).setText(Tools.getPrintedUnit(unit));


	}




}
