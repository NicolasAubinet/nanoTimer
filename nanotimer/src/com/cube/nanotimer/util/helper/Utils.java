package com.cube.nanotimer.util.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import com.cube.nanotimer.App;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveType;

import java.security.SecureRandom;
import java.util.Random;

public class Utils {

  public static String parseFloatToString(Float f) {
    return f == null ? null : String.valueOf(f);
  }

  public static String getAppVersion(Context c) {
    try {
      return c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {
      e.printStackTrace();
    }
    return "";
  }

  public static Random getRandom() {
    return new SecureRandom();
  }

  public static void playSound(int soundId) {
    MediaPlayer mp = MediaPlayer.create(App.INSTANCE.getContext(), soundId);
    mp.start();
  }

  public static CubeType getCurrentCubeType(Context c) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
    return CubeType.getCubeType(prefs.getInt("cubeTypeId", CubeType.THREE_BY_THREE.getId()));
  }

  public static int getCurrentSolveTypeId(Context c) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
    return prefs.getInt("solveTypeId", -1);
  }

  public static void setCurrentCubeType(Context c, CubeType cubeType) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
    int id = (cubeType == null) ? CubeType.THREE_BY_THREE.getId() : cubeType.getId();
    prefs.edit().putInt("cubeTypeId", id).commit();
  }

  public static void setCurrentSolveType(Context c, SolveType solveType) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
    int id = (solveType == null) ? -1 : solveType.getId();
    prefs.edit().putInt("solveTypeId", id).commit();
  }

  public static int getRSScrambleLengthFromQuality(CubeType cubeType) {
    switch (cubeType) {
      case TWO_BY_TWO:
        switch (Options.INSTANCE.getScramblesQuality()) {
          case HIGH:
            return 11;
          case MEDIUM:
            return 11;
          case LOW:
            return 12;
        }
        break;
      case THREE_BY_THREE:
        switch (Options.INSTANCE.getScramblesQuality()) {
          case HIGH:
            return 21;
          case MEDIUM:
            return 23;
          case LOW:
            return 24;
        }
        break;
    }
    return -1;
  }

}
