package com.teraim.nils.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import android.content.Context;
import android.os.AsyncTask;

import com.teraim.nils.Constants;
import com.teraim.nils.FileLoadedCb;
import com.teraim.nils.FileLoadedCb.ErrorCode;
import com.teraim.nils.GlobalState;
import com.teraim.nils.LoggerI;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.Table;
import com.teraim.nils.dynamic.types.Table.ErrCode;

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
	LoggerI o;
	
	




	public ConfigFileParser(PersistenceHelper ph, FileLoadedCb fileLoadedCb) {
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
		if (!serverUrl.endsWith("/"))
			serverUrl+="/";
		if (!serverUrl.startsWith("http://")) {
			serverUrl = "http://"+serverUrl;
			o.addRow("server url name missing http header...adding");		
		}
		return parse(serverUrl+ph.get(PersistenceHelper.CONFIG_LOCATION));
	}

	@Override
	protected void onPostExecute(ErrorCode code) {

		if (code == ErrorCode.newVersionLoaded) {
			boolean ok= Tools.witeObjectToFile(ctx, myTable, Constants.CONFIG_FILES_DIR+Constants.CONFIG_FROZEN_FILE_ID);
			if (!ok)
				code = ErrorCode.ioError;
			else {
				ph.put(PersistenceHelper.CURRENT_VERSION_OF_CONFIG_FILE,myVersion);
				code = ErrorCode.newVersionLoaded;
				
				o.addRow("");
				o.addYellowText("New Configuration file loaded. Version: "+myVersion);
			}
		}
		cb.onFileLoaded(code);	
	}


	//Creates the ArtLista arteface from a configuration file.

	public ErrorCode parse(String fileUrl) {
		o.addRow("");
		o.addYellowText("Now parsing variable configuration file. ");
		o.addRow("File URL: "+fileUrl);
		try {	
			URL url = new URL(fileUrl);
			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();
			InputStream in = ucon.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String header;
			String row;
			String versionR = br.readLine();
			if (versionR !=null) {
				String vers[] = versionR.split(",");
				if (vers.length<2) {
					o.addRow("");
					o.addRedText("Unable to read version row...corrupt? "+versionR);
				}
				else {
					myVersion = vers[1];
					o.addRow("Config file version: ");o.addYellowText(myVersion);
					if (ph.getB(PersistenceHelper.VERSION_CONTROL_SWITCH_OFF)) {
						o.addRow("Version control is switched off.");
					} else
						if (myVersion.equals(ph.get(PersistenceHelper.CURRENT_VERSION_OF_CONFIG_FILE))) {
							o.addRow("No need to parse...no changes ");
							br.close();
							return ErrorCode.sameold;
						}

				}				
			}
			header = br.readLine();

			o.addRow("File header reads:["+header+"]");
			if (header != null) {		
				//TODO: REMOVE CONSTANT PEEK
				myTable = new Table(header.split(","),VariableConfiguration.KEY_CHAIN,VariableConfiguration.VARIABLE_NAME);
				//Find all RutIDs from csv. Create Ruta Class for each.
				int rowC=1;
				while((row = br.readLine())!=null) {
					String[]  r = row.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");				
					if (r!=null) {
						for(int i=0;i<r.length;i++)
							if (r[i]!=null)
								r[i] = r[i].replace("\"", "");
						Table.ErrCode e = myTable.addRow(Arrays.asList(r));	
						if (e!=ErrCode.ok) {
							o.addRow("");
							if (e==ErrCode.tooFewColumns) 
								o.addRedText("First element empty or corrupt on line: "+rowC+" Row:"+Arrays.asList(r).toString());
							else
								o.addRedText("Too many columns on line: "+rowC);	
						}
					}
					rowC++;
				}
			} else {
				return ErrorCode.parseError;
			}
			//o.addText("Adding additional Variables to Table: AntalArter, SumTackning");
			//myTable.addRow(Arrays.asList("EE0020,AntalArter,Antal Arter,,,,TRUE,st,delyta,,,,,,,,".split(",")));
			//myTable.addRow(Arrays.asList("EE0030,SumTackning,Summa Täckning,,,,TRUE,%,delyta,,,,,,,,".split(",")));
			br.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			o.addRow("");
			o.addRedText("Could not find the file at the specified location");
			return ErrorCode.notFound;			
		} catch (IOException e) {
			e.printStackTrace();
			o.addRow("IO ERROR!");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);		
			o.addRedText(sw.toString());
			return ErrorCode.ioError;			
		}
		return ErrorCode.newVersionLoaded;
	}
}