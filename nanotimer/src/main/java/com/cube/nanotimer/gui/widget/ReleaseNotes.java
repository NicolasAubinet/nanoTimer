package com.cube.nanotimer.gui.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.helper.Utils;

public class ReleaseNotes {

  private static final String VERSION_KEY = "app_version";

  public static void appLaunched(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    String prefVersion = prefs.getString(VERSION_KEY, null);
    String curVersion = Utils.getAppVersion(context);

    SharedPreferences.Editor editor = prefs.edit();
    if (prefVersion == null) {
      // first launch
      editor.putString(VERSION_KEY, curVersion);
      editor.apply();
    } else {
      if (!prefVersion.equals(curVersion)) {
        // app upgrade
        editor.putString(VERSION_KEY, curVersion);
        showReleaseNotesDialog(context);
        editor.apply();
      }
    }
  }

  public static void showReleaseNotesDialog(Context context) {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ViewGroup v = (ViewGroup) inflater.inflate(R.layout.release_notes_dialog, null);

    final AlertDialog dialog = new AlertDialog.Builder(context).setView(v).create();
    dialog.setTitle(context.getString(R.string.release_notes));
    dialog.setCanceledOnTouchOutside(true);

    WebView tvInfo = (WebView) v.findViewById(R.id.tvInfo);
    tvInfo.setBackgroundColor(context.getResources().getColor(R.color.transparent));
    tvInfo.loadData(context.getString(R.string.release_notes_features_html), "text/html", "utf-8");

    dialog.show();
  }

}
