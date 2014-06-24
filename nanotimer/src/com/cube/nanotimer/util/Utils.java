package com.cube.nanotimer.util;

public class Utils {

  public static String parseFloatToString(Float f) {
    return f == null ? null : String.valueOf(f);
  }

  public static String deleteLineBreaks(String s) {
    return s.replace("\n", "");
  }

  public static String formatTime(float time) {
    return String.format("%.2f", time);
  }

  public static String formatTime(int time) {
    return String.format("%.2f", (float) time / 1000);
  }

}
