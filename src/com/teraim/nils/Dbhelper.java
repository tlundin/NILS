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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.widget.ImageView;
public class Dbhelper {
	static final String TAG = "DBAdapter";
	static final String DATABASE_NAME = "nilsdb";
	static final String ASSET_DATABASE_NAME = "sqlver";
	static final String VAULEPAIR_TABLE = "params";


	static final int DATABASE_VERSION = 1;
	private static final String PIC_TABLE = "PICTURES";
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
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which is not implemented yet");
			//db.execSQL("DROP TABLE IF EXISTS params");
			onCreate(db);
		}
		@Override
		public void onCreate(SQLiteDatabase arg0) {
			Log.e(TAG, "database is missing");
		}
	}
	//---opens the database---
	public Dbhelper open() throws SQLException
	{
		db = DBHelper.getWritableDatabase();//DBHelper.getWritableDatabase();	
		Log.d("NILS","Name of db: "+DBHelper.getDatabaseName());		
		return this;
	}
	//---closes the database---
	public void close()
	{
		DBHelper.close();
	}
	//---insert a contact into the database---
	public long insertPicture(ContentValues filedata) {
		return db.insert(PIC_TABLE, null, filedata);
	}
	//---deletes a particular contact---
	public boolean deleteValue(long rowId)
	{
		return true;
		//db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	//---retrieves all the params---
	public Cursor getAllParams()
	{
		return db.rawQuery("select rowid _id, * from params",null);
		//	return db.query(DATABASE_TABLE, null, null, null, null, null, null, null);
		//	return db.query(DATABASE_TABLE, new String[] {KEY_NAME, KEY_VALUE}, null, null, null, null, null);
	}

	public ImageView getImage(String direction, int delyta) {

		String selArgs[] = {Integer.toString(delyta)};
		Cursor cursor = db.rawQuery("select '"+direction+"' from "+PIC_TABLE+" where yta=?",selArgs);
		//get the blob with the picdata from the returned column.
		ImageView ret = null;
		if(cursor.moveToFirst()) {
			Log.d("NILS","col name: "+cursor.getColumnName(0));
			Log.d("NILS","col_index: "+cursor.getColumnIndex(cursor.getColumnName(0)));
			ret = new ImageView(context);
			byte[] imageByteArray=cursor.getBlob(cursor.getColumnIndex(cursor.getColumnName(0)));      
			cursor.close();
			Bitmap bimp = BitmapFactory.decodeByteArray( imageByteArray, 
					0,imageByteArray.length);
			assert(bimp!=null);
			
			ret.setImageBitmap(bimp);
			
		} else
			Log.e("NILS","no image found!!");
		
		return ret;

	}



	@SuppressWarnings("deprecation")
	public SimpleCursorAdapter getAdapter() {
		Cursor c = getAllParams();

		return new SimpleCursorAdapter(
				context, 
				android.R.layout.two_line_list_item, 
				c, 
				new String[] {"NAME", "VALUE"},
				new int[] {android.R.id.text1, android.R.id.text2});
	}

	//---retrieves a particular contact---
	/*public Cursor getContact(long rowId) throws SQLException
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
	 */
	//---updates a contact---
	/*
	public boolean updateContact(long rowId, String name, String VALUE)
	{
		ContentValues args = new ContentValues();
		args.put(KEY_NAME, name);
		args.put(KEY_VALUE, VALUE);
		return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
	 */

	public void selectAllTables() {
		Cursor mCursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table'",null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		do {

		} while (mCursor.moveToNext());

	}

	public void copyDBIfNeeded() {
		try {
			String destPath = "/data/data/" + context.getPackageName() +
					"/databases";
			File dir = new File(destPath);
			File f = new File(destPath+"/"+DATABASE_NAME );
			//if (!dir.exists()) {
			if (true) {
				Log.d("NILS","did not find target folder...creating.");
				dir.mkdirs();
			}
			//if (!f.exists()) {
			if (true) {
				Log.d("NILS","did not find db in target...creating.");
				f.createNewFile();
				copyDB(context.getAssets().open(ASSET_DATABASE_NAME),
						new FileOutputStream(destPath+"/"+DATABASE_NAME));
			} 
			Log.d("NILS","PATH:"+f.getPath());
			Log.d("NILS","LENGTH:"+f.length());

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