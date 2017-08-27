package com.cube.nanotimer.gui.widget;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import com.cube.nanotimer.util.helper.Utils;

public class NanoTimerDialogFragment extends DialogFragment {

  @Override
  public void onAttach(Context context) {
    super.onAttach(Utils.getLocaleContextFromPrefs(context));
  }
}
