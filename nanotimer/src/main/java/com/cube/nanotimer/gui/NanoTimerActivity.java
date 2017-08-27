package com.cube.nanotimer.gui;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import com.cube.nanotimer.util.helper.Utils;

public class NanoTimerActivity extends AppCompatActivity {

  @Override
  protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(Utils.getLocaleContextFromPrefs(newBase));
  }
}
