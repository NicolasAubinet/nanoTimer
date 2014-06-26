package com.cube.nanotimer.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.scrambler.ScramblerFactory;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;

import java.util.Timer;
import java.util.TimerTask;

public class TimerActivity extends Activity {

  enum TimerState { STOPPED, STARTED }

  private TextView tvTimer;
  private TextView tvInspection;
  private TextView tvScramble;

  private CubeType cubeType;
  private SolveType solveType;
  private String[] currentScramble;

  private final long REFRESH_INTERVAL = 25;
  private Timer timer;
  private Handler timerHandler = new Handler();
  private Object timerSync = new Object();
  private long timerStartTs;
  private volatile TimerState timerState = TimerState.STOPPED;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.timer);
    cubeType = (CubeType) getIntent().getSerializableExtra("cubeType");
    solveType = (SolveType) getIntent().getSerializableExtra("solveType");
    App.INSTANCE.getService().getSolveAverages(solveType, new SolveAverageCallback());

    tvTimer = (TextView) findViewById(R.id.tvTimer);
    tvInspection = (TextView) findViewById(R.id.tvInspection);
    tvScramble = (TextView) findViewById(R.id.tvScramble);
    resetTimer();

    final RelativeLayout layout = (RelativeLayout) findViewById(R.id.layoutTimer);
    layout.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        if (timerState == TimerState.STARTED) {
          stopTimer(true);
          return false;
        } else if (timerState == TimerState.STOPPED) {
          if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            layout.setBackgroundColor(getResources().getColor(R.color.darkblue));
            startInspectionTimer();
          } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            layout.setBackgroundColor(getResources().getColor(R.color.black));
            stopInspectionTimer();
            startTimer();
          }
        }
        return true;
      }
    });

    generateScramble();
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
    tvInspection.setText("");
  }

  private void resetTimer() {
    synchronized (timerSync) {
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
  }

  private String formatAvgField(Long f) {
    return FormatterService.INSTANCE.formatSolveTime(f, "-");
  }

  private synchronized void updateTimerText(long curTime) {
    tvTimer.setText(FormatterService.INSTANCE.formatSolveTime(curTime));
  }

  private synchronized void updateInspectionTimerText() {
    long curTime = System.currentTimeMillis() - timerStartTs;
    tvTimer.setText(String.valueOf((int) (curTime / 1000) % 60));
  }

  private void generateScramble() {
    if (cubeType != null) {
      currentScramble = ScramblerFactory.getScrambler(cubeType).getNewScramble();
      String fScramble = FormatterService.INSTANCE.formatScramble(currentScramble, cubeType);
      tvScramble.setText(FormatterService.INSTANCE.formatToColoredScramble(fScramble));
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
        }
      });
    }
  }

}
