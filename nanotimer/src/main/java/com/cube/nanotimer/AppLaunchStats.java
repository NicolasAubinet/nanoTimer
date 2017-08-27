package com.cube.nanotimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppLaunchStats {

  public static final String LAUNCH_COUNT_KEY = "launch_count";
  public static final String FIRST_LAUNCH_KEY = "date_firstlaunch";

  public static void appLaunched(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    long firstLaunchDate = prefs.getLong(FIRST_LAUNCH_KEY, -1);
    if (firstLaunchDate == -1) {
      retrieveAndInitPreviousStats(context);
    }

    SharedPreferences.Editor editor = prefs.edit();
    // Increment launch counter
    long launchCount = prefs.getLong(LAUNCH_COUNT_KEY, 0);
    editor.putLong(LAUNCH_COUNT_KEY, launchCount + 1);

    editor.apply();
  }

  public static int getLaunchCount(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    long defaultValue = 0;
    if (prefs == null) {
      return (int) defaultValue;
    }
    return (int) prefs.getLong(LAUNCH_COUNT_KEY, defaultValue);
  }

  public static long getFirstLaunchDate(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    long defaultValue = System.currentTimeMillis();
    if (prefs == null) {
      return defaultValue;
    }
    return prefs.getLong(FIRST_LAUNCH_KEY, defaultValue);
  }

  /**
   * Retrieve stats that were previously saved in apprater preferences,
   * and save them to the default preferences.
   * Will also set default values if no values exist in apprater preferences.
   */
  private static void retrieveAndInitPreviousStats(Context context) {
    SharedPreferences globalPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = globalPrefs.edit();
    SharedPreferences appRaterPrefs = context.getSharedPreferences("apprater", 0);
    if (appRaterPrefs == null) {
      editor.putLong(LAUNCH_COUNT_KEY, 0);
      editor.putLong(FIRST_LAUNCH_KEY, System.currentTimeMillis());
    } else {
      long launchCount = appRaterPrefs.getLong(LAUNCH_COUNT_KEY, 0);
      editor.putLong(LAUNCH_COUNT_KEY, launchCount);
      long firstLaunchDate = appRaterPrefs.getLong(FIRST_LAUNCH_KEY, System.currentTimeMillis());
      editor.putLong(FIRST_LAUNCH_KEY, firstLaunchDate);
    }
    editor.apply();
  }

}
