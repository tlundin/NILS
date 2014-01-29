package com.teraim.nils.flowtemplates;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.teraim.nils.CommonVars;
import com.teraim.nils.DataTypes.Block;
import com.teraim.nils.DataTypes.ButtonBlock;
import com.teraim.nils.DataTypes.Container;
import com.teraim.nils.DataTypes.ContainerDefineBlock;
import com.teraim.nils.DataTypes.CreateListEntriesBlock;
import com.teraim.nils.DataTypes.Rule;
import com.teraim.nils.DataTypes.SortingBlock;
import com.teraim.nils.DataTypes.StartBlock;
import com.teraim.nils.DataTypes.WF_Container;
import com.teraim.nils.DataTypes.WF_Context;
import com.teraim.nils.DataTypes.Workflow;
import com.teraim.nils.R;
import com.teraim.nils.Variable;
import com.teraim.nils.exceptions.RuleException;
import com.teraim.nils.expr.SyntaxException;

/*
 * Executes workflow blocks. Child classes define layouts and other specialized behavior
 */
public abstract class Executor extends Activity {

	protected Workflow wf;
	
	//Extended context.
	protected WF_Context myContext;
	//Keep track of input in below arraylist.

	protected final Map<Rule,Boolean>executedRules = new LinkedHashMap<Rule,Boolean>();	

	protected List<Rule> rules = new ArrayList<Rule>();
	
	
	protected abstract List<WF_Container> getContainers();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		myContext = new WF_Context(this);
		myContext.addContainers(getContainers());
		wf = getFlow();
		//Execute called from child onCreate.
	}


	

	protected Workflow getFlow() {
		Workflow wf=null;

		//Find out the name of the workflow to execute.
		Bundle b = getIntent().getExtras();
		String name = b.getString("workflow_name");
		if (name!=null) 
			wf = CommonVars.cv().getWorkflow(name);

		if (wf==null||name==null) {
			Log.e("NILS","Workflow "+name+" NOT found!");
			new AlertDialog.Builder(this).setTitle("Ups!")
			.setMessage("Kan tyvärr inte hitta workflow med namn: '"+name+"'. Kan det vara en felstavning?")
			.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {

				}})
				.show();				

			String heck[] = CommonVars.cv().getWorkflowNames();
			for (int i = 0 ; i< heck.length; i++)
				Log.e("NILS","Workflow "+i+": "+heck[i]);
			return null;
		} else {
			Log.d("NILS","Now executing workflow "+name);

		}
		return wf;
	}
	
	
	/**
	 * Execute the workflow.
	 */
	protected void execute() {
		//TEST CODE
		//LinearLayout my_root = (LinearLayout) findViewById(R.id.myRoot);

		List<Block>blocks = wf.getBlocks();
		for (Block b:blocks) {
			if (b instanceof StartBlock)
				Log.d("NILS","Startblock found");
			else if (b instanceof ContainerDefineBlock) {
				//for now all containers are assumed to be part of template.
				String id = (((ContainerDefineBlock) b).getContainerName());
				if (id!=null) {
					if (myContext.getContainer(id)!=null)
						Log.d("nils","found hardcoded templatecontainer for "+id);
					else
						Log.e("nils","Could not find container "+id+". Will default to root");
				}
			}			
			else if (b instanceof ButtonBlock) {

			}			
			else if (b instanceof SortingBlock) {
				
			}
			else if (b instanceof CreateListEntriesBlock) {
				CreateListEntriesBlock bl = (CreateListEntriesBlock)b;
				bl.createListFromFile(myContext);
			}

		}
		
		//Now all blocks are executed.
		//Draw the UI.
		
		Container root = myContext.getContainer("root");
		root.draw();

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*			final Map<String, ViewGroup> layoutContainers = getBlockContainers();

	Log.d("NILS","Drawable_block found");
	final Drawable_Block bl = (Drawable_Block)b;
	//find out if template has a container for this element..
	ViewGroup target=null;
	if (layoutContainers!=null && bl.getContainerId()!=null) {					
		target = layoutContainers.get(bl.getContainerId());
	}
	if (target!=null)
		Log.d("nils","Template had this container :"+bl.getContainerId());
	else {
		Log.d("nils","Did not find container :"+bl.getContainerId()+" will try default: ");
		target= getTemplateDefaultContainer();
	}
	if (target!=null) 
		bl.draw(this, target);
	else
		Log.e("nils","no container found to draw block.");
	
*/



	

	
	/*
		final InputAlertBuilder.AlertBuildHelper abh = new AlertBuildHelper(this.getBaseContext()) {
			@Override
			public View createView(ViewGroup root) {
				// Set an EditText view to get user input 
				myView = root;

				int typeId = (inputType ==InputType.TYPE_CLASS_NUMBER)?R.layout.edit_field:R.layout.edit_field_komma;
				final EditText input = (EditText)LayoutInflater.from(c).inflate(typeId, null);

				//input.setText(et.getText());
				//input.setInputType(inputType);

				return input;
			}

			@Override
			public void setResult(StoredVariable[] sv,View inputView,View outputView) {
				setStringValue(id,((EditText)inputView).getText().toString(),outputView);
			}};

			v.setOnClickListener(InputAlertBuilder.createAlert(id,headerT,bodyT,abh,v));

			return v;		

	}
	 */




	//Evaluate all rules.
	//Show the rules that were broken in the UI.
	private void validate() {

		boolean result=false;
		executedRules.clear();
		for(Rule rule:rules) {
			try {
				//Test...
				result = rule.execute();
			}  catch (SyntaxException e) {
				Log.e("NILS","SyntaxException! "+e.getMessage()+" in "+rule.getName());

				continue;
			}
			//Find the target.
			Variable target;
			try {
				target = rule.getTarget();
			} catch (RuleException e) {
				//If the variable does not exist, continue
				Log.e("NILS","Variable was missing in rule validation: "+rule.getName());
				continue;
			}
			Log.d("NILS","Target is "+target.getName());
			//View v = bindings.get(target);
			View v = null;
			if (v==null)
				Log.e("NILS", "TARGET NOT FOUND FROM BINDINGS!!");
			//Found a broken rule!
			if(result==false) {
				v.setBackgroundColor(Color.RED);

				Toast.makeText(this, rule.getErrorMessage(), Toast.LENGTH_LONG).show();
			}
			else 
				v.setBackgroundColor(getResources().getColor(R.color.background));

			executedRules.put(rule,result);

		}
		//TODO: Add rules.
		if (executedRules.size()>0) {
			/*validator_layer.setVisibility(View.VISIBLE);
			mAdapter.notifyDataSetChanged();
			lv.requestFocusFromTouch();
			lv.setSelection(0);
			Iterator<Entry<Rule, Boolean>> it = executedRules.entrySet().iterator();
			Entry<Rule, Boolean>e = null;
			e = it.next();
			if (e!=null) 
				errorView.setText(e.getKey().getErrorMessage());
			 */

		} //else
		//validator_layer.setVisibility(View.GONE);
	}

	

}