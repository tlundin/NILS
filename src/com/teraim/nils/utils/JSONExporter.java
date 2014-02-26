package com.teraim.nils.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.util.JsonWriter;
import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.utils.DbHelper.DBColumnPicker;
import com.teraim.nils.utils.DbHelper.StoredVariableData;

public class JSONExporter {

	GlobalState gs;
	DbHelper db;
	JsonWriter writer;
	PersistenceHelper ph;
	StringWriter sw;

	public class Report {
		public String result;
		public int noOfVars = 0;
	}

	private static JSONExporter instance = null;

	public static JSONExporter getInstance(Context ctx) {
		if (instance == null) {
			instance = new JSONExporter(ctx);
		}
		return instance;
	}
	private JSONExporter(Context ctx) {
		this.gs=GlobalState.getInstance(ctx);
		ph = gs.getPersistence();
		sw = new StringWriter();
	}



	public Report writeVariables(DBColumnPicker cp) {

		writer = new JsonWriter(sw);	

		try {
			if (cp.moveToFirst()) {
				writeHeader("BEGIN");
				boolean foreverEverEver = true;
				Map<String,String> currentKeys = cp.getKeyColumnValues();
				while (foreverEverEver) {
					writeSubHeader(currentKeys);
					boolean moreVariables=true;
					while (moreVariables) {
						writeVariable(cp.getVariable());
						if (!cp.next()) {						
							closeHeader();
							writer.close();
							Log.d("nils","finished writing JSON");
							Log.d("nils", sw.toString());
							Report r = new Report();
							r.result=sw.toString();
							return r;
						}
						Map<String,String> newKeys = cp.getKeyColumnValues();
						if (!sameKeys(currentKeys,newKeys)) {
							currentKeys = newKeys;
							moreVariables = false;

						}


					}


				}


			} else {
				Log.e("nils","cursor empty in writeVariables.");
				writer.close();			
			}





		} catch (IOException e) {
			e.printStackTrace();
			cp.close();
		} finally {
			cp.close();
		}

		return null;
	}





	private boolean sameKeys(Map<String, String> m1,
			Map<String, String> m2) {
		if (m1.size() != m2.size())
			return false;
		for (String key: m1.keySet())
			if (!m1.get(key).equals(m2.get(key)))
				return false;
		Log.d("nils","keys equal..no header");
		return true;
	}

	private void closeHeader() throws IOException {
		//Close body
		writer.endArray();
		//Close header
		writer.endArray();

	}
	private void write(String name,String value) throws IOException {

		String val = (value==null||value.length()==0)?"NULL":value;
		writer.name(name).value(val);
	}

	private void writeVariable(StoredVariableData variable) throws IOException {
		writer.beginObject();
		write("name",variable.name);
		write("value",variable.value);
		write("lag",variable.lagId);
		write("author",variable.creator);
		write("timestamp",variable.timeStamp);
		writer.endObject();
	}

	private void writeSubHeader(Map<String,String> currentKeys) throws IOException {
		//subheader.
		writer.beginObject();
		Set<String> keys = currentKeys.keySet();
		for (String key:keys) 
			write(key,currentKeys.get(key));
		writer.endObject();
	}

	public void writeHeader(String header) throws IOException {
		Date now = new Date();
		writer.setIndent("  ");
		//File header.
		writer.beginArray();
		//Header object.
		writer.beginObject();
		Log.d("nils","Exporting database");
		write("date",DateFormat.getInstance().format(now));
		write("time",DateFormat.getTimeInstance().format(now));
		write("programversion",ph.get(PersistenceHelper.CURRENT_VERSION_OF_PROGRAM));
		write("workflow bundle version",ph.get(PersistenceHelper.CURRENT_VERSION_OF_WF_BUNDLE));
		write("Artlista version",ph.get(PersistenceHelper.CURRENT_VERSION_OF_CONFIG_FILE));
		write("Variable Definition version",ph.get(PersistenceHelper.CURRENT_VERSION_OF_VARPATTERN_FILE));
		writer.endObject();
		Log.d("nils",writer.toString());
		//Body is an array of objects.
		writer.beginArray();
		//...header and body must be closed.
	}


}
