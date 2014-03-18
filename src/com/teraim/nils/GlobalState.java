package com.teraim.nils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.teraim.nils.bluetooth.BluetoothConnectionService;
import com.teraim.nils.bluetooth.MasterMessageHandler;
import com.teraim.nils.bluetooth.MessageHandler;
import com.teraim.nils.bluetooth.SlaveMessageHandler;
import com.teraim.nils.bluetooth.SyncEntry;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.Provyta;
import com.teraim.nils.dynamic.types.Ruta;
import com.teraim.nils.dynamic.types.SpinnerDefinition;
import com.teraim.nils.dynamic.types.Table;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Workflow;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.expr.Aritmetic;
import com.teraim.nils.expr.Parser;
import com.teraim.nils.log.DummyLogger;
import com.teraim.nils.log.Logger;
import com.teraim.nils.log.LoggerI;
import com.teraim.nils.non_generics.Constants;
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
	private LoggerI log;
	private PersistenceHelper ph = null;	
	private DbHelper db = null;
	private Parser parser=null;
	private VariableConfiguration artLista=null;
	//Map workflows into a hash with name as key.
	private Map<String,Workflow> myWfs; 
	//Spinner definitions
	private SpinnerDefinition mySpinnerDef;
	//Cash for ruta/Provyta/Delyta..
	private ArrayList<Ruta> rutor = new ArrayList<Ruta>();
	private MessageHandler myHandler;

	//Global state for sync.
	private int syncStatus=BluetoothConnectionService.SYNK_STOPPED;	



	public String TEXT_LARGE;


	private WF_Context currentContext;


	private ParameterSafe mySafe;

	public static GlobalState getInstance(Context c) {
		if (singleton == null) {			
			singleton = new GlobalState(c.getApplicationContext());
		}
		return singleton;

	}

	private GlobalState(Context ctx)  {

		myC = ctx;
		//Shared PreferenceHelper 
		SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(ctx);
		ph = new PersistenceHelper(sp);
		//Logger. Note that logger must be initialized with a TextView when used! 
		if (ph.getB(PersistenceHelper.DEVELOPER_SWITCH))
			log = new Logger(this.getContext());
		else
			removeLogger();
		//Parser for rules
		parser = new Parser(this);
		//Artlista

		artLista = new VariableConfiguration(this);	

		//Database Helper
		db = new DbHelper(ctx,artLista.getTable(),ph);
		myWfs = thawWorkflows();		

		db.printAuditVariables();
		Tools.scanRutData(ctx.getResources().openRawResource(R.raw.rutdata_v3),this);

		//Event Handler on the Bluetooth interface.
		myHandler = isMaster()?new MasterMessageHandler(this):new SlaveMessageHandler(this);
		
		//Get ParameterSafe.
		mySafe = (ParameterSafe) Tools.readObjectFromFile(myC, Constants.CONFIG_FILES_DIR+"mysafe");

	}


	/*Validation
	 * 
	 */
	public ErrorCode validateFrozenObjects() {
		if (artLista == null||myWfs==null)
			return ErrorCode.file_not_found;
		else {
			ErrorCode artL = artLista.validateAndInit();
			if (artL != ErrorCode.ok)
				return artL;
			else
				if(myWfs.size()==0)
					return ErrorCode.workflows_not_found;
				else
					return ErrorCode.ok;
		}
	}

	
	/*Singletons available for all classes
	 * 
	 */
	public SpinnerDefinition getSpinnerDefinitions() {
		return mySpinnerDef;
	}

	public void setSpinnerDefinitions(SpinnerDefinition sd) {
		if (sd!=null)
			Log.d("nils","SetSpinnerDef called with "+sd.size()+" spinners");
		else 
			Log.e("nils","Spinnerdef null!!!");
		mySpinnerDef=sd;
	}

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
	
	public ParameterSafe getSafe() {
		if (mySafe!=null)
			return mySafe;
		else
			return new ParameterSafe();
	}

	/**************************************************
	 * 
	 * Thawing of files to objects.
	 */

	public Map<String,Workflow> thawWorkflows() {
		Map<String,Workflow> ret = new HashMap<String,Workflow>();
		List<Workflow> l = ((ArrayList<Workflow>)Tools.readObjectFromFile(myC,Constants.CONFIG_FILES_DIR+Constants.WF_FROZEN_FILE_ID));		
		if (l==null) 
			Log.e("NILS","Parse Error: Workflowlist is null in SetWorkFlows");
		else {
			for (Workflow wf:l)
				if (wf!=null) {
					if (wf.getName()!=null) {
						Log.d("NILS","Adding wf with id "+wf.getName()+" and length "+wf.getName().length());
						ret.put(wf.getName(), wf);
					} else
						Log.d("NILS","Workflow name was null in setWorkflows");
				} else
					Log.d("NILS","Workflow was null in setWorkflows");
		}
		return ret;
	}

	public Table thawTable() { 	
		return ((Table)Tools.readObjectFromFile(myC,Constants.CONFIG_FILES_DIR+Constants.CONFIG_FROZEN_FILE_ID));		
	}

	public Workflow getWorkflow(String id) {
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
		Variable v = artLista.getVariableInstance("Current_Ruta");
		String va=null;
		if (v!=null)
			va = v.getValue();
		if (va!=null)
			return findRuta(va);
		else
			return null;
	}

	public Provyta getCurrentProvyta() {
		Variable v = artLista.getVariableInstance("Current_Provyta");
		String va=null;
		if (v!=null)
			va = v.getValue();
		if (va!=null) {
			Ruta r = getCurrentRuta();
			if (r!=null) 
				return r.findProvYta(va);
			else
				Log.e("nils","getCurrentprovyta returns null, since getCurrentRuta failed");
		}
		return null;
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






	/**************************************
	 * Getter/Setter for sync status and Globally accessible method for sending data asynchronously to twin device.
	 */

	public int getSyncStatus() {
		return syncStatus;
	}

	public String getSyncStatusS() {
		switch (syncStatus) {
		case BluetoothConnectionService.SYNK_STOPPED:
			return "AV";
		case BluetoothConnectionService.SYNK_SEARCHING:
			return "SÖKER";
		case BluetoothConnectionService.SYNC_READY_TO_ROCK:
			return "PÅ";
		case BluetoothConnectionService.SYNC_RUNNING:
			return "SYNKAR";
		default:
			return "?";
		}
	}

	public void setSyncStatus(int status) {
		syncStatus = status;
	}

	public void sendMessage(Object message) {

		if (syncStatus == BluetoothConnectionService.SYNK_STOPPED)	{
			Log.d("nils","Sync fail in sendmessage! BT is stopped");
			Toast.makeText(getContext(), "Failed to send message - no connection", Toast.LENGTH_SHORT).show();		
		}
		else {
			Log.d("nils","Message is being sent now..");
			BluetoothConnectionService.getSingleton().send(message);
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

	public String getDeviceType() {
		return ph.get(PersistenceHelper.DEVICE_COLOR_KEY);		
	}

	//IF new configuration files have been loaded, replace existing instances.
	public void refresh() {
		artLista = new VariableConfiguration(this);	
		myWfs = thawWorkflows();	
		if (artLista.getTable()!=null)
			db.init(artLista.getTable().getKeyParts());
		else {
			log.addRow("");
			log.addRedText("Refresh failed - Table is missing. This is likely due to previous errors on startup");
		}
	}


	public LoggerI getLogger() {
		return log;
	}

	public void setCurrentContext(WF_Context myContext) {
		currentContext = myContext;
	}

	public WF_Context getCurrentContext() {
		return currentContext;
	}

	public void createLogger() {
		log = new Logger(this.getContext());
	}

	public void removeLogger() {
		log = new DummyLogger();
	}

	Map<String,String> myKeyHash;


	public Map<String,String> getCurrentKeyHash() {
		return myKeyHash;
	}


	public void  setKeyHash(Map<String,String> h) { 
		artLista.destroyCache();
		myKeyHash=h;
	}

	public String getYear() {
		return Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
	}

	public boolean isMaster() {
		String m;
		if ((m = ph.get(PersistenceHelper.DEVICE_COLOR_KEY)).equals(PersistenceHelper.UNDEFINED)) {
			ph.put(PersistenceHelper.DEVICE_COLOR_KEY, "Mästare");
			return true;
		}
		else
			return m.equals("Mästare");

	}

	public MessageHandler getHandler() {
		return myHandler;
	}

	public void setMaster(boolean master) {
		if (master)
			myHandler = new MasterMessageHandler(this);
		else
			myHandler = new SlaveMessageHandler(this);
	}

	public enum ErrorCode {
		ok,
		missing_required_column,
		file_not_found, workflows_not_found,
		tagdata_not_found,parse_error,
		missing_lag_id,
		missing_user_id,
		current_ruta_not_set,
		current_provyta_not_set,
		no_handler_available

	}

	public boolean syncIsActive() {
		return (syncStatus == BluetoothConnectionService.SYNC_READY_TO_ROCK);
	}

	public ErrorCode syncIsAllowed() {
		if (ph.get(PersistenceHelper.LAG_ID_KEY).equals(PersistenceHelper.UNDEFINED))
			return ErrorCode.missing_lag_id;
		else if (ph.get(PersistenceHelper.USER_ID_KEY).equals(PersistenceHelper.UNDEFINED))
			return ErrorCode.missing_user_id;
		else if (myHandler ==null)
			return ErrorCode.no_handler_available;
		else if (isMaster()&&getArtLista().getVariableValue(null, "Current_Ruta")==null)
			return ErrorCode.current_ruta_not_set;
		else if (isMaster()&&getArtLista().getVariableValue(null, "Current_Provyta")==null)
			return ErrorCode.current_provyta_not_set;
		else 
			return ErrorCode.ok;
	}

	public void triggerTransfer() {
		if (syncIsActive()&&syncIsAllowed()==ErrorCode.ok) {
			Log.d("nils","Doing da sync..");
			setSyncStatus(BluetoothConnectionService.SYNC_RUNNING);
			sendEvent(BluetoothConnectionService.SYNK_INITIATE);
			SyncEntry[] changes = db.getChanges();
			Log.d("nils","Syncrequest received. Sending "+(changes==null?"no changes":changes.toString()));
			if (changes==null)
				log.addRow("[SENDING_SYNC-->0 rows]");
			else
				log.addRow("[SENDING_SYNC-->"+changes.length+" rows]");
			if (changes == null) 
				changes = new SyncEntry[]{};
			sendMessage(changes);
		} else 
			Log.d("nils","Sync not allowed");
	}

	public void sendEvent(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		getContext().sendBroadcast(intent);
	}






}




