package com.teraim.nils.dynamic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.teraim.nils.GlobalState;
import com.teraim.nils.R;
import com.teraim.nils.bluetooth.BluetoothConnectionService;
import com.teraim.nils.dynamic.blocks.AddEntryToFieldListBlock;
import com.teraim.nils.dynamic.blocks.AddSumOrCountBlock;
import com.teraim.nils.dynamic.blocks.AddVariableToEntryFieldBlock;
import com.teraim.nils.dynamic.blocks.AddVariableToEveryListEntryBlock;
import com.teraim.nils.dynamic.blocks.AddVariableToListEntry;
import com.teraim.nils.dynamic.blocks.Block;
import com.teraim.nils.dynamic.blocks.BlockCreateListEntriesFromFieldList;
import com.teraim.nils.dynamic.blocks.ButtonBlock;
import com.teraim.nils.dynamic.blocks.ConditionalContinuationBlock;
import com.teraim.nils.dynamic.blocks.ContainerDefineBlock;
import com.teraim.nils.dynamic.blocks.CreateEntryFieldBlock;
import com.teraim.nils.dynamic.blocks.CreateSortWidgetBlock;
import com.teraim.nils.dynamic.blocks.DisplayValueBlock;
import com.teraim.nils.dynamic.blocks.StartBlock;
import com.teraim.nils.dynamic.types.Numerable;
import com.teraim.nils.dynamic.types.Rule;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Variable.DataType;
import com.teraim.nils.dynamic.types.Workflow;
import com.teraim.nils.dynamic.workflow_abstracts.Container;
import com.teraim.nils.dynamic.workflow_abstracts.Event;
import com.teraim.nils.dynamic.workflow_abstracts.Event.EventType;
import com.teraim.nils.dynamic.workflow_abstracts.EventListener;
import com.teraim.nils.dynamic.workflow_realizations.WF_Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.dynamic.workflow_realizations.WF_Event_OnSave;
import com.teraim.nils.dynamic.workflow_realizations.WF_Static_List;
import com.teraim.nils.exceptions.RuleException;
import com.teraim.nils.expr.SyntaxException;
import com.teraim.nils.log.LoggerI;
import com.teraim.nils.non_generics.Constants;
import com.teraim.nils.utils.Tools;

/*
 * Executes workflow blocks. Child classes define layouts and other specialized behavior
 */
public abstract class Executor extends Fragment {


	public static final String STOP_ID = "STOP";

	protected Workflow wf;

	//Extended context.
	protected WF_Context myContext;

	//Normal context
	protected Activity activity;
	//Keep track of input in below arraylist.

	protected final Map<Rule,Boolean>executedRules = new LinkedHashMap<Rule,Boolean>();	

	protected List<Rule> rules = new ArrayList<Rule>();


	protected abstract List<WF_Container> getContainers();
	public abstract void execute(String function, String target);

	protected GlobalState gs;

	protected LoggerI o;
	private IntentFilter ifi;
	private BroadcastReceiver brr;
	private Map<String,String> jump= new HashMap<String,String>();
	private Set<Variable> visiVars;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this.getActivity();
		myContext = new WF_Context((Context)activity,this,R.id.content_frame);
		gs = GlobalState.getInstance((Context)activity);
		gs.setCurrentContext(myContext);
		o = gs.getLogger();
		wf = getFlow();


		ifi = new IntentFilter();
		ifi.addAction(BluetoothConnectionService.SYNK_DATA_RECEIVED);
		brr = new BroadcastReceiver() {
			@Override
			public void onReceive(Context ctx, Intent intent) {
				Log.d("nils","GETS HERE::::::");
				gs.getArtLista().invalidateCache();
				myContext.registerEvent(new WF_Event_OnSave(Constants.SYNC_ID));
			}
		};

	}










	/* (non-Javadoc)
	 * @see android.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		activity.registerReceiver(brr, ifi);
		super.onResume();
	}

	@Override
	public void onPause()
	{
		Log.d("NILS", "In the onPause() event");

		//Stop listening for bluetooth events.
		activity.unregisterReceiver(brr);
		super.onPause();

	}

	protected Workflow getFlow() {
		Workflow wf=null;

		//Find out the name of the workflow to execute.
		Bundle b = this.getArguments();
		String name = b.getString("workflow_name");
		if (name!=null && name.length()>0) 
			wf = gs.getWorkflow(name);

		if (wf==null&&name!=null&&name.length()>0) {
			o.addRow("");
			o.addYellowText("Workflow "+name+" NOT found!");
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

		visiVars = new HashSet<Variable>();
		//LinearLayout my_root = (LinearLayout) findViewById(R.id.myRoot);		
		List<Block>blocks = wf.getBlocks();
		boolean notDone = true;
		int blockP = 0;
		Set<Variable>blockVars;
		while(notDone) {
			Block b = blocks.get(blockP);

			if (b instanceof StartBlock) {
				o.addRow("");
				o.addYellowText("Startblock found");
				StartBlock bl = (StartBlock)b;
				String context = bl.getWorkFlowContext();
				if (context==null||context.isEmpty()) {
					Log.d("nils","No context!!");
					o.addRow("No context...will use existing");
				} else {
					Log.d("nils","Found context!!");
					String[] pairs = context.split(",");
					if (pairs==null||pairs.length==0) {
						o.addRow("Could not split context on comma (,). Syntax error?");
					} else {
						boolean err = false;
						Map<String, String> keyHash = new HashMap<String, String>();
						for (String pair:pairs) {
							Log.d("nils","found pair: "+pair);
							if (pair!=null&&!pair.isEmpty()) {
								String[] kv = pair.split("=");
								if (kv==null||kv.length<2) {
									o.addRow("");
									o.addRedText("Could not split context on comma (,). Syntax error?");
									err=true;
								} else {
									//Calculate value of context variables, if any.
									//is it a variable or a value?
									String arg = kv[0].trim();
									String val = kv[1].trim();
									Log.d("nils","Keypair: "+arg+","+val);

									if (val.isEmpty()||arg.isEmpty()) {
										o.addRow("");
										o.addRedText("Empty variable or argument in definition");
										err=true;
									} else {

										if (Character.isDigit(val.charAt(0))) {
											//constant
											keyHash.put(arg, val);
											Log.d("nils","Added "+arg+","+val+" to current context");
										} else {
											//Variable. need to evaluate first..
											Variable v = gs.getArtLista().getVariableInstance(val);
											if (v==null) {
												err=true;
												o.addRow("");
												o.addRedText("One of the variables missing: "+val);
												Log.d("nils","Couldn't find variable "+val);
											} else {
												String varVal = v.getValue();
												if(varVal==null||varVal.isEmpty()) {
													err=true;
													o.addRow("");
													o.addRedText("One of the variables used in current context("+v.getId()+") has no value in database");
													Log.e("nils","var was null or empty: "+v.getId());
												} else {

													keyHash.put(arg, varVal);
													Log.d("nils","Added "+arg+","+varVal+" to current context");

												}

											}

										}
									}
								}
							} else
								Log.d("nils","Found empty or null pair");
						} if (!err && !keyHash.isEmpty()) {
							Log.d("nils","added keyhash to gs");
							//TODO IF we ever implement workflows as subroutines, this need change. Currently only one context.
							gs.setKeyHash(keyHash);
						}
					}
				}
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
			else if (b instanceof CreateSortWidgetBlock) {
				o.addRow("");
				o.addYellowText("CreateSortWidgetBlock found");
				CreateSortWidgetBlock bl = (CreateSortWidgetBlock) b;
				bl.create(myContext);
			}/*
			else if (b instanceof ListFilterBlock) {
				o.addRow("");
				o.addYellowText("ListFilterBlock found");
				ListFilterBlock bl = (ListFilterBlock)b;
				bl.create(myContext);
			}*/

			else if (b instanceof CreateEntryFieldBlock) {
				o.addRow("");
				o.addYellowText("CreateEntryFieldBlock found");
				CreateEntryFieldBlock bl = (CreateEntryFieldBlock)b;
				Log.d("NILS","CreateEntryFieldBlock found");
				Variable v=bl.create(myContext);
				if (v!=null)
					visiVars.add(v);
			}
			else if (b instanceof AddSumOrCountBlock) {
				o.addRow("");
				o.addYellowText("AddSumOrCountBlock found");
				AddSumOrCountBlock bl = (AddSumOrCountBlock)b;
				bl.create(myContext);
			}
			else if (b instanceof DisplayValueBlock) {
				o.addRow("");
				o.addYellowText("DisplayValueBlock found");
				DisplayValueBlock bl = (DisplayValueBlock)b;
				bl.create(myContext);
			}
			else if (b instanceof AddVariableToEveryListEntryBlock) {
				o.addRow("");
				o.addYellowText("AddVariableToEveryListEntryBlock found");
				AddVariableToEveryListEntryBlock bl = (AddVariableToEveryListEntryBlock)b;
				blockVars = bl.create(myContext);
				if (blockVars!=null)
					visiVars.addAll(blockVars);

			}
			else if (b instanceof BlockCreateListEntriesFromFieldList) {
				o.addRow("");
				o.addYellowText("BlockCreateListEntriesFromFieldList found");
				BlockCreateListEntriesFromFieldList bl = (BlockCreateListEntriesFromFieldList)b;
				bl.create(myContext);
			}
			else if (b instanceof AddVariableToEntryFieldBlock) {
				o.addRow("");
				o.addYellowText("AddVariableToEntryFieldBlock found");
				AddVariableToEntryFieldBlock bl = (AddVariableToEntryFieldBlock)b;
				Variable v = bl.create(myContext);
				if (v!=null)
					visiVars.add(v);

			}
			else if (b instanceof AddVariableToListEntry) {
				o.addRow("");
				o.addYellowText("AddVariableToEntryFieldBlock found");
				AddVariableToListEntry bl = (AddVariableToListEntry)b;
				Variable v = bl.create(myContext);
				
			}
			else if (b instanceof AddEntryToFieldListBlock) {
				o.addRow("");
				o.addYellowText("AddEntryToFieldListBlock found");
				AddEntryToFieldListBlock bl = (AddEntryToFieldListBlock)b;
				bl.create(myContext);

			}
			else if (b instanceof ConditionalContinuationBlock) {
				final ConditionalContinuationBlock bl = (ConditionalContinuationBlock)b;
				final String formula = bl.getFormula();
				final Set<Entry<String, DataType>> vars = Tools.parseFormula(gs, formula);
				if (vars!=null) {
					final Boolean newValue = false;			
					EventListener tiva = new EventListener() {


						@Override
						public void onEvent(Event e) {
							//If evaluation different than earlier, re-render workflow.
							if(bl.evaluate(gs,formula,vars)) {
								myContext.onResume();
								myContext = new WF_Context((Context)activity,Executor.this,R.id.content_frame);
								myContext.addContainers(getContainers());	
								Set<Variable> previouslyVisibleVars = visiVars;
								run();
								for (Variable v:previouslyVisibleVars) {
									Log.d("nils","Previously visible: "+v.getId());
									boolean found = false;
									for(Variable x:visiVars) {									
										found = x.getId().equals(v.getId());
										if (found)
											break;
									}
									
									if (!found) {
										Log.d("nils","Variable "+v.getId()+" not found.Removing");
										v.deleteValue();
									}
										
								}
							}

						}
					};				
					myContext.addEventListener(tiva, EventType.onSave);	
					//trigger event.
					if (bl.getCurrentEval()==null)
						bl.evaluate(gs,formula,vars);

					switch (bl.getCurrentEval()) {
					case ConditionalContinuationBlock.STOP:
						jump.put(bl.getBlockId(), Executor.STOP_ID);
						break;
					case ConditionalContinuationBlock.JUMP:
						jump.put(bl.getBlockId(), bl.getElseId());						
						break;
					case ConditionalContinuationBlock.NEXT:
						jump.remove(bl.getBlockId());
					}

				}
				else 
					Log.d("nils","Parsing of formula failed - no variables: ["+formula+"]");
			}
			String cId = b.getBlockId();
			String jNext = jump.get(cId);
			if (jNext!=null) {	
				if (jNext.equals(Executor.STOP_ID))
					notDone = false;
				else
					blockP = indexOf(jNext,blocks);
			} else
				blockP++;

			if (blockP>=blocks.size())
				notDone=false;
		}

		//Now all blocks are executed.
		//Draw the UI.
		o.addRow("");
		o.addYellowText("Now Drawing components recursively");
		//Draw all lists first.
		for (WF_Static_List l:myContext.getLists()) 
			l.draw();

		Container root = myContext.getContainer("root");
		if (root!=null)
			myContext.drawRecursively(root);
		else {
			o.addRow("");
			o.addRedText("TEMPLATE ERROR: Cannot find the root container. \nEach template must have a root! Execution aborted.");				
		}


	}









	private int indexOf(String jNext, List<Block> blocks) {

		for(int i=0;i<blocks.size();i++) {
			String id = blocks.get(i).getBlockId();
			Log.d("nils","checking id: "+id);
			if(id.equals(jNext)) {
				Log.d("nils","found block to jump to!");
				return i;
			}
		}

		Log.e("nils","Jump pointer to non-existing block. Faulty ID: "+jNext);
		return blocks.size();
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
			Numerable target;
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