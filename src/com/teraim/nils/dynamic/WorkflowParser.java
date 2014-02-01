package com.teraim.nils.dynamic;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import com.teraim.nils.GlobalState;
import com.teraim.nils.Variable;
import com.teraim.nils.dynamic.blocks.AddDisplayOfSelectionsBlock;
import com.teraim.nils.dynamic.blocks.AddRuleBlock;
import com.teraim.nils.dynamic.blocks.Block;
import com.teraim.nils.dynamic.blocks.ButtonBlock;
import com.teraim.nils.dynamic.blocks.ContainerDefineBlock;
import com.teraim.nils.dynamic.blocks.CreateFieldBlock;
import com.teraim.nils.dynamic.blocks.CreateListEntriesBlock;
import com.teraim.nils.dynamic.blocks.CreateListEntryBlock;
import com.teraim.nils.dynamic.blocks.LayoutBlock;
import com.teraim.nils.dynamic.blocks.ListFilterBlock;
import com.teraim.nils.dynamic.blocks.ListSortingBlock;
import com.teraim.nils.dynamic.blocks.PageDefineBlock;
import com.teraim.nils.dynamic.blocks.SetValueBlock;
import com.teraim.nils.dynamic.blocks.StartBlock;
import com.teraim.nils.dynamic.types.Workflow;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.dynamic.types.XML_Variable;
import com.teraim.nils.exceptions.EvalException;
import com.teraim.nils.utils.Tools;

/**
 * 
 * @author Terje
 *
 * Parser that reads XML descriptions of workflows.
 * Will try to parse the XML into a list.
 * TODO: Must implement good syntax error messages.
 * TODO: Remove parsing of Label. Not used.
 */
public class WorkflowParser extends AsyncTask<Context,Void,List<Workflow>>{

	Context ctx;
	GlobalState gs;
	//Location of bundle.
	//private final static String serverUrl = "http://83.250.104.137:8080/nilsbundle.xml";
	private final static String serverUrl = "http://teraim.com/nilsbundle.xml";
	//Take input file from remote web server and parse it.
	//Generates a list of workflows from a Bundle.
	@Override
	protected List<Workflow> doInBackground(Context... params) {
		ctx = params[0];
		gs = GlobalState.getInstance(ctx);
		return parse(ctx);
	}

	@Override
	protected void onPostExecute(List<Workflow> result) {
		Log.d("NILS","Workflows parsed");

		gs.setWorkflows(result);

		//Intent startMenu = new Intent(context, StartMenuActivity.class);

		//context.startActivity(startMenu);

	}

	public List<Workflow> parse(Context c)  {
		List<Workflow> myFlow = null;
		//FileInputStream in = null;
		try {	
			URL url = new URL(serverUrl);
			Log.d("NILS", "downloading page "+serverUrl);

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
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		if (myFlow !=null)
			Log.d("NILS","Found "+myFlow.size()+" workflows");
		return myFlow;
	}


	private List<Workflow> readBundle(XmlPullParser parser) throws XmlPullParserException, IOException {

		List<Workflow> bundle = new ArrayList<Workflow>();
		parser.require(XmlPullParser.START_TAG, null, "bundle");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				Log.d("NILS","Skipping "+parser.getName());
				continue;
			}
			String name = parser.getName();
			Log.d("NILS","Doing: "+name);
			// Starts by looking for the entry tag
			if (name.equals("workflow")) {
				Log.d("NILS","Adding workflow");
				bundle.add(readWorkflow(parser));
			} else {
				skip(parser);
				Log.d("NILS","Skip");
			}
			Log.d("NILS","Loopstep..");
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
			String name = parser.getName();
			if (name.equals("blocks")) {
				wf.addBlocks(readBlocks(parser));
			} else {
				skip(parser);
			}
		}
		Log.d("NILS","wf read");
		return wf;


	}



	/**
	 * Read blocks. Create respective class and return as a list.
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
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
				else if (name.equals("block_set_value")) 
					blocks.add(readBlockSetValue(parser));
				else if (name.equals("block_create_field")) 
					blocks.add(readBlockCreateField(parser));
				else if (name.equals("block_add_rule")) 
					blocks.add(readBlockAddRule(parser));
				else if (name.equals("block_create_list_entry")) 
					blocks.add(readBlockCreateListEntry(parser));
				else if (name.equals("block_add_number_of_selections_display")) 
					blocks.add(readBlockAddSelections(parser));
				else if (name.equals("block_create_list_sorting_function")) 
					blocks.add(readBlockCreateSorting(parser));
				else if (name.equals("block_create_list_filter")) 
					blocks.add(readBlockCreateFilter(parser));
				else if (name.equals("block_create_list_entries")) 
					blocks.add(readBlockCreateListEntries(parser));
				else
					skip(parser);
			} catch (EvalException e) {
				Log.e("NILS","XML Error: "+e.getMessage());
				errorString += "\n"+(++errCount)+". "+e.getMessage();
			}
		}

		return blocks;
	}



	/**
	 * Creates a Block for adding a sorting function on Target List. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private static ListFilterBlock readBlockCreateFilter(XmlPullParser parser) throws IOException, XmlPullParserException {
		Log.d("NILS","Block create list filter...");
		String containerId=null,type=null,target=null,label=null,function=null;
		
		parser.require(XmlPullParser.START_TAG, null,"block_create_list_filter");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name= parser.getName();
			
			if (name.equals("label")) {
				label = readText("label",parser);
			} else if (name.equals("type")) {
				type = readText("type",parser);
			} else if (name.equals("function_name")) {
				function = readText("function_name",parser);
			} else if (name.equals("container_name")) {
				containerId = readText("container_name",parser);
			} else if (name.equals("target")) {
			target = readText("target",parser);}
			else
				skip(parser);

		}
		return new ListFilterBlock(containerId, type, target,label, function);
	}

	/**
	 * Creates a Block for adding a sorting function on Target List. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private static ListSortingBlock readBlockCreateSorting(XmlPullParser parser) throws IOException, XmlPullParserException {
		Log.d("NILS","Sorting block...");
		String containerName=null,type=null,target=null;
		parser.require(XmlPullParser.START_TAG, null,"block_create_list_sorting_function");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name= parser.getName();
			
			if (name.equals("container_name")) {
				containerName = readText("container_name",parser);

			} else if (name.equals("type")) {
				type = readText("type",parser);
			} else if (name.equals("target")) {
				target = readText("target",parser);
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
	private static AddDisplayOfSelectionsBlock readBlockAddSelections(XmlPullParser parser) throws IOException, XmlPullParserException {
		Log.d("NILS","AddSelections block...");

		parser.require(XmlPullParser.START_TAG, null,"block_add_number_of_selections_display");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}		
			skip(parser);

		}
		return new AddDisplayOfSelectionsBlock();
	}	

	/**
	 *  Creates a CreateListEntryBlock. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws EvalException 
	 */	
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


	/**
	 *  Creates a CreateListEntriesBlock. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws EvalException 
	 */	
	private static CreateListEntriesBlock readBlockCreateListEntries(XmlPullParser parser) throws IOException, XmlPullParserException, EvalException {
		Log.d("NILS","Create List Entries block...");
		String fileName="",containerName=null,namn=null;
		XML_Variable Xvar=null;
		parser.require(XmlPullParser.START_TAG, null,"block_create_list_entries");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}	
			String name= parser.getName();
			//If a unique varname tag found, instantiate a new XML_variable. 
			if (name.equals("file_name")) {
				fileName = readText("file_name",parser);
				Log.d("NILS","fileName: "+fileName);
			} else if (name.equals("container_name")) {
				containerName = readText("container_name",parser);

			} else if (name.equals("name")) {
				namn = readText("name",parser);

			} else
				skip(parser);


		}

		return new CreateListEntriesBlock(fileName,containerName,namn);
	}


	/**
	 *  Creates a CreateFieldBlock. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws EvalException 
	 */	private CreateFieldBlock readBlockCreateField(XmlPullParser parser) throws IOException, XmlPullParserException, EvalException {
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

	 /**
	  *  Creates a Buttonblock. 
	  * @param parser
	  * @return
	  * @throws IOException
	  * @throws XmlPullParserException
	  */
	 //For now just create dummy.
	 private static ButtonBlock readBlockButton(XmlPullParser parser) throws IOException, XmlPullParserException {
		 Log.d("NILS","Button block...");
		 String label=null,action=null,myname=null,containerName=null,target=null;
		 parser.require(XmlPullParser.START_TAG, null,"block_button");
		 while (parser.next() != XmlPullParser.END_TAG) {
			 if (parser.getEventType() != XmlPullParser.START_TAG) {
				 continue;
			 }	
			 String name = parser.getName();
			 if (name.equals("action")) 
				 action = readText("action",parser);
			 else if (name.equals("name")) 
				 myname = readText("name",parser);
			 else if (name.equals("label")) 
				 label = readText("label",parser);
			 else if (name.equals("container_name")) 
				 containerName = readText("container_name",parser);		
			 else if (name.equals("target")) 
				 target = readText("target",parser);
			 else
				 skip(parser);
		 }
		 return new ButtonBlock(label,action,myname,containerName,target);
	 }

	 /**
	  *  Creates a Startblock. 
	  * @param parser
	  * @return
	  * @throws IOException
	  * @throws XmlPullParserException
	  */
	 //Block Start contains the name of the worklfow and the Arguments.
	 private static StartBlock readBlockStart(XmlPullParser parser) throws IOException, XmlPullParserException {
		 Log.d("NILS","Startblock...");
		 String workflowName=null; String args[]=null;
		 parser.require(XmlPullParser.START_TAG, null,"block_start");
		 while (parser.next() != XmlPullParser.END_TAG) {
			 if (parser.getEventType() != XmlPullParser.START_TAG) {
				 continue;
			 }
			 String name= parser.getName();
			 if (name.equals("workflowname"))  
				 workflowName = readSymbol("workflowname",parser);
			 else if (name.equals("inputvar")) 
				 args = readArray("inputvar",parser);
			 else
				 skip(parser);
		 }
		 if (workflowName == null)  {
			 Log.e("NILS","Error reading Startblock. Workflowname missing");
			 throw new XmlPullParserException("Parameter missing");
		 }
		 return new StartBlock(args,workflowName);
	 }

	 /**
	  *  Creates a set SetValueblock. Used to assign values to a variable from an expression.
	  * @param parser
	  * @return
	  * @throws IOException
	  * @throws XmlPullParserException
	  */
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
	 /**
	  * Creates a LayoutBlock. LayoutBlocks are used to set the direction of the layout 
	  * @param parser
	  * @return
	  * @throws IOException
	  * @throws XmlPullParserException
	  */
	 private static LayoutBlock readBlockLayout(XmlPullParser parser) throws IOException, XmlPullParserException {
		 Log.d("NILS","Layout block...");
		 String layout=null,align=null,label=null;
		 parser.require(XmlPullParser.START_TAG, null,"block_layout");
		 while (parser.next() != XmlPullParser.END_TAG) {
			 if (parser.getEventType() != XmlPullParser.START_TAG) {
				 continue;
			 }		
			 String name= parser.getName();
			 if (name.equals("layout")) {
				 layout = readText("layout",parser);
			 } else if (name.equals("align")) {
				 align = readText("align",parser);
			 } else if (name.equals("label")) {
				 label = readText("label",parser);
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
	 private static PageDefineBlock readPageDefineBlock(XmlPullParser parser) throws IOException, XmlPullParserException {
		 Log.d("NILS","Page define block...");
		 String pageType=null,pageLabel="";
		 parser.require(XmlPullParser.START_TAG, null,"block_define_page");
		 while (parser.next() != XmlPullParser.END_TAG) {
			 if (parser.getEventType() != XmlPullParser.START_TAG) {
				 continue;
			 }		
			 String name= parser.getName();
			 if (name.equals("type")) {
				 pageType = readText("type",parser);
			 } else if (name.equals("label")) {
				 pageLabel = readText("label",parser);
			 } else
				 skip(parser);
		 }
		 return new PageDefineBlock("root", pageType,pageLabel);
	 }


	 /**
	  * Creates a PageDefinitionBlock. Pages are the templates for a given page. Defines layout etc. 
	  * @param parser
	  * @return
	  * @throws IOException
	  * @throws XmlPullParserException
	  */
	 private static ContainerDefineBlock readContainerDefineBlock(XmlPullParser parser) throws IOException, XmlPullParserException {
		 Log.d("NILS","Container define block...");
		 String containerType=null,containerName="";
		 parser.require(XmlPullParser.START_TAG, null,"block_define_container");
		 while (parser.next() != XmlPullParser.END_TAG) {
			 if (parser.getEventType() != XmlPullParser.START_TAG) {
				 continue;
			 }		
			 String name= parser.getName();
			 if (name.equals("name")) {
				 containerName = readText("name",parser);
			 } else if (name.equals("container_type")) {
				 containerType = readText("container_type",parser);
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

		 Log.d("NILS","Add rule block...");		
		 String label=null, target=null, condition=null, action=null, errorMsg=null,myname=null;
		 parser.require(XmlPullParser.START_TAG, null,"block_add_rule");
		 while (parser.next() != XmlPullParser.END_TAG) {
			 if (parser.getEventType() != XmlPullParser.START_TAG) {
				 continue;
			 }		
			 String name= parser.getName();
			 if (name.equals("target")) {
				 target = readText("target",parser);
			 } else if (name.equals("condition")) {
				 condition = readText("condition",parser);
			 } else if (name.equals("action")) {
				 action = readText("action",parser);
			 } else if (name.equals("errorMsg")) {
				 errorMsg = readText("errorMsg",parser);
			 } else if (name.equals("name")) {
				 myname = readText("name",parser);
			 }else if (name.equals("label")) {
				 label = readText("label",parser);
			 } else 
				 skip(parser);

		 }
		 return new AddRuleBlock(ctx,label,myname,target,condition,action,errorMsg);
	 }

	 // Read symbol from tag.
	 private static String readSymbol(String tag,XmlPullParser parser) throws IOException, XmlPullParserException {
		 parser.require(XmlPullParser.START_TAG, null,tag);
		 String text = readText(parser);
		 parser.require(XmlPullParser.END_TAG, null,tag);
		 //Check that it does not start with a number.
		 if (text!=null) {
			 if (text.length()>0 && Character.isDigit(text.charAt(0)))
				 throw new XmlPullParserException("Symbol cannot start with integer");	
		 } else
			 throw new XmlPullParserException("Symbol cannot be null");
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
