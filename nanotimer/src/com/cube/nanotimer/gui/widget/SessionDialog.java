package com.cube.nanotimer.gui.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.vo.SessionDetails;
import com.cube.nanotimer.vo.SolveType;

public class SessionDialog extends DialogFragment {

  private static final String ARG_SOLVETYPE = "solvetype";

  private LayoutInflater inflater;

  public static SessionDialog newInstance(SolveType solveType) {
    SessionDialog sessionDialog = new SessionDialog();
    Bundle bundle = new Bundle();
    bundle.putSerializable(ARG_SOLVETYPE, solveType);
    sessionDialog.setArguments(bundle);
    return sessionDialog;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final View v = getActivity().getLayoutInflater().inflate(R.layout.sessiondetail_dialog, null);
    final SolveType solveType = (SolveType) getArguments().getSerializable(ARG_SOLVETYPE);
    App.INSTANCE.getService().getSessionDetails(solveType, new DataCallback<SessionDetails>() {
      @Override
      public void onData(SessionDetails data) {
        displaySessionDetails(v, data);
      }
    });

    final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(v).create();
    dialog.setCanceledOnTouchOutside(true);
    return dialog;
  }

  private void displaySessionDetails(View v, SessionDetails sessionDetails) {
    ((TextView) v.findViewById(R.id.tvSessionRA)).setText(FormatterService.INSTANCE.formatSolveTime(sessionDetails.getRA()));
    ((TextView) v.findViewById(R.id.tvSessionMean)).setText(FormatterService.INSTANCE.formatSolveTime(sessionDetails.getMean()));
    ((TextView) v.findViewById(R.id.tvSessionSolves)).setText(String.valueOf(sessionDetails.getSessionSolvesCount()));
    ((TextView) v.findViewById(R.id.tvTotalSolves)).setText(String.valueOf(sessionDetails.getTotalSolvesCount()));
    LinearLayout sessionTimesLayout = (LinearLayout) v.findViewById(R.id.sessionTimesLayout);

    inflater = getActivity().getLayoutInflater();
    int sessionTimesCount = sessionDetails.getSessionTimes().size();
    if (sessionTimesCount == 0) {
      TableRow tr = new TableRow(getActivity());
      for (int i = 0; i < 4; i++) {
        TextView tv = getSolveTimeTextView();
        tr.addView(tv);
      }
      sessionTimesLayout.addView(tr);
    } else if (sessionTimesCount < 4) {
      TableRow tr = new TableRow(getActivity());
      for (Long time : sessionDetails.getSessionTimes()) {
        TextView tv = getSolveTimeTextView();
        tv.setText(FormatterService.INSTANCE.formatSolveTime(time));
        tr.addView(tv);
      }
      sessionTimesLayout.addView(tr);
    } else {
      TableRow tr = new TableRow(getActivity());
      for (int i = 0; i < sessionTimesCount; i++) {
        TextView tv = getSolveTimeTextView();
        tv.setText(FormatterService.INSTANCE.formatSolveTime(sessionDetails.getSessionTimes().get(i)));
        tr.addView(tv);

        if (i % 4 == 3) {
          sessionTimesLayout.addView(tr);
          tr = new TableRow(getActivity());
        }
      }
      if (sessionTimesCount % 4 != 0) {
        sessionTimesLayout.addView(tr);
        for (int i = 0; i < 4 - (sessionTimesCount % 4); i++) {
          TextView tv = getSolveTimeTextView();
          tr.addView(tv);
        }
      }
    }

    // adjust session times layout parameters
    for (int i = 0; i < sessionTimesLayout.getChildCount(); i++) {
      if (sessionTimesLayout.getChildAt(i) instanceof TableRow) {
        TableRow tr = (TableRow) sessionTimesLayout.getChildAt(i);
        for (int j = 0; j < tr.getChildCount(); j++) {
          TextView tv = (TextView) tr.getChildAt(j);
          LayoutParams params = (LayoutParams) tv.getLayoutParams();
          params.setMargins(2, 2, 2, 2);
          params.height = 32;
          tv.setLayoutParams(params);
        }
      }
    }
  }

  private TextView getSolveTimeTextView() {
    return (TextView) inflater.inflate(R.layout.timecell_textview, null);
  }

}
