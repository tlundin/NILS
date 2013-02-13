package com.teraim.nils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import android.util.Xml;

import com.teraim.nils.DataTypes.Block;
import com.teraim.nils.DataTypes.ButtonBlock;
import com.teraim.nils.DataTypes.LayoutBlock;
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

public class WorkflowParser {


	//Take input file and parse it.
	//Generates a list of workflows.

	public static List<Workflow> parse(Context c)  {
		List<Workflow> myFlow = null;
		//FileInputStream in = null;
		try {	
			InputStream in = c.getResources().openRawResource(R.raw.mainflow);
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
			String _name= parser.getName();
			if (_name.equals("id")) {
				wf.id = readId(parser);
			} else if (_name.equals("name")) {
				wf.name = readName(parser);
			} else if (_name.equals("blocks")) {
				wf.addBlocks(readBlocks(parser));
			} else {
				skip(parser);
			}
		}
		Log.d("NILS","wf id: "+wf.id+" name: "+wf.name);
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
		}
				
		return blocks;
	}

	/**
	 *  Creates a startblock. For now just a dummy.
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	//For now just create dummy.
	private static ButtonBlock readBlockButton(XmlPullParser parser) throws IOException, XmlPullParserException {
		String text=null;
		parser.require(XmlPullParser.START_TAG, null,"block_button");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}	
			String name= parser.getName();
			if (name.equals("text")) 
				text = readText("text",parser);
			else
				skip(parser);
		}
		return new ButtonBlock(text);
	}
	
	/**
	 *  Creates a startblock. For now just a dummy.
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	//For now just create dummy.
	private static StartBlock readBlockStart(XmlPullParser parser) throws IOException, XmlPullParserException {
		StartBlock block =new StartBlock();
		parser.require(XmlPullParser.START_TAG, null,"block_start");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}	
			skip(parser);
		}
		return block;
	}
	
	/**
	 * Creates a LayoutBlock. LayoutBlocks are used to set the direction of the layout 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private static LayoutBlock readBlockLayout(XmlPullParser parser) throws IOException, XmlPullParserException {
		String layout=null,align=null;
		parser.require(XmlPullParser.START_TAG, null,"block_layout");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}		
			String _name= parser.getName();
			if (_name.equals("layout")) {
				layout = readText("layout",parser);
			} else if (_name.equals("align")) {
				align = readText("align",parser);
			} else {
			skip(parser);
			}
		}
		return new LayoutBlock(layout,align);
	}
	
	
	
	// Processes name tags in the feed.
	private static String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
		return readText("name",parser);
	}	

	// Processes id tags in the feed.
	private static String readId(XmlPullParser parser) throws IOException, XmlPullParserException {
		return readText("id",parser);
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
