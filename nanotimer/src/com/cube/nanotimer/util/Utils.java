package com.cube.nanotimer.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.widget.Toast;
import com.cube.nanotimer.App;

public class Utils {

  public static void showFragment(Activity a, DialogFragment df) {
    df.show(a.getFragmentManager(), "dialog");
  }

  public static String parseFloatToString(Float f) {
    return f == null ? null : String.valueOf(f);
  }

  public static void showInfoMessage(String message) {
    showInfoMessage(App.INSTANCE.getContext(), message);
  }

  public static void showInfoMessage(Context context, String message) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
  }

  public static void showInfoMessage(int messageId) {
    showInfoMessage(App.INSTANCE.getContext(), messageId);
  }

  public static void showInfoMessage(Context context, int messageId) {
    Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show();
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

}
