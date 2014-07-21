package com.cube.nanotimer.activity.widget;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import com.cube.nanotimer.App;

public class ClearHistoryDialog extends DialogPreference {

  public ClearHistoryDialog(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    super.onDialogClosed(positiveResult);
    if (positiveResult) {
      App.INSTANCE.getService().deleteHistory(null);
    }
  }

}
