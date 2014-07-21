package com.cube.nanotimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public enum Options {
  INSTANCE;

  enum InspectionMode { HOLD_AND_RELEASE, AUTOMATIC }

  private Context context;
  private SharedPreferences sharedPreferences;

  private static final String INSPECTION_MODE_KEY = "inspection_mode";
  private static final String INSPECTION_TIME_KEY = "inspection_time";

  private static final int MAX_STEPS_COUNT = 8;

  public int getMaxStepsCount() {
    return MAX_STEPS_COUNT;
  }

  public InspectionMode getInspectionMode() {
    Integer defaultValue = context.getResources().getInteger(R.integer.inspection_mode);
    int mode = sharedPreferences.getInt(INSPECTION_MODE_KEY, defaultValue);
    switch (mode) {
      case 1:
        return InspectionMode.HOLD_AND_RELEASE;
      case 2:
        return InspectionMode.AUTOMATIC;
      default:
        return InspectionMode.HOLD_AND_RELEASE;
    }
  }

  public int getInspectionTime() {
    Integer defaultValue = context.getResources().getInteger(R.integer.inspection_time);
    return sharedPreferences.getInt(INSPECTION_TIME_KEY, defaultValue);
  }

  public void setContext(Context context) {
    this.context = context;
    this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
  }

}
