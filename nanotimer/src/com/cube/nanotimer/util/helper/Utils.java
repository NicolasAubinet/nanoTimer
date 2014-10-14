package com.cube.nanotimer.util.helper;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import com.cube.nanotimer.App;

import java.security.SecureRandom;
import java.util.List;
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

  public static long getMeanOf(List<Long> times) {
    long mean = 0;
    int validTimes = 0;
    if (times.size() > 0) {
      for (Long t : times) {
        if (t >= 0) {
          mean += t;
          validTimes++;
        }
      }
      mean /= validTimes;
    } else {
      mean = -2;
    }
    return mean;
  }

}
