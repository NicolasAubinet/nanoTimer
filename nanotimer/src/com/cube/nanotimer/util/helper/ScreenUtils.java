package com.cube.nanotimer.util.helper;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.TypedValue;
import android.view.Surface;
import com.cube.nanotimer.App;

public class ScreenUtils {

  private static int getDeviceDefaultOrientation(Activity a) {
    int rotation = a.getWindowManager().getDefaultDisplay().getRotation();
    int orientation = a.getResources().getConfiguration().orientation;
    if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && orientation == Configuration.ORIENTATION_LANDSCAPE) ||
        ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && orientation == Configuration.ORIENTATION_PORTRAIT)) {
      return Configuration.ORIENTATION_LANDSCAPE;
    } else {
      return Configuration.ORIENTATION_PORTRAIT;
    }
  }

  /**
   * Enables or disables screen rotation changes.
   *
   * @param enable enable or disable screen rotations
   */
  public static void enableScreenRotationChanges(Activity a, boolean enable) {
    if (enable) {
      a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    } else {
      int currentOrientation = a.getResources().getConfiguration().orientation;
      if (VERSION.SDK_INT < VERSION_CODES.GINGERBREAD) {
        // API prior to 9 does not allow to set reverse orientations
        // There's still a bug here if switching from horizontal to vertical, but it's only for 2.2 in this specific case.
        a.setRequestedOrientation(currentOrientation);
      } else {
        int rotation = a.getWindowManager().getDefaultDisplay().getRotation();
        int defaultOrientation = ScreenUtils.getDeviceDefaultOrientation(a);
        int orientation;
        switch (rotation) {
          case Surface.ROTATION_0:
            if (defaultOrientation == Configuration.ORIENTATION_PORTRAIT) {
              orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            } else {
              orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
            break;
          case Surface.ROTATION_90:
            if (defaultOrientation == Configuration.ORIENTATION_PORTRAIT) {
              orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            } else {
              orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            }
            break;
          case Surface.ROTATION_180:
            if (defaultOrientation == Configuration.ORIENTATION_PORTRAIT) {
              orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            } else {
              orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            }
            break;
          case Surface.ROTATION_270:
            if (defaultOrientation == Configuration.ORIENTATION_PORTRAIT) {
              orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            } else {
              orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
            break;
          default:
            orientation = currentOrientation;
            break;
        }
        a.setRequestedOrientation(orientation);
      }
    }
  }

  public static int dipToPixels(int dip) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, App.INSTANCE.getContext().getResources().getDisplayMetrics());
  }

}
