package com.cube.nanotimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public enum Options {
  INSTANCE;

  public enum InspectionMode { HOLD_AND_RELEASE, AUTOMATIC }
  public enum AdsStyle { BANNER, INTERSTITIAL, MIXED }
  public enum BigCubesNotation { RUF, RWUWFW }
  public enum ScramblesQuality { LOW, MEDIUM, HIGH }
  public enum ScrambleNotificationMode { ALWAYS, MANUAL, NEVER }

  private Context context;
  private SharedPreferences sharedPreferences;

  public static final String INSPECTION_MODE_KEY = "inspection_mode";
  public static final String INSPECTION_TIME_KEY = "inspection_time";
  public static final String INSPECTION_SOUNDS_KEY = "inspection_sounds";
  public static final String KEEP_TIMER_SCREEN_ON_KEY = "keep_timer_screen_on";
  public static final String BIG_CUBES_NOTATION_KEY = "big_cubes_notation";
  public static final String SOLVE_TYPES_SHORTCUT_KEY = "solve_types_shortcut";

  public static final String RANDOMSTATE_SCRAMBLES_KEY = "randomstate_scrambles";
  public static final String SCRAMBLES_QUALITY_KEY = "scrambles_quality";
  public static final String SCRAMBLE_NOTIFICATION_MODE_KEY = "scramble_notification_mode";
  public static final String SCRAMBLES_GEN_WHEN_PLUGGED_IN_KEY = "scrambles_gen_when_plugged_in";
  public static final String SCRAMBLES_GEN_COUNT_WHEN_PLUGGED_IN_KEY = "scrambles_gen_count_when_plugged_in";
  public static final String SCRAMBLES_MIN_CACHE_SIZE_KEY = "scrambles_min_cache_size";
  public static final String SCRAMBLES_MAX_CACHE_SIZE_KEY = "scrambles_max_cache_size";
  public static final String PREGEN_SCRAMBLES_KEY = "pregen_scrambles";

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
    return AdsStyle.BANNER;
  }

  public boolean isAdsEnabled() {
    return !App.INSTANCE.isProEnabled();
  }

  public boolean isSolveTypesShortcutEnabled() {
    Boolean defaultValue = context.getResources().getBoolean(R.bool.solve_types_shortcut);
    return sharedPreferences.getBoolean(SOLVE_TYPES_SHORTCUT_KEY, defaultValue);
  }

  public boolean isRandomStateScrambles() {
    Boolean defaultValue = context.getResources().getBoolean(R.bool.randomstate_scrambles);
    return sharedPreferences.getBoolean(RANDOMSTATE_SCRAMBLES_KEY, defaultValue);
  }

  public ScramblesQuality getScramblesQuality() {
    int quality = Integer.parseInt(sharedPreferences.getString(SCRAMBLES_QUALITY_KEY, "-1"));
    switch (quality) {
      case 1:
        return ScramblesQuality.HIGH;
      case 2:
        return ScramblesQuality.MEDIUM;
      case 3:
        return ScramblesQuality.LOW;
      default:
        return ScramblesQuality.MEDIUM;
    }
  }

  public ScrambleNotificationMode getGenScrambleNotificationMode() {
    int mode = Integer.parseInt(sharedPreferences.getString(SCRAMBLE_NOTIFICATION_MODE_KEY, "-1"));
    switch (mode) {
      case 1:
        return ScrambleNotificationMode.ALWAYS;
      case 2:
        return ScrambleNotificationMode.MANUAL;
      case 3:
        return ScrambleNotificationMode.NEVER;
      default:
        return ScrambleNotificationMode.ALWAYS;
    }
  }

  public boolean isGenerateScramblesWhenPluggedIn() {
    Boolean defaultValue = context.getResources().getBoolean(R.bool.scrambles_gen_when_plugged_in);
    return sharedPreferences.getBoolean(SCRAMBLES_GEN_WHEN_PLUGGED_IN_KEY, defaultValue);
  }

  public int getPluggedInScramblesGenerateCount() {
    Integer defaultValue = context.getResources().getInteger(R.integer.scramble_gen_count_when_plugged_in);
    return sharedPreferences.getInt(SCRAMBLES_GEN_COUNT_WHEN_PLUGGED_IN_KEY, defaultValue);
  }

  public int getScramblesMinCacheSize() {
    Integer defaultValue = context.getResources().getInteger(R.integer.min_scramble_cache_size);
    return sharedPreferences.getInt(SCRAMBLES_MIN_CACHE_SIZE_KEY, defaultValue);
  }

  public int getScramblesMaxCacheSize() {
    Integer defaultValue = context.getResources().getInteger(R.integer.max_scramble_cache_size);
    return sharedPreferences.getInt(SCRAMBLES_MAX_CACHE_SIZE_KEY, defaultValue);
  }

}
