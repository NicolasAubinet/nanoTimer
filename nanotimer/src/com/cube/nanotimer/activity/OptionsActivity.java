package com.cube.nanotimer.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.cube.nanotimer.R;

public class OptionsActivity extends PreferenceActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
  }

}
