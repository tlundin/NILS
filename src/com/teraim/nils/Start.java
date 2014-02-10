package com.teraim.nils;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.teraim.nils.FileLoadedCb.ErrorCode;
import com.teraim.nils.dynamic.Executor;
import com.teraim.nils.dynamic.types.Workflow;
import com.teraim.nils.ui.DrawerMenuAdapter;
import com.teraim.nils.ui.DrawerMenuHeader;
import com.teraim.nils.ui.DrawerMenuItem;
import com.teraim.nils.ui.DrawerMenuSelectable;
import com.teraim.nils.utils.ConfigFileParser;
import com.teraim.nils.utils.PersistenceHelper;
import com.teraim.nils.utils.WorkflowParser;

public class Start extends MenuActivity {


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
	private enum State {INITIAL, WF_LOADED, CONF_LOADED};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//This is the frame for all pages, defining the Action bar and Navigation menu.
		setContentView(R.layout.naviframe);

		//GlobalState
		gs = GlobalState.getInstance(this.getApplicationContext());
		ph = gs.getPersistence();
		//TODO: REMOVE
		ph.put(PersistenceHelper.CURRENT_RUTA_ID_KEY, "262");
		ph.put(PersistenceHelper.CURRENT_PROVYTA_ID_KEY, "6");
		ph.put(PersistenceHelper.CURRENT_DELYTA_ID_KEY, "1");


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

		loginConsole = new LoginConsoleFragment();
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
		.replace(R.id.content_frame, loginConsole)
		.commit();

		//Executon continues in onStart() since we have to wait for fragment to load.

	}

	LoginConsoleFragment loginConsole;




	@Override
	protected void onStart() {
		loginConsole.clear();
		super.onStart();

		//If network, go and check for new files.
		if (this.isNetworkAvailable()) {

			loginConsole.addRow("Loading configuration");
			loginConsole.addRow("Server URL: "+ph.get(PersistenceHelper.SERVER_URL));

			loader(State.INITIAL,null);
		}

	}

	private boolean doRefresh=false;

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


		case WF_LOADED:
		case CONF_LOADED:
			switch(errCode) {
			case ioError:
				loginConsole.addRedText("[IO-ERROR]");
				loginConsole.addRow("Couldn't load "+(state==State.WF_LOADED?"workflow":"artlista") +" config.\nWrong server url? :"+ph.get(PersistenceHelper.SERVER_URL)+
						"\nWrong filename? : "+(state==State.WF_LOADED?ph.get(PersistenceHelper.BUNDLE_LOCATION):ph.get(PersistenceHelper.CONFIG_LOCATION)));
				break;

			case sameold:
				loginConsole.addGreenText("[Latest version already installed]");
				break;
			case parseError:
				loginConsole.addRedText("[Parse Error]");
				break;
			case newVersionLoaded:
				loginConsole.addGreenText("[New version loaded]");
				doRefresh=true;
				break;
			case notFound:
				loginConsole.addRedText("[Not found]");
				loginConsole.addRow("(Existing configuration will be used if found)");
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
			} else {
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
					break;

				}
				//Get workflows
				if (ec == GlobalState.ErrorCode.ok) {

					String[] wfs = gs.getWorkflowNames();
					loginConsole.addRow("Found "+wfs.length+" workflows.");
					loginConsole.addRow("Start program in Drawer menu to the left.");
					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							mDrawerLayout.openDrawer(Gravity.LEFT);
						}}, 1000);


					//We know the workflows. We can create the menu.
					createDrawerMenu(wfs);
					adapter.notifyDataSetChanged();
				} else {
					loginConsole.addRow("");
					loginConsole.addRedText("Program cannot start because of previous errors. Please correct your configuration");					
				}

			}

			break;


		}

	}




	private void createDrawerMenu(String[] wfs) {
		
		final String[] mainItems = {"Välj ruta","Hitta yta","Ta bilder och geo"};
	
		if (items.size()>0)
			return;
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
		setTitle(wfId);
		
		Workflow wf = gs.getWorkflow(wfId);
		// Create a new fragment and specify the  to show based on position
		Fragment fragment=null;
		if (wf!=null) 
			fragment = wf.createFragment();
		else
			fragment = new Fragment();
		Bundle args = new Bundle();
		args.putString("workflow_name", mapItemsToName.get(position));
		fragment.setArguments(args);

		// Insert the fragment by replacing any existing fragment
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
		.replace(R.id.content_frame, fragment)
		.addToBackStack(null)
		.commit();
		mDrawerLayout.closeDrawer(mDrawerList);
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



}
