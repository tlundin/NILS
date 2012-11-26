package com.teraim.nils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class ConfigMenu extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	 public void onCreate(Bundle savedInstanceState) {			
			super.onCreate(savedInstanceState);

			// Display the fragment as the main content.
	        getFragmentManager().beginTransaction()
	                .replace(android.R.id.content, new SettingsFragment())
	                .commit();
	        
	 }
	 
	 
	 public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
		    @Override
		    public void onCreate(Bundle savedInstanceState) {
		        super.onCreate(savedInstanceState);

		        // Load the preferences from an XML resource
		        addPreferencesFromResource(R.xml.myprefs);
		        //Set default values for the prefs.
		        getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);

		        
		    }

			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				 Toast.makeText(this.getActivity().getApplicationContext(), "sdfdsfprefchange", Toast.LENGTH_SHORT).show();

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
