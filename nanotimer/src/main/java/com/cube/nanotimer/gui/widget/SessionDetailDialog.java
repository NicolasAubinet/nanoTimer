package com.cube.nanotimer.gui.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.session.TimesStatistics;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.util.helper.GUIUtils;
import com.cube.nanotimer.util.helper.ScreenUtils;
import com.cube.nanotimer.vo.SessionDetails;
import com.cube.nanotimer.vo.SolveType;

import java.util.ArrayList;
import java.util.List;

public class SessionDetailDialog extends NanoTimerDialogFragment {

  private static final int PAGE_LINES_COUNT = 10;
  private static final int TIMES_PER_LINE = 4;
  private static final int SESSION_TIMES_HEIGHT_DP = 26;
  private static final int BEST_AVERAGES_HEIGHT_DP = 22;
  private static final String ARG_SOLVETYPE = "solvetype";

  private LayoutInflater inflater;
  private TextView tvSessionStart;
  private Spinner spSessionsList;
  private ArrayAdapter<String> spinnerAdapter;
  private TableLayout sessionTimesLayout;
  private List<Long> sessionStarts;
  private boolean sessionStartsInitialized;
  private List<Long> sessionTimes;
  private SolveType solveType;
  private int bestInd;
  private int worstInd;

  public static SessionDetailDialog newInstance(SolveType solveType) {
    SessionDetailDialog sessionDetailDialog = new SessionDetailDialog();
    Bundle bundle = new Bundle();
    bundle.putSerializable(ARG_SOLVETYPE, solveType);
    sessionDetailDialog.setArguments(bundle);
    return sessionDetailDialog;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    inflater = getActivity().getLayoutInflater();
    final View v = inflater.inflate(R.layout.sessiondetail_dialog, null);
    solveType = (SolveType) getArguments().getSerializable(ARG_SOLVETYPE);
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
    View titleView = getTitleView();
    if (App.INSTANCE.isProEnabled()) {
      initSessionsList(v);
    }

    final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(v).create();
    dialog.setCustomTitle(titleView);
    dialog.setCanceledOnTouchOutside(true);
    return dialog;
  }

  private void displaySessionDetails(View v, SessionDetails sessionDetails) {
    sessionTimes = sessionDetails.getSessionTimes();
    TimesStatistics session = new TimesStatistics(sessionTimes);
    bestInd = session.getBestTimeInd(solveType.isBlind());
    worstInd = session.getWorstTimeInd(solveType.isBlind());

    if (!App.INSTANCE.isProEnabled()) { // pro uses a spinner, not a textview
      tvSessionStart.setText(FormatterService.INSTANCE.formatDateTimeWithoutSeconds(sessionDetails.getSessionStart()));
    }

    if (solveType.isBlind()) {
      v.findViewById(R.id.bestAveragesLayout).setVisibility(View.GONE);
      v.findViewById(R.id.trBestMeanOfThree).setVisibility(View.VISIBLE);
      v.findViewById(R.id.trAccuracy).setVisibility(View.VISIBLE);
      ((TextView) v.findViewById(R.id.tvBestMeanOfThree)).setText(FormatterService.INSTANCE.formatSolveTime(getBestMeanOf(sessionTimes, 3)));
      ((TextView) v.findViewById(R.id.tvAccuracy)).setText(FormatterService.INSTANCE.formatPercentage(session.getAccuracy(sessionTimes.size())));
      ((TextView) v.findViewById(R.id.tvLabelAverage)).setText(R.string.session_success_average);
      long successAverage = session.getSuccessAverageOf(sessionTimes.size(), true);
      ((TextView) v.findViewById(R.id.tvAverage)).setText(FormatterService.INSTANCE.formatSolveTime(successAverage));
    } else {
      setupBestAverages(v, sessionTimes);
      ((TextView) v.findViewById(R.id.tvAverage)).setText(FormatterService.INSTANCE.formatSolveTime(session.getAverageOf(sessionTimes.size())));
    }

    ((TextView) v.findViewById(R.id.tvSolves)).setText(String.valueOf(sessionDetails.getSessionSolvesCount()));
    ((TextView) v.findViewById(R.id.tvBest)).setText(FormatterService.INSTANCE.formatSolveTime(session.getBestTime(sessionTimes.size())));
    ((TextView) v.findViewById(R.id.tvDeviation)).setText(FormatterService.INSTANCE.formatSolveTime(session.getDeviation(sessionTimes.size())));
    sessionTimesLayout = (TableLayout) v.findViewById(R.id.sessionTimesLayout);
    sessionTimesLayout.removeAllViews();

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

    adjustTableLayoutParams(sessionTimesLayout, SESSION_TIMES_HEIGHT_DP);
  }

  private long getBestMeanOf(List<Long> times, int n) {
    long best = Long.MAX_VALUE;
    for (int i = 0; i <= times.size() - n; i++) {
      TimesStatistics session = new TimesStatistics(times.subList(i, Math.min(i + n, times.size())));
      long mean = session.getMeanOf(n);
      if (mean > 0 && mean < best) {
        best = mean;
      }
    }
    return best == Long.MAX_VALUE ? -2 : best;
  }

  private long getBestAverageOf(List<Long> times, int n) {
    long best = Long.MAX_VALUE;
    for (int i = 0; i <= times.size() - n; i++) {
      TimesStatistics session = new TimesStatistics(times.subList(i, Math.min(i + n, times.size())));
      long avg = session.getAverageOf(n);
      if (avg > 0 && avg < best) {
        best = avg;
      }
    }
    return best == Long.MAX_VALUE ? -2 : best;
  }

  private void setupBestAverages(View v, List<Long> times) {
    long avg5 = getBestAverageOf(times, 5);
    long avg12 = getBestAverageOf(times, 12);
    long avg50 = getBestAverageOf(times, 50);
    long avg100 = getBestAverageOf(times, 100);

    if (avg5 < 0 && avg12 < 0 && avg50 < 0 && avg100 < 0) {
      v.findViewById(R.id.bestAveragesLayout).setVisibility(View.GONE);
      return;
    }
    TableLayout bestAveragesTableLayout = (TableLayout) v.findViewById(R.id.bestAveragesTableLayout);
    bestAveragesTableLayout.removeAllViews();
    TableRow trHeaders = new TableRow(getActivity());
    TableRow trAverages = new TableRow(getActivity());
    if (avg5 > 0) {
      addToBestAveragesTable(trHeaders, trAverages, 5, avg5);
    }
    if (avg12 > 0) {
      addToBestAveragesTable(trHeaders, trAverages, 12, avg12);
    }
    if (avg50 > 0) {
      addToBestAveragesTable(trHeaders, trAverages, 50, avg50);
    }
    if (avg100 > 0) {
      addToBestAveragesTable(trHeaders, trAverages, 100, avg100);
    }
    bestAveragesTableLayout.addView(trHeaders);
    bestAveragesTableLayout.addView(trAverages);
    adjustTableLayoutParams(bestAveragesTableLayout, BEST_AVERAGES_HEIGHT_DP);
  }

  private void addToBestAveragesTable(TableRow trHeaders, TableRow trAverages, int avgHeader, long average) {
    TextView tv = getNewSolveTimeTextView();
    tv.setTextColor(getResources().getColor(R.color.lightblue));
    tv.setText(String.valueOf(avgHeader));
    tv.setTypeface(null, Typeface.BOLD);
    trHeaders.addView(tv);

    tv = getNewSolveTimeTextView();
    tv.setText(FormatterService.INSTANCE.formatSolveTime(average));
    trAverages.addView(tv);
  }

  private void addSolveTimesPage() {
    TableRow tr = new TableRow(getActivity());
    int sessionTimesCount = sessionTimes.size();
    for (int i = 0; i < sessionTimesCount; i++) {
      TextView tv = getNewSolveTimeTextView();
      GUIUtils.setSessionTimeCellText(tv, sessionTimes.get(i), i, bestInd, worstInd, solveType.isBlind());
      tr.addView(tv);
      if (i % TIMES_PER_LINE == 3) {
        sessionTimesLayout.addView(tr);
        tr = new TableRow(getActivity());
      }
    }
    // add remaining cells to have the same cells count than the above lines
    if (sessionTimesCount % TIMES_PER_LINE != 0) {
      sessionTimesLayout.addView(tr);
      for (int i = 0; i < TIMES_PER_LINE - (sessionTimesCount % TIMES_PER_LINE); i++) {
        TextView tv = getNewSolveTimeTextView();
        tr.addView(tv);
      }
    }
  }

  private void adjustTableLayoutParams(TableLayout tableLayout, int tvHeightDp) {
    for (int i = 0; i < tableLayout.getChildCount(); i++) {
      if (tableLayout.getChildAt(i) instanceof TableRow) {
        TableRow tr = (TableRow) tableLayout.getChildAt(i);
        for (int j = 0; j < tr.getChildCount(); j++) {
          TextView tv = (TextView) tr.getChildAt(j);
          LayoutParams params = (LayoutParams) tv.getLayoutParams();
          params.setMargins(2, 2, 2, 2);
          params.height = ScreenUtils.dipToPixels(tvHeightDp);
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

  private void initSessionsList(final View v) {
    App.INSTANCE.getService().getSessionStarts(solveType, new DataCallback<List<Long>>() {
      @Override
      public void onData(List<Long> data) {
        sessionStarts = data;
        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            List<String> sessionStartsTexts = new ArrayList<String>();
            for (long sessionStart : sessionStarts) {
              sessionStartsTexts.add(FormatterService.INSTANCE.formatDateTimeWithoutSeconds(sessionStart));
            }
            spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, sessionStartsTexts);
            spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            spSessionsList.setAdapter(spinnerAdapter);
          }
        });
      }
    });

    spSessionsList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (!sessionStartsInitialized) { // used to avoid calling the service twice when dialog is opened
          sessionStartsInitialized = true;
          return;
        }
        long from = sessionStarts.get(i);
        long to = (i-1 >= 0) ? sessionStarts.get(i-1) : System.currentTimeMillis();
        App.INSTANCE.getService().getSessionDetails(solveType, from, to, new DataCallback<SessionDetails>() {
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
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });
  }

  private View getTitleView() {
    int fontSize = 20;
    LinearLayout view = new LinearLayout(getActivity());
    view.setOrientation(LinearLayout.VERTICAL);

    RelativeLayout titleLayout = new RelativeLayout(getActivity());
    titleLayout.setPadding(10, 10, 10, 10);
    titleLayout.setGravity(Gravity.CENTER_VERTICAL);
    titleLayout.setBackgroundColor(getResources().getColor(R.color.graybg));

    TextView tvStart = new TextView(getActivity());
    tvStart.setPadding(10, 0, 0, 0);
    tvStart.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
    tvStart.setText(R.string.session_start);
    RelativeLayout.LayoutParams tvStartParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    tvStartParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

    View sessionView;
    if (App.INSTANCE.isProEnabled()) {
      spSessionsList = new Spinner(getActivity());
      sessionView = spSessionsList;
    } else {
      tvSessionStart = new TextView(getActivity());
      tvSessionStart.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
      sessionView = tvSessionStart;
    }
    sessionView.setPadding(0, 0, 5, 0);
    RelativeLayout.LayoutParams sessionViewParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    sessionViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

    titleLayout.addView(tvStart, tvStartParams);
    titleLayout.addView(sessionView, sessionViewParams);

    LinearLayout separator = new LinearLayout(getActivity());
    separator.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 3));
    separator.setBackgroundColor(getResources().getColor(R.color.iceblue));

    view.addView(titleLayout);
    view.addView(separator);

    return view;
  }

}
