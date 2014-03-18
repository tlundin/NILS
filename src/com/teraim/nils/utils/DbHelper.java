package com.teraim.nils.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.bluetooth.SyncEntry;
import com.teraim.nils.dynamic.types.Table;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.non_generics.Constants;
import com.teraim.nils.utils.JSONExporter.Report;

public class DbHelper extends SQLiteOpenHelper {

	// Database Version
	private static final int DATABASE_VERSION = 5;
	// Database Name
	private static final String DATABASE_NAME = "Nils";

	// Books table name
	private static final String TABLE_VARIABLES = "variabler";
	private static final String TABLE_AUDIT = "audit";

	private static final String VALUE="value",TIMESTAMP="timestamp",LAG="lag",AUTHOR="author";
	private static final String[] VAR_COLS = new String[] { TIMESTAMP, AUTHOR, LAG, VALUE };
	private static final Set<String> MY_VALUES_SET = new HashSet<String>(Arrays.asList(VAR_COLS));

	private static final int NO_OF_KEYS = 10;
	private final SQLiteDatabase db;
	private final PersistenceHelper ph;

	private final Map<String,String> keyColM = new HashMap<String,String>();
	private Map<String,String> colKeyM = new HashMap<String,String>();

	Context ctx;


	//Helper class that wraps the Cursor.
	public class DBColumnPicker {
		Cursor c;
		private static final String NAME = "var",VALUE="value",TIMESTAMP="timestamp",LAG="lag",CREATOR="author";

		public DBColumnPicker(Cursor c) {
			this.c=c;
		}

		public StoredVariableData getVariable() {
			return new StoredVariableData(pick(NAME),pick(VALUE),pick(TIMESTAMP),pick(LAG),pick(CREATOR));
		}
		public Map<String,String> getKeyColumnValues() {
			Map<String,String> ret = new HashMap<String,String>();
			Set<String> keys = keyColM.keySet();
			String col=null;
			for(String key:keys) {
				col = keyColM.get(key);
				ret.put(key, pick(col));
			}
			return ret; 
		}

		private String pick(String key) {
			return c.getString(c.getColumnIndex(key));
		}

		public boolean moveToFirst() {
			if (c==null)
				return false;
			else
				return c.moveToFirst();
		}

		public boolean next() {
			boolean b = c.moveToNext();
			if (!b)
				c.close();
			return b;
		}

		public void close() {
			c.close();
		}

	}


	public DbHelper(Context context,Table t, PersistenceHelper ph) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);  
		ctx = context;
		db = this.getWritableDatabase();
		this.ph=ph;
		if (t!=null)
			init(t.getKeyParts());
		else {
			Log.d("nils","Table doesn't exist yet...postpone init");
		}
	}



	public void init(ArrayList<String> keyParts) {

		//check if keyParts are known or if a new is needed.

		//Load existing map from sharedStorage.
		String colKey="";
		Log.d("nils","DBhelper init");
		for(int i=1;i<=NO_OF_KEYS;i++) {

			colKey = ph.get("L"+i);
			//If empty, I'm done.
			if (colKey.equals(PersistenceHelper.UNDEFINED)) {
				Log.d("nils","didn't find key L"+i);
				break;
			}
			else 
				keyColM.put(colKey,"L"+i);
		}
		//Now check the new keys. If a new key is found, add it.
		if (keyParts == null) {
			Log.e("nils","Keyparts were null in DBHelper");
		} else {
			Log.e("nils","Keyparts has"+keyParts.size()+" elements");
			for(int i=0;i<keyParts.size();i++) {
				Log.d("nils","checking keypart "+keyParts.get(i));
				if (keyColM.containsKey(keyParts.get(i))) {
					Log.d("nils","Key "+keyParts.get(i)+" already exists..skipping");
					continue;
				} else {
					Log.d("nils","Found new column key "+keyParts.get(i));
					if (keyParts.get(i).isEmpty()) {
						Log.d("nils","found empty keypart! Skipping");
					} else {
						String colId = "L"+(keyColM.size()+1);
						//Add key to memory
						keyColM.put(keyParts.get(i),colId);
						colKeyM.put(colId,keyParts.get(i));
						//Persist new column identifier.
						ph.put(colId, keyParts.get(i));
					}
				}

			}
		}
		Log.d("nils","Keys added: ");
		Set<String> s = keyColM.keySet();
		for (String e:s)
			Log.d("nils","Key: "+e+"Value:"+keyColM.get(e));


	}



	@Override
	public void onCreate(SQLiteDatabase db) {

		// create variable table Lx columns are key parts.
		String CREATE_VARIABLE_TABLE = "CREATE TABLE variabler ( " +
				"id INTEGER PRIMARY KEY ," + 
				"L1 TEXT , "+
				"L2 TEXT , "+
				"L3 TEXT , "+
				"L4 TEXT , "+
				"L5 TEXT , "+
				"L6 TEXT , "+
				"L7 TEXT , "+
				"L8 TEXT , "+
				"L9 TEXT , "+
				"L10 TEXT , "+
				"var TEXT COLLATE NOCASE, "+
				"value TEXT, "+
				"lag TEXT, "+
				"timestamp TEXT, "+
				"author TEXT ) ";

		//audit table to keep track of all insert,updates and deletes. 
		String CREATE_AUDIT_TABLE = "CREATE TABLE audit ( " +
				"id INTEGER PRIMARY KEY ," + 				
				"timestamp TEXT, "+
				"action TEXT, "+
				"changes TEXT ) ";

		// 
		db.execSQL(CREATE_VARIABLE_TABLE);
		db.execSQL(CREATE_AUDIT_TABLE);

		Log.d("NILS","DB CREATED");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older books table if existed
		db.execSQL("DROP TABLE IF EXISTS variabler");
		db.execSQL("DROP TABLE IF EXISTS audit");

		// create fresh books table
		this.onCreate(db);
	}


	public void exportAllData() {
		Cursor c = db.query(TABLE_VARIABLES,null,
				null,null,null,null,null,null);
		if (c!=null) {

			//"timestamp","lag","author"
			Log.d("nils","Variables found in db:");
			String L[] = new String[keyColM.size()];
			String var,value,timeStamp,lag,author;
			while (c.moveToNext()) {
				var = c.getString(c.getColumnIndex("var"));
				value = c.getString(c.getColumnIndex("value"));
				timeStamp = c.getString(c.getColumnIndex("timestamp"));
				lag = c.getString(c.getColumnIndex("lag"));
				author = c.getString(c.getColumnIndex("author"));
				for (int i=0;i<L.length;i++)
					L[i]=c.getString(c.getColumnIndex("L"+(i+1)));	

			}
		}
	}

	//Export all rows that have Column = Value
	public void export(String column,String key) {
		Log.d("nils","Started exportRuta");
		JSONExporter exporter = JSONExporter.getInstance(ctx);
		String col = keyColM.get(column);
		if (col==null)
			Log.e("nils","Could not find column mapping to columnHeader "+column);
		else {
			String selection = col+"= ?";
			String selectionArgs[] = {key};
			Cursor c = db.query(TABLE_VARIABLES,null,selection,
					selectionArgs,null,null,null,null);	
			if (c!=null) {
				Log.d("nils","Variables found in db for column "+column);
				//Wrap the cursor in an object that understand how to pick it!
				Report r = exporter.writeVariables(new DBColumnPicker(c));
				if (r!=null) {
					if (Tools.writeToFile(Constants.CONFIG_FILES_DIR+"json_"+column+"_"+key,r.result)) {
						Log.d("nils","Exported json file succesfully");
					} else
						Log.d("nils","Export of json file failed");
				} else
					Log.e("nils", "Got NULL back from JSONwriter!");

			} else {
				Log.e("nils","NO Variables found in db for column "+column+" with key value "+key);

			}
		}
	}


	public void printAuditVariables() {
		Cursor c = db.query(TABLE_AUDIT,null,
				null,null,null,null,null,null);
		if (c!=null) {
			Log.d("nils","Variables found in db:");
			while (c.moveToNext()) {
				Log.d("nils","ACTION: "+c.getString(c.getColumnIndex("action"))+
						"CHANGES: "+c.getString(c.getColumnIndex("changes"))+
						"TIMESTAMP: "+c.getString(c.getColumnIndex("timestamp")));

			}
		} 
		else 
			Log.e("nils","NO AUDIT VARIABLES FOUND");
		c.close();
	}


	public void printAllVariables() {

		Cursor c = db.query(TABLE_VARIABLES,null,
				null,null,null,null,null,null);
		if (c!=null) {
			Log.d("nils","Variables found in db:");
			while (c.moveToNext()) {
				Log.d("nils","VAR:"+c.getString(c.getColumnIndex("var"))+
						"VALUE:"+c.getString(c.getColumnIndex("value"))+
						"L1:"+c.getString(c.getColumnIndex("L1"))+
						"L2:"+c.getString(c.getColumnIndex("L2"))+
						"L3:"+	c.getString(c.getColumnIndex("L3"))+
						"L4:"+	c.getString(c.getColumnIndex("L4"))+
						"L5:"+	c.getString(c.getColumnIndex("L5"))+
						"L6:"+	c.getString(c.getColumnIndex("L6"))+
						"L7:"+	c.getString(c.getColumnIndex("L7"))+
						"L8:"+	c.getString(c.getColumnIndex("L8"))+
						"L9:"+	c.getString(c.getColumnIndex("L9"))+
						"L10:"+	c.getString(c.getColumnIndex("L10")));
			}
		}
		c.close();
	}


	enum ActionType {
		insert,
		delete
	}
	/*
	public void insertAudit(Variable var, ActionType a){
		//for logging
		Log.d("nils", "Audit"); 
		// 1. get reference to writable DB
		//SQLiteDatabase db = this.getWritableDatabase();

		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put("ruta", var.getRutId()); // get ruta
		values.put("provyta", var.getProvytaId()); // get provyta
		values.put("delyta", var.getDelytaId()); // get delyta
		values.put("smayta", var.getSmaytaId());
		values.put("var", var.getVarId());
		values.put("timestamp", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
		values.put("action", (a==ActionType.insert)?"i":"d");

		// 3. insert
		long rId = db.insert(TABLE_AUDIT, // table
				null, //nullColumnHack
				values); // key/value -> keys = column names/ values = column values

		// 4. close
		//db.close(); 
		var.setDatabaseId(rId);
		Log.d("nils","Inserted new variable with ID "+rId);
	} 
	 */




	public void deleteVariable(String name,Selection s) {
		// 1. get reference to writable DB
		//SQLiteDatabase db = this.getWritableDatabase();


		int aff = db.delete(TABLE_VARIABLES, //table name
				s.selection,  // selections
				s.selectionArgs); //selections args

		if(aff==0) 
			Log.e("nils","Couldn't delete "+name+" from database. Not found. Sel: "+s.selection+" Args: "+print(s.selectionArgs));
		else 
			Log.d("deleted", name);

		insertDeleteAuditEntry(s);
	}

	private void insertDeleteAuditEntry(Selection s) {
		boolean notDone = false;
		//package the value array.
		String dd = "";
		if (s.selectionArgs!=null) {
			for (int i = 0; i<s.selectionArgs.length-1;i++)
				dd+=s.selectionArgs[i]+"|";
			dd+=s.selectionArgs[s.selectionArgs.length-1];
		} else
			dd=null;
		//store
		storeAuditEntry("D",s.selection+","+dd);
		Log.d("nils",dd);
	}

	private void insertAuditEntry(ContentValues values) {

		//long aff = db.insert(TABLE_AUDIT, null, values);
		Log.d("nils","In audit insert");
		if (values!=null) {
			Log.d("nils","VALUES: "+values.toString());
			Set<Entry<String, Object>> s=values.valueSet();
			Iterator itr = s.iterator();

			Log.d("DatabaseSync", "ContentValue Length :: " +values.size());
			String res="";
			while(itr.hasNext())
			{
				Map.Entry me = (Map.Entry)itr.next(); 
				String key = me.getKey().toString();
				String value =  me.getValue().toString();
				res+=key+"="+value+(itr.hasNext()?"|":"");

			}
			Log.d("nils","STRING RESULT: "+res);
			storeAuditEntry("I",res);
		}

	} 

	private void storeAuditEntry(String action, String changes) {
		ContentValues values=new ContentValues();
		values.put("action",action);
		values.put("changes",changes);
		values.put("timestamp", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
		//need to save timestamp + value
		db.insert(TABLE_AUDIT, null, values);
	}


	public StoredVariableData getVariable(String name, Selection s) {

		Cursor c = db.query(TABLE_VARIABLES,new String[]{"value","timestamp","lag","author"},
				s.selection,s.selectionArgs,null,null,null,null);
		if (c != null && c.moveToFirst() ) {
			StoredVariableData sv = new StoredVariableData(name,c.getString(0),c.getString(1),c.getString(2),c.getString(3));

			//Log.d("nils","Found value and ts in db for "+name+" :"+sv.value+" "+sv.timeStamp);
			c.close();
			return sv;
		} 
		Log.e("nils","Variable "+name+" not found in getVariable DbHelper");
		c.close();
		return null;
	}

	public class StoredVariableData {
		public StoredVariableData(String name,String value, String timestamp,
				String lag, String author) {
			this.timeStamp=timestamp;
			this.value=value;
			this.lagId=lag;
			this.creator=author;
			this.name=name;
		}
		public String name;
		public String timeStamp;
		public String value;
		public String lagId;
		public String creator;
	}

	public final static int MAX_RESULT_ROWS = 500;
	public List<String[]> getValues(String[] columns,Selection s) {
		String dd="";
		for (int i=0;i<columns.length;i++) {
			dd+=columns[i]+",";
		}
		Log.d("nils","In getvalues with columns "+dd+", selection "+s.selection+" and selectionargs "+print(s.selectionArgs));
		//Get cached selectionArgs if exist.
		//this.printAllVariables();
		Cursor c = db.query(TABLE_VARIABLES,columns,
				s.selection,s.selectionArgs,null,null,null,null);
		if (c != null && c.moveToFirst()) {
			List<String[]> ret = new ArrayList<String[]>();
			String[] row;
			do {
				row = new String[c.getColumnCount()];
				Log.d("nils","Cursor count "+c.getCount()+" columns "+c.getColumnCount());
				boolean nullRow = true;
				for (int i=0;i<c.getColumnCount();i++) {
					Log.d("nils","Found values in db for "+columns[i]+" :"+c.getString(i));				
					if (c.getString(i)==null) {
						Log.e("nils","Null!");

					} else {
						if (c.getString(i).equalsIgnoreCase("null"))
							Log.e("nils","StringNull!");					
						row[i]=c.getString(i);
						nullRow = false;
					}

				}
				if (!nullRow) {
					Log.d("nils","found row not null");
					//only add row if one of the values is not null.
					ret.add(row);
				}
			} while (c.moveToNext());	
			return ret;
		} 
		Log.d("nils","Did NOT find value in db for "+columns.toString());
		c.close();
		return null;
	}


	public String getValue(String name, Selection s) {
		Log.d("nils","In getvalue with name "+name+" and selection "+s.selection+" and selectionargs "+print(s.selectionArgs));
		//Get cached selectionArgs if exist.
		//this.printAllVariables();
		Cursor c = db.query(TABLE_VARIABLES,new String[]{"value"},
				s.selection,s.selectionArgs,null,null,null,null);
		if (c != null && c.moveToFirst()) {
			//Log.d("nils","Cursor count "+c.getCount()+" columns "+c.getColumnCount());
			String value = c.getString(0);
			//Log.d("nils","Found value in db for "+name+" :"+value);
			c.close();
			return value;
		} 
		Log.d("nils","Did NOT find value in db for "+name);
		c.close();
		return null;
	}

	public int getId(String name, Selection s) {
		Log.d("nils","In getId with name "+name+" and selection "+s.selection+" and selectionargs "+print(s.selectionArgs));
		Cursor c = db.query(TABLE_VARIABLES,new String[]{"id"},
				s.selection,s.selectionArgs,null,null,null,null);
		if (c != null && c.moveToFirst()) {
			Log.d("nils","Cursor count "+c.getCount()+" columns "+c.getColumnCount());
			int value = c.getInt(0);
			Log.d("nils","Found id in db for "+name+" :"+value);
			c.close();
			return value;
		} 
		Log.d("nils","Did NOT find value in db for "+name);
		c.close();
		return -1;
	}


	private String print(String[] selectionArgs) {
		if (selectionArgs == null)
			return "NULL";
		String ret="";
		for(int i=0;i<selectionArgs.length;i++)
			ret+=(i+": "+selectionArgs[i]+" ");
		return ret;
	}


	//Insert or Update existing value. Synchronize tells if var should be synched over blutooth.

	public void insertVariable(Variable var,String newValue,boolean isLocal){
		PersistenceHelper ph = GlobalState.getInstance(ctx).getPersistence();
		//for logging
		//Log.d("nils", "Inserting variable "+var.getId()+" into database with value "+var.getValue()); 

		int found = getId(var.getId(),var.getSelection());
		boolean replace = (found != -1);
		// 1. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();

		//Add column,value mapping.
		Map<String,String> keyChain=var.getKeyChain();
		//If no key column mappings, skip. Variable is global with Id as key.
		if (keyChain!=null) {
			//Log.d("nils","keychain has "+keyChain.size()+" elements");
			for(String key:keyChain.keySet()) {
				String value = keyChain.get(key);
				String column = keyColM.get(key);
				values.put(column,value);
				//Log.d("nils","Adding column "+column+" with value "+value);
			}
		}  
		//Log.d("nils","Inserting global variable "+var.getId());
		values.put("var", var.getId());
		values.put("value", newValue);
		values.put("lag",ph.get(PersistenceHelper.LAG_ID_KEY));
		values.put("timestamp", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
		values.put("author", ph.get(PersistenceHelper.USER_ID_KEY));
		if (replace) 			
			values.put("id", found);

		// 3. insert
		long rId;
		if (!replace) {
			rId = db.insert(TABLE_VARIABLES, // table
					null, //nullColumnHack
					values
					); 
		} else {
			//Log.d("nils","REPLACING in INSERTX");
			rId = db.replace(TABLE_VARIABLES, // table
					null, //nullColumnHack
					values
					); 	
		}

		if (rId==-1) {
			Log.e("nils","Could not insert variable "+var.getId());
		} else {
			//If this variable is not local, store the action for synchronization.
			if (!isLocal)
				insertAuditEntry(values);
			else
				Log.d("nils","Variable "+var.getId()+" not inserted in Audit: local");
		}
	}


	long maxStamp = 0;
	public SyncEntry[] getChanges() {

		String timestamp = ph.get(PersistenceHelper.TIME_OF_LAST_CHANGE);
		if (timestamp==null||timestamp.equals(PersistenceHelper.UNDEFINED))
			timestamp = "0";
		Log.d("nils","Time of last change is "+timestamp+" in getChanges (dbHelper)");
		int cn = 0;
		Cursor c = db.query(TABLE_AUDIT,null,
				"timestamp > ?",new String[] {timestamp},null,null,"timestamp asc",null);
		if (c != null && c.getCount()>0 && c.moveToFirst()) {
			SyncEntry[] sa = new SyncEntry[c.getCount()];
			String entryStamp,action,changes;
			maxStamp=0;
			do {
				action = 	 c.getString(c.getColumnIndex("action"));
				changes =	 c.getString(c.getColumnIndex("changes"));
				entryStamp = c.getString(c.getColumnIndex("timestamp"));
				long es = Long.parseLong(entryStamp);
				if (es>maxStamp)
					maxStamp=es;
				sa[cn] = new SyncEntry(action,changes,entryStamp);
				Log.d("nils","Added sync entry : "+action+" changes: "+changes+" index: "+cn);
				cn++;				
			} while(c.moveToNext());

			return sa;

		} else 
			Log.d("nils","no sync needed...no new audit data");

		return null;
	}


	Map <Set<String>,String>cachedSelArgs = new HashMap<Set<String>,String>();

	public static class Selection {
		String[] selectionArgs=null;
		String selection=null;
	}

	public Selection createSelection(Map<String, String> keySet, String name, boolean withFuzz) {

		Selection ret = new Selection();
		//Create selection String.

		//If keyset is null, the variable is potentially global with only name as a key.
		String selection="";
		if (keySet!=null) {
			selection = cachedSelArgs.get(keySet.keySet());
			if (selection==null) {
				//Log.d("nils","found cached selArgs: "+selection);

				//Log.d("nils","selection null...creating");
				//Does not exist...need to create.
				String col;
				selection="";
				//1.find the matching column.

				for (String key:keySet.keySet()) {
					if (withFuzz)
						key = keyColM.get(key);
					selection+=key+"= ? and ";

				}
				cachedSelArgs.put(keySet.keySet(), selection);

			} else {
				Log.d("nils","Found cached selection Args: "+selection);
			}
		}
		selection+="var= ?";

		ret.selection=selection;	

		//Log.d("nils","created new selection: "+selection);

		String[] selectionArgs;
		//Create selectionArgs
		if (keySet == null) {
			selectionArgs=new String[] {name};
		} else {
			selectionArgs = new String[keySet.keySet().size()+1];
			int c=0;
			for (String key:keySet.keySet()) {			
				selectionArgs[c++]=keySet.get(key);
				//Log.d("nils","Adding selArg "+keySet.get(key)+" for key "+key);
			}
			//add name part
			selectionArgs[keySet.keySet().size()]=name;
		}
		ret.selectionArgs=selectionArgs;
		//Log.d("nils","CREATE SELECTION RETURNS: "+ret.selection+" "+print(ret.selectionArgs));
		return ret;
	}



	public Selection createCoulmnSelection(Map<String, String> keySet) {
		Selection ret = new Selection();
		//Create selection String.

		//If keyset is null, the variable is potentially global with only name as a key.
		String selection="";
		if (keySet!=null) {
			selection = cachedSelArgs.get(keySet.keySet());
			if (selection!=null) {
				Log.d("nils","found cached selArgs: "+selection);
			} else {
				//Log.d("nils","selection null...creating");
				//Does not exist...need to create.
				String col;
				selection="";
				//1.find the matching column.
				List<String>keys = new ArrayList<String>();
				keys.addAll(keySet.keySet());
				for (int i=0;i<keys.size();i++) {
					String key = keys.get(i);

					col = keyColM.get(key);
					selection+=col+"= ?"+((i < (keys.size()-1))?" and ":"");


				}

				cachedSelArgs.put(keySet.keySet(), selection);

			} 
		}
		ret.selection=selection;

		String[] selectionArgs = new String[keySet.keySet().size()];
		int c=0;
		for (String key:keySet.keySet()) 		
			selectionArgs[c++]=keySet.get(key);
		ret.selectionArgs=selectionArgs;		

		return ret;
	}



	public String getColumnName(String colId) {
		if (colId==null||colId.length()==0)
			return null;
		return keyColM.get(colId);
	}





	public void synchronise(SyncEntry[] ses) {
		String changes;
		Log.d("nils","In Synchronize with "+ses.length+" arguments");
		for (SyncEntry s:ses) {		
			changes = s.getChanges();
			if (s.isInsert()) {
				Map<String, String> keySet=null; 
				ContentValues cv = new ContentValues();
				String ch[] = changes.split("\\|");
				String[] pair;
				boolean found=false; String name = null;
				int c=0;
				for (String vPair:ch) {
					pair = vPair.split("=");
					if (pair!=null) {	
						if (pair.length==1) {
							String k = pair[0];
							pair = new String[2];
							pair[0] = k;
							pair[1] = "";
						}
						Log.d("nils","Pair "+(c++)+": Key:"+pair[0]+" Value: "+pair[1]);
						if (pair[0].equals("id")) {						
							Log.d("nils","discarding ID parameter ");
						} else {
							//build keychain.

							if (pair[0].equals("var")) {
								name = pair[1];
							} else if (!partOfValues(pair[0])) {
								Log.d("nils",pair[0]+" was not part of values");
								if (keySet == null)
									keySet = new HashMap<String, String>();
								keySet.put(pair[0],pair[1]);
							} else
								Log.d("nils",pair[0]+" was part of values!");

							//cv contains all elements, except id.
							cv.put(pair[0],pair[1]);													
						}					
					}
					else {
						Log.e("nils","Something not good in synchronize (dbHelper). A valuepair was either null or not 2 long: "+vPair);
					}

				}
				//Try find the vairable.
				if (keySet == null) 
					Log.d("nils","Keyset was null");
				Selection sel = this.createSelection(keySet, name,false);
				int id = this.getId(name, sel);
				long rId=-1;
				if (id==-1) {
					Log.d("nils","Vairable doesn't exist. Inserting..");
					//now there should be ContentValues that can be inserted.
					rId = db.insert(TABLE_VARIABLES, // table
							null, //nullColumnHack
							cv
							); 	


				} else {
					Log.d("nils","Vairable exists! Replacing..");
					cv.put("id", id);
					rId = db.replace(TABLE_VARIABLES, // table
							null, //nullColumnHack
							cv
							); 	

					if (rId!=id) 
						Log.e("nils","CRY FOUL!!! New Id not equal to found! "+" ID: "+id+" RID: "+rId);
				}
				if (rId==-1) 
					Log.e("nils","Could not insert row "+cv.toString());

				else
					Log.d("nils","Insert row: "+cv.toString());
			} else {
				//If delete.

				String selection;
				String[] pair = changes.split(",");
				if (pair==null||pair.length!=2) {
					Log.e("nils","Something wrong with Delete in Synchronize: "+changes);
				} else {
					selection = pair[0];
					String[] selectionArgs = pair[1].split("\\|");
					if (selectionArgs == null||selectionArgs.length<2) {
						Log.d("nils","something wrong with Delete selectionArgs: "+changes);
					} else {
						Selection sel = new Selection();
						sel.selection=selection;
						sel.selectionArgs=selectionArgs;
						this.deleteVariable("abrakadabra", sel);
					}
				}
			}

		}

	}

	private boolean partOfValues(String key) {
		return (MY_VALUES_SET.contains(key));		
	}

	public void syncDone() {
		if (maxStamp!=0)
			ph.put(PersistenceHelper.TIME_OF_LAST_CHANGE, Long.toString(maxStamp));
		else
			Log.d("nils","maxstamp 0");
	}


}