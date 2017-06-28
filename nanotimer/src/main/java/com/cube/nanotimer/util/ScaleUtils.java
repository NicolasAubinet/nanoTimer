package com.cube.nanotimer.util;

import android.content.Context;
import android.content.res.Configuration;
import android.view.WindowManager;

public class ScaleUtils {

  private static final int LAYOUT_WIDTH = 480;
  private static final int LAYOUT_HEIGHT = 762;

  private static WindowManager windowManager;

  private static void init(Context c) {
    if (windowManager == null) {
      windowManager = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
    }
  }

  public static int getScreenHeight(Context c) {
    init(c);
    return windowManager.getDefaultDisplay().getHeight();
  }

  public static int getScreenWidth(Context c) {
    init(c);
    return windowManager.getDefaultDisplay().getWidth();
  }

  public static float getXScale(Context c) {
    init(c);
    float xScale;
    int orientation = c.getResources().getConfiguration().orientation;
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      xScale = (float) getScreenWidth(c) / LAYOUT_WIDTH;
    } else {
      xScale = (float) getScreenWidth(c) / LAYOUT_HEIGHT;
    }
    return xScale;
  }

  public static float getYScale(Context c) {
    init(c);
    float yScale;
    int orientation = c.getResources().getConfiguration().orientation;
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      yScale = (float) getScreenHeight(c) / LAYOUT_HEIGHT;
    } else {
      yScale = (float) getScreenHeight(c) / LAYOUT_WIDTH;
    }
    return yScale;
  }

}
