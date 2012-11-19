package com.teraim.nils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class ConfigMenu extends PreferenceActivity {

	 public void onCreate(Bundle savedInstanceState) {			
			super.onCreate(savedInstanceState);

			// Display the fragment as the main content.
	        getFragmentManager().beginTransaction()
	                .replace(android.R.id.content, new SettingsFragment())
	                .commit();
	 }
	 
	 
	 public static class SettingsFragment extends PreferenceFragment {
		    @Override
		    public void onCreate(Bundle savedInstanceState) {
		        super.onCreate(savedInstanceState);

		        // Load the preferences from an XML resource
		        addPreferencesFromResource(R.xml.myprefs);
		        PreferenceManager.setDefaultValues(getActivity(),
	                    R.xml.myprefs, false);
		        
		    }
	    
		}
	 
	 public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		 Toast.makeText(getApplicationContext(), "onsharedprefchange", Toast.LENGTH_SHORT).show();
		 
		    Preference pref = findPreference(key);
		    if (pref instanceof EditTextPreference) {
		        EditTextPreference etp = (EditTextPreference) pref;
		        pref.setSummary(etp.getText());
		    }
	 }


	
}
