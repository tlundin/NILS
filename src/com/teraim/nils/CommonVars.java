package com.teraim.nils;

/**
 * @author Terje 
 * 
 * This is the Common Vars class giving access to global state stored in the Persisted memory.
 * For now, persistence implemented via SharedPreferences only.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.teraim.nils.DataTypes.Delyta;
import com.teraim.nils.DataTypes.Provyta;
import com.teraim.nils.DataTypes.Ruta;
import com.teraim.nils.DataTypes.Workflow;
import com.teraim.nils.exceptions.SharedPrefMissingException;
import com.teraim.nils.expr.Aritmetic;
import com.teraim.nils.expr.Bool;
import com.teraim.nils.expr.Literal;
import com.teraim.nils.expr.Numeric;

public class CommonVars {

	private static CommonVars singleton = null;
	
	public static PersistenceHelper ph = null;
	
	public static DbHelper db = null;
	
	//String constants
	//The root folder for the SD card is in the global Environment.
		private final static String path = Environment.getExternalStorageDirectory().getPath();
		//Remember to always add system root path before any app specific path!

	//Root for NILS
	public final static String NILS_ROOT_DIR = path+"/nils/";
	public final static String CONFIG_FILES_DIR = NILS_ROOT_DIR + "config/";
	//public static String NILS_BASE_DIR = "/nils";
	public static String UNDEFINED = "undefined";

	
	//NILS uid
	public static final UUID RED_UID = UUID.fromString("58500d27-6fd9-47c9-bf6b-d0969ce78bb3");
	public static final UUID BLUE_UID = UUID.fromString("ce8ec829-30e3-469b-886e-6cf8f1168e98");
		
	
	//Static methods
	public static String compassToPicName(int compass) {
		return (compass==0?"vast":(compass==1?"norr":(compass==2?"syd":(compass==3?"ost":null))));
	}
	
	

	//Static constants
	public final static String[] colors = {"Röd","Blå","Ofärgad"};

	public static final String TRUE = "true";
	public static final String FALSE= "false";

	public static String blue() {
		return colors[1];
	}
	public static String red() {
		return colors[0];
	}
	public static String nocolor() {
		return colors[2];
	}
	
	public UUID getmyUUID() {
		/*
		String myC = getDeviceColor();
		if (myC.equals(nocolor()))
			return null;
		else if (myC.equals(red()))
			return RED_UID;
		else
		*/
			return BLUE_UID;
	}

	public static void init(Context ctx) {
    		try {
				singleton = new CommonVars(ctx);
			} catch (SharedPrefMissingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
    		db = new DbHelper(ctx);
    		  
    	Log.d("NILS","Printing all database values in init");
    	ArrayList<StoredVariable> sds = db.getAllVariables();
    	int i=0;
    	for(StoredVariable s:sds) 
    		Log.d("nils",(i++) + ": "+s.getRutId()+","+s.getProvytaId()+","+s.getDelytaId()+","+s.getVarId()+":  "+s.getValue());
		Log.d("NILS","exit init. singleton is "+singleton);
	}
	
	public static CommonVars cv() {
		if (singleton == null) {
			Log.e("NILS","Singleton cv is null");
		}
		return singleton;			 
	}
	
	public static PersistenceHelper ph() {
		if (ph == null) {
			Log.e("NILS","Singleton ph is null");
		}
		return ph;
	}
	
	
	private int syncStatus=BluetoothRemoteDevice.SYNK_STOPPED;

	private CommonVars(Context ctx) throws SharedPrefMissingException  {
			
		SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(ctx);
    		if (sp == null)
    			throw new SharedPrefMissingException();
    		else {
    			ph = new PersistenceHelper(sp);
    		}
	}

	
	public static class PersistenceHelper {
		public static final String UNDEFINED = "";
		public static final String CURRENT_RUTA_ID_KEY = "ruta_id";
		public static final String CURRENT_PROVYTA_ID_KEY = "provyta_id";
		public static final String CURRENT_DELYTA_ID_KEY = "delyta_id";
		public static final String USER_ID_KEY = "user_id";
		public static final String LAG_ID_KEY = "lag_id";
		SharedPreferences sp;
		
		ArrayList<String> delta = new ArrayList<String>();
		
		public PersistenceHelper(SharedPreferences sp) {
			this.sp = sp;
		}
		

		public String get(String key) {
			return sp.getString(key,UNDEFINED);
		}

		public void put(String key, String value) {
			sp.edit().putString(key,value).commit();
		}
		

		/*
		public void setR(String varId, String value) {
			assert(currentRuta!=null);
			String fullId = currentRuta+"|"+varId;
			put(fullId,value);
			delta.add(fullId);
		}

		public void setP(String varId, String value) {
			assert(currentRuta!=null);
			assert(currentProvyta!=null);
			String fullId = currentRuta+"|"+currentProvyta+"|"+varId;
			put(fullId,value);
			delta.add(fullId);
		}
		public void setD(String varId, String value) {
			assert(currentRuta!=null);
			assert(currentProvyta!=null);
			assert(currentDelyta!=null);
			String fullId = currentRuta+"|"+currentProvyta+"|"+currentDelyta+varId;
			put(fullId,value);
			delta.add(fullId);
		}
		
		*/
		/*
		public StoredVariable getVar(String varId) {
			if (varId == null)
				return null;
			String[] s = varId.split("|");
			if (s==null || s.length==1) {
				Log.e("nils","This does not seem to be a Variable: "+varId);
				return null;
			}
			StoredVariable sv = new StoredVariable();
			//Ruta
			if (s.length==2) {
				sv.rutId = s[0];
				sv.value = s[1];
				sv.type = Type.ruta;
			} else if (s.length==3) {
				sv.rutId = s[0];
				sv.provytaId = s[1];
				sv.value = s[2];
				sv.type = Type.provyta;
			} else if (s.length==4) {
				sv.rutId = s[0];
				sv.provytaId = s[1];
				sv.delytaId = s[3];
				sv.value = s[4];
				sv.type = Type.delyta;
			} else {
				Log.e("nils","This Variable has too many parts: "+varId);
				return null;		
			}
			return sv;
		
		}
		*/
	}
	
	
	

	private Hashtable<String,Variable> myVars = new Hashtable<String,Variable>();

	public synchronized Bool makeBoolean(String name, String label) {
		Variable result = myVars.get(name);
		if (result == null) {
		    myVars.put(name, result = new Bool(name,label));
		    return (Bool)result;
		}
		else {
			return (Bool)result;
		}
	}
		
	public synchronized Numeric makeNumeric(String name, String label) {
		Variable result = myVars.get(name);
		if (result == null) {
		    myVars.put(name, result = new Numeric(name,label));
		    return (Numeric)result;
		}
		else {
			return (Numeric)result;
		}
	}public synchronized Aritmetic makeAritmetic(String name, String label) {
		Variable result = myVars.get(name);
		if (result == null) {
		    myVars.put(name, result = new Aritmetic(name,label));
		    return (Aritmetic)result;
		}
		else {
			return (Aritmetic)result;
		}
	}
	public synchronized Literal makeLiteral(String name, String label) {
		Variable result = myVars.get(name);
		if (result == null) {
		    myVars.put(name, result = new Literal(name,label));
		    return (Literal)result;
		}
		else {
			return (Literal)result;
		}
	}
	
	public Variable getVariable(String name) {
		return myVars.get(name);
	}
	
	
	//Enter workflows into a hash with name as key.
	private Map<String,Workflow> myWfs = new HashMap<String,Workflow>();
	
	public void setWorkflows(List<Workflow> l) {
		if (l==null)
			Log.e("NILS","Parse Error: Workflowlist is null in SetWorkFlows");
		else 
			for (Workflow wf:l)
				if (wf!=null) {
					if (wf.getName()!=null) {
						Log.d("NILS","Adding wf with id "+wf.getName()+" and length "+wf.getName().length());
						myWfs.put(wf.getName(), wf);
					} else
						Log.d("NILS","Workflow name was null in setWorkflows");
				} else
					Log.d("NILS","Workflow was null in setWorkflows");
	}
	
	public Workflow getWorkflow(String id) {
		//String o = myWfs.keySet().iterator().next();
		return myWfs.get(id);
	}
	
	public String[] getWorkflowNames() {
		if (myWfs==null)
			return null;
		String[] array = new String[myWfs.keySet().size()];
		myWfs.keySet().toArray(array);
		return array;
		 
	}
	
	
	//getter & setter for current ruta,provyta,delyta..
	Delyta myDelyta=null; Provyta myProvyta=null;
	
	
	public Ruta getCurrentRuta() {
		return DataTypes.getSingleton().findRuta(ph.get(PersistenceHelper.CURRENT_RUTA_ID_KEY));
			
	}
	
	public Provyta getCurrentProvyta() {
		Ruta r = getCurrentRuta();
		if (r!=null) 
			return r.findProvYta(ph.get(PersistenceHelper.CURRENT_PROVYTA_ID_KEY));
		else
			Log.e("nils","getCurrentprovyta returns null, since getCurrentRuta failed");
			return null;
	}
		
	public Delyta getCurrentDelyta() {
		Provyta p = getCurrentProvyta();
		if (p!=null) 
			return p.findDelyta(ph.get(PersistenceHelper.CURRENT_DELYTA_ID_KEY));
		else {
			Log.e("nils","getCurrentdelyta returns null, since getCurrentProvyta failed");
			Log.e("nils","Current provyta ID: "+ph.get(PersistenceHelper.CURRENT_PROVYTA_ID_KEY));
			return null;
		}
	}
	
	
	
	
	//Persisted variables.

	public void setDeviceColor(String color) {
		ph.put("deviceColor", color);
		
	}
	
	public String getDeviceColor() {
		return ph.get("deviceColor");		
	}

	public String getRemoteDeviceColor() {
		String myC = getDeviceColor();
		return (myC==null||myC.equals(UNDEFINED)||myC.equals(nocolor())?null:
			myC.equals(red())?blue():red());
	}
	
	
	
	public String getUserName() {
		final int MAX_NAME_LENGTH = 16;
		String un = ph.get("username");
		if (un.equals(UNDEFINED))
			un = "?";
		if (un.length()>MAX_NAME_LENGTH)
			un = un.substring(0, MAX_NAME_LENGTH);
		return un;
	}
	public String getCurrentPictureBasePath() {
		return NILS_ROOT_DIR+"/ruta/"+
		"1"+"/bilder";
	}
	
	public static void createFoldersIfMissing(File file) {
		final File parent_directory = file.getParentFile();

		if (null != parent_directory)
		{
		    parent_directory.mkdirs();
		}
	}


	public int getSyncStatus() {
		return syncStatus;
	}
	
	public String getSyncStatusS() {
		switch (syncStatus) {
		case BluetoothRemoteDevice.SYNK_STOPPED:
			return "AV";
		case BluetoothRemoteDevice.SYNK_SEARCHING:
			return "SÖKER";
		case BluetoothRemoteDevice.SYNK_RUNNING:
			return "PÅ";
		default:
			return "?";
		}
	}
	
	public void setSyncStatus(int status) {
		syncStatus = status;
	}

	
	public void sendParameter(Context ctx,String key,String value,int scope) {
		if (syncStatus == BluetoothRemoteDevice.SYNK_RUNNING)
			BluetoothRemoteDevice.getSingleton().sendParameter(key, value, scope);
		else if (syncStatus == BluetoothRemoteDevice.SYNK_STOPPED)
		{
			Intent intent = new Intent(ctx,BluetoothRemoteDevice.class);
			ctx.startService(intent);
		}
		//Otherwise ongoing sync. just wait?
			
	}
	
	 public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	            int reqWidth, int reqHeight) {

	        // First decode with inJustDecodeBounds=true to check dimensions
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeResource(res, resId, options);

	        // Calculate inSampleSize
	        options.inSampleSize = CommonVars.calculateInSampleSize(options, reqWidth, reqHeight);

	        // Decode bitmap with inSampleSize set
	        options.inJustDecodeBounds = false;
	        return BitmapFactory.decodeResource(res, resId, options);
	    }
	    

	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        // Calculate ratios of height and width to requested height and width
        final int heightRatio = Math.round((float) height / (float) reqHeight);
        final int widthRatio = Math.round((float) width / (float) reqWidth);

        // Choose the smallest ratio as inSampleSize value, this will guarantee
        // a final image with both dimensions larger than or equal to the
        // requested height and width.
        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
    }

    return inSampleSize;
}

}
