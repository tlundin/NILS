package com.teraim.nils.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import com.teraim.nils.Constants;
import com.teraim.nils.FileLoadedCb;
import com.teraim.nils.FileLoadedCb.ErrorCode;
import com.teraim.nils.GlobalState;
import com.teraim.nils.Logger;
import com.teraim.nils.dynamic.blocks.AddRuleBlock;
import com.teraim.nils.dynamic.blocks.AddSumOrCountBlock;
import com.teraim.nils.dynamic.blocks.Block;
import com.teraim.nils.dynamic.blocks.ButtonBlock;
import com.teraim.nils.dynamic.blocks.ContainerDefineBlock;
import com.teraim.nils.dynamic.blocks.CreateEntryFieldBlock;
import com.teraim.nils.dynamic.blocks.CreateListEntriesBlock;
import com.teraim.nils.dynamic.blocks.DisplayValueBlock;
import com.teraim.nils.dynamic.blocks.LayoutBlock;
import com.teraim.nils.dynamic.blocks.ListSortingBlock;
import com.teraim.nils.dynamic.blocks.PageDefineBlock;
import com.teraim.nils.dynamic.blocks.StartBlock;
import com.teraim.nils.dynamic.types.Workflow;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.dynamic.workflow_realizations.WF_Not_ClickableField_SumAndCountOfVariables;
import com.teraim.nils.exceptions.EvalException;
import com.teraim.nils.exceptions.SameOldException;

/**
 * 
 * @author Terje
 *
 * Parser that reads XML descriptions of workflows.
 * Will try to parse the XML into a list.
 * TODO: Must implement good syntax error messages.
 * TODO: Remove parsing of Label. Not used.
 */
public class WorkflowParser extends AsyncTask<Context,Void,ErrorCode>{

	Context ctx;
	//Location of bundle.
	PersistenceHelper ph;
	FileLoadedCb cb;
	String myVersion = null;
	List<Workflow> myFlow = null;
	Logger o;


	public WorkflowParser(PersistenceHelper ph, FileLoadedCb fileLoadedCb) {
		this.ph=ph;
		this.cb = fileLoadedCb;
	}



	//Take input file from remote web server and parse it.
	//Generates a list of workflows from a Bundle.
	@Override
	protected ErrorCode doInBackground(Context... params) {
		ctx = params[0];
		o = GlobalState.getInstance(ctx).getLogger();
		String serverUrl = ph.get(PersistenceHelper.SERVER_URL);
		if (serverUrl ==null || serverUrl.equals(PersistenceHelper.UNDEFINED) || serverUrl.length()==0)
			return ErrorCode.configurationError;
		//Add / if missing.
		if (!serverUrl.endsWith("/")) {
			serverUrl+="/";
		}
		if (!serverUrl.startsWith("http://")) {
			serverUrl = "http://"+serverUrl;
			o.addRow("server url name missing http header...adding");		
		}
		return parse(serverUrl+ph.get(PersistenceHelper.BUNDLE_LOCATION));
	}

	@Override
	protected void onPostExecute(ErrorCode code) {
		if (code == ErrorCode.newVersionLoaded) {
			boolean ok= Tools.witeObjectToFile(ctx, myFlow, Constants.CONFIG_FILES_DIR+Constants.WF_FROZEN_FILE_ID);
			if (!ok)
				code = ErrorCode.ioError;
			else {
				o.addRow("Setting current version of workflow bundle to "+myVersion);
				ph.put(PersistenceHelper.CURRENT_VERSION_OF_WF_BUNDLE,myVersion);
				code = ErrorCode.newVersionLoaded;

			}
		}
		cb.onFileLoaded(code);	
	}
	public ErrorCode parse(String fileUrl)  {
		myFlow=null;
		Log.d("nils","File url: "+fileUrl);
		try {	
			URL url = new URL(fileUrl);
			o.addRow("Fetching workflow bundle "+fileUrl);

			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();
			InputStream in = ucon.getInputStream();

			//InputStream in = c.getResources().openRawResource(R.raw.mainflow);

			//File flow = new File("res/raw/"+fileName+".xml");
			//in = new FileInputStream(flow);
			//We now have the XML file opened. Time to parse.
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			myFlow = readBundle(parser);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			return FileLoadedCb.ErrorCode.parseError;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return FileLoadedCb.ErrorCode.notFound;			
		} catch (IOException e) {			
			e.printStackTrace();
			return FileLoadedCb.ErrorCode.ioError;

		} catch (SameOldException e) {
			return FileLoadedCb.ErrorCode.sameold;
		} 
		if (myFlow !=null) {
			o.addRow("");
			o.addYellowText("Parsed in total: "+myFlow.size()+" workflows");			
			return ErrorCode.newVersionLoaded;
		}
		//This should never happen.
		return FileLoadedCb.ErrorCode.parseError;
	}


	private List<Workflow> readBundle(XmlPullParser parser) throws XmlPullParserException, IOException, SameOldException {

		List<Workflow> bundle = new ArrayList<Workflow>();
		parser.require(XmlPullParser.START_TAG, null, "bundle");
		String version = parser.getAttributeValue(null, "version");
		if(version.equals(ph.get(PersistenceHelper.CURRENT_VERSION_OF_WF_BUNDLE))) {
			o.addRow("This is the same version...no need to update");
			throw new SameOldException();
		} else {
			o.addRedText("Current workflow bundle version"+ph.get(PersistenceHelper.CURRENT_VERSION_OF_WF_BUNDLE));
			myVersion = version;
		}
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				Log.d("NILS","Skipping "+parser.getName());
				continue;
			}
			String name = parser.getName();
			Log.d("NILS","Doing: "+name);
			// Starts by looking for the entry tag
			if (name.equals("workflow")) {
				o.addRow("");
				o.addYellowText("Adding workflow");
				bundle.add(readWorkflow(parser));
			} else {
				skip(parser);
				o.addRow("Skip "+name);
			}
			o.addRow("Loopstep..");
		}  
		return bundle;
	}

	static String errorString ="";
	static int errCount = 0;
	private Workflow readWorkflow(XmlPullParser parser) throws XmlPullParserException, IOException {
		Workflow wf = new Workflow();
		parser.require(XmlPullParser.START_TAG, null, "workflow");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			o.addRow("TAG: WORKFLOW");
			String name = parser.getName();
			if (name.equals("blocks")) {
				o.addRow("TAG: BLOCKS");
				wf.addBlocks(readBlocks(parser));
			} else {
				skip(parser);
			}
		}
		
		return wf;


	}



	/**
	 * Read blocks. Create respective class and return as a list.
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private static boolean isSum = false;
	private static boolean isCount = true;

	private List<Block> readBlocks(XmlPullParser parser) throws IOException, XmlPullParserException {
		List<Block> blocks=new ArrayList<Block>();
		parser.require(XmlPullParser.START_TAG, null,"blocks");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			try {
				if (name.equals("block_start")) 
					blocks.add(readBlockStart(parser));
				else if (name.equals("block_define_page")) 
					blocks.add(readPageDefineBlock(parser));				
				else if (name.equals("block_define_container")) 
					blocks.add(readContainerDefineBlock(parser));				
				else if (name.equals("block_layout")) 
					blocks.add(readBlockLayout(parser));				
				else if (name.equals("block_button")) 
					blocks.add(readBlockButton(parser));
				//				else if (name.equals("block_set_value")) 
				//					blocks.add(readBlockSetValue(parser));
				else if (name.equals("block_add_rule")) 
					blocks.add(readBlockAddRule(parser));
				else if (name.equals("block_add_sum_of_selected_variables_display")) 
					blocks.add(readBlockAddSelectionOrSum(parser,isSum));
				else if (name.equals("block_add_number_of_selections_display")) 
					blocks.add(readBlockAddSelectionOrSum(parser,isCount));
				else if (name.equals("block_create_list_sorting_function")) 
					blocks.add(readBlockCreateSorting(parser));
				//This is a dummy call. Not supported block.
				else if (name.equals("block_create_list_filter")) 
					readBlockCreateFilter(parser);					
				else if (name.equals("block_create_list_entries")) 
					blocks.add(readBlockCreateListEntries(parser));
				else if (name.equals("block_create_entry_field")) 
					blocks.add(readBlockCreateEntryField(parser));
				else if (name.equals("block_display_value"))
					blocks.add(readBlockDisplayValue(parser));
				else
					skip(parser);
			} catch (EvalException e) {
				Log.e("NILS","XML Error: "+e.getMessage());
				errorString += "\n"+(++errCount)+". "+e.getMessage();
			}
		}

		return blocks;
	}


	private DisplayValueBlock readBlockDisplayValue(XmlPullParser parser)throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_display_value...");
		String namn=null, type=null, label=null,variable=null,containerId=null;	
		parser.require(XmlPullParser.START_TAG, null,"block_display_value");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name= parser.getName();

			if (name.equals("label")) {
				label = readText("label",parser);
				o.addRow("LABEL: "+label);
			} else if (name.equals("type")) {
				type = readText("type",parser);
				o.addRow("TYPE: "+type);							
			} else if (name.equals("name")) {
				namn = readText("name",parser);
				o.addRow("NAME: "+namn);			
			} else if (name.equals("container_name")) {
				containerId = readText("container_name",parser);
				o.addRow("CONTAINER_NAME: "+containerId);
			} else if (name.equals("variable")) {
				variable = readText("variable",parser); 
				o.addRow("VARIABLE: "+variable);
			} 
			else
				skip(parser);

		}
		return new DisplayValueBlock(namn, type, label,
				variable,containerId);
	}



	private CreateEntryFieldBlock readBlockCreateEntryField(XmlPullParser parser)throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_create_entry_field...");
		String namn=null, type=null, label=null,purpose=null,containerId=null;
		Unit unit = Unit.nd;		
		parser.require(XmlPullParser.START_TAG, null,"block_create_entry_field");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name= parser.getName();

			if (name.equals("label")) {
				label = readText("label",parser);
				o.addRow("LABEL: "+label);
			} else if (name.equals("type")) {
				type = readText("type",parser);
				o.addRow("TYPE: "+type);
				if (!type.equals("numeric")) {
					o.addRow("");
					o.addRedText("EntryField ONLY supports NUMERIC!");
				}				
			} else if (name.equals("name")) {
				namn = readText("name",parser);
				o.addRow("NAME: "+namn);			
			} else if (name.equals("container_name")) {
				containerId = readText("container_name",parser);
				o.addRow("CONTAINER_NAME: "+containerId);
			} else if (name.equals("purpose")) {
				purpose = readText("purpose",parser); 
				o.addRow("PURPOSE: "+purpose);
			} else if (name.equals("unit")) {
				unit = Tools.convertToUnit(readText("unit",parser));
				o.addRow("UNIT: "+unit.name());
			}
			else
				skip(parser);

		}
		return new CreateEntryFieldBlock(namn, type, label,
				purpose, unit,containerId);
	}

	/**
	 * Creates a Block for adding a sorting function on Target List. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	
	private void readBlockCreateFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_create_list_filter...");
		o.addRow("");
		o.addRedText("This type of block is not supported");
	}
/*		String containerId=null,type=null,target=null,label=null,function=null;

		parser.require(XmlPullParser.START_TAG, null,"block_create_list_filter");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name= parser.getName();

			if (name.equals("label")) {
				label = readText("label",parser);
				o.addRow("LABEL: "+label);
			} else if (name.equals("type")) {
				type = readText("type",parser);
				o.addRow("TYPE: "+type);
			} else if (name.equals("function_name")) {
				function = readText("function_name",parser);
			} else if (name.equals("container_name")) {
				containerId = readText("container_name",parser);
				o.addRow("CONTAINER_NAME: "+containerId);
			} else if (name.equals("target")) {
				target = readText("target",parser);
				o.addRow("TARGET: "+target);
			}
			else
				skip(parser);

		}
		return new ListFilterBlock(containerId, type, target,label, function);
	}
	*/

	/**
	 * Creates a Block for adding a sorting function on Target List. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private ListSortingBlock readBlockCreateSorting(XmlPullParser parser) throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_create_list_sorting_function...");
		String containerName=null,type=null,target=null;
		parser.require(XmlPullParser.START_TAG, null,"block_create_list_sorting_function");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name= parser.getName();

			if (name.equals("container_name")) {
				containerName = readText("container_name",parser);
				o.addRow("CONTAINER_NAME: "+containerName);
			} else if (name.equals("type")) {
				type = readText("type",parser);
				o.addRow("TYPE: "+type);
			} else if (name.equals("target")) {
				target = readText("target",parser);
				o.addRow("TARGET: "+target);
			}else 
				skip(parser);

		}
		return new ListSortingBlock(type, containerName,target);
	}

	/**
	 * Creates a Block for displaying the number of selected entries currently in a list. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private AddSumOrCountBlock readBlockAddSelectionOrSum(XmlPullParser parser,boolean isCount) throws IOException, XmlPullParserException {
		String containerName=null,label=null,filter=null,target=null;
		WF_Not_ClickableField_SumAndCountOfVariables.Type type;

		if (isCount)
			type = WF_Not_ClickableField_SumAndCountOfVariables.Type.count;
		else
			type = WF_Not_ClickableField_SumAndCountOfVariables.Type.sum;

		if (isCount) {
			o.addRow("Parsing block: block_add_number_of_selections_display...");
			parser.require(XmlPullParser.START_TAG, null,"block_add_number_of_selections_display");
		}
		else {
			o.addRow("Parsing block: block_add_sum_of_selected_variables_display...");
			parser.require(XmlPullParser.START_TAG, null,"block_add_sum_of_selected_variables_display");
		}
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}	
			String name= parser.getName();

			if (name.equals("container_name")) {
				containerName = readText("container_name",parser);
				o.addRow("CONTAINER_NAME: "+containerName);
			} 
			else if (name.equals("label")) {
				label = readText("label",parser);
				o.addRow("LABEL: "+label);
			}
			else if (name.equals("filter")) {
				filter = readText("filter",parser);
				o.addRow("FILTER: "+filter);
			}
			else if (name.equals("target")) {
				target = readText("target",parser);
				o.addRow("TARGET: "+target);
			}	
			else
				skip(parser);

		}
		return new AddSumOrCountBlock(containerName,label,filter,target,type);
	}	

	/*


	private CreateListEntryBlock readBlockCreateListEntry(XmlPullParser parser) throws IOException, XmlPullParserException, EvalException {
		Log.d("NILS","Create List Entry block...");
		ArrayList<XML_Variable> vars = new ArrayList<XML_Variable>();
		String listName="";
		XML_Variable Xvar=null;
		parser.require(XmlPullParser.START_TAG, null,"block_create_list_entry");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}	
			String name= parser.getName();
			//If a unique varname tag found, instantiate a new XML_variable. 
			if (name.equals("name")) {
				listName = readText("name",parser);
				Log.d("NILS","listName: "+listName);
			}
			else {
				if (name.equals("varname")) {
					if (Xvar !=null) {
						Log.d("NILS","Added variable "+Xvar.name);
						vars.add(Xvar);
					}
					Xvar = new XML_Variable();
					Xvar.name = readText("varname",parser);
					Xvar.type = Variable.Type.NUMERIC;
					Xvar.purpose = "editable";
					Xvar.label = Xvar.name;
				}
				else if (Xvar!=null) {
					if (name.equals("vartype")) 
						Xvar.type = Tools.convertToType(readText("vartype",parser));

					else if (name.equals("purpose")) 
						Xvar.purpose = readText("purpose",parser);
					else if (name.equals("field_label")) 
						Xvar.label = readText("field_label",parser);
					else
						skip(parser);
				} else
					skip(parser);


			}
		} if (Xvar !=null) {
			Log.d("NILS","Added variable "+Xvar.name);
			vars.add(Xvar);
		}
		return new CreateListEntryBlock(ctx,vars,listName);
	}
	 */


	/**
	 *  Creates a CreateListEntriesBlock. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws EvalException 
	 */	
	private CreateListEntriesBlock readBlockCreateListEntries(XmlPullParser parser) throws IOException, XmlPullParserException, EvalException {
		o.addRow("Parsing block: block_create_list_entries...");
		String type=null,fileName="",containerName=null,namn=null,selectionField=null,selectionPattern=null,filter=null;
		parser.require(XmlPullParser.START_TAG, null,"block_create_list_entries");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}	
			String name= parser.getName();
			//If a unique varname tag found, instantiate a new XML_variable. 
			if (name.equals("file_name")) {
				fileName = readText("file_name",parser);
				o.addRow("FILE_NAME: "+fileName);
			} else if (name.equals("container_name")) {
				containerName = readText("container_name",parser);
				o.addRow("CONTAINER_NAME: "+containerName);
			} else if (name.equals("name")) {
				namn = readText("name",parser);
				o.addRow("NAME: "+namn);
			} else if (name.equals("type")) {
				type = readText("type",parser);
				o.addRow("TYPE: "+type);
			}  else if (name.equals("selection_pattern")) {
				selectionPattern = readText("selection_pattern",parser);
				o.addRow("SELECTION_PATTERN: "+selectionPattern);
			} else if (name.equals("selection_field")) {
				selectionField = readText("selection_field",parser);
				o.addRow("SELECTION_FIELD: "+selectionField);
			} else if (name.equals("filter")) {
				filter = readText("filter",parser);
				o.addRow("FILTER: "+filter);
			} else
				skip(parser);


		}

		return new CreateListEntriesBlock(type,fileName,containerName,namn,selectionField,selectionPattern,filter);
	}


	/*	private CreateFieldBlock readBlockCreateField(XmlPullParser parser) throws IOException, XmlPullParserException, EvalException {
		 Log.d("NILS","Create Field block...");
		 XML_Variable var = new XML_Variable();
		 parser.require(XmlPullParser.START_TAG, null,"block_create_field");
		 while (parser.next() != XmlPullParser.END_TAG) {
			 if (parser.getEventType() != XmlPullParser.START_TAG) {
				 continue;
			 }	
			 String name= parser.getName();
			 if (name.equals("name")) 
				 var.name = readText("name",parser);
			 else if (name.equals("type")) 
				 var.type = Tools.convertToType(readText("type",parser));
			 //TODO: PAGENAME.			
			 else if (name.equals("purpose")) 
				 var.purpose = readText("purpose",parser);
			 else if (name.equals("label")) 
				 var.label = readText("label",parser);
			 else if (name.equals("unit")) {
				 var.unit = Tools.convertToUnit(readText("unit",parser));
				 if (var.unit==null) {
					 var.unit=Unit.undefined;
					 Log.e("nils","Failed to understand Unit: "+readText("unit",parser)+" revert to no unit.");
					 Log.e("nils","Allowed units: ");
					 for (int i = 0 ; i<Unit.values().length;i++)
						 Log.e("nils",Unit.values()[i].name());
				 }
			 }

			 else
				 skip(parser);
		 }
		 return new CreateFieldBlock(ctx,var);
	 }
	 */

	/**
	 *  Creates a Buttonblock. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	//For now just create dummy.
	private ButtonBlock readBlockButton(XmlPullParser parser) throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_button...");
		String label=null,onClick=null,myname=null,containerName=null,target=null,type=null;
		parser.require(XmlPullParser.START_TAG, null,"block_button");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}	
			String name = parser.getName();
			if (name.equals("onClick")) {
				onClick = readText("onClick",parser);
				o.addRow("onClick: "+onClick);
			}
			else if (name.equals("type")) {
				type = readText("type",parser);
				o.addRow("TYPE: "+type);
			}
			else if (name.equals("name")) {
				myname = readText("name",parser);
				o.addRow("NAME: "+myname);
			}
			else if (name.equals("label")) {
				label = readText("label",parser);
				o.addRow("LABEL: "+label);
			}
			else if (name.equals("container_name")) {
				containerName = readText("container_name",parser);		
				o.addRow("CONTAINER_NAME: "+containerName);
			}
			else if (name.equals("target")) {
				target = readText("target",parser);
				o.addRow("TARGET: "+target);
			}
			else
				skip(parser);
		}

		return new ButtonBlock(label,onClick,myname,containerName,target,type);
	}

	/**
	 *  Creates a Startblock. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	//Block Start contains the name of the worklfow and the Arguments.
	private StartBlock readBlockStart(XmlPullParser parser) throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_start...");
		String workflowName=null; String args[]=null;
		parser.require(XmlPullParser.START_TAG, null,"block_start");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name= parser.getName();
			if (name.equals("workflowname"))  {
				workflowName = readSymbol("workflowname",parser);
				o.addRow("WORKFLOWNAME: "+workflowName);
			}
			else if (name.equals("inputvar")) {
				args = readArray("inputvar",parser);
				o.addRow("input variables: ");
				for(int i=0;i<args.length;i++)
					o.addYellowText(args[i]+",");
			}
			else
				skip(parser);
		}
		if (workflowName == null)  {
			o.addRow("");
			o.addRedText("Error reading startblock. Workflowname missing");
			throw new XmlPullParserException("Parameter missing");
		}
		return new StartBlock(args,workflowName);
	}

	/*
	 //For now just create dummy.
	 private SetValueBlock readBlockSetValue(XmlPullParser parser) throws IOException, XmlPullParserException {
		 Log.d("NILS","set value block...");
		 String varRef=null,expr=null,label=null;
		 parser.require(XmlPullParser.START_TAG, null,"block_set_value");
		 while (parser.next() != XmlPullParser.END_TAG) {
			 if (parser.getEventType() != XmlPullParser.START_TAG) {
				 continue;
			 }
			 String name= parser.getName();
			 if (name.equals("varname"))  
				 varRef = readSymbol("varname",parser);
			 else if (name.equals("expression"))  
				 expr = readText("expression",parser);
			 else if (name.equals("label")) 
				 label = readText("label",parser);
			 else
				 skip(parser);
		 }
		 if (varRef == null || expr == null)  {
			 Log.e("NILS","Error reading SetValueblock. Either varRef or Expression is null. Varref: "+varRef);
			 throw new XmlPullParserException("Parameter missing");
		 }
		 return new SetValueBlock(ctx,label,varRef,expr);
	 }
	 */

	/**
	 * Creates a LayoutBlock. LayoutBlocks are used to set the direction of the layout 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private LayoutBlock readBlockLayout(XmlPullParser parser) throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_layout...");
		String layout=null,align=null,label=null;
		parser.require(XmlPullParser.START_TAG, null,"block_layout");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}		
			String name= parser.getName();
			if (name.equals("layout")) {
				layout = readText("layout",parser);
				o.addRow("LAYOUT: "+layout);
			} else if (name.equals("align")) {
				align = readText("align",parser);
				o.addRow("ALIGN: "+align);
			} else if (name.equals("label")) {
				label = readText("label",parser);
				o.addRow("LABEL: "+label);

			} else 
				skip(parser);

		}
		return new LayoutBlock(label,layout,align);
	}

	/**
	 * Creates a PageDefinitionBlock. Pages are the templates for a given page. Defines layout etc. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private PageDefineBlock readPageDefineBlock(XmlPullParser parser) throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_define_page...");
		String pageType=null,label="";
		parser.require(XmlPullParser.START_TAG, null,"block_define_page");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}		
			String name= parser.getName();
			if (name.equals("type")) {
				pageType = readText("type",parser);
				o.addRow("TYPE: "+pageType);
			} else if (name.equals("label")) {
				label = readText("label",parser);
				o.addRow("LABEL: "+label);
			} else
				skip(parser);
		}
		return new PageDefineBlock("root", pageType,label);
	}


	/**
	 * Creates a PageDefinitionBlock. Pages are the templates for a given page. Defines layout etc. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private ContainerDefineBlock readContainerDefineBlock(XmlPullParser parser) throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_define_container...");
		String containerType=null,containerName="";
		parser.require(XmlPullParser.START_TAG, null,"block_define_container");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}		
			String name= parser.getName();
			if (name.equals("name")) {
				containerName = readText("name",parser);
				o.addRow("NAME: "+containerName);
			} else if (name.equals("container_type")) {
				containerType = readText("container_type",parser);
				o.addRow("CONTAINER_TYPE: "+containerType);
			} else
				skip(parser);
		}
		return new ContainerDefineBlock(containerName, containerType);
	}
	/**
	 * Creates a AddRuleBlock. Adds a rule to a variable or object. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private AddRuleBlock readBlockAddRule(XmlPullParser parser) throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_add_rule...");
		String label=null, target=null, condition=null, action=null, errorMsg=null,myname=null;
		parser.require(XmlPullParser.START_TAG, null,"block_add_rule");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}		
			String name= parser.getName();
			if (name.equals("target")) {
				target = readText("target",parser);
				o.addRow("TARGET: "+target);
			} else if (name.equals("condition")) {
				condition = readText("condition",parser);
				o.addRow("CONDITION: "+condition);
			} else if (name.equals("action")) {
				action = readText("action",parser);
				o.addRow("ACTION: "+action);
			} else if (name.equals("errorMsg")) {
				errorMsg = readText("errorMsg",parser);
				o.addRow("ERRORMSG: "+errorMsg);
			} else if (name.equals("name")) {
				myname = readText("name",parser);
				o.addRow("NAME: "+myname);
			}else if (name.equals("label")) {
				label = readText("label",parser);
				o.addRow("LABEL: "+label);
			} else 
				skip(parser);

		}
		return new AddRuleBlock(ctx,label,myname,target,condition,action,errorMsg);
	}

	// Read symbol from tag.
	private String readSymbol(String tag,XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null,tag);
		String text = readText(parser);
		parser.require(XmlPullParser.END_TAG, null,tag);
		//Check that it does not start with a number.
		if (text!=null) {
			if (text.length()>0 && Character.isDigit(text.charAt(0))) {
				o.addRow("");
				o.addRedText("XML: EXCEPTION - Symbol started with integer");
				throw new XmlPullParserException("Symbol cannot start with integer");	
			} 
		} else {
			o.addRow("");
			o.addRedText("XML: EXCEPTION - Symbol was NULL");
			throw new XmlPullParserException("Symbol cannot be null");
		}
		return text;
	}
	// Read string from tag.
	private static String readText(String tag,XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null,tag);
		String text = readText(parser);
		parser.require(XmlPullParser.END_TAG, null,tag);
		return text;
	}

	private static String[] readArray(String tag,XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null,tag);
		String temp = readText(parser);
		String[] res = null;
		if (temp!=null) 
			res = temp.split(",");			 

		parser.require(XmlPullParser.END_TAG, null,tag);
		return res;
	}

	// Extract string values.
	private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}
	//Skips entry...return one level up in recursion if end reached.
	private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}







}
