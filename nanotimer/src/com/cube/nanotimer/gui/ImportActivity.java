package com.cube.nanotimer.gui;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import com.cube.nanotimer.R;

public class ImportActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.import_screen);
  }

  private void initViews() {
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    setContentView(R.layout.import_screen);
    initViews();
  }
}
