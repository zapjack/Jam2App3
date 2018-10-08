package com.kiowok.jam2app;

import android.preference.PreferenceActivity;
import android.os.Bundle;

public class Jam2PreferenceActivity extends PreferenceActivity {

	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    addPreferencesFromResource(R.xml.preference);
	}

}
