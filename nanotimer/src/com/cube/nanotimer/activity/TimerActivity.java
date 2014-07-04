package com.cube.nanotimer.activity;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
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
  private TextView tvInspection;
  private TextView tvScramble;
  private TextView tvSessionTimes;
  private TextView tvCubeType;
  private TextView tvRA5;
  private TextView tvRA12;
  private RelativeLayout layout;

  private CubeType cubeType;
  private SolveType solveType;
  private String[] currentScramble;
  private SolveTime lastSolveTime;
  private CubeSession cubeSession;

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

    tvTimer = (TextView) findViewById(R.id.tvTimer);
    tvInspection = (TextView) findViewById(R.id.tvInspection);
    tvScramble = (TextView) findViewById(R.id.tvScramble);
    tvSessionTimes = (TextView) findViewById(R.id.tvSessionTimes);
    tvCubeType = (TextView) findViewById(R.id.tvCubeType);
    tvRA5 = (TextView) findViewById(R.id.tvRA5);
    tvRA12 = (TextView) findViewById(R.id.tvRA12);

    resetTimer();
    setCubeTypeText();

    layout = (RelativeLayout) findViewById(R.id.layoutTimer);
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

    App.INSTANCE.getService().getSessionTimes(solveType, new DataCallback<List<Long>>() {
      @Override
      public void onData(List<Long> data) {
        cubeSession = new CubeSession(data);
        updateSessionView();
      }
    });

    generateScramble();
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
          if (lastSolveTime.getTime() > 0) {
            lastSolveTime.setTime(lastSolveTime.getTime() + 2000);
            App.INSTANCE.getService().saveTime(lastSolveTime, new SolveAverageCallback());
            tvTimer.setText(FormatterService.INSTANCE.formatSolveTime(lastSolveTime.getTime()));
            cubeSession.setLastAsPlusTwo();
            updateSessionView();
          }
          break;
        case R.id.itDNF:
          if (lastSolveTime.getTime() > 0) {
            lastSolveTime.setTime(-1);
            App.INSTANCE.getService().saveTime(lastSolveTime, new SolveAverageCallback());
            tvTimer.setText(FormatterService.INSTANCE.formatSolveTime(lastSolveTime.getTime()));
            cubeSession.setLastAsDNF();
            updateSessionView();
          }
          break;
        case R.id.itDelete:
          App.INSTANCE.getService().removeTime(lastSolveTime, new SolveAverageCallback());
          cubeSession.deleteLast();
          updateSessionView();
          resetTimer();
          break;
      }
    }
    return true;
  }

  private void updateSessionView() {
    StringBuilder sbTimes = new StringBuilder();
    List<Long> sessionTimes = cubeSession.getSessionTimes();
    if (!sessionTimes.isEmpty()) {
      int bestInd = (sessionTimes.size() < 5) ? -1 : cubeSession.getBestTimeInd(sessionTimes.size());
      int worstInd = (sessionTimes.size() < 5) ? -1 : cubeSession.getWorstTimeInd(sessionTimes.size());
      for (int i = 0; i < sessionTimes.size(); i++) {
        String strTime = FormatterService.INSTANCE.formatSolveTime(sessionTimes.get(i));
        if (i == bestInd) {
          sbTimes.append("<font color='").append(getResources().getColor(R.color.green)).append("'>");
          sbTimes.append(strTime).append("</font>");
        } else if (i == worstInd) {
          sbTimes.append("<font color='").append(getResources().getColor(R.color.red)).append("'>");
          sbTimes.append(strTime).append("</font>");
        } else {
          sbTimes.append(strTime);
        }
        if (i < sessionTimes.size() - 1) {
          sbTimes.append(", ");
        }
        if (i == 5) { // half way to 12 (to split the times equally on two lines)
          sbTimes.append("<br>");
        }
      }
    }
    tvSessionTimes.setText(Html.fromHtml(sbTimes.toString()));
    tvRA5.setText(FormatterService.INSTANCE.formatSolveTime(cubeSession.getAverageOfFive()));
    tvRA12.setText(FormatterService.INSTANCE.formatSolveTime(cubeSession.getAverageOfTwelve()));
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
    layout.setBackgroundColor(getResources().getColor(R.color.darkblue));
    tvInspection.setText(getString(R.string.inspection) + ":");
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
    tvInspection.setText("");
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
      updateSessionView();
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
      Utils.playSound(R.raw.highbell);
      layout.setBackgroundColor(getResources().getColor(R.color.darkred));
    } else if (seconds >= INSPECTION_LIMIT - 3 && seconds < INSPECTION_LIMIT) {
      Utils.playSound(R.raw.cowbell);
    }
  }

  private void generateScramble() {
    if (cubeType != null) {
      currentScramble = ScramblerFactory.getScrambler(cubeType).getNewScramble();
      tvScramble.setText(FormatterService.INSTANCE.formatToColoredScramble(currentScramble, cubeType));
    }
  }

  private class SolveAverageCallback extends DataCallback<SolveAverages> {
    @Override
    public void onData(final SolveAverages data) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          ((TextView) findViewById(R.id.tvAvgOfFive)).setText(formatAvgField(data.getAvgOf5()));
          ((TextView) findViewById(R.id.tvAvgOfTwelve)).setText(formatAvgField(data.getAvgOf12()));
          ((TextView) findViewById(R.id.tvAvgOfHundred)).setText(formatAvgField(data.getAvgOf100()));
          ((TextView) findViewById(R.id.tvAvgOfLifetime)).setText(formatAvgField(data.getAvgOfLifetime()));
          ((TextView) findViewById(R.id.tvBestOfFive)).setText(formatAvgField(data.getBestOf5()));
          ((TextView) findViewById(R.id.tvBestOfTwelve)).setText(formatAvgField(data.getBestOf12()));
          ((TextView) findViewById(R.id.tvBestOfHundred)).setText(formatAvgField(data.getBestOf100()));
          ((TextView) findViewById(R.id.tvBestOfLifetime)).setText(formatAvgField(data.getBestOfLifetime()));
          lastSolveTime = data.getSolveTime();
        }
      });
    }
  }

}
