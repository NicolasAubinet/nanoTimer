package com.cube.nanotimer.activity.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.util.view.FontFitTextView;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;

import java.util.Arrays;

public class HistoryDetailDialog extends DialogFragment {

  private static final String ARG_SOLVETIME = "solvetime";
  private static final String ARG_CUBETYPE = "cubetype";

  private TimeChangedHandler handler;

  public static HistoryDetailDialog newInstance(SolveTime solveTime, CubeType cubeType, TimeChangedHandler handler) {
    HistoryDetailDialog hd = new HistoryDetailDialog(handler);
    Bundle bundle = new Bundle();
    bundle.putSerializable(ARG_SOLVETIME, solveTime);
    bundle.putSerializable(ARG_CUBETYPE, cubeType);
    hd.setArguments(bundle);
    return hd;
  }

  private HistoryDetailDialog(TimeChangedHandler handler) {
    this.handler = handler;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    View v = getActivity().getLayoutInflater().inflate(R.layout.historydetail_dialog, null);

    final SolveTime solveTime = (SolveTime) getArguments().getSerializable(ARG_SOLVETIME);
    final CubeType cubeType = (CubeType) getArguments().getSerializable(ARG_CUBETYPE);

    final TextView tvTime = (TextView) v.findViewById(R.id.tvTime);
    FontFitTextView tvScramble = (FontFitTextView) v.findViewById(R.id.tvScramble);
    TextView tvPlusTwo = (TextView) v.findViewById(R.id.tvPlusTwo);
    TextView tvDNF = (TextView) v.findViewById(R.id.tvDNF);
    TextView tvDelete = (TextView) v.findViewById(R.id.tvDelete);

    if (!solveTime.hasSteps()) {
      v.findViewById(R.id.trSteps).setVisibility(View.GONE);
    } else {
      ((TextView) v.findViewById(R.id.tvSteps)).setText(
          FormatterService.INSTANCE.formatStepsTimes(Arrays.asList(solveTime.getStepsTimes())));
    }

    tvScramble.setText(FormatterService.INSTANCE.formatToColoredScramble(solveTime.getScramble(), cubeType));
    tvTime.setText(FormatterService.INSTANCE.formatSolveTime(solveTime.getTime()));

    final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(v).create();
    dialog.setCanceledOnTouchOutside(true);

    tvPlusTwo.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (solveTime.getTime() > 0 && !solveTime.isPlusTwo()) {
          solveTime.plusTwo();
          saveTime(solveTime);
          handler.onTimeChanged(solveTime);
        }
        dialog.dismiss();
      }
    });

    tvDNF.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (solveTime.getTime() > 0) {
          solveTime.setTime(-1);
          saveTime(solveTime);
          handler.onTimeChanged(solveTime);
        }
        dialog.dismiss();
      }
    });

    tvDelete.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        App.INSTANCE.getService().removeTime(solveTime, new DataCallback<SolveAverages>() {
          public void onData(SolveAverages data) {
          }
        });
        handler.onTimeDeleted(solveTime);
        dialog.dismiss();
      }
    });

    return dialog;
  }

  private void saveTime(SolveTime solveTime) {
    App.INSTANCE.getService().saveTime(solveTime, new DataCallback<SolveAverages>() {
      @Override
      public void onData(SolveAverages data) {
      }
    });
  }

  @Override
  public void show(FragmentManager manager, String tag) {
    if (manager.findFragmentByTag(tag) == null) {
      super.show(manager, tag);
    }
  }

}