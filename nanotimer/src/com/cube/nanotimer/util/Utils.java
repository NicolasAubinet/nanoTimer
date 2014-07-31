package com.cube.nanotimer.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;
import com.cube.nanotimer.App;

public class Utils {

  public static void showFragment(FragmentActivity a, DialogFragment df) {
    df.show(a.getSupportFragmentManager(), "dialog");
  }

  public static String parseFloatToString(Float f) {
    return f == null ? null : String.valueOf(f);
  }

  public static void showInfoMessage(String message) {
    showInfoMessage(App.INSTANCE.getContext(), message);
  }

  public static void showInfoMessage(Context context, String message) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
  }

  public static void showInfoMessage(int messageId) {
    showInfoMessage(App.INSTANCE.getContext(), messageId);
  }

  public static void showInfoMessage(Context context, int messageId) {
    Toast.makeText(context, messageId, Toast.LENGTH_LONG).show();
  }

  public static void showYesNoConfirmation(Context context, String message, final YesNoListener listener) {
    DialogInterface.OnClickListener clickListener = getYesNoClickListener(listener);

    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setMessage(message)
        .setPositiveButton("Yes", clickListener)
        .setNegativeButton("No", clickListener).show();
  }

  public static void showYesNoConfirmation(Context context, int messageId, final YesNoListener listener) {
    DialogInterface.OnClickListener clickListener = getYesNoClickListener(listener);

    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setMessage(messageId)
        .setPositiveButton("Yes", clickListener)
        .setNegativeButton("No", clickListener).show();
  }

  private static DialogInterface.OnClickListener getYesNoClickListener(final YesNoListener listener) {
    return new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        switch (which) {
          case DialogInterface.BUTTON_POSITIVE:
            listener.onYes();
            break;
          case DialogInterface.BUTTON_NEGATIVE:
            listener.onNo();
            break;
        }
      }
    };
  }

  public static void playSound(int soundId) {
    MediaPlayer mp = MediaPlayer.create(App.INSTANCE.getContext(), soundId);
    mp.start();
  }

  public static void expandView(final LinearLayout v) {
    v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    final int targetHeight = v.getMeasuredHeight();

    v.getLayoutParams().height = 0;
    v.setVisibility(View.VISIBLE);
    Animation a = new Animation() {
      @Override
      protected void applyTransformation(float interpolatedTime, Transformation t) {
        v.getLayoutParams().height = interpolatedTime == 1
            ? LayoutParams.WRAP_CONTENT
            : (int) (targetHeight * interpolatedTime);
        v.requestLayout();
      }

      @Override
      public boolean willChangeBounds() {
        return true;
      }
    };

    // 1dp/ms
    a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
    v.startAnimation(a);
  }

  public static void collapseView(final View v) {
    final int initialHeight = v.getMeasuredHeight();

    Animation a = new Animation() {
      @Override
      protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (interpolatedTime == 1) {
          v.setVisibility(View.GONE);
        } else {
          v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
          v.requestLayout();
        }
      }

      @Override
      public boolean willChangeBounds() {
        return true;
      }
    };

    // 1dp/ms
    a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
    v.startAnimation(a);
  }

  public static int getColorCodeBetween(int color1, int color2, float stepProgression) {
    int a1 = (color1 >> 24) & 0xFF;
    int r1 = (color1 >> 16) & 0xFF;
    int g1 = (color1 >> 8) & 0xFF;
    int b1 = (color1) & 0xFF;
    int a2 = (color2 >> 24) & 0xFF;
    int r2 = (color2 >> 16) & 0xFF;
    int g2 = (color2 >> 8) & 0xFF;
    int b2 = (color2) & 0xFF;
    int a = (int) ((a2 - a1) * stepProgression) + a1;
    int r = (int) ((r2 - r1) * stepProgression) + r1;
    int g = (int) ((g2 - g1) * stepProgression) + g1;
    int b = (int) ((b2 - b1) * stepProgression) + b1;

    int res = a << 24;
    res |= r << 16;
    res |= g << 8;
    res |= b;
    return res;
  }

}
