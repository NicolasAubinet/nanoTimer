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
      editor.commit();
    } else {
      if (!prefVersion.equals(curVersion)) {
        // app upgrade
        editor.putString(VERSION_KEY, curVersion);
        showReleaseNotesDialog(context);
        editor.commit();
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
    tvInfo.loadData(getReleaseNotesHTMLString(), "text/html", "utf-8");

    dialog.show();
  }

  private static String getReleaseNotesHTMLString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<html><body>");
    sb.append("<font color=\"white\">");

    sb.append("<u><b>v1.1.0</b></u>");
    sb.append("<ul>");
    sb.append("<li>Improved graphical interface</li>");
    sb.append("<li>New \"session details\" timer window</li>");
    sb.append("<li>Total solves count now shown in history</li>");
    sb.append("</ul>");

    sb.append("<u><b>v1.0.2</b></u>");
    sb.append("<ul>");
    sb.append("<li>Scrambles for Square-1 and Clock</li>");
    sb.append("<li>Average of 50</li>");
    sb.append("<li>Shortcut to solve types editing from solve types list</li>");
    sb.append("<li>Improved scrambles format based on type and orientation</li>");
    sb.append("<li>Displayed averages in history details</li>");
    sb.append("<li>Option to choose big cubes notation system</li>");
    sb.append("<li>Set Skewb and Pyraminx scramble sizes to 15</li>");
    sb.append("</ul>");

    sb.append("<u><b>v1.0.1</b></u>");
    sb.append("<ul>");
    sb.append("<li>Changed session times grid order (the latest time is now in the top-left cell)</li>");
    sb.append("<li>Reduced length of pyraminx and skewb scrambles from 25 to 20</li>");
    sb.append("<li>Pyraminx corner moves are now at the end of the scramble</li>");
    sb.append("<li>Minor GUI improvements</li>");
    sb.append("</ul>");

    sb.append("</font>");
    sb.append("</body></html>");

    return sb.toString();
  }

}
