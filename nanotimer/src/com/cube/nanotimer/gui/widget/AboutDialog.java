package com.cube.nanotimer.gui.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.Utils;

public class AboutDialog extends DialogFragment {

  public static AboutDialog newInstance() {
    return new AboutDialog();
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    View v = getActivity().getLayoutInflater().inflate(R.layout.about_dialog, null);

    TextView tvAppName = (TextView) v.findViewById(R.id.tvAppName);
    tvAppName.setText(tvAppName.getText().toString() + " v" + Utils.getAppVersion(getActivity()));

    final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(v).create();
    dialog.setCanceledOnTouchOutside(true);
    dialog.setCustomTitle(getTitleView());

    return dialog;
  }

  @Override
  public void show(FragmentManager manager, String tag) {
    if (manager.findFragmentByTag(tag) == null) {
      super.show(manager, tag);
    }
  }

  private View getTitleView() {
    LinearLayout view = new LinearLayout(getActivity());
    view.setOrientation(LinearLayout.VERTICAL);

    RelativeLayout titleLayout = new RelativeLayout(getActivity());
    titleLayout.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, 150));
    titleLayout.setPadding(10, 10, 10, 10);
    titleLayout.setGravity(Gravity.CENTER_VERTICAL);
    titleLayout.setBackgroundColor(getResources().getColor(R.color.graybg));

    ImageView imgIcon = new ImageView(getActivity());
    imgIcon.setImageResource(R.drawable.icon);

    ImageView imgTitle = new ImageView(getActivity());
    imgTitle.setImageResource(R.drawable.nanotimer);
    imgTitle.setPadding(0, 10, 10, 10);

    titleLayout.addView(imgIcon);
    titleLayout.addView(imgTitle);

    LinearLayout separator = new LinearLayout(getActivity());
    separator.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 3));
    separator.setBackgroundColor(getResources().getColor(R.color.iceblue));

    view.addView(titleLayout);
    view.addView(separator);

    return view;
  }

}
