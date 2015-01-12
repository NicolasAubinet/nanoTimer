package com.cube.nanotimer.gui.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.cube.nanotimer.App;
import com.cube.nanotimer.AppLaunchStats;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.helper.Utils;

public class ProVersionAd {

  private final static int DAYS_UNTIL_PROMPT = 8;
  private final static int LAUNCHES_UNTIL_PROMPT = 5;
  private final static int CHECKS_UNTIL_PROMPT = 2;

  public static final String CHECKS_COUNT_KEY = "checks_count";
  public static final String DIALOG_SHOWN_COUNT_KEY = "dialog_shown_count";
  public static final String NEXT_SHOW_DATE_KEY = "next_show_date";

  public static void appLaunched(Context context) {
    if (App.INSTANCE.isProEnabled()) {
      return;
    }
    long currentTime = System.currentTimeMillis();
    SharedPreferences prefs = context.getSharedPreferences("proversion", 0);
    Editor editor = prefs.edit();

    long launchCount = AppLaunchStats.getLaunchCount(context);
    long firstLaunchDate = AppLaunchStats.getFirstLaunchDate(context);

    // Increment the number of times we passed here (mostly used to not show this dialog at the same time than the release notes dialog)
    int checksCount = prefs.getInt(CHECKS_COUNT_KEY, 0);
    checksCount++;
    editor.putInt(CHECKS_COUNT_KEY, checksCount);

    int dialogShownCount = prefs.getInt(DIALOG_SHOWN_COUNT_KEY, -1);
    if (dialogShownCount == -1) {
      dialogShownCount = 0;
      editor.putInt(DIALOG_SHOWN_COUNT_KEY, dialogShownCount);
    }

    long nextShowDate = prefs.getLong(NEXT_SHOW_DATE_KEY, -1);
    if (nextShowDate == -1) {
      nextShowDate = currentTime;
      editor.putLong(NEXT_SHOW_DATE_KEY, nextShowDate);
    }

    if (launchCount >= LAUNCHES_UNTIL_PROMPT &&
        checksCount >= CHECKS_UNTIL_PROMPT &&
        currentTime >= firstLaunchDate + Utils.daysToMs(DAYS_UNTIL_PROMPT) &&
        currentTime >= nextShowDate) {
      showProVersionAdDialog(context);

      dialogShownCount++;
      editor.putInt(DIALOG_SHOWN_COUNT_KEY, dialogShownCount);

      nextShowDate = currentTime + (dialogShownCount * Utils.daysToMs(25));
      editor.putLong(NEXT_SHOW_DATE_KEY, nextShowDate);
    }

    editor.commit();
  }

  public static void showProVersionAdDialog(Context context) {
    AlertDialog dialog = new AlertDialog.Builder(context).setView(getProVersionAdView(context)).create();
    dialog.setTitle(R.string.pro_version_promotion_title);
    dialog.setCanceledOnTouchOutside(true);
    dialog.show();
  }

  public static View getProVersionAdView(final Context context) {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ViewGroup v = (ViewGroup) inflater.inflate(R.layout.pro_version_ad_dialog, null);

    Button buOpenPage = (Button) v.findViewById(R.id.buOpenPage);
    buOpenPage.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        Utils.openPlayStorePage(context, App.PRO_PACKAGE_NAME);
      }
    });

    return v;
  }

}
