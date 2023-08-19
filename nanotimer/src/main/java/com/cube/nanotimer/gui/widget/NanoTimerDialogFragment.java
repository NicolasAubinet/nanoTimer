package com.cube.nanotimer.gui.widget;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import com.cube.nanotimer.util.helper.Utils;

public class NanoTimerDialogFragment extends DialogFragment {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Utils.updateContextWithPrefsLocale(getContext());
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    Utils.updateContextWithPrefsLocale(getContext());
  }
}
