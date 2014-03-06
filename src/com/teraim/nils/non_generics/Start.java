package com.teraim.nils.non_generics;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.teraim.nils.FileLoadedCb;
import com.teraim.nils.GlobalState;
import com.teraim.nils.R;
import com.teraim.nils.FileLoadedCb.ErrorCode;
import com.teraim.nils.R.drawable;
import com.teraim.nils.R.id;
import com.teraim.nils.R.layout;
import com.teraim.nils.R.string;
import com.teraim.nils.dynamic.templates.TagTemplate;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Workflow;
import com.teraim.nils.log.Logger;
import com.teraim.nils.ui.DrawerMenuAdapter;
import com.teraim.nils.ui.DrawerMenuHeader;
import com.teraim.nils.ui.DrawerMenuItem;
import com.teraim.nils.ui.DrawerMenuSelectable;
import com.teraim.nils.ui.LoginConsoleFragment;
import com.teraim.nils.ui.MenuActivity;
import com.teraim.nils.utils.ConfigFileParser;
import com.teraim.nils.utils.PersistenceHelper;
import com.teraim.nils.utils.TagFileParser;
import com.teraim.nils.utils.WorkflowParser;

public class Start extends MenuActivity {

	private final String NILS_VERSION = "0.16";

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private GlobalState gs;
	private List<DrawerMenuItem> items;
	private PersistenceHelper ph;
	private DrawerMenuAdapter adapter;
	//	private Map<String,List<String>> menuStructure;
	private SparseArray<String>mapItemsToName;
	//	private ArrayList<String> rutItems;
	//	private ArrayList<String> wfItems;
	private enum State {INITIAL, TAG_LOADED,WF_LOADED, CONF_LOADED, VALIDATE,POST_INIT};
	private LoginConsoleFragment loginFragment;
	private Logger loginConsole;
	private State myState=null;




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//This is the frame for all pages, defining the Action bar and Navigation menu.
		setContentView(R.layout.naviframe);


		loginConsole = new Logger(this);

		//create subfolders. Copy assets..
		if (this.initIfFirstTime()) {		
			loginConsole.addRow("First time use...creating folders");
			loginConsole.addRow("");
			loginConsole.addYellowText("To change defaults, go to the config (wrench) menu");

		}

		//GlobalState
		gs = GlobalState.getInstance(this.getApplicationContext());
		gs.createLogger();
		//TODO:REMOVE
		ph = gs.getPersistence();

		//write down version..quickly! :)
		ph.put(PersistenceHelper.CURRENT_VERSION_OF_PROGRAM, NILS_VERSION);

		//drawer items
		items = new ArrayList<DrawerMenuItem>();

		//Maps itemheaders to items.
		//		menuStructure = new HashMap<String,List<String>>();
		//		rutItems = new ArrayList<String>();
		//		wfItems = new ArrayList<String>();
		//Maps item numbers to Fragments.
		mapItemsToName = new SparseArray<String>();

		//		menuStructure.put("Ruta och Provyta",rutItems);
		//		menuStructure.put("Delyta",wfItems);


		adapter = new DrawerMenuAdapter(this, items);



		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);


		// Set the adapter for the list view
		mDrawerList.setAdapter(adapter);
		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description */
				R.string.drawer_close  /* "close drawer" description */
				) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getActionBar().setTitle("");
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getActionBar().setTitle("");
			}
		};



		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		loginFragment = new LoginConsoleFragment();
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
		.replace(R.id.content_frame, loginFragment)
		.commit();

		//Executon continues in onStart() since we have to wait for fragment to load.

	}




	@Override
	protected void onStart() {
		super.onStart();
		this.invalidateOptionsMenu();
		if (myState != State.POST_INIT) {
			loginConsole.setOutputView(loginFragment.getTextWindow());
			loginConsole.clear();
			loginConsole.addRow("NILS VERSION ");
			loginConsole.addYellowText("["+NILS_VERSION+"]");
			loginConsole.addRow("New features: Expressions. DisplayValue Field.");


			//If network, go and check for new files.
			if (this.isNetworkAvailable()) {

				loginConsole.addRow("Loading configuration");
				loginConsole.addRow("Server URL: "+ph.get(PersistenceHelper.SERVER_URL));

				loader(State.INITIAL,null);
			} else {
				loginConsole.addRow("No network available...will use existing configuration");
				loader(State.VALIDATE,null);
			}
		}
	}

	private boolean doRefresh=false,tagSuccess=false;


	private void loader(State state,ErrorCode errCode) {

		switch (state) {

		case INITIAL:
			loginConsole.addRow(ph.get(PersistenceHelper.BUNDLE_LOCATION)+": ");
			new WorkflowParser(ph, new FileLoadedCb() {

				@Override
				public void onFileLoaded(ErrorCode errCode) {
					loader(State.WF_LOADED,errCode);
				}

			}).execute(this);
			break;

		case TAG_LOADED:
		case WF_LOADED:
		case CONF_LOADED:
			switch(errCode) {
			case ioError:
				loginConsole.addRedText("[IO-ERROR]");
				loginConsole.addRow("Couldn't load "+(state==State.WF_LOADED?"workflow":"artlista and/or varpattern") +" config.\nWrong server url? :"+ph.get(PersistenceHelper.SERVER_URL)+
						"\nWrong filename? : "+(state==State.WF_LOADED?ph.get(PersistenceHelper.BUNDLE_LOCATION):("Artlista: "+ph.get(PersistenceHelper.CONFIG_LOCATION)
								+" Varpattern: "+ph.get("varpattern.csv"))));
				break;
			case sameold:
				loginConsole.addGreenText("[Latest version(s) already installed]");
				break;
			case parseError:
				loginConsole.addRedText("[Parse Error]");
				break;
			case newConfigVersionLoaded:
				loginConsole.addGreenText("[New artlista version loaded]");
				doRefresh=true;
				break;
			case newVarPatternVersionLoaded:
				loginConsole.addGreenText("[New varpattern version loaded]");
				doRefresh=true;
				break;
			case bothFilesLoaded:
				loginConsole.addGreenText("[New artlista and varpattern version loaded]");
				doRefresh=true;
				break;
			case notFound:
				loginConsole.addRedText("[Not found]");
				loginConsole.addRow("(Existing configuration will be used if found)");
				break;
			case configurationError:
				loginConsole.addRedText("[Configuration error]");
				loginConsole.addRow("");
				loginConsole.addRedText("Please check the name of your configuration files under the Wrench menu.");
				break;
			case tagLoaded:
				loginConsole.addGreenText("[OK]");
				tagSuccess = true;
				break;

			}	
			if (state==State.WF_LOADED) {
				loginConsole.addRow(ph.get(PersistenceHelper.CONFIG_LOCATION)+": ");			
				new ConfigFileParser(ph, new FileLoadedCb() {
					@Override
					public void onFileLoaded(ErrorCode errCode) {
						Log.d("nils","Got "+errCode+" after configfileloaded");
						loader(State.CONF_LOADED,errCode);
					}
				}
						).execute(this);
			} else if(state==State.CONF_LOADED)
				loader(State.VALIDATE,ErrorCode.whatever);
			else if(state==State.TAG_LOADED) {
				String[] wfs = gs.getWorkflowNames();
				loginConsole.addRow("Found "+wfs.length+" workflows.");
				loginConsole.addRow("Start program in Drawer menu to the left.");
				gs.getLogger().addRow("Initialization done");
				gs.getLogger().addRow("************************************************");
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						mDrawerLayout.openDrawer(Gravity.LEFT);
					}}, 1000);


				//We know the workflows. We can create the menu.
				createDrawerMenu(wfs);
				adapter.notifyDataSetChanged();
				myState = State.POST_INIT;
				loginConsole.draw();
				//init current year.
				Variable v = gs.getArtLista().getVariableInstance("Current_Year");
				if (v!=null)
					v.setValue(Calendar.getInstance().get(Calendar.YEAR)+"");
				
			}
				
		break;

		case VALIDATE:
			//If a new version has been loaded and frozen, refresh global state.
			if (doRefresh) {
				loginConsole.addRow("Refreshing frozen objects with new configuration: ");
				loginConsole.addGreenText("[OK]");
				gs.refresh();
			}
			loginConsole.addRow("Validating frozen objects: ");
			GlobalState.ErrorCode ec = gs.validateFrozenObjects();
			switch (ec) {
			case file_not_found:
				loginConsole.addRedText("[Frozen object missing]");
				break;
			case workflows_not_found:
				loginConsole.addRedText("[Could not find any workflows]");
				break;
			case missing_required_column:
				loginConsole.addRedText("[A required column is missing from "+ph.get(PersistenceHelper.CONFIG_LOCATION)+"]");
				break;
			case ok:
				loginConsole.addGreenText("[OK]");	
				loginConsole.addRow("TÅG loaded: ");
				if (!ph.getB(PersistenceHelper.TAG_DATA_HAS_BEEN_READ)) {	
					loginConsole.draw();
					final Dialog dialog = new Dialog(this);
	                dialog.setContentView(R.layout.tag_load);
	                dialog.setTitle("Loading Tåg..please standby");
	                dialog.setCanceledOnTouchOutside(false);
	                dialog.show();
	                ProgressBar pb = (ProgressBar)dialog.findViewById(R.id.tag_progress_bar);
	                TextView tv = (TextView)dialog.findViewById(R.id.tag_progress_text);       	                
	                  (new TagFileParser(pb,tv,new FileLoadedCb() {
	    				@Override
	    				public void onFileLoaded(ErrorCode errCode) {
	    					dialog.dismiss();
	    					if (errCode == ErrorCode.tagLoaded)
	    						ph.put(PersistenceHelper.TAG_DATA_HAS_BEEN_READ,true);
	    					loader(State.TAG_LOADED,errCode);
	    				            
	    				}
	    				})).execute(gs);
	                 
				} else {
					loader(State.TAG_LOADED,ErrorCode.tagLoaded);
				}
				/*
				if (!success) {
					Log.e("nils","scandelningsdata failed");
					ec = GlobalState.ErrorCode.tagdata_not_found;
					loginConsole.addRedText("[Tågdata corrupt or not found]");
				} else 
					loginConsole.addGreenText("[OK]");
*/
				break;
			}
			//Get workflows
			if (ec != GlobalState.ErrorCode.ok) {
				loginConsole.addRow("");
				loginConsole.addRedText("Program cannot start because of previous errors. Please correct your configuration");					
				loginConsole.draw();
			}
			break;	
			} 

			
			
	


		}
	
	




	private void createDrawerMenu(String[] wfs) {

		final String[] mainItems = {"Välj ruta","Tåg och delytor","Ta bilder och geo"};

		items.clear();
		//Add "static" headers to menu.
		items.add(new DrawerMenuHeader("Rutor och Provyta"));
		for(int i=0;i<mainItems.length;i++)
			addItem(i+1,mainItems[i]);	
		items.add(new DrawerMenuHeader("Delyta"));
		for (int i=0;i<wfs.length;i++) 
			addItem(i+2+mainItems.length,wfs[i]);

	}




	private void addItem(int i, String s) {
		items.add(new DrawerMenuSelectable(s));
		mapItemsToName.put(i,s);
	}




	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d("nils","In oncofigChanged");
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle your other action bar items...

		return super.onOptionsItemSelected(item);
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	//


	/** Swaps fragments in the main content view */
	private void selectItem(int position) {

		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		String wfId = mapItemsToName.get(position);

		Workflow wf = gs.getWorkflow(wfId);


		// Create a new fragment and specify the  to show based on position
		Fragment fragment=null;
		if (wf!=null) 
			fragment = wf.createFragment();
		else {
			//HACK TODO: REMOVE
			if (position == 2) {
				fragment = new TagTemplate();
			}
			else
				fragment = new Fragment();
		}
		Bundle args = new Bundle();
		args.putString("workflow_name", wfId);
		fragment.setArguments(args);

		// Insert the fragment by replacing any existing fragment
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
		.replace(R.id.content_frame, fragment)
		.addToBackStack(null)
		.commit();
		mDrawerLayout.closeDrawer(mDrawerList);
		setTitle(wfId);
	}

	@Override
	public void setTitle(CharSequence title) {

		getActionBar().setTitle(title);
	}

	/******************************
	 * Network?
	 */

	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/******************************
	 * First time? If so, create subfolders.
	 */
	private boolean initIfFirstTime() {
		//If testFile doesnt exist it will be created and found next time.
		PersistenceHelper ph = new PersistenceHelper(PreferenceManager.getDefaultSharedPreferences(this));
		Log.d("Strand","Checking if this is first time use...");
		boolean first = (ph.get(PersistenceHelper.FIRST_TIME_KEY).equals(PersistenceHelper.UNDEFINED));

		
		if (first) {
			ph.put(PersistenceHelper.FIRST_TIME_KEY,"NotEmpty");
			Log.d("Strand","Yes..executing  first time init");
			initialize(ph);   
			return true;
		}
		else {
			Log.d("Strand","..Not first time");
			return false;
		}

	}

	private void initialize(PersistenceHelper ph) {
		//create data folder. This will also create the ROOT folder for the Strand app.
		File folder = new File(Constants.CONFIG_FILES_DIR);
		if(!folder.mkdirs())
			Log.e("NILS","Failed to create config root folder");
		ph.put(PersistenceHelper.CURRENT_VERSION_OF_CONFIG_FILE, PersistenceHelper.UNDEFINED);
		ph.put(PersistenceHelper.CURRENT_VERSION_OF_WF_BUNDLE, PersistenceHelper.UNDEFINED);
		//Set defaults if none.
		if (ph.get(PersistenceHelper.SERVER_URL).equals(PersistenceHelper.UNDEFINED))
			ph.put(PersistenceHelper.SERVER_URL, "www.teraim.com");
		if (ph.get(PersistenceHelper.BUNDLE_LOCATION).equals(PersistenceHelper.UNDEFINED))
			ph.put(PersistenceHelper.BUNDLE_LOCATION, "nilsbundle3.xml");
		if (ph.get(PersistenceHelper.CONFIG_LOCATION).equals(PersistenceHelper.UNDEFINED))
			ph.put(PersistenceHelper.CONFIG_LOCATION, "configv2.csv");
		ph.put(PersistenceHelper.DEVELOPER_SWITCH,true);
		ph.put(PersistenceHelper.VERSION_CONTROL_SWITCH_OFF, true);



		//copy the configuration files into the root dir.
		//copyAssets();
	}






}
