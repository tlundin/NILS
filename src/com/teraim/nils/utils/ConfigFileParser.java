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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.teraim.nils.FileLoadedCb;
import com.teraim.nils.FileLoadedCb.ErrorCode;
import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.SpinnerDefinition;
import com.teraim.nils.dynamic.types.Table;
import com.teraim.nils.dynamic.types.Table.ErrCode;
import com.teraim.nils.log.LoggerI;
import com.teraim.nils.non_generics.Constants;

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
	String fVersion = null;
	String vVersion = null;
	Table myTable=null;
	LoggerI o;
	
	
	final static int VAR_PATTERN_ROW_LENGTH = 9;






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
		ErrorCode retValue = parse(serverUrl,ph.get(PersistenceHelper.CONFIG_LOCATION));
		
		GlobalState.getInstance(ctx).setSpinnerDefinitions(Tools.scanSpinerDef(ctx, o));
		
		return retValue;
	}

	@Override
	protected void onPostExecute(ErrorCode code) {

		if (code == ErrorCode.newVarPatternVersionLoaded||
				code == ErrorCode.newConfigVersionLoaded|| 
				code == ErrorCode.bothFilesLoaded) {
			boolean ok= Tools.witeObjectToFile(ctx, myTable, Constants.CONFIG_FILES_DIR+Constants.CONFIG_FROZEN_FILE_ID);
			if (!ok)
				code = ErrorCode.ioError;
			else {
				if (code ==ErrorCode.bothFilesLoaded||code ==  ErrorCode.newConfigVersionLoaded)
					o.addRow("");
					o.addYellowText("Configuration file loaded. Version: "+fVersion);
					ph.put(PersistenceHelper.CURRENT_VERSION_OF_CONFIG_FILE,fVersion);
				if (code ==ErrorCode.bothFilesLoaded||code == ErrorCode.newVarPatternVersionLoaded) {
					o.addRow("");
					o.addYellowText("Varpattern file loaded. Version: "+vVersion);
					ph.put(PersistenceHelper.CURRENT_VERSION_OF_VARPATTERN_FILE,vVersion);
				}
			}
		}
		


		cb.onFileLoaded(code);	
	}


	//Creates the ArtLista arteface from a configuration file.

	
	
	public ErrorCode parse(String serverUrl, String fileName) {
		final String FileUrl = serverUrl+fileName;
		final String VarUrl = serverUrl+"varpattern.csv";
		boolean parseConfig=true,parseVarPattern=true;
		
		o.addRow("");
		o.addYellowText("Now parsing variable configuration files. ");
		o.addRow("Artlista URL: "+FileUrl);
		o.addRow("Variable URL: "+VarUrl);
		try {	
			URL url = new URL(FileUrl);
			URL url2 = new URL(VarUrl);
			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();
			URLConnection ucon2 = url2.openConnection();
			InputStream in = ucon.getInputStream();
			InputStream in2 = ucon2.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			BufferedReader br2 = new BufferedReader(new InputStreamReader(in2, "UTF-8"));

			String configFileHeader,varPatternFileHeader;
			String row;
			String configFileVersion = br.readLine();
			String varPatternFileVersion = br2.readLine();
			if (configFileVersion !=null && varPatternFileVersion !=null) {
				String fvers[] = configFileVersion.split(",");
				String vvers[] = varPatternFileVersion.split(",");
				if (fvers.length<2) {
					o.addRow("");
					o.addRedText("Unable to read config file version. Row corrupt? "+configFileVersion);					
				}
				if (vvers.length<2) {
					o.addRow("");
					o.addRedText("Unable to read varpattern file version. Row corrupt? "+configFileVersion);
				}

				fVersion = fvers[1];
				vVersion = vvers[1];
				o.addRow("Config file version: ");o.addYellowText(fVersion);
				if (ph.getB(PersistenceHelper.VERSION_CONTROL_SWITCH_OFF)) {
					o.addRow("Version control is switched off.");
				} else {
					if (fVersion.equals(ph.get(PersistenceHelper.CURRENT_VERSION_OF_CONFIG_FILE))) {
						o.addRow("No need to parse...no changes ");
						br.close();
						parseConfig = false;

					} 
					if (vVersion.equals(ph.get(PersistenceHelper.CURRENT_VERSION_OF_VARPATTERN_FILE))) {
						o.addRow("No need to parse...no changes ");
						br.close();
						parseVarPattern = false;
					} 
					
					if (!parseConfig && !parseVarPattern)
						return ErrorCode.sameold;
				}

			}				

			configFileHeader = br.readLine();
			varPatternFileHeader = br2.readLine();

			o.addRow("Config file header reads:["+configFileHeader+"]");
			o.addRow("Varpattern file header reads:["+varPatternFileHeader+"]");
			if (configFileHeader != null && varPatternFileHeader != null) {	

				String[] configFileHeaderS = configFileHeader.split(",");
				String[] varPatternHeaderS = varPatternFileHeader.split(",");

				//Go through varpattern. Generate rows for the master table.
				//...but first - find the key columns in Artlista.
				int nameIndex = -1;
				int groupIndex = -1;
				int pNameIndex = 2;
				int pGroupIndex = 1;


				//Find the Variable key row.
				for (int i = 0; i<configFileHeaderS.length;i++) {
					if (configFileHeaderS[i].trim().equals(VariableConfiguration.Col_Functional_Group))
						groupIndex = i;
					else if  (configFileHeaderS[i].trim().equals(VariableConfiguration.Col_Variable_Name))
						nameIndex = i;
				}

				if (nameIndex ==-1 || groupIndex == -1) {
					o.addRow("");
					o.addRedText("Config file Header missing either name or functional group column. Load cannot proceed");
					br.close();
					return ErrorCode.parseError;
				}



				//Split config file into parts according to functional group.

				Map <String, List<List<String>>> groups=new HashMap<String,List<List<String>>>();

				while((row = br.readLine())!=null) {
					//String[]  r = row.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)",-1);	
					String[] r = Tools.split(row);
					if (r!=null) {
						for(int i=0;i<r.length;i++) {
							if (r[i]!=null)
								r[i] = r[i].replace("\"", "");

						}
						String group = r[groupIndex];
						List<List<String>> elem = groups.get(group); 
						if (elem==null) {
							elem = new ArrayList<List<String>>();
							groups.put(group,elem);
						}
						elem.add(Arrays.asList(r));



						//Table.ErrCode e = myTable.addRow(Arrays.asList(r));	

					}
				}

				//Now all groups are created. 

				//Create header. Remove duplicte group column and varname. 
				final List<String> cheaderL =  new ArrayList<String>();
				Collections.addAll(cheaderL,configFileHeaderS); 
				cheaderL.remove(VariableConfiguration.Col_Functional_Group);
				cheaderL.remove(VariableConfiguration.Col_Variable_Name);
				List<String> vheaderL = new ArrayList<String>(trimmed(varPatternHeaderS));
				vheaderL.addAll(cheaderL);
				myTable = new Table(vheaderL,0,pNameIndex);

				int rowC=1;
				//Scan through VarPattern to generate variables.
				Log.d("nils","Starting scan of varPattern");
				String r[];
				
				List<List<String>> elems;
				while((row = br2.readLine())!=null) {
					r =row.split(",",-1);
					if (r==null || r.length<VAR_PATTERN_ROW_LENGTH) {
						Log.e("nils","found null or too short row at "+rowC+" in config file");
					} else {			
						String pGroup = r[pGroupIndex];
						if (pGroup==null || pGroup.trim().length()==0) {
							//Log.d("nils","found variable "+r[pNameIndex]+" in varpattern");
							myTable.addRow(trimmed(r));
						} else {
							Log.d("nils","found group name: "+pGroup);
							elems = groups.get(pGroup);
							String varPatternName = r[pNameIndex];
							if (elems==null) {
								//If the variable has a group,add it 
								Log.d("nils","Group "+pGroup+" in line#"+rowC+" does not exist in config file. Will use name: "+varPatternName);
								
								myTable.addRow(trimmed(r));
							} else {
								for (List<String>elem:elems) {
									//Go through all rows in group. Generate variables.
									String cFileNamePart = elem.get(nameIndex);
									
									if (cFileNamePart == null||varPatternName==null) {
										Log.e("nils","Either varPatternNamepart or configFile namepart evaluates to null at line#"+rowC+" in varpattern file");
									} else {
										String fullVarName = pGroup+"_"+cFileNamePart+"_"+varPatternName;
										//Remove duplicate elements from Config File row.
										//Make a copy.
										List<String>elemCopy = new ArrayList<String>(elem);
										elemCopy.remove(nameIndex);
										elemCopy.remove(groupIndex);
										List<String>varPatternL = new ArrayList<String>(trimmed(r));
										varPatternL.addAll(elemCopy);
										//Replace name column with full name.
										varPatternL.set(pNameIndex, fullVarName);
										//Log.d("nils","Generated variable with values "+varPatternL.toString());
										ErrCode err = myTable.addRow(varPatternL);
										if (err!=ErrCode.ok) {
											switch (err) {
											case keyError:
											case tooFewColumns:
											case tooManyColumns:
												Log.d("nils", "row not inserted. Something wrong at line "+rowC);
												break;
											}
										}
									}
								}
							}
						}
					}
					rowC++;
				}
				Log.d("nils","Scanned "+rowC+" rows");
			} else {
				br.close();
				br2.close();
				return ErrorCode.parseError;
			}			
			br.close();	
			br2.close();
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
		if (parseConfig && parseVarPattern) 
			return ErrorCode.bothFilesLoaded;
		if (parseConfig)
			return ErrorCode.newConfigVersionLoaded;
		else 
			return ErrorCode.newVarPatternVersionLoaded;
	}



	



	private List<String> trimmed(String[] r) {
		ArrayList<String> ret = new ArrayList<String>();
		for(int i=0;i<VAR_PATTERN_ROW_LENGTH;i++)
			ret.add(r[i]);
		return ret;
	}
	
}