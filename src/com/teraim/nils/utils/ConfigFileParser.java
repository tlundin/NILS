package com.teraim.nils.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import com.teraim.nils.Constants;
import com.teraim.nils.FileLoadedCb;
import com.teraim.nils.FileLoadedCb.ErrorCode;
import com.teraim.nils.dynamic.blocks.AddRuleBlock;
import com.teraim.nils.dynamic.blocks.AddSumOrCountBlock;
import com.teraim.nils.dynamic.blocks.Block;
import com.teraim.nils.dynamic.blocks.ButtonBlock;
import com.teraim.nils.dynamic.blocks.ContainerDefineBlock;
import com.teraim.nils.dynamic.blocks.CreateEntryFieldBlock;
import com.teraim.nils.dynamic.blocks.CreateListEntriesBlock;
import com.teraim.nils.dynamic.blocks.LayoutBlock;
import com.teraim.nils.dynamic.blocks.ListFilterBlock;
import com.teraim.nils.dynamic.blocks.ListSortingBlock;
import com.teraim.nils.dynamic.blocks.PageDefineBlock;
import com.teraim.nils.dynamic.blocks.StartBlock;
import com.teraim.nils.dynamic.types.Table;
import com.teraim.nils.dynamic.types.Workflow;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.dynamic.workflow_realizations.WF_Not_ClickableField_SumAndCountOfVariables;
import com.teraim.nils.exceptions.EvalException;
import com.teraim.nils.exceptions.SameOldException;

/**
 * 
 * @author Terje
 *
 * Parser that reads .CSV file with configuration data.
 * Will try to insert data into a table object and freeze it.
 * 
 */

public class ConfigFileParser extends AsyncTask<Context,Void,ErrorCode>{

	Context ctx;
	//Location of bundle.
	PersistenceHelper ph;
	FileLoadedCb cb;
	String myVersion = null;
	Table myTable=null;




	public ConfigFileParser(PersistenceHelper ph, FileLoadedCb fileLoadedCb) {
		this.ph=ph;
		this.cb = fileLoadedCb;
	}



	//Take input file from remote web server and parse it.
	//Generates a list of workflows from a Bundle.
	@Override
	protected ErrorCode doInBackground(Context... params) {
		ctx = params[0];
		return parse(ph.get(PersistenceHelper.SERVER_URL)+ph.get(PersistenceHelper.CONFIG_LOCATION));
	}

	@Override
	protected void onPostExecute(ErrorCode code) {

		Log.d("nils","Confdir: "+Constants.CONFIG_FILES_DIR+" "+"frozen "+Constants.CONFIG_FROZEN_FILE_ID);
		if (code == ErrorCode.newVersionLoaded) {
			boolean ok= Tools.witeObjectToFile(ctx, myTable, Constants.CONFIG_FILES_DIR+Constants.CONFIG_FROZEN_FILE_ID);
			if (!ok)
				code = ErrorCode.ioError;
			else {
				ph.put(PersistenceHelper.CURRENT_VERSION_OF_CONFIG_FILE,myVersion);
				code = ErrorCode.newVersionLoaded;
			}
		}
		cb.onFileLoaded(code);	
	}
	
	
	//Creates the ArtLista arteface from a configuration file.
	
	public ErrorCode parse(String fileUrl) {
		Log.d("nils","File url: "+fileUrl);
		try {	
			URL url = new URL(fileUrl);
			Log.d("NILS", "downloading page "+fileUrl);
			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();
			InputStream in = ucon.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String header;
			String row;
			String versionR = br.readLine();
			if (versionR !=null) {
				String vers[] = versionR.split(",");
				if (vers.length<2)
					Log.e("nils","Unable to read version row...corrupt? "+versionR);
				else {
					myVersion = vers[1];
					if (myVersion.equals(ph.get(PersistenceHelper.CURRENT_VERSION_OF_CONFIG_FILE))) {
						Log.d("nils","No need to parse...no changes ");
						br.close();
						return ErrorCode.sameold;
					}
					
				}				
			}
			header = br.readLine();
			Log.d("NILS","Scanning listdatafile with header "+header);
			if (header != null) {
				
			myTable = new Table(header.split(","));
			//Find all RutIDs from csv. Create Ruta Class for each.
			while((row = br.readLine())!=null) {
				String[]  r = row.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");				
				if (r!=null) {
					for(int i=0;i<r.length;i++)
						if (r[i]!=null)
							r[i] = r[i].replace("\"", "");
					
					myTable.addRow(Arrays.asList(r));		
				}
			}
			} else {
				return ErrorCode.parseError;
			}
			myTable.addRow(Arrays.asList("EE0020,AntalArter,Antal Arter,,,,TRUE,st,delyta,,,,,,,,".split(",")));
			myTable.addRow(Arrays.asList("EE0030,SumTackning,Summa Täckning,,,,TRUE,%,delyta,,,,,,,,".split(",")));
			br.close();			
		} catch (FileNotFoundException e) {
				e.printStackTrace();
				return ErrorCode.notFound;			
		} catch (IOException e) {
			e.printStackTrace();
			return ErrorCode.ioError;			
		}
		return ErrorCode.newVersionLoaded;
	}
}