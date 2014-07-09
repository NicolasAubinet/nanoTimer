package com.cube.nanotimer.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.SpannedString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.scrambler.ScramblerFactory;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.CubeSession;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.util.Utils;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimerActivity extends Activity {

  enum TimerState { STOPPED, STARTED }

  private TextView tvTimer;
  private TextView tvScramble;
  private TextView tvCubeType;
  private TextView tvRA5;
  private TextView tvRA12;
  private ViewGroup layout;
  private TableLayout layoutSessionTimes;

  private CubeType cubeType;
  private SolveType solveType;
  private String[] currentScramble;
  private SolveTime lastSolveTime;
  private CubeSession cubeSession;
  private SolveAverages solveAverages;

  private final long REFRESH_INTERVAL = 25;
  private final int INSPECTION_LIMIT = 15;
  private Timer timer;
  private Handler timerHandler = new Handler();
  private Object timerSync = new Object();
  private long timerStartTs;
  private volatile TimerState timerState = TimerState.STOPPED;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.timer);
    App.INSTANCE.setContext(this);
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    cubeType = (CubeType) getIntent().getSerializableExtra("cubeType");
    solveType = (SolveType) getIntent().getSerializableExtra("solveType");
    App.INSTANCE.getService().getSolveAverages(solveType, new SolveAverageCallback());

    initViews();

    resetTimer();
    setCubeTypeText();

    App.INSTANCE.getService().getSessionTimes(solveType, new DataCallback<List<Long>>() {
      @Override
      public void onData(List<Long> data) {
        cubeSession = new CubeSession(data);
        refreshSessionFields();
      }
    });

    generateScramble();
  }

  private void initViews() {
    tvTimer = (TextView) findViewById(R.id.tvTimer);
    tvScramble = (TextView) findViewById(R.id.tvScramble);
    tvCubeType = (TextView) findViewById(R.id.tvCubeType);
    tvRA5 = (TextView) findViewById(R.id.tvRA5);
    tvRA12 = (TextView) findViewById(R.id.tvRA12);
    layoutSessionTimes = (TableLayout) findViewById(R.id.layoutSessionTimes);

    layout = (ViewGroup) findViewById(R.id.layoutTimer);
    layout.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        if (timerState == TimerState.STARTED) {
          stopTimer(true);
          return false;
        } else if (timerState == TimerState.STOPPED) {
          if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            startInspectionTimer();
          } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            stopInspectionTimer();
            startTimer();
          }
        }
        return true;
      }
    });
  }

  private void setCubeTypeText() {
    StringBuilder sb = new StringBuilder();
    sb.append(cubeType.getName());
    if (!solveType.getName().equals(getString(R.string.def))) {
      sb.append(" (").append(solveType.getName()).append(")");
    }
    tvCubeType.setText(sb.toString());
  }

  @Override
  public void onBackPressed() {
    if (timerState == TimerState.STARTED) {
      stopTimer(false);
      resetTimer();
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.timer_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (lastSolveTime != null && timerState == TimerState.STOPPED) {
      switch (item.getItemId()) {
        case R.id.itPlusTwo:
          if (lastSolveTime.getTime() > 0 && !lastSolveTime.isPlusTwo()) {
            lastSolveTime.plusTwo();
            App.INSTANCE.getService().saveTime(lastSolveTime, new SolveAverageCallback());
            tvTimer.setText(FormatterService.INSTANCE.formatSolveTime(lastSolveTime.getTime()));
            cubeSession.setLastAsPlusTwo();
            refreshSessionFields();
          }
          break;
        case R.id.itDNF:
          if (lastSolveTime.getTime() > 0) {
            lastSolveTime.setTime(-1);
            App.INSTANCE.getService().saveTime(lastSolveTime, new SolveAverageCallback());
            tvTimer.setText(FormatterService.INSTANCE.formatSolveTime(lastSolveTime.getTime()));
            cubeSession.setLastAsDNF();
            refreshSessionFields();
          }
          break;
        case R.id.itDelete:
          App.INSTANCE.getService().removeTime(lastSolveTime, new SolveAverageCallback());
          cubeSession.deleteLast();
          refreshSessionFields();
          resetTimer();
          break;
        case R.id.itNewSession:
          App.INSTANCE.getService().startNewSession(solveType, System.currentTimeMillis(), null);
          cubeSession.clearSession();
          refreshSessionFields();
          break;
      }
    }
    return true;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    String timerText = tvTimer.getText().toString();
    SpannedString scrambleText = (SpannedString) tvScramble.getText();
    String cubeTypeText = tvCubeType.getText().toString();

    setContentView(R.layout.timer);
    initViews();

    tvTimer.setText(timerText);
    tvScramble.setText(scrambleText);
    tvCubeType.setText(cubeTypeText);
    refreshSessionFields();
    refreshAvgFields();
  }

  private void refreshSessionFields() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        clearSessionTextViews();
        if (cubeSession == null) {
          return;
        }
        List<Long> sessionTimes = cubeSession.getSessionTimes();
        if (!sessionTimes.isEmpty()) {
          int bestInd = (sessionTimes.size() < 5) ? -1 : cubeSession.getBestTimeInd(sessionTimes.size());
          int worstInd = (sessionTimes.size() < 5) ? -1 : cubeSession.getWorstTimeInd(sessionTimes.size());
          for (int i = 0; i < sessionTimes.size(); i++) {
            String strTime = FormatterService.INSTANCE.formatSolveTime(sessionTimes.get(i));
            StringBuilder sbTimes = new StringBuilder();
            TextView tv = getSessionTextView(i);
            if (i == bestInd) {
              sbTimes.append("<font color='").append(getResources().getColor(R.color.green)).append("'>");
              sbTimes.append(strTime).append("</font>");
              tv.setText(Html.fromHtml(sbTimes.toString()));
            } else if (i == worstInd) {
              sbTimes.append("<font color='").append(getResources().getColor(R.color.red)).append("'>");
              sbTimes.append(strTime).append("</font>");
              tv.setText(Html.fromHtml(sbTimes.toString()));
            } else {
              tv.setText(strTime);
            }
          }
        }
        tvRA5.setText(FormatterService.INSTANCE.formatSolveTime(cubeSession.getAverageOfFive()));
        tvRA12.setText(FormatterService.INSTANCE.formatSolveTime(cubeSession.getAverageOfTwelve()));
      }
    });
  }

  private void clearSessionTextViews() {
    for (int i = 0; i < layoutSessionTimes.getChildCount(); i++) {
      TableRow tr = (TableRow) layoutSessionTimes.getChildAt(i);
      for (int j = 0; j < tr.getChildCount(); j++) {
        ((TextView) tr.getChildAt(j)).setText("");
      }
    }
  }

  private TextView getSessionTextView(int i) {
    View v = null;
    if (i >= 0 && i < 4) {
      v = ((TableRow) layoutSessionTimes.getChildAt(0)).getChildAt(i);
    } else if (i >= 4 && i < 8) {
      v = ((TableRow) layoutSessionTimes.getChildAt(1)).getChildAt(i - 4);
    } else if (i >= 8 && i < 12) {
      v = ((TableRow) layoutSessionTimes.getChildAt(2)).getChildAt(i - 8);
    }
    return (TextView) v;
  }

  private void startTimer() {
    timerStartTs = System.currentTimeMillis();
    timer = new Timer();
    TimerTask timerTask = new TimerTask() {
      public void run() {
        timerHandler.post(new Runnable() {
          public void run() {
            synchronized (timerSync) {
              if (timerState == TimerState.STARTED) {
                updateTimerText(System.currentTimeMillis() - timerStartTs);
              }
            }
          }
        });
      }
    };
    timer.schedule(timerTask, 1, REFRESH_INTERVAL);
    timerState = TimerState.STARTED;
  }

  private void stopTimer(boolean save) {
    long time = (System.currentTimeMillis() - timerStartTs);
    timerState = TimerState.STOPPED;
    if (timer != null) {
      timer.cancel();
      timer.purge();
    }
    // update time once more to get the ms right
    // (as all ms do not necessarily appear when timing, some are skipped due to refresh interval)
    updateTimerText(time);
    if (save) {
      saveTime(time);
    }
    generateScramble();
  }

  private void startInspectionTimer() {
    timerStartTs = System.currentTimeMillis();
    layout.setBackgroundColor(getResources().getColor(R.color.nightblue));
    tvCubeType.setText(getString(R.string.inspection));
    timer = new Timer();
    TimerTask timerTask = new TimerTask() {
      public void run() {
        timerHandler.post(new Runnable() {
          public void run() {
            updateInspectionTimerText();
          }
        });
      }
    };
    timer.schedule(timerTask, 1, 1000);
  }

  private void stopInspectionTimer() {
    if (timer != null) {
      timer.cancel();
      timer.purge();
    }
    layout.setBackgroundColor(getResources().getColor(R.color.black));
    setCubeTypeText();
  }

  private void resetTimer() {
    synchronized (timerSync) {
      if (timerState == TimerState.STARTED && timer != null) {
        timer.cancel();
        timer.purge();
      }
      timerStartTs = 0;
      tvTimer.setText("0.00");
    }
  }

  private void saveTime(long time) {
    SolveTime solveTime = new SolveTime();
    solveTime.setTime(time);
    solveTime.setTimestamp(System.currentTimeMillis());
    solveTime.setSolveType(solveType);
    solveTime.setScramble(FormatterService.INSTANCE.formatScrambleAsSingleLine(currentScramble, cubeType));
    App.INSTANCE.getService().saveTime(solveTime, new SolveAverageCallback());
    if (cubeSession != null) {
      cubeSession.addTime(time);
      refreshSessionFields();
    }
  }

  private String formatAvgField(Long f) {
    return FormatterService.INSTANCE.formatSolveTime(f, "-");
  }

  private synchronized void updateTimerText(long curTime) {
    tvTimer.setText(FormatterService.INSTANCE.formatSolveTime(curTime));
  }

  private synchronized void updateInspectionTimerText() {
    long curTime = System.currentTimeMillis() - timerStartTs;
    int seconds = (int) (curTime / 1000) % 60;
    tvTimer.setText(String.valueOf(seconds));
    if (seconds == INSPECTION_LIMIT) {
      Utils.playSound(R.raw.beep2);
      layout.setBackgroundColor(getResources().getColor(R.color.darkred));
    } else if (seconds >= INSPECTION_LIMIT - 3 && seconds < INSPECTION_LIMIT) {
      Utils.playSound(R.raw.beep1);
    }
  }

  private void generateScramble() {
    if (cubeType != null) {
      currentScramble = ScramblerFactory.getScrambler(cubeType).getNewScramble();
      tvScramble.setText(FormatterService.INSTANCE.formatToColoredScramble(currentScramble, cubeType));
    }
  }

  private void refreshAvgFields() {
    ((TextView) findViewById(R.id.tvAvgOfFive)).setText(formatAvgField(solveAverages.getAvgOf5()));
    ((TextView) findViewById(R.id.tvAvgOfTwelve)).setText(formatAvgField(solveAverages.getAvgOf12()));
    ((TextView) findViewById(R.id.tvAvgOfHundred)).setText(formatAvgField(solveAverages.getAvgOf100()));
    ((TextView) findViewById(R.id.tvBestOfFive)).setText(formatAvgField(solveAverages.getBestOf5()));
    ((TextView) findViewById(R.id.tvBestOfTwelve)).setText(formatAvgField(solveAverages.getBestOf12()));
    ((TextView) findViewById(R.id.tvBestOfHundred)).setText(formatAvgField(solveAverages.getBestOf100()));
    ((TextView) findViewById(R.id.tvLifetimeAvg)).setText(FormatterService.INSTANCE.formatSolveTime(
        solveAverages.getAvgOfLifetime(), getString(R.string.NA)));
    ((TextView) findViewById(R.id.tvLifetimeBest)).setText(FormatterService.INSTANCE.formatSolveTime(
        solveAverages.getBestOfLifetime(), getString(R.string.NA)));
  }

  private class SolveAverageCallback extends DataCallback<SolveAverages> {
    @Override
    public void onData(final SolveAverages data) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          solveAverages = data;
          lastSolveTime = data.getSolveTime();
          refreshAvgFields();
        }
      });
    }
  }

}
