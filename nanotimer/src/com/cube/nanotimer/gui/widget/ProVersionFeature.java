package com.cube.nanotimer.gui.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.cube.nanotimer.ProChecker;
import com.cube.nanotimer.R;

public class ProVersionFeature {

  public static Dialog getDialog(Context context) {
    View v = ProVersionAd.getProVersionAdView(context);
    ProChecker.ProState proState = ProChecker.getProState(context);
    if (proState == ProChecker.ProState.UNINSTALLED) {
      ((TextView) v.findViewById(R.id.tvText)).setText(R.string.need_pro_version);
    } else if (proState == ProChecker.ProState.INVALID_VERSION) {
      ((TextView) v.findViewById(R.id.tvText)).setText(R.string.upgrade_pro_app);
    }

    AlertDialog dialog = new AlertDialog.Builder(context).setView(v).create();
    dialog.setTitle(R.string.pro_version_promotion_title);
    dialog.setCanceledOnTouchOutside(true);
    return dialog;
  }

}
