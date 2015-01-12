package com.cube.nanotimer.gui.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.cube.nanotimer.R;

public class ProVersionFeature {

  public static Dialog getDialog(Context context) {
    View v = ProVersionAd.getProVersionAdView(context);
    ((TextView) v.findViewById(R.id.tvText)).setText(R.string.need_pro_version);

    AlertDialog dialog = new AlertDialog.Builder(context).setView(v).create();
    dialog.setTitle(R.string.pro_version_promotion_title);
    dialog.setCanceledOnTouchOutside(true);
    return dialog;
  }

}
