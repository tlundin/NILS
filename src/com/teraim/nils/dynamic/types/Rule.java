package com.teraim.nils.dynamic.types;

import android.content.Context;
import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.Variable;
import com.teraim.nils.exceptions.RuleException;
import com.teraim.nils.expr.Expr;
import com.teraim.nils.expr.Parser;
import com.teraim.nils.expr.SyntaxException;

public  class Rule {

	public String targetName, condition, action, errorMsg,name;
	private Context ctx;
	private GlobalState gs;
	public Rule(Context ctx,String ruleName, String target, String condition,
			String action, String errorMsg) {
		this.ctx = ctx;
		this.gs = GlobalState.getInstance(ctx);
		this.name=ruleName;
		this.targetName=target;
		this.condition=condition;
		this.action=action;
		this.errorMsg=errorMsg;
		
		Log.e("NILS","Create Rule with name "+ruleName+" and target "+target+" and cond "+ condition);

	}
	public Variable getTarget() throws RuleException {
		//TODO: Change!!
		Variable var = null;//GlobalState.getInstance(ctx).getVariable(targetName);
		if (var==null)
			throw new RuleException("Variable "+targetName+" must exist");

		return var;
	}	
	//Execute Rule. Target will be colored accordingly.
	public boolean execute() throws SyntaxException {
		Expr result=null;
		result = gs.getParser().parse(condition);
		Log.d("NILS","Result of eval was: "+result.value());
		return (result.value()==1.0);
	}

	public String getErrorMessage() {
		return errorMsg;
	}
	public String getName() {
		return name;
	}	
}
