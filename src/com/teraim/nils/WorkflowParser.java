package com.teraim.nils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;

import com.teraim.nils.DataTypes.Block;
import com.teraim.nils.DataTypes.ButtonBlock;
import com.teraim.nils.DataTypes.CreateFieldBlock;
import com.teraim.nils.DataTypes.LayoutBlock;
import com.teraim.nils.DataTypes.SetValueBlock;
import com.teraim.nils.DataTypes.StartBlock;
import com.teraim.nils.DataTypes.Workflow;

/**
 * 
 * @author Terje
 *
 * Parser that reads XML descriptions of workflows.
 * Will try to parse the XML into a list.
 * TODO: Must implement good syntax error messages.
 */
//class RetreiveFeedTask extends AsyncTask<String, Void, RSSFeed> {
public class WorkflowParser extends AsyncTask<Context,Void,List<Workflow>>{

	Context context;
	//Location of bundle.
	private final static String serverUrl = "http://83.250.104.137:8080/nilsbundle.xml";
	//private final static String serverUrl = "http://teraim.com/nilsbundle.xml";
	//Take input file from remote web server and parse it.
	//Generates a list of workflows from a Bundle.
	@Override
	protected List<Workflow> doInBackground(Context... params) {
		context = params[0];
		return parse(context);
	}
	
	 @Override
	 protected void onPostExecute(List<Workflow> result) {
		 Log.d("NILS","Gets here!");
		 CommonVars.cv().setWorkflows(result);
		Intent intent = new Intent(context, FlowEngineActivity.class);
		Bundle b = new Bundle();
		b.putString("workflow_name", "main"); //Your id
		intent.putExtras(b); //Put your id to your next Intent
		context.startActivity(intent);
			
	   }

	public static List<Workflow> parse(Context c)  {
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


	private static List<Workflow> readBundle(XmlPullParser parser) throws XmlPullParserException, IOException {

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


	private static Workflow readWorkflow(XmlPullParser parser) throws XmlPullParserException, IOException {
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
	private static List<Block> readBlocks(XmlPullParser parser) throws IOException, XmlPullParserException {
		List<Block> blocks=new ArrayList<Block>();
		parser.require(XmlPullParser.START_TAG, null,"blocks");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("block_start")) 
				blocks.add(readBlockStart(parser));
			else if (name.equals("block_layout")) 
				blocks.add(readBlockLayout(parser));				
				else if (name.equals("block_button")) 
					blocks.add(readBlockButton(parser));
				else if (name.equals("block_set_value")) 
					blocks.add(readBlockSetValue(parser));
				else if (name.equals("block_create_field")) 
					blocks.add(readBlockCreateField(parser));
				else
					skip(parser);
		}
				
		return blocks;
	}
	
	/**
	 *  Creates a CreateFieldBlock. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */	private static CreateFieldBlock readBlockCreateField(XmlPullParser parser) throws IOException, XmlPullParserException {
		 Log.d("NILS","Create Field block...");
		 String varname=null,label=null,vartype=null,purpose="edit";
		parser.require(XmlPullParser.START_TAG, null,"block_create_field");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}	
			String name= parser.getName();
			if (name.equals("varname")) 
				varname = readText("varname",parser);
			else if (name.equals("vartype")) 
				vartype = readText("vartype",parser);
			//TODO: PAGENAME.			
			else if (name.equals("purpose")) 
				purpose = readText("purpose",parser);
			else if (name.equals("label")) 
				label = readText("label",parser);
			else
				skip(parser);
		}
		return new CreateFieldBlock(label,varname,vartype,purpose);
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
		String text=null,label=null,action=null;
		parser.require(XmlPullParser.START_TAG, null,"block_button");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}	
			String name= parser.getName();
			if (name.equals("text")) 
				text = readText("text",parser);
			else if (name.equals("action")) 
				action = readText("action",parser);
			else if (name.equals("label")) 
				label = readText("label",parser);
			else
				skip(parser);
		}
		return new ButtonBlock(label,text,action);
	}
	
	/**
	 *  Creates a Startblock. 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	//For now just create dummy.
	private static StartBlock readBlockStart(XmlPullParser parser) throws IOException, XmlPullParserException {
		Log.d("NILS","Startblock...");
		String workflowName=null,label=null;
		parser.require(XmlPullParser.START_TAG, null,"block_start");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name= parser.getName();
			if (name.equals("workflowname"))  
				workflowName = readSymbol("workflowname",parser);
			else if (name.equals("label")) 
				label = readText("label",parser);
			else
				skip(parser);
		}
		if (workflowName == null)  {
			Log.e("NILS","Error reading Startblock. Workflowname missing");
			throw new XmlPullParserException("Parameter missing");
		}
		return new StartBlock(label,workflowName);
	}

	/**
	 *  Creates a set SetValueblock. Used to assign values to a variable from an expression.
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	//For now just create dummy.
	private static SetValueBlock readBlockSetValue(XmlPullParser parser) throws IOException, XmlPullParserException {
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
		return new SetValueBlock(label,varRef,expr);
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
