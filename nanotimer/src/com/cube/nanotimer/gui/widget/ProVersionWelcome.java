package com.cube.nanotimer.gui.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.cube.nanotimer.R;

public class ProVersionWelcome {

  public static final String ACTIVATED_KEY = "activated";

  private static AlertDialog dialog;

  public static void onResume(Context context, boolean proEnabled) {
    if (proEnabled) {
      SharedPreferences prefs = context.getSharedPreferences("proversion", 0);
      if (!prefs.getBoolean(ACTIVATED_KEY, false)) {
        showWelcomeDialog(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(ACTIVATED_KEY, true);
        editor.commit();
      }
    }
  }

  public static void showWelcomeDialog(Context context) {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ViewGroup v = (ViewGroup) inflater.inflate(R.layout.pro_version_welcome, null);

    if (dialog != null) {
      dialog.dismiss();
    }

    dialog = new AlertDialog.Builder(context).setView(v).create();
    dialog.setTitle(context.getString(R.string.pro_version_welcome_title));
    dialog.setCanceledOnTouchOutside(true);

    WebView tvFeaturesList = (WebView) v.findViewById(R.id.tvProFeatures);
    tvFeaturesList.setBackgroundColor(context.getResources().getColor(R.color.transparent));
    tvFeaturesList.loadData(context.getString(R.string.pro_version_features_html), "text/html", "utf-8");

    dialog.show();
  }

}
