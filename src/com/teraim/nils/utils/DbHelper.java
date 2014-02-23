package com.teraim.nils.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.types.Table;
import com.teraim.nils.dynamic.types.Variable;

public class DbHelper extends SQLiteOpenHelper {

	// Database Version
	private static final int DATABASE_VERSION = 2;
	// Database Name
	private static final String DATABASE_NAME = "Nils";

	// Books table name
	private static final String TABLE_VARIABLES = "variabler";
	private static final String TABLE_AUDIT = "audit";

	private static final int NO_OF_KEYS = 10;
	private final SQLiteDatabase db;
	private final PersistenceHelper ph;

	private final Map<String,String> keyColM = new HashMap<String,String>();
	Context ctx;
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
	
	public void speziale() {
		//TODO: REMOVE
		//Insert values for current.
		
		Variable v;
		v=GlobalState.getInstance(ctx).getArtLista().getVariableInstance("current_ruta");
		this.insertVariable(v, "3");
		v=GlobalState.getInstance(ctx).getArtLista().getVariableInstance("current_provyta");
		this.insertVariable(v, "2");
		v=GlobalState.getInstance(ctx).getArtLista().getVariableInstance("current_delyta");
		this.insertVariable(v, "2");

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
				"id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				"ruta TEXT, "+
				"provyta TEXT, "+
				"delyta TEXT, "+
				"var TEXT, "+
				"timestamp TEXT, "+
				"action TEXT )";

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


	//Create update delete

	public void printAllVariables() {
		ArrayList<Variable> ret = new ArrayList<Variable>();
		//SQLiteDatabase db = this.getReadableDatabase();

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
			Log.e("nils","Couldn't delete "+name+" from database. Not found. Sel: "+s.selection+" Args: "+s.selectionArgs.toString());
		else 
			Log.d("deleted", name);
	}




	/*
	private long findRow(Variable var,SQLiteDatabase db) {
		String selection = null;
		String[] selectionArgs = null;

		if (var.getType()==StorageType.ruta) {
			selection = "var = ? and ruta = ?";
			selectionArgs = new String[]{var.getVarId(),var.getRutId()};
		} else if (var.getType()==StorageType.provyta) {
			selection = "var = ? and ruta = ? and provyta = ?";
			selectionArgs = new String[]{var.getVarId(),var.getRutId(), var.getProvytaId()};
		} else if (var.getType()==StorageType.delyta) {
			selection = "var = ? and ruta = ? and provyta = ? and delyta = ?";
			selectionArgs = new String[]{var.getVarId(), var.getRutId(), var.getProvytaId(), var.getDelytaId()};
		}


		Cursor c = db.query(TABLE_VARIABLES,new String[]{"id"},
				selection,selectionArgs,null,null,null,null);
		// 3. if we got results get the first one
		long id = -1;
		if (c != null && c.moveToFirst()) {
			String sid = c.getString(0);
			//Log.d("nils","DBHelper: found variable on row with Id "+sid);
			id = Long.parseLong(sid);

		}
		else 
			;//Log.e("nils","DBHelper: Did not find variable on row with name "+var.getVarId());
		c.close();
		return id;
	}
	 */

	public StoredVariableData getVariable(String name, Selection s) {

		Cursor c = db.query(TABLE_VARIABLES,new String[]{"value","timestamp","lag","author"},
				s.selection,s.selectionArgs,null,null,null,null);
		if (c != null && c.moveToFirst() ) {
			StoredVariableData sv = new StoredVariableData(c.getString(0),c.getString(1),c.getString(2),c.getString(3));

			Log.d("nils","Found value and ts in db for "+name+" :"+sv.value+" "+sv.timeStamp);
			c.close();
			return sv;
		} 
		Log.e("nils","Variable "+name+" not found in getVariable DbHelper");
		c.close();
		return null;
	}

	public class StoredVariableData {
		public StoredVariableData(String value, String timestamp,
				String lag, String author) {
			this.timeStamp=timestamp;
			this.value=value;
			this.lagId=lag;
			this.creator=author;
		}

		public String timeStamp;
		public String value;
		public String lagId;
		public String creator;
	}

	public String getValue(String name, Selection s) {
		Log.d("nils","In getvalue with name "+name+" and selection "+s.selection+" and selectionargs "+print(s.selectionArgs));
		//Get cached selectionArgs if exist.
		//this.printAllVariables();
		Cursor c = db.query(TABLE_VARIABLES,new String[]{"value"},
				s.selection,s.selectionArgs,null,null,null,null);
		if (c != null && c.moveToFirst()) {
			Log.d("nils","Cursor count "+c.getCount()+" columns "+c.getColumnCount());
			String value = c.getString(0);
			Log.d("nils","Found value in db for "+name+" :"+value);
			c.close();
			return value;
		} 
		Log.d("nils","Did NOT find value in db for "+name);
		c.close();
		return null;
	}
	
	public int getId(String name, Selection s) {
		Log.d("nils","In getvalue with name "+name+" and selection "+s.selection+" and selectionargs "+print(s.selectionArgs));
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
			ret+=(i+": "+selectionArgs[i]);
		return ret;
	}


	//Insert or Update existing value.

	public void insertVariable(Variable var,String newValue){
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
		} else 
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
		}
	}



	Map <Set<String>,String>cachedSelArgs = new HashMap<Set<String>,String>();

	public class Selection {
		String[] selectionArgs;
		String selection;
	}

	public Selection createSelection(Map<String, String> keySet, String name) {

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
				
				for (String key:keySet.keySet()) {
					col = keyColM.get(key);
					selection+=col+"= ? and ";
					
				}
				cachedSelArgs.put(keySet.keySet(), selection);

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



}