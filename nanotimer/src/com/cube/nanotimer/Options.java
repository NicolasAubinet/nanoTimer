package com.cube.nanotimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public enum Options {
  INSTANCE;

  public enum InspectionMode { HOLD_AND_RELEASE, AUTOMATIC }
  public enum AdsStyle { BANNER, INTERSTITIAL, MIXED }
  public enum BigCubesNotation { RUF, RWUWFW }

  private Context context;
  private SharedPreferences sharedPreferences;

  private static final String INSPECTION_MODE_KEY = "inspection_mode";
  private static final String INSPECTION_TIME_KEY = "inspection_time";
  private static final String INSPECTION_SOUNDS_KEY = "inspection_sounds";
  private static final String KEEP_TIMER_SCREEN_ON_KEY = "keep_timer_screen_on";
  private static final String SHOW_SESSION_SOLVES_COUNT_KEY = "show_session_solves_count";
  private static final String BIG_CUBES_NOTATION_KEY = "big_cubes_notation";
  private static final String ADS_STYLE_KEY = "ads_style";
  private static final String SOLVE_TYPES_SHORTCUT = "solve_types_shortcut";

  private static final int MAX_STEPS_COUNT = 8;

  public void setContext(Context context) {
    this.context = context;
    this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
  }

  public int getMaxStepsCount() {
    return MAX_STEPS_COUNT;
  }

  public InspectionMode getInspectionMode() {
    int mode = Integer.parseInt(sharedPreferences.getString(INSPECTION_MODE_KEY, "-1"));
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

  public boolean isInspectionSoundsEnabled() {
    Boolean defaultValue = context.getResources().getBoolean(R.bool.inspection_sounds);
    return sharedPreferences.getBoolean(INSPECTION_SOUNDS_KEY, defaultValue);
  }

  public boolean isKeepTimerScreenOnWhenTimerOff() {
    Boolean defaultValue = context.getResources().getBoolean(R.bool.keep_timer_screen_on);
    return sharedPreferences.getBoolean(KEEP_TIMER_SCREEN_ON_KEY, defaultValue);
  }

  public boolean isSessionSolvesCountShown() {
    Boolean defaultValue = context.getResources().getBoolean(R.bool.show_session_solves_count);
    return sharedPreferences.getBoolean(SHOW_SESSION_SOLVES_COUNT_KEY, defaultValue);
  }

  public BigCubesNotation getBigCubesNotation() {
    int notation = Integer.parseInt(sharedPreferences.getString(BIG_CUBES_NOTATION_KEY, "-1"));
    switch (notation) {
      case 1:
        return BigCubesNotation.RUF;
      case 2:
        return BigCubesNotation.RWUWFW;
      default:
        return BigCubesNotation.RUF;
    }
  }

  public AdsStyle getAdsStyle() {
    int style = Integer.parseInt(sharedPreferences.getString(ADS_STYLE_KEY, "-1"));
    switch (style) {
      case 1:
        return AdsStyle.BANNER;
      case 2:
        return AdsStyle.INTERSTITIAL;
      case 3:
        return AdsStyle.MIXED;
      default:
        return AdsStyle.MIXED;
    }
  }

  public boolean isAdsEnabled() {
    return true;
  }

  public boolean isSolveTypesShortcutEnabled() {
    Boolean defaultValue = context.getResources().getBoolean(R.bool.solve_types_shortcut);
    return sharedPreferences.getBoolean(SOLVE_TYPES_SHORTCUT, defaultValue);
  }

}
