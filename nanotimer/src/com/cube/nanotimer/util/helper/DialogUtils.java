package com.cube.nanotimer.util.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.YesNoListener;

public class DialogUtils {

  public static void showFragment(FragmentActivity a, DialogFragment df) {
    df.show(a.getSupportFragmentManager(), "dialog");
  }

  public static void showInfoMessage(Context context, String message) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
  }

  public static void showInfoMessage(Context context, int messageId) {
    Toast.makeText(context, messageId, Toast.LENGTH_LONG).show();
  }

  public static AlertDialog showYesNoConfirmation(Context context, String message, final YesNoListener listener) {
    DialogInterface.OnClickListener clickListener = getYesNoClickListener(listener);

    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    return builder.setMessage(message)
        .setPositiveButton("Yes", clickListener)
        .setNegativeButton("No", clickListener).show();
  }

  public static AlertDialog showYesNoConfirmation(Context context, int messageId, final YesNoListener listener) {
    DialogInterface.OnClickListener clickListener = getYesNoClickListener(listener);

    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    return builder.setMessage(messageId)
        .setPositiveButton("Yes", clickListener)
        .setNegativeButton("No", clickListener).show();
  }

  public static AlertDialog showOkDialog(Context context, String title, String message) {
    return new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show();
  }

  public static AlertDialog showOkDialog(Context context, int titleId, int messageId) {
    return new AlertDialog.Builder(context)
            .setTitle(titleId)
            .setMessage(messageId)
            .setPositiveButton(R.string.ok, null)
            .show();
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

}
