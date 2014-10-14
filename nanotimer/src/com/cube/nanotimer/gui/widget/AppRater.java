package com.cube.nanotimer.gui.widget;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.helper.DialogUtils;

public class AppRater {

  private final static int DAYS_UNTIL_PROMPT = 5;
  private final static int DAYS_ASK_LATER_PROMPT_AGAIN = 2;
  private final static int LAUNCHES_UNTIL_PROMPT = 7;

  public static void appLaunched(Context context) {
    SharedPreferences prefs = context.getSharedPreferences("apprater", 0);
    if (prefs.getBoolean("dontshowagain", false)) {
      return;
    }

    SharedPreferences.Editor editor = prefs.edit();
    // Increment launch counter
    long launchCount = prefs.getLong("launch_count", 0) + 1;
    editor.putLong("launch_count", launchCount);

    // Get date of first launch
    Long firstLaunchDate = prefs.getLong("date_firstlaunch", 0);
    if (firstLaunchDate == 0) {
      firstLaunchDate = System.currentTimeMillis();
      editor.putLong("date_firstlaunch", firstLaunchDate);
    }

    // Get date when user clicked on "Ask later"
    Long askLaterDate = prefs.getLong("date_asklater", 0);

    long currentTime = System.currentTimeMillis();
    if (launchCount >= LAUNCHES_UNTIL_PROMPT &&
        currentTime >= firstLaunchDate + daysToMs(DAYS_UNTIL_PROMPT) &&
        (askLaterDate == 0 || currentTime >= askLaterDate + daysToMs(DAYS_ASK_LATER_PROMPT_AGAIN))) {
      showRateDialog(context, editor);
    }

    editor.commit();
  }

  public static void showRateDialog(final Context context, final SharedPreferences.Editor editor) {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ViewGroup v = (ViewGroup) inflater.inflate(R.layout.rate_dialog, null);

    final AlertDialog dialog = new AlertDialog.Builder(context).setView(v).create();
    dialog.setTitle(context.getString(R.string.rate) + " " + context.getString(R.string.app_name));
    dialog.setOnCancelListener(new OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialogInterface) {
        editor.putLong("date_asklater", System.currentTimeMillis());
        editor.commit();
      }
    });

    Button buRateNow = (Button) v.findViewById(R.id.buRateNow);
    buRateNow.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (openStoreForRating(context)) {
          // avoid asking again if the user rated the app (there's no way to know for sure if he actually rated it)
          editor.putBoolean("dontshowagain", true);
          editor.commit();
        }
        dialog.dismiss();
      }
    });

    Button buAskLater = (Button) v.findViewById(R.id.buAskLater);
    buAskLater.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (editor != null) {
          editor.putLong("date_asklater", System.currentTimeMillis());
          editor.commit();
        }
        dialog.dismiss();
      }
    });

    Button buDontAskAgain = (Button) v.findViewById(R.id.buDontAskAgain);
    buDontAskAgain.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (editor != null) {
          editor.putBoolean("dontshowagain", true);
          editor.commit();
        }
        dialog.dismiss();
      }
    });

    dialog.show();
  }

  private static boolean openStoreForRating(Context context) {
    Intent rateAppIntent;
    String storePackage = context.getPackageManager().getInstallerPackageName(context.getPackageName());
    if (storePackage == null) {
      DialogUtils.showInfoMessage(R.string.could_not_find_market);
      return false;
    } else if (storePackage.equals("com.android.vending")) { // google
      rateAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName()));
    } else if (storePackage.equals("com.amazon.venezia")) { // amazon
      rateAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("amzn://apps/android?p=" + context.getPackageName()));
    } else {
      DialogUtils.showInfoMessage(R.string.could_not_find_market);
      return false;
    }

    if (context.getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0) {
      try {
        context.startActivity(rateAppIntent);
        return true;
      } catch (ActivityNotFoundException e) {
        DialogUtils.showInfoMessage(R.string.could_not_launch_market);
      }
    } else {
      DialogUtils.showInfoMessage(R.string.could_not_find_market);
    }
    return false;
  }

  private static long daysToMs(int days) {
    return days * 24 * 60 * 60 * 1000;
  }

}