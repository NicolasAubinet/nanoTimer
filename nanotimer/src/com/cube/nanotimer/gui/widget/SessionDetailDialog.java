package com.cube.nanotimer.gui.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.session.CubeBaseSession;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.util.helper.GUIUtils;
import com.cube.nanotimer.util.helper.ScreenUtils;
import com.cube.nanotimer.vo.SessionDetails;
import com.cube.nanotimer.vo.SolveType;

import java.util.List;

public class SessionDetailDialog extends DialogFragment {

  private static final int PAGE_LINES_COUNT = 10;
  private static final int TIMES_PER_LINE = 4;
  private static final String ARG_SOLVETYPE = "solvetype";

  private LayoutInflater inflater;
  private LinearLayout sessionTimesLayout;
  private Button buMore;
  private List<Long> sessionTimes;
  private int bestInd;
  private int worstInd;
  private int curPageInd = 0;

  public static SessionDetailDialog newInstance(SolveType solveType) {
    SessionDetailDialog sessionDetailDialog = new SessionDetailDialog();
    Bundle bundle = new Bundle();
    bundle.putSerializable(ARG_SOLVETYPE, solveType);
    sessionDetailDialog.setArguments(bundle);
    return sessionDetailDialog;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final View v = getActivity().getLayoutInflater().inflate(R.layout.sessiondetail_dialog, null);
    final SolveType solveType = (SolveType) getArguments().getSerializable(ARG_SOLVETYPE);
    App.INSTANCE.getService().getSessionDetails(solveType, new DataCallback<SessionDetails>() {
      @Override
      public void onData(final SessionDetails data) {
        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            displaySessionDetails(v, data);
          }
        });
      }
    });

    final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(v).create();
    dialog.setCanceledOnTouchOutside(true);
    return dialog;
  }

  private void displaySessionDetails(View v, SessionDetails sessionDetails) {
    sessionTimes = sessionDetails.getSessionTimes();
    CubeBaseSession session = new CubeBaseSession(sessionTimes);
    bestInd = (sessionTimes.size() < 5) ? -1 : session.getBestTimeInd(sessionTimes.size());
    worstInd = (sessionTimes.size() < 5) ? -1 : session.getWorstTimeInd(sessionTimes.size());
    buMore = (Button) v.findViewById(R.id.buMore);

    ((TextView) v.findViewById(R.id.tvSessionRA)).setText(FormatterService.INSTANCE.formatSolveTime(session.getRAOf(Math.max(5, sessionTimes.size()))));
    ((TextView) v.findViewById(R.id.tvSessionMean)).setText(FormatterService.INSTANCE.formatSolveTime(session.getMeanOf(sessionTimes.size())));
    ((TextView) v.findViewById(R.id.tvSessionSolves)).setText(String.valueOf(sessionDetails.getSessionSolvesCount()));
    ((TextView) v.findViewById(R.id.tvTotalSolves)).setText(String.valueOf(sessionDetails.getTotalSolvesCount()));
    sessionTimesLayout = (LinearLayout) v.findViewById(R.id.sessionTimesLayout);

    inflater = getActivity().getLayoutInflater();
    int sessionTimesCount = sessionTimes.size();
    if (sessionTimesCount == 0) {
      TableRow tr = new TableRow(getActivity());
      for (int i = 0; i < TIMES_PER_LINE; i++) {
        TextView tv = getNewSolveTimeTextView();
        tr.addView(tv);
      }
      sessionTimesLayout.addView(tr);
    } else if (sessionTimesCount < TIMES_PER_LINE) {
      TableRow tr = new TableRow(getActivity());
      for (Long time : sessionTimes) {
        TextView tv = getNewSolveTimeTextView();
        tv.setText(FormatterService.INSTANCE.formatSolveTime(time));
        tr.addView(tv);
      }
      sessionTimesLayout.addView(tr);
    } else {
      addSolveTimesPage();
    }

    adjustSessionTimesLayoutParams();

    buMore.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        addSolveTimesPage();
      }
    });
  }

  private void addSolveTimesPage() {
    TableRow tr = new TableRow(getActivity());
    int sessionTimesCount = sessionTimes.size();
    int timesPerPage = PAGE_LINES_COUNT * TIMES_PER_LINE;
    int pageStartInd = curPageInd * timesPerPage;
    int pageEndInd = Math.min(sessionTimesCount, pageStartInd + timesPerPage);
    for (int i = pageStartInd; i < pageEndInd; i++) {
      TextView tv = getNewSolveTimeTextView();
      GUIUtils.setSessionTimeCellText(tv, sessionTimes.get(i), i, bestInd, worstInd);
      tr.addView(tv);
      if (i % TIMES_PER_LINE == 3) {
        sessionTimesLayout.addView(tr);
        tr = new TableRow(getActivity());
      }
    }
    if (pageEndInd < sessionTimesCount) { // some more times remain
      buMore.setVisibility(View.VISIBLE);
    } else { // all times are displayed
      buMore.setVisibility(View.GONE);
      // add remaining cells to have the same cells count than above lines
      if (sessionTimesCount % TIMES_PER_LINE != 0) {
        sessionTimesLayout.addView(tr);
        for (int i = 0; i < TIMES_PER_LINE - (sessionTimesCount % TIMES_PER_LINE); i++) {
          TextView tv = getNewSolveTimeTextView();
          tr.addView(tv);
        }
      }
    }
    adjustSessionTimesLayoutParams();
    curPageInd++;
  }

  private void adjustSessionTimesLayoutParams() {
    int startInd = curPageInd * PAGE_LINES_COUNT;
    for (int i = startInd; i < sessionTimesLayout.getChildCount(); i++) {
      if (sessionTimesLayout.getChildAt(i) instanceof TableRow) {
        TableRow tr = (TableRow) sessionTimesLayout.getChildAt(i);
        for (int j = 0; j < tr.getChildCount(); j++) {
          TextView tv = (TextView) tr.getChildAt(j);
          LayoutParams params = (LayoutParams) tv.getLayoutParams();
          params.setMargins(2, 2, 2, 2);
          params.height = ScreenUtils.dipToPixels(26);
          tv.setLayoutParams(params);
        }
      }
    }
  }

  private TextView getNewSolveTimeTextView() {
    TextView tv = (TextView) inflater.inflate(R.layout.timecell_textview, null);
    tv.setTextSize(15);
    return tv;
  }

}
