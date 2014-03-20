package com.teraim.nils.dynamic.blocks;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.Executor;
import com.teraim.nils.dynamic.types.Variable.DataType;
import com.teraim.nils.utils.Tools;

public class ConditionalContinuationBlock extends Block {

	String elseID,expr;
	List<String>variables;
	public ConditionalContinuationBlock(String id, List<String> varL,
			String expr, String elseBlockId) {
		this.blockId=id;
		this.variables=varL;
		this.expr=expr;
		this.elseID=elseBlockId;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5923203475793337276L;
	public String getFormula() {
		return expr;
	}
	public String getElseId() {
		return elseID;
	}
    public final static int STOP = 1,JUMP=2,NEXT = 3;
    
    Integer lastEval = null;
    
	public boolean evaluate(GlobalState gs,String formula,
				Set<Entry<String, DataType>> vars) {
			//assume fail
			int eval=STOP;
			Log.d("nils","Variables found: "+vars.size());
			String subst = Tools.substituteVariables(gs,vars,formula,false);
			if (subst!=null) {
				String strRes = Tools.parseExpression(gs,formula,subst);
				if (strRes != null) {
					Log.e("nils","YIPEEE "+strRes);
					if (Double.parseDouble(strRes)==1) {
						Log.d("nils","Evaluates to true");
						eval=JUMP;
					} else {
						eval=NEXT;
						Log.d("nils","Evaluates to false");
					}
				} else {
					Log.e("nils","AWWWWWWWWwwwww");
					eval=STOP;
				}
			} else {
				Log.e("nils","Substitution failed for formula ["+formula+"]");
				eval=STOP;
			}

		
		boolean ret = lastEval==null?true:eval!=lastEval;
		lastEval = eval;
		return ret;
	}
	public Integer getCurrentEval() {
		return lastEval;
	}

	
}
