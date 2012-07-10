package com.teraim.nils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
public class Dbhelper {
	static final String KEY_ROWID="ID";
	static final String KEY_NAME="NAME";
	static final String KEY_VALUE="VALUE";
	static final String TAG = "DBAdapter";
	static final String DATABASE_TABLE = "params";
	static final String DATABASE_NAME = "nilso";
	static final int DATABASE_VERSION = 1;
	static final String DATABASE_CREATE =
			"create table params (ID integer primary key autoincrement, "
					+ "NAME text, VALUE text);";
	private static final String ASSET_DATABASE_NAME = "devnilsdb";
	
	final Context context;
	DatabaseHelper DBHelper;
	SQLiteDatabase db;
	public Dbhelper(Context ctx)
	{
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			try {
				db.execSQL(DATABASE_CREATE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS params");
			onCreate(db);
		}
	}
	//---opens the database---
	public Dbhelper open() throws SQLException
	{
		db = DBHelper.getWritableDatabase();//DBHelper.getWritableDatabase();	
		Log.d("NILS","Name of db: "+DBHelper.getDatabaseName());
		if (db!=null)
			Log.d("NILS","DB is not null");
			
		return this;
	}
	//---closes the database---
	public void close()
	{
		DBHelper.close();
	}
	//---insert a contact into the database---
	public long insertValue(String name, String VALUE)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, name);
		initialValues.put(KEY_VALUE, VALUE);
		return db.insert(DATABASE_TABLE, null, initialValues);
	}
	//---deletes a particular contact---
	public boolean deleteValue(long rowId)
	{
		return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	//---retrieves all the params---
	public Cursor getAllParams()
	{
	return db.rawQuery("select * from params",null);
	//	return db.query(DATABASE_TABLE, null, null, null, null, null, null, null);
	//	return db.query(DATABASE_TABLE, new String[] {KEY_NAME}, null, null, null, null, null);
	}
	//---retrieves a particular contact---
	public Cursor getContact(long rowId) throws SQLException
	{

		Cursor mCursor =
				db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
						KEY_NAME, KEY_VALUE}, KEY_ROWID + "=" + rowId, null,
						null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	//---updates a contact---
	public boolean updateContact(long rowId, String name, String VALUE)
	{
		ContentValues args = new ContentValues();
		args.put(KEY_NAME, name);
		args.put(KEY_VALUE, VALUE);
		return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
	public void copyDBIfNeeded() {
		try {
		String destPath = "/data/data/" + context.getPackageName() +
		"/databases";
		File dir = new File(destPath);
		File f = new File(destPath+"/"+DATABASE_NAME );
		if (!dir.exists()) {
		Log.d("NILS","did not find db in target...creating.");
		dir.mkdirs();
		}
		if (!f.exists()) {
			f.createNewFile();
			copyDB(context.getAssets().open(ASSET_DATABASE_NAME),
				new FileOutputStream(destPath+"/"+DATABASE_NAME));
		}
		//---copy the db from the assets folder into
		// the databases folder--			
		} catch (FileNotFoundException e) {
		e.printStackTrace();
		} catch (IOException e) {
		e.printStackTrace();
		}		
	}
	public void copyDB(InputStream inputStream,
			OutputStream outputStream) throws IOException {
			//---copy 1K bytes at a time---
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) > 0) {
			outputStream.write(buffer, 0, length);
			}
			inputStream.close();
			outputStream.close();
			}
}