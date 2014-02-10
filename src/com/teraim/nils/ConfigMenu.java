package com.teraim.nils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

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
			
			ListPreference ruta = (ListPreference)findPreference(PersistenceHelper.CURRENT_RUTA_ID_KEY);
			
			if (ruta != null) {
				ruta.setSummary(ruta.getValue());
				String[] rutlist = GlobalState.getInstance(this.getActivity()).getRutIds();
			    CharSequence entries[] = rutlist;
			    CharSequence entryValues[] = rutlist;
			    ruta.setEntries(entries);
			    ruta.setEntryValues(entryValues);
			}

			epref = (EditTextPreference) findPreference(PersistenceHelper.SERVER_URL);
			epref.setSummary(epref.getText());
			
			
			epref = (EditTextPreference) findPreference(PersistenceHelper.BUNDLE_LOCATION);
			epref.setSummary(epref.getText());
			
			epref = (EditTextPreference) findPreference(PersistenceHelper.CONFIG_LOCATION);
			epref.setSummary(epref.getText());

		
		}

		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			Preference pref = findPreference(key);
			if (pref instanceof EditTextPreference) {
				EditTextPreference etp = (EditTextPreference) pref;
				pref.setSummary(etp.getText());
			}
			else if (pref instanceof ListPreference) {
				ListPreference letp = (ListPreference) pref;
				pref.setSummary(letp.getValue());
				
			}

		}

	}

}




