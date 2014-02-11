package com.teraim.nils.dynamic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.teraim.nils.GlobalState;
import com.teraim.nils.Logger;
import com.teraim.nils.R;
import com.teraim.nils.dynamic.blocks.AddSumOrCountBlock;
import com.teraim.nils.dynamic.blocks.Block;
import com.teraim.nils.dynamic.blocks.ButtonBlock;
import com.teraim.nils.dynamic.blocks.ContainerDefineBlock;
import com.teraim.nils.dynamic.blocks.CreateEntryFieldBlock;
import com.teraim.nils.dynamic.blocks.CreateListEntriesBlock;
import com.teraim.nils.dynamic.blocks.ListSortingBlock;
import com.teraim.nils.dynamic.blocks.StartBlock;
import com.teraim.nils.dynamic.types.Rule;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Workflow;
import com.teraim.nils.dynamic.workflow_abstracts.Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.exceptions.RuleException;
import com.teraim.nils.expr.SyntaxException;

/*
 * Executes workflow blocks. Child classes define layouts and other specialized behavior
 */
public abstract class Executor extends Fragment {

	protected Workflow wf;
	
	//Extended context.
	protected WF_Context myContext;
	
	//Normal context
	protected Activity activity;
	//Keep track of input in below arraylist.

	protected final Map<Rule,Boolean>executedRules = new LinkedHashMap<Rule,Boolean>();	

	protected List<Rule> rules = new ArrayList<Rule>();
	
	
	protected abstract List<WF_Container> getContainers();
	public abstract void execute(String function);

	protected GlobalState gs;
	
	protected Logger o;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this.getActivity();
		myContext = new WF_Context((Context)activity,this,R.id.content_frame);
		gs = GlobalState.getInstance((Context)activity);
		o = gs.getLogger();
		wf = getFlow();

		//Execute called from child onCreate.
	}


	

	protected Workflow getFlow() {
		Workflow wf=null;

		//Find out the name of the workflow to execute.
		Bundle b = this.getArguments();
		String name = b.getString("workflow_name");
		if (name!=null) 
			wf = gs.getWorkflow(name);

		if (wf==null||name==null) {
			o.addRow("");
			o.addRedText("Workflow "+name+" NOT found!");
			new AlertDialog.Builder((Context)activity).setTitle("Ups!")
			.setMessage("Kan tyv�rr inte hitta workflow med namn: '"+name+"'. Kan det vara en felstavning?")
			.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {

				}})
				.show();				

			String heck[] = gs.getWorkflowNames();
			o.addRow("Following workflows found: ");
			for (int i = 0 ; i< heck.length; i++)
				o.addRow("Workflow "+i+": "+heck[i]);
			return null;
		} else {
			o.addRow("*******EXECUTING: "+name);

		}
		return wf;
	}
	
	
	/**
	 * Execute the workflow.
	 */
	protected void run() {
		
		//LinearLayout my_root = (LinearLayout) findViewById(R.id.myRoot);		
		List<Block>blocks = wf.getBlocks();

		for (Block b:blocks) {
			
			if (b instanceof StartBlock) {
				o.addRow("");
				o.addYellowText("Startblock found");
			}
			
			else if (b instanceof ContainerDefineBlock) {
				o.addRow("");
				o.addYellowText("ContainerDefineBlock found");
				String id = (((ContainerDefineBlock) b).getContainerName());
				if (id!=null) {
					if (myContext.getContainer(id)!=null) {
						o.addRow("");
						o.addGreenText("found hardcoded templatecontainer for "+id);
					}
					else {
						o.addRow("");
						o.addRedText("Could not find container "+id+" in template! Will default to root");
					}
						
				}
			}			
			else if (b instanceof ButtonBlock) {
				o.addRow("");
				o.addYellowText("ButtonBlock found");
				ButtonBlock bl = (ButtonBlock) b;
				bl.create(myContext);
			}			
			else if (b instanceof ListSortingBlock) {
				o.addRow("");
				o.addYellowText("ListSortingBlock found");
				ListSortingBlock bl = (ListSortingBlock) b;
				bl.create(myContext);
			}/*
			else if (b instanceof ListFilterBlock) {
				o.addRow("");
				o.addYellowText("ListFilterBlock found");
				ListFilterBlock bl = (ListFilterBlock)b;
				bl.create(myContext);
			}*/
			else if (b instanceof CreateListEntriesBlock) {
				o.addRow("");
				o.addYellowText("CreateListEntriesBlock found");
				CreateListEntriesBlock bl = (CreateListEntriesBlock)b;
				bl.create(myContext);
			}
			else if (b instanceof CreateEntryFieldBlock) {
				o.addRow("");
				o.addYellowText("CreateEntryFieldBlock found");
				CreateEntryFieldBlock bl = (CreateEntryFieldBlock)b;
				Log.d("NILS","CreateEntryFieldBlock found");
				bl.create(myContext);
			}
			else if (b instanceof AddSumOrCountBlock) {
				o.addRow("");
				o.addYellowText("AddSumOrCountBlock found");
				AddSumOrCountBlock bl = (AddSumOrCountBlock)b;
				bl.create(myContext);
			}
			

		}
		
		//Now all blocks are executed.
		//Draw the UI.
		o.addRow("");
		o.addYellowText("Now Drawing components recursively");
		Container root = myContext.getContainer("root");
		if (root!=null)
			myContext.drawRecursively(root);
		else {
			o.addRow("");
			o.addRedText("TEMPLATE ERROR: Cannot find the root container. \nEach template must have a root! Execution aborted.");				
		}

		
		
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

				Toast.makeText((Context)activity, rule.getErrorMessage(), Toast.LENGTH_LONG).show();
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
	/* (non-Javadoc)
	 * @see android.app.Fragment#onViewCreated(android.view.View, android.os.Bundle)
	 */
	/* (non-Javadoc)
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */



	

}