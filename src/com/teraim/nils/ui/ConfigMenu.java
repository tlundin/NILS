package com.teraim.nils.ui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.teraim.nils.GlobalState;
import com.teraim.nils.R;
import com.teraim.nils.utils.PersistenceHelper;

public class ConfigMenu extends PreferenceActivity {

	public void onCreate(Bundle savedInstanceState) {			
		super.onCreate(savedInstanceState);
			
		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
		.replace(android.R.id.content, new SettingsFragment())
		.commit();
		setTitle("Ändra inställningar");
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
		
			EditTextPreference epref = (EditTextPreference) findPreference(PersistenceHelper.LAG_ID_KEY);
			epref.setSummary(epref.getText());

			ListPreference color = (ListPreference)findPreference("deviceColor");
			color.setSummary(color.getValue());

			epref = (EditTextPreference) findPreference(PersistenceHelper.USER_ID_KEY);
			epref.setSummary(epref.getText());
			

			epref = (EditTextPreference) findPreference(PersistenceHelper.SERVER_URL);
			epref.setSummary(epref.getText());
			
			
			epref = (EditTextPreference) findPreference(PersistenceHelper.BUNDLE_LOCATION);
			epref.setSummary(epref.getText());
			
			epref = (EditTextPreference) findPreference(PersistenceHelper.CONFIG_LOCATION);
			epref.setSummary(epref.getText());

			//CheckBoxPreference cpref = (CheckBoxPreference) findPreference(PersistenceHelper.DEVELOPER_SWITCH);
			
		}
		
	
		

		/* (non-Javadoc)
		 * @see android.app.Fragment#onPause()
		 */
		@Override
		public void onPause() {
			getPreferenceScreen().getSharedPreferences()
			.unregisterOnSharedPreferenceChangeListener(this);
			super.onPause();
		}




		/* (non-Javadoc)
		 * @see android.app.Fragment#onResume()
		 */
		@Override
		public void onResume() {
			getPreferenceScreen().getSharedPreferences()
			.registerOnSharedPreferenceChangeListener(this);
			super.onResume();
		}




		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			Preference pref = findPreference(key);
			if (pref instanceof EditTextPreference) {
				EditTextPreference etp = (EditTextPreference) pref;
				pref.setSummary(etp.getText());
				if (key.equals(PersistenceHelper.BUNDLE_LOCATION)) {
					Log.d("nils","Bundle file changed. Removing version check");
					GlobalState.getInstance(this.getActivity()).getPersistence().put(PersistenceHelper.CURRENT_VERSION_OF_WF_BUNDLE, PersistenceHelper.UNDEFINED);
					
				} else
					if (key.equals(PersistenceHelper.CONFIG_LOCATION)) {
						Log.d("nils","Bundle file changed. Removing version check");
						GlobalState.getInstance(this.getActivity()).getPersistence().put(PersistenceHelper.CURRENT_VERSION_OF_CONFIG_FILE, PersistenceHelper.UNDEFINED);					
					}

					
			}
			else if (pref instanceof ListPreference) {
				ListPreference letp = (ListPreference) pref;
				pref.setSummary(letp.getValue());
				
			}
			else if (pref instanceof CheckBoxPreference) {
				CheckBoxPreference cpref = (CheckBoxPreference)pref;
				if (key.equals(PersistenceHelper.DEVELOPER_SWITCH))
					if (cpref.isChecked()) {
						GlobalState.getInstance(getActivity()).createLogger();
						Log.d("NILS","CREATED ZIZ LOGGER");
					}
					else {
						Log.d("NILS","UNCREATED ZIZ LOGGER");
						GlobalState.getInstance(getActivity()).removeLogger();
					}
				}

		}

	}

}




