package com.teraim.nils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.teraim.nils.dynamic.types.VariableConfiguration;
import com.teraim.nils.dynamic.types.Delyta;
import com.teraim.nils.dynamic.types.Provyta;
import com.teraim.nils.dynamic.types.Ruta;
import com.teraim.nils.dynamic.types.Workflow;
import com.teraim.nils.expr.Aritmetic;
import com.teraim.nils.expr.Bool;
import com.teraim.nils.expr.Literal;
import com.teraim.nils.expr.Numeric;
import com.teraim.nils.expr.Parser;
import com.teraim.nils.utils.DbHelper;
import com.teraim.nils.utils.PersistenceHelper;
import com.teraim.nils.utils.Tools;


/**
 * 
 * @author Terje
 *
 * Classes defining datatypes for ruta, provyta, delyta and tåg.
 * There are two Scan() functions reading data from two input files (found under the /raw project folder).
 */
public class GlobalState  {

	//access only through getSingleton(Context).
	//This is because of the Activity lifecycle. This object might need to be re-instantiated any time.
	private static GlobalState singleton;

	
	private Context myC;
	private PersistenceHelper ph = null;	
	private DbHelper db = null;
	private Parser parser=null;
	private VariableConfiguration artLista=null;
	
	//Cash for variables.
	//private Hashtable<String,Variable> myVars = new Hashtable<String,Variable>();
	//Cash for ruta/Provyta/Delyta..
	private ArrayList<Ruta> rutor = new ArrayList<Ruta>();
	//Map workflows into a hash with name as key.
	private Map<String,Workflow> myWfs = new HashMap<String,Workflow>();

	//Global state for sync.
	private int syncStatus=BluetoothRemoteDevice.SYNK_STOPPED;	
	
	public enum ErrorCode {
		ok,
		missing_required_column,
		file_not_found
	}
	
	
	
	public static GlobalState getInstance(Context c) {
	   if (singleton == null) {
		   singleton = new GlobalState(c.getApplicationContext());
		   //Make sure that Initialization went fine
		   ErrorCode err = singleton.validate();
		   if (err!=ErrorCode.ok) {
			   new AlertDialog.Builder(c).setTitle("Ups!")
				.setMessage("Kan inte starta eftersom "+(err==ErrorCode.file_not_found?"artlistan saknas.":"artlistan saknar en kolumn som måste finnas."))
				.setNeutralButton("Vad synd!", new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						
					}})
					.show();
			   return null;
		   }
	   }
	   return singleton;
	   
	}

	private GlobalState(Context ctx)  {
		
		myC = ctx;
		//Database Helper
		db = new DbHelper(ctx);
		//Shared PreferenceHelper 
		SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(ctx);
		ph = new PersistenceHelper(sp);
		//Parser for rules
		parser = new Parser(this);
		//Artlista
		artLista = new VariableConfiguration(Tools.scanListConfigData("artlista_v1.config.csv"));		
		//TODO: REMOVE
		//Cached data from files.		
		Tools.scanRutData(ctx.getResources().openRawResource(R.raw.rutdata_v3),this);
		Tools.scanDelningsData(ctx.getResources().openRawResource(R.raw.delningsdata),this);	
		Tools.printDatabase(db);
	}
	
	
	/*Validation
	 * 
	 */
	private ErrorCode validate() {
		if (artLista == null)
			return ErrorCode.file_not_found;
			else
			return artLista.validateAndInit();
	}
	
	/*Singletons available for all classes
	 * 
	 */
	public PersistenceHelper getPersistence() {
		return ph;
	}

	public DbHelper getDb() {
		return db;
	}
	
	public Parser getParser() {
		return parser;
	}
	
	public Context getContext() {
		return myC;
	}
	
	public ArrayList<Ruta> getRutor() {
		return rutor;
	}
	
	public VariableConfiguration getArtLista() {
		return artLista;
	}

	/**************************************************
	 * 
	 * Getter & Setter for Workflow.
	 */
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
	
	
	

	/*************************************************
	 * 
	 * Getter/Setter for Ruta/Provyta/Delyta
	 * 
	 */
	
	public String getCurrentPictureBasePath() {
		return Constants.NILS_ROOT_DIR+"/ruta/"+
		getCurrentRuta().getId()+"/bilder";
	} 
	
	public Ruta getCurrentRuta() {
		return findRuta(ph.get(PersistenceHelper.CURRENT_RUTA_ID_KEY));
			
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
	
	public Ruta findRuta(String id) {
		//Log.d("nils","Findruta called with ID> "+id);
		if (id == null) {
			return null;
		}
		for (Ruta r:rutor) 
			if (r.getId().equals(id))
				return r;
		return null;
	}

	public String[] getRutIds() {
		if (rutor != null) {
			String[] contents = new String[rutor.size()];		
			int i=0;
			for (Ruta r:rutor)
				contents[i++]=r.getId();
			return contents;
		}
		return null;
	}



	public ArrayList<Delyta> getDelytor(String rutId, String provyteId) {
		Ruta r = findRuta(rutId);
		if (r!=null) {
			Log.d("NILS","found ruta "+ rutId);
			Provyta p = r.findProvYta(provyteId);
			if (p!=null) {
				Log.d("NILS","Found provyta"+ provyteId);			
				return (p.getDelytor());
			} else {
				Log.e("NILS","DID NOT FIND Provyta for id "+provyteId);
				//TODO: Files must contains same provytor!
				//Fix for now: Generate default if missing.
				//p.addDelyta("1", null);
				//r.addProvYta(provyteId, "1", null);
				//return getDelytor(rutId,provyteId);
			}
		} else
			Log.e("NILS","DID NOT FIND RUTA "+ rutId);
		return null;
	}
	
	
	/**************************************
	 * Getter/Setter for sync status and Globally accessible method for sending data asynchronously to twin device.
	 */
	
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
	
	
	public synchronized Aritmetic makeAritmetic(String name, String label) {
		/*Variable result = myVars.get(name);
		if (result == null) {
		    myVars.put(name, result = new Aritmetic(name,label));
		    return (Aritmetic)result;
		}
		else {
			return (Aritmetic)result;
		}
		*/
		return new Aritmetic(name,label);
	}
	
	/*************************************
	 * 
	 * Variable Generators.
	 * 
	 */
	/*
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
	*/
	
	
/********************************************************
 * Color
 */

	public void setDeviceColor(String color) {
		ph.put("deviceColor", color);
		
	}
	
	public String getDeviceColor() {
		return ph.get(PersistenceHelper.DEVICE_COLOR_KEY);		
	}
	public String getRemoteDeviceColor() {
		String myC = getDeviceColor();
		return (myC==null||myC.equals(Constants.UNDEFINED)||myC.equals(Constants.nocolor())?null:
			myC.equals(Constants.red())?Constants.blue():Constants.red());
	}


	

	
	







}




