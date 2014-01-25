package com.teraim.nils;

import java.util.concurrent.TimeUnit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.teraim.nils.StoredVariable.Type;
import com.teraim.nils.utils.Tools;
 
public class DbHelper extends SQLiteOpenHelper {
 
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "Nils";
 
 // Books table name
    private static final String TABLE_VARIABLES = "variabler";
    private static final String TABLE_AUDIT = "audit";

   
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
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
    
    
    public void insertVariable(StoredVariable var){
        //for logging
    	Log.d("nils", var.toString()); 

    	// 1. get reference to writable DB
    	SQLiteDatabase db = this.getWritableDatabase();

    	// 2. create ContentValues to add key "column"/value
    	ContentValues values = new ContentValues();
    	values.put("ruta", var.getRutId()); // get ruta
    	values.put("provyta", var.getProvytaId()); // get provyta
    	values.put("delyta", var.getDelytaId()); // get delyta
    	values.put("smayta", var.getSmaytaId());
    	values.put("var", var.getVarId());
    	values.put("value", var.getValue());
    	values.put("lag",var.getLag());
    	values.put("timestamp", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
    	values.put("serialized", Tools.serialize(var));
    	values.put("author", CommonVars.ph.get("username"));

    	// 3. insert
    	db.insert(TABLE_VARIABLES, // table
        null, //nullColumnHack
        values); // key/value -> keys = column names/ values = column values

    	// 4. close
    	db.close(); 
}
    
    enum ActionType {
    	insert,
    	delete
    }
    public void insertAudit(StoredVariable var, ActionType a){
        //for logging
    	Log.d("nils", "Audit"); 
    	// 1. get reference to writable DB
    	SQLiteDatabase db = this.getWritableDatabase();

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
    	db.close(); 
    	var.setId(rId);
    	Log.d("nils","Inserted new variable with ID "+rId);
    } 
    
    	 
    
    
    public void deleteVariable(StoredVariable var) {
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
 
        // 2. Figure out the row index
        long rId = var.getId();
        if (rId==-1)
        	rId = findRow(var,db);
        if (rId == -1) 
        	Log.e("nils","Attempt to delete variable that does not exist! VAR: "+var.getVarId());
        else {
            db.delete(TABLE_VARIABLES, //table name
                    "id = ?,",  // selections
                    new String[] { String.valueOf(rId) }); //selections args
     
            // 3. close
            db.close();
            //log
            Log.d("deleted", var.getVarId());
        }
        	
     }
    
    public StoredVariable getRutVariable(String ruta, String varId) {
    	
    }
    public StoredVariable getProvyteVariable(String ruta, String varId) {
    	
    }
    public StoredVariable getDelyteVariable(String ruta, String varId) {
    	
    }
    
    private long findRow(StoredVariable var, SQLiteDatabase db) {
    	String selection = null;
    	String[] selectionArgs = null;
    	
    	if (var.getType()==Type.ruta) {
    		selection = "ruta = ?";
    		selectionArgs = new String[]{var.getRutId()};
    	} else if (var.getType()==Type.provyta) {
    		selection = "ruta = ? and provyta = ?";
    		selectionArgs = new String[]{var.getRutId(), var.getProvytaId()};
    	} else if (var.getType()==Type.delyta) {
    		selection = "ruta = ? and provyta = ? and delyta = ?";
    		selectionArgs = new String[]{var.getRutId(), var.getProvytaId(), var.getDelytaId()};
    	}
    	
    	
    	Cursor c = db.query(TABLE_VARIABLES,new String[]{"id"},
    			selection,selectionArgs,null,null,null,null);
    	
    	 // 3. if we got results get the first one
        if (c != null) {
            c.moveToFirst();
            String id = c.getString(0);
            if (id!=null) {
            	return Long.parseLong(id);
            } else
            	return -1;
        }
        else
        	return -1;
        	
    }
 
}