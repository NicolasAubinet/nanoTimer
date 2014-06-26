package com.cube.nanotimer.activity.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveTime;

public class HistoryDetailFragment extends DialogFragment {

  private static final String ARG_SOLVETIME = "solvetime";
  private static final String ARG_CUBETYPE = "cubetype";

  private boolean plusTwo = false;

  public static HistoryDetailFragment newInstance(SolveTime solveTime, CubeType cubeType) {
    HistoryDetailFragment hd = new HistoryDetailFragment();
    Bundle bundle = new Bundle();
    bundle.putSerializable(ARG_SOLVETIME, solveTime);
    bundle.putSerializable(ARG_CUBETYPE, cubeType);
    hd.setArguments(bundle);
    return hd;
  }

  private HistoryDetailFragment() {
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    View v = getActivity().getLayoutInflater().inflate(R.layout.historydetail, null);

    final SolveTime solveTime = (SolveTime) getArguments().getSerializable(ARG_SOLVETIME);
    final CubeType cubeType = (CubeType) getArguments().getSerializable(ARG_CUBETYPE);

    final TextView tvTime = (TextView) v.findViewById(R.id.tvTime);
    TextView tvScramble = (TextView) v.findViewById(R.id.tvScramble);
    TextView tvPlusTwo = (TextView) v.findViewById(R.id.tvPlusTwo);
    TextView tvDNF = (TextView) v.findViewById(R.id.tvDNF);
    TextView tvDelete = (TextView) v.findViewById(R.id.tvDelete);

    String fScramble = FormatterService.INSTANCE.formatScramble(solveTime.getScramble(), cubeType);
    tvScramble.setText(FormatterService.INSTANCE.formatToColoredScramble(fScramble));
    refreshTime(tvTime, solveTime.getTime());

    tvPlusTwo.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (!plusTwo) {
          solveTime.setTime(solveTime.getTime() + 2000);
          plusTwo = true;
          refreshTime(tvTime, solveTime.getTime());
          // TODO : update solveTime in DB
        }
      }
    });

    tvDNF.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        solveTime.setTime(-1);
        refreshTime(tvTime, solveTime.getTime());
        // TODO : update solveTime in DB
      }
    });

    tvDelete.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        // TODO : delete solveTime from DB
      }
    });

    AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(v).create();
    return dialog;
  }

  private void refreshTime(TextView tvTime, Long time) {
    tvTime.setText(FormatterService.INSTANCE.formatSolveTime(time));
  }

}
