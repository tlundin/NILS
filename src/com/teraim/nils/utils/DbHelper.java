package com.teraim.nils.utils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Variable.StorageType;
 
public class DbHelper extends SQLiteOpenHelper {
 
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "Nils";
 
 // Books table name
    private static final String TABLE_VARIABLES = "variabler";
    private static final String TABLE_AUDIT = "audit";
    //column indexes.
    private static final int COLUMN_SERIALIZED = 9;

    private final SQLiteDatabase db;
    
    Context ctx;
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
        ctx = context;
        db = this.getWritableDatabase();
        
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
    	
        // create variable table
        String CREATE_VARIABLE_TABLE = "CREATE TABLE variabler ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                "ruta TEXT, "+
                "provyta TEXT, "+
                "delyta TEXT, "+
                "smayta TEXT, "+
                "var TEXT, "+
                "value TEXT, "+
                "lag TEXT, "+
                "timestamp TEXT, "+
                "serialized TEXT, "+
                "author TEXT )";
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
    
    public ArrayList<Variable> getAllVariables() {
    	ArrayList<Variable> ret = new ArrayList<Variable>();
        //SQLiteDatabase db = this.getReadableDatabase();
        	
       	Cursor c = db.query(TABLE_VARIABLES,null,
    			null,null,null,null,null,null);
       	if (c!=null) {
       		Variable stV = null;
       		while (c.moveToNext()) {
       			Log.d("nils","Found variable "+c.getString(5)+" in database");
                String id = c.getString(0);
                stV = (Variable)Tools.deSerialize(c.getBlob(COLUMN_SERIALIZED));
                if (stV==null) {
                	Log.d("nils","Deserialize failed.");
                } else {
                	stV.setDatabaseId(Long.parseLong(id));
                	ret.add(stV);
                	//delete all
                	//deleteVariable(stV);
                }
       		}
       		
       		return ret;
       	}
       	return null;
    }
    
    
    
    //Insert or Update existing value.
    
    public void insertVariable(Variable var){
    	PersistenceHelper ph = GlobalState.getInstance(ctx).getPersistence();
        //for logging
    	Log.d("nils", "Inserting variable "+var.toString()+" into database with value "+var.getValue()); 
    	
    	// 1. get reference to writable DB
    	//SQLiteDatabase db = this.getWritableDatabase();

    	// 2. create ContentValues to add key "column"/value
    	ContentValues values = new ContentValues();
    	//Log.d("nils","value: "+var.getValue()+" id: "+var.getId());
    	if (existsInDB(var,db)) {
    		//Log.d("nils","Variable already exists in database with ID "+var.getId());
    		values.put("id", var.getId());
    	} 
    	values.put("ruta", var.getRutId()); // get ruta
    	values.put("provyta", var.getProvytaId()); // get provyta
    	values.put("delyta", var.getDelytaId()); // get delyta
    	values.put("smayta", var.getSmaytaId());
    	values.put("var", var.getVarId());
    	values.put("value", var.getValue());
    	values.put("lag",ph.get(PersistenceHelper.LAG_ID_KEY));
    	values.put("timestamp", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
    	values.put("serialized", Tools.serialize(var));
    	values.put("author", ph.get(PersistenceHelper.USER_ID_KEY));
    	
    	// 3. insert
    	long rId = db.insertWithOnConflict(TABLE_VARIABLES, // table
        null, //nullColumnHack
        values,
        SQLiteDatabase.CONFLICT_REPLACE); 
    	
    	if (rId == var.getId())
    		Log.d("nils","Updated value for "+var.getVarId()+" to "+var.getValue());
    	else
    		var.setDatabaseId(rId);  	
    	//db.close();
}
    
    private boolean existsInDB(Variable var,SQLiteDatabase db) {
		//if we know for sure already, return true.
    	if (var.existsInDB())
			return true;
    	//look for it & try again.
    	var.setDatabaseId(findRow(var,db));
    	return var.existsInDB();
 	}

	enum ActionType {
    	insert,
    	delete
    }
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
    
    	 
    
    
    public void deleteVariable(Variable var) {
        // 1. get reference to writable DB
        //SQLiteDatabase db = this.getWritableDatabase();
        long rId = -1;
        // 2. Figure out the row index
       if(!var.existsInDB())
        	rId = findRow(var,db);
       else
    	   rId = var.getId();
        if (rId == -1) 
        	Log.e("nils","Attempt to delete variable that does not exist! VAR: "+var.getVarId());
        else {
            db.delete(TABLE_VARIABLES, //table name
                    "id = ?",  // selections
                    new String[] { String.valueOf(rId) }); //selections args
     
            // 3. close
            //db.close();
            //log
            Log.d("deleted", var.getVarId());
        }
        	
     }
    
    
    
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

	public Variable getVariable(StorageType t, String rutId, String provyteId,
			String delyteId, String varId) {
	   	String selection = null;
    	String[] selectionArgs = null;
 
        SQLiteDatabase db = this.getReadableDatabase();
        //Log.e("nils","GetVar: R:"+rutId+" P:"+provyteId+" D:"+delyteId+" V:"+varId);
    	if (t==StorageType.ruta) {
    		selection = "var = ? and ruta = ?";
    		selectionArgs = new String[]{varId,rutId};
    	} else if (t==StorageType.provyta) {
    		selection = "var = ? and ruta = ? and provyta = ?";
    		selectionArgs = new String[]{varId,rutId, provyteId};
    	} else if (t==StorageType.delyta) {
    		selection = "var = ? and ruta = ? and provyta = ? and delyta = ?";
    		selectionArgs = new String[]{varId, rutId, provyteId, delyteId};
    	}	
    	
    	Cursor c = db.query(TABLE_VARIABLES,new String[]{"id","serialized","timestamp"},
    			selection,selectionArgs,null,null,null,null);
    	Variable stV=null;
        if (c != null && c.moveToFirst() ) {
            String id = c.getString(0);
            stV = (Variable)Tools.deSerialize(c.getBlob(1));
           
            if (stV==null) {
            	Log.d("nils","Deserialize failed in getVariable.");
            } else {
            	//Log.d("nils","Found variable "+stV.getVarId()+" in database with value "+stV.getValue()+" and db ID "+id);
            	
            	stV.setDatabaseId(Long.parseLong(id));
            	stV.setTimeStamp(c.getString(2));
            }
        } else 
        	;//Log.d("nils","Did NOT find variable "+varId+" in database");
        
        return stV;
	}
 
}