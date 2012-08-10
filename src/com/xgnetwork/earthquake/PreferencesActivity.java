package com.xgnetwork.earthquake;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity {
	public static final String PREF_MIN_MAG = "PREF_MIN_MAG";
	public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";
	public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.userpreferences);
	}
	
}
