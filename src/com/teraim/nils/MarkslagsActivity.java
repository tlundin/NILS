package com.teraim.nils;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class MarkslagsActivity extends TabActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, MarkslagTab_Glaciar_Activity.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("arter").setIndicator("GLACIÄR/SNÖTÄCKT",
	                      null)
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, MarkslagTab_Akvatisk_Activity.class);
	    spec = tabHost.newTabSpec("terrain").setIndicator("AKVATISK",
	                      null)
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, MarkslagTab_SA_Activity.class);
	    spec = tabHost.newTabSpec("terrain").setIndicator("SA UTOM SKOG",
	                      null)
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, MarkslagTab_Anlagd_Activity.class);
	    spec = tabHost.newTabSpec("terrain").setIndicator("ANLAGD UTOM ÅKERMARK",
	                      null)
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    intent = new Intent().setClass(this, MarkslagTab_Anlagd_Activity.class);
	    spec = tabHost.newTabSpec("terrain").setIndicator("ÅKERMARK/TIDIGARE ÅKER",
	                      null)
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    intent = new Intent().setClass(this, MarkslagTab_Anlagd_Activity.class);
	    spec = tabHost.newTabSpec("terrain").setIndicator("SEMINATURLIG FODERMARK",
	                      null)
	                  .setContent(intent);
	    tabHost.addTab(spec);	
	    intent = new Intent().setClass(this, MarkslagTab_Anlagd_Activity.class);
	    spec = tabHost.newTabSpec("terrain").setIndicator("NATURMARK EJ SKOG",
	                      null)
	                  .setContent(intent);
	    tabHost.addTab(spec);	
	    intent = new Intent().setClass(this, MarkslagTab_Anlagd_Activity.class);
	    spec = tabHost.newTabSpec("terrain").setIndicator("NATURMARK MED SKOG",
	                      null)
	                  .setContent(intent);
	    tabHost.addTab(spec);	
	}
}
/*
	        // Tab for Photos
	        TabSpec photospec = tabHost.newTabSpec("Photos");
	        // setting Title and Icon for the Tab
	        photospec.setIndicator("Photos", getResources().getDrawable(R.drawable.icon_photos_tab));
	        Intent photosIntent = new Intent(this, PhotosActivity.class);
	        photospec.setContent(photosIntent);
	 
	        // Tab for Songs
	        TabSpec songspec = tabHost.newTabSpec("Songs");
	        songspec.setIndicator("Songs", getResources().getDrawable(R.drawable.icon_songs_tab));
	        Intent songsIntent = new Intent(this, SongsActivity.class);
	        songspec.setContent(songsIntent);
	 
	        // Tab for Videos
	        TabSpec videospec = tabHost.newTabSpec("Videos");
	        videospec.setIndicator("Videos", getResources().getDrawable(R.drawable.icon_videos_tab));
	        Intent videosIntent = new Intent(this, VideosActivity.class);
	        videospec.setContent(videosIntent);
	 
	        // Adding all TabSpec to TabHost
	        tabHost.addTab(photospec); // Adding photos tab
	        tabHost.addTab(songspec); // Adding songs tab
	        tabHost.addTab(videospec); // Adding videos tab
	    }
	}

*/