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

import com.teraim.nils.FileLoadedCb;
import com.teraim.nils.FileLoadedCb.ErrorCode;
import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.blocks.AddEntryToFieldListBlock;
import com.teraim.nils.dynamic.blocks.AddRuleBlock;
import com.teraim.nils.dynamic.blocks.AddSumOrCountBlock;
import com.teraim.nils.dynamic.blocks.AddVariableToEntryFieldBlock;
import com.teraim.nils.dynamic.blocks.AddVariableToEveryListEntryBlock;
import com.teraim.nils.dynamic.blocks.AddVariableToListEntry;
import com.teraim.nils.dynamic.blocks.Block;
import com.teraim.nils.dynamic.blocks.BlockCreateListEntriesFromFieldList;
import com.teraim.nils.dynamic.blocks.ButtonBlock;
import com.teraim.nils.dynamic.blocks.ContainerDefineBlock;
import com.teraim.nils.dynamic.blocks.CreateEntryFieldBlock;
import com.teraim.nils.dynamic.blocks.CreateListEntriesBlock;
import com.teraim.nils.dynamic.blocks.CreateSortWidgetBlock;
import com.teraim.nils.dynamic.blocks.DisplayValueBlock;
import com.teraim.nils.dynamic.blocks.LayoutBlock;
import com.teraim.nils.dynamic.blocks.PageDefineBlock;
import com.teraim.nils.dynamic.blocks.StartBlock;
import com.teraim.nils.dynamic.types.Workflow;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.dynamic.workflow_realizations.WF_Not_ClickableField_SumAndCountOfVariables;
import com.teraim.nils.exceptions.EvalException;
import com.teraim.nils.exceptions.SameOldException;
import com.teraim.nils.log.LoggerI;
import com.teraim.nils.non_generics.Constants;

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
	LoggerI o;


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
		if (code == ErrorCode.newConfigVersionLoaded) {
			boolean ok= Tools.witeObjectToFile(ctx, myFlow, Constants.CONFIG_FILES_DIR+Constants.WF_FROZEN_FILE_ID);
			if (!ok)
				code = ErrorCode.ioError;
			else {
				o.addRow("Setting current version of workflow bundle to "+myVersion);
				ph.put(PersistenceHelper.CURRENT_VERSION_OF_WF_BUNDLE,myVersion);
				code = ErrorCode.newConfigVersionLoaded;

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
			return ErrorCode.newConfigVersionLoaded;
		}
		//This should never happen.
		return FileLoadedCb.ErrorCode.parseError;
	}


	private List<Workflow> readBundle(XmlPullParser parser) throws XmlPullParserException, IOException, SameOldException {

		List<Workflow> bundle = new ArrayList<Workflow>();
		parser.require(XmlPullParser.START_TAG, null, "bundle");
		String version = parser.getAttributeValue(null, "version");
		o.addRow("File workflow bundle version: ");o.addYellowText(version);
		if (ph.getB(PersistenceHelper.VERSION_CONTROL_SWITCH_OFF)) {
			o.addRow("Version control is switched off.");
		} else 
			if(version.equals(ph.get(PersistenceHelper.CURRENT_VERSION_OF_WF_BUNDLE))) 			
				throw new SameOldException();		
		o.addRow("Saved workflow bundle version: ");o.addYellowText(ph.get(PersistenceHelper.CURRENT_VERSION_OF_WF_BUNDLE));
		myVersion = version;		
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
			//try {
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
			else if (name.equals("block_add_rule")) 
				blocks.add(readBlockAddRule(parser));
			else if (name.equals("block_add_sum_of_selected_variables_display")) 
				blocks.add(readBlockAddSelectionOrSum(parser,isSum));
			else if (name.equals("block_add_number_of_selections_display")) 
				blocks.add(readBlockAddSelectionOrSum(parser,isCount));
			else if (name.equals("block_create_sort_widget")) 
				blocks.add(readBlockCreateSorting(parser));
			//This is a dummy call. Not supported block.
			else if (name.equals("block_create_list_filter")) 
				dummyWarning("block_create_list_filter",parser);					
			else if (name.equals("block_create_list_entries")) 
				dummyWarning("block_create_list_entries",parser);
			else if (name.equals("block_create_entry_field")) 
				blocks.add(readBlockCreateEntryField(parser));
			else if (name.equals("block_create_display_field"))
				blocks.add(readBlockCreateDisplayField(parser));
			else if (name.equals("block_create_list_entries_from_field_list"))
				blocks.add(readBlockCreateListEntriesFromFieldList(parser));
			else if (name.equals("block_add_variable_to_every_list_entry"))
				blocks.add(readBlockAddVariableToEveryListEntry(parser));
			else if (name.equals("block_add_variable_to_entry_field"))
				blocks.add(readBlockAddVariableToEntryField(parser));	
			else if (name.equals("block_add_entry_to_field_list")) 
				blocks.add(readBlockAddEntryToFieldList(parser));
			else if (name.equals("block_add_variable_to_list_entry")) 
				blocks.add(readBlockAddVariableToListEntry(parser));


			else				
				skip(parser);

			/*catch (EvalException e) {
				Log.e("NILS","XML Error: "+e.getMessage());
				errorString += "\n"+(++errCount)+". "+e.getMessage();
			}*/
		}

		return blocks;
	}



	private Block readBlockAddVariableToListEntry(XmlPullParser parser) throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_add_variable_to_list_entry...");
		boolean isVisible = true;
		String targetList= null,targetField= null,namn=null,format= null; 
		parser.require(XmlPullParser.START_TAG, null,"block_add_variable_to_list_entry");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name= parser.getName();

			if (name.equals("name")) {
				namn = readText("name",parser);
				o.addRow("NAME: "+namn);			
			} else if (name.equals("target_list")) {
				targetList = readText("target_list",parser);
				o.addRow("TARGETLIST: "+targetList);
			}
			else if (name.equals("target_field")) {
				targetField = readText("target_field",parser);
				o.addRow("TARGETFIELD: "+targetField);
			} 
			else if (name.equals("is_displayed")) {
				isVisible = !readText("is_displayed",parser).equals("false");
				o.addRow("IS_VISIBLE: "+isVisible);	
			} 
			else if (name.equals("format")) {
				format = readText("format",parser);
				o.addRow("FORMAT: "+format);	
			}
			else
				skip(parser);
		}
		return new AddVariableToListEntry(namn,
				targetList,targetField, isVisible,format);	

	}




	private Block readBlockAddEntryToFieldList(XmlPullParser parser) throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_add_entry_to_field_list...");

		String target= null,namn= null,label=null,description=null;
		parser.require(XmlPullParser.START_TAG, null,"block_add_entry_to_field_list");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name= parser.getName();

			if (name.equals("name")) {
				namn = readText("name",parser);
				o.addRow("NAME: "+namn);	

			} else if (name.equals("target")) {
				target = readText("target",parser);
				o.addRow("TARGET: "+target);
			} 

			else if (name.equals("label")) {
				label = readText("label",parser);
				o.addRow("LABEL: "+label);	
			}
			else if (name.equals("description")) {
				description = readText("description",parser);
				o.addRow("DESCRIPTION: "+label);	
			}
			else if (name.equals("target")) {
				target = readText("target",parser);
				o.addRow("TARGET: "+target);	
			}
			else
				skip(parser);
		}
		return new AddEntryToFieldListBlock(namn,target,label,description);
	}

	private Block readBlockAddVariableToEntryField(XmlPullParser parser) throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_add_variable_to_entry_field...");
		boolean isVisible = true;
		String target= null,namn= null,format= null; 
		parser.require(XmlPullParser.START_TAG, null,"block_add_variable_to_entry_field");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name= parser.getName();

			if (name.equals("name")) {
				namn = readText("name",parser);
				o.addRow("NAME: "+namn);			
			} else if (name.equals("target")) {
				target = readText("target",parser);
				o.addRow("TARGET: "+target);
			} 
			else if (name.equals("is_displayed")) {
				isVisible = !readText("is_displayed",parser).equals("false");
				o.addRow("IS_VISIBLE: "+isVisible);	
			} 
			else if (name.equals("format")) {
				format = readText("format",parser);
				o.addRow("FORMAT: "+format);	
			}
			else
				skip(parser);
		}
		return new AddVariableToEntryFieldBlock(target,namn,isVisible,format);
	}
	private Block readBlockCreateListEntriesFromFieldList(XmlPullParser parser) throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_create_list_entries_from_field_list...");
		boolean isVisible = true;
		String namn=null, keyField = null, type=null,containerId=null,selectionField=null,selectionPattern=null;
		String labelField=null,descriptionField=null,typeField=null,uriField=null;
		parser.require(XmlPullParser.START_TAG, null,"block_create_list_entries_from_field_list");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name= parser.getName();
			if (name.equals("type")) {
				type = readText("type",parser);
				o.addRow("TYPE: "+type);
			} else if (name.equals("selection_field")) {
				selectionField = readText("selection_field",parser);
				o.addRow("SELECTION_FIELD: "+selectionField);
			} else if (name.equals("selection_pattern")) {
				selectionPattern = readText("selection_pattern",parser);
				o.addRow("SELECTION_PATTERN: "+selectionPattern);	
			} else if (name.equals("key_field")) {
				keyField = readText("key_field",parser);
				o.addRow("KEYFIELD: "+keyField);							
			} else if (name.equals("name")) {
				namn = readText("name",parser);
				o.addRow("NAME: "+namn);			
			} else if (name.equals("container_name")) {
				containerId = readText("container_name",parser);
				o.addRow("CONTAINER_NAME: "+containerId);
			}  else if (name.equals("label_field")) {
				labelField = readText("label_field",parser);
				o.addRow("LABELFIELD: "+labelField);	
			} else if (name.equals("description_field")) {
				descriptionField = readText("description_field",parser);
				o.addRow("DESCRIPTIONFIELD: "+descriptionField);	
			} else if (name.equals("type_field")) {
				typeField = readText("type_field",parser);
				o.addRow("TYPEFIELD: "+typeField);	
			} else if (name.equals("uri_field")) {
				uriField = readText("uri_field",parser);
				o.addRow("URI_FIELD: "+uriField);	
			} else
				skip(parser);

		}
		return new BlockCreateListEntriesFromFieldList(namn, type,
				containerId,selectionPattern,selectionField,keyField);
	}



	private Block readBlockAddVariableToEveryListEntry(XmlPullParser parser) throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_add_variable_to_every_list_entry...");
		String target=null,variableSuffix=null,format=null;
		boolean displayOut=false;

		parser.require(XmlPullParser.START_TAG, null,"block_add_variable_to_every_list_entry");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name= parser.getName();

			if (name.equals("target")) {
				target = readText("target",parser);
				o.addRow("LABEL: "+target);
			} else if (name.equals("format")) {
				format = readText("format",parser);
				o.addRow("FORMAT: "+format);							
			}else if (name.equals("name")) {
				variableSuffix = readText("name",parser);
				o.addRow("NAME: "+variableSuffix);							
			} else if (name.equals("is_displayed")) {
				displayOut = readText("is_displayed",parser).trim().equals("true");
				o.addRow("IS_DISPLAYED: "+displayOut);			
			} 
			else
				skip(parser);

		}
		return new 	AddVariableToEveryListEntryBlock(target,
				variableSuffix, displayOut,format);
	}

	private DisplayValueBlock readBlockCreateDisplayField(XmlPullParser parser)throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_create_display_field...");
		boolean isVisible = true;
		String namn=null, formula = null, label=null,containerId=null,format = null;
		Unit unit=null;	
		parser.require(XmlPullParser.START_TAG, null,"block_create_display_field");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name= parser.getName();

			if (name.equals("label")) {
				label = readText("label",parser);
				o.addRow("LABEL: "+label);
			} else if (name.equals("expression")) {
				formula = readText("expression",parser);
				o.addRow("EXPRESSION (formula): "+formula);							
			} else if (name.equals("name")) {
				namn = readText("name",parser);
				o.addRow("NAME: "+namn);			
			} else if (name.equals("container_name")) {
				containerId = readText("container_name",parser);
				o.addRow("CONTAINER_NAME: "+containerId);
			}  else if (name.equals("unit")) {
				unit = Tools.convertToUnit(readText("unit",parser));
				o.addRow("UNIT: "+unit);	
			} else if (name.equals("is_visible")) {
				isVisible = !readText("is_visible",parser).equals("false");
				o.addRow("IS_VISIBLE: "+isVisible);	
			} else if (name.equals("format")) {
				format = readText("format",parser);
				o.addRow("FORMAT: "+format);	
			}
			else
				skip(parser);

		}
		return new DisplayValueBlock(namn, label,unit,
				formula,containerId,isVisible,format);
	}


	private CreateEntryFieldBlock readBlockCreateEntryField(XmlPullParser parser)throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_create_entry_field...");
		boolean isVisible = true;
		String namn=null,containerId=null,postLabel="",format=null;
		Unit unit = Unit.nd;		
		parser.require(XmlPullParser.START_TAG, null,"block_create_entry_field");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name= parser.getName();

			if (name.equals("name")) {
				namn = readText("name",parser);
				o.addRow("NAME: "+namn);			
			} else if (name.equals("container_name")) {
				containerId = readText("container_name",parser);
				o.addRow("CONTAINER_NAME: "+containerId);
			} 
			else if (name.equals("is_visible")) {
				isVisible = !readText("is_visible",parser).equals("false");
				o.addRow("IS_VISIBLE: "+isVisible);	
			} 
			else if (name.equals("format")) {
				format = readText("format",parser);
				o.addRow("FORMAT: "+format);	
			}
			else
				skip(parser);
		}
		return new CreateEntryFieldBlock(namn, containerId,isVisible,format);
	}

	/**
	 * Creates a Block for adding a sorting function on Target List. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */

	private void dummyWarning(String block,XmlPullParser parser) throws IOException, XmlPullParserException {
		o.addRow("Parsing block: "+block);
		o.addRow("");
		o.addRedText("This type of block is not supported");
	}


	/**
	 * Creates a Block for adding a sorting function on Target List. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private CreateSortWidgetBlock readBlockCreateSorting(XmlPullParser parser) throws IOException, XmlPullParserException {
		o.addRow("Parsing block: block_create_sort_widget...");
		String namn=null,containerName=null,type=null,target=null,selectionField=null,displayField=null,selectionPattern=null;
		boolean isVisible = true;
		parser.require(XmlPullParser.START_TAG, null,"block_create_sort_widget");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name= parser.getName();

			if (name.equals("container_name")) {
				containerName = readText("container_name",parser);
				o.addRow("CONTAINER_NAME: "+containerName);				
			} else if (name.equals("name")) {
				namn = readText("name",parser);
				o.addRow("NAME: "+namn);
			} else if (name.equals("type")) {
				type = readText("type",parser);
				o.addRow("TYPE: "+type);
			} else if (name.equals("target")) {
				target = readText("target",parser);
				o.addRow("TARGET: "+target);
			} else if (name.equals("is_visible")) {
				isVisible = !readText("is_visible",parser).equals("false");
				o.addRow("IS_VISIBLE: "+isVisible);	
			}  else if (name.equals("selection_field")) {
				selectionField = readText("selection_field",parser);
				o.addRow("SELECTION_FIELD: "+selectionField);
			}  else if (name.equals("display_field")) {
				displayField = readText("display_field",parser);
				o.addRow("DISPLAY_FIELD: "+displayField);
			}  else if (name.equals("selection_pattern")) {
				selectionPattern = readText("selection_pattern",parser);
				o.addRow("SELECTION_PATTERN: "+selectionPattern);	
			}

			else 
				skip(parser);

		}
		return new CreateSortWidgetBlock(namn,type, containerName,target,selectionField,displayField,selectionPattern,isVisible);
	}

	/**
	 * Creates a Block for displaying the number of selected entries currently in a list. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private AddSumOrCountBlock readBlockAddSelectionOrSum(XmlPullParser parser,boolean isCount) throws IOException, XmlPullParserException {
		String containerName=null,label=null,postLabel = null,filter=null,target=null,result=null,format=null;
		WF_Not_ClickableField_SumAndCountOfVariables.Type type;

		boolean isVisible = true;

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
				o.addRow("TARGET list: "+target);
			}
			else if (name.equals("unit")) {
				postLabel = readText("unit",parser);
				o.addRow("UNIT (postLabel): "+postLabel);
			}
			else if (name.equals("result")) {
				result = readText("result",parser);
				o.addRow("RESULT Variable: "+result);
			}
			else if (name.equals("is_visible")) {
				isVisible = !readText("is_visible",parser).equals("false");
				o.addRow("IS_VISIBLE: "+isVisible);	
			} 
			else if (name.equals("format")) {
				format = readText("format",parser);
				o.addRow("FORMAT: "+format);	
			}

			else
				skip(parser);

		}
		return new AddSumOrCountBlock(containerName,label,postLabel,filter,target,type,result,isVisible,format);
	}	


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

		o.addRow("");
		o.addRedText("block_create_list_entries is no longer supported. Use block_create_list_entries_from_field_list instead");
		return null;
	}
	/*
		boolean isVisible = true;
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
			} else if (name.equals("is_visible")) {
				isVisible = !readText("is_visible",parser).equals("false");
				o.addRow("IS_VISIBLE: "+isVisible);	
			} 			
			else
				skip(parser);


		}

		return new CreateListEntriesBlock(type,fileName,containerName,namn,selectionField,selectionPattern,filter,isVisible);
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
		boolean isVisible = true;
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
			else if (name.equals("is_visible")) {
				isVisible = !readText("is_visible",parser).equals("false");
				o.addRow("IS_VISIBLE: "+isVisible);	
			} 
			else
				skip(parser);
		}

		return new ButtonBlock(label,onClick,myname,containerName,target,type,isVisible);
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
		String workflowName=null; String args[]=null,context=null;
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
			else if (name.equals("context")) {
				context = readText("context",parser);
				o.addRow("WF Context: "+context);			
			}
			else
				skip(parser);
		}
		if (workflowName == null)  {
			o.addRow("");
			o.addRedText("Error reading startblock. Workflowname missing");
			throw new XmlPullParserException("Parameter missing");
		}
		return new StartBlock(args,workflowName,context);
	}

	/*


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
