package com.teraim.nils;

import android.os.Bundle;
import android.preference.PreferenceActivity;
public class AppPreferenceActivity extends PreferenceActivity {
@Override
public void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
//---load the preferences from an XML file---
addPreferencesFromResource(R.xml.myprefs);
}
}