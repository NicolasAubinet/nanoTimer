package com.cube.nanotimer.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cube.nanotimer.R;
import com.cube.nanotimer.scrambler.ScramblerFactory;
import com.cube.nanotimer.vo.CubeType;

public class TimerActivity extends Activity {

  enum TimerState {STOPPED, STARTED}

  private CubeType cubeType;
  private TextView tvTimer;
  private TextView tvInspection;
  private TextView tvScramble;

  private final long REFRESH_INTERVAL = 10;
  private Handler timerHandler;
  private Runnable updateTimerRunnable;
  private Runnable updateInspecTimerRunnable;
  private long timerStartTs;
  private volatile TimerState timerState = TimerState.STOPPED;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.timer);
    cubeType = (CubeType) getIntent().getSerializableExtra("type");

    tvTimer = (TextView) findViewById(R.id.tvTimer);
    tvInspection = (TextView) findViewById(R.id.tvInspection);
    resetTimer();
    tvScramble = (TextView) findViewById(R.id.tvScramble);

    timerHandler = new Handler();
    updateTimerRunnable = new Runnable() {
      @Override
      public void run() {
        if (timerState == TimerState.STARTED) {
          timerHandler.postDelayed(this, REFRESH_INTERVAL);
          updateTimerText();
        }
      }
    };
    updateInspecTimerRunnable = new Runnable() {
      @Override
      public void run() {
        timerHandler.postDelayed(this, 1000);
        updateInspectionTimerText();
      }
    };

    final RelativeLayout layout = (RelativeLayout) findViewById(R.id.layoutTimer);
    layout.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        if (timerState == TimerState.STARTED) {
          stopTimer();
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

    /*((TextView) findViewById(R.id.tvAvgOfFive)).setText("10.2"); // TODO : dummy test data
    ((TextView) findViewById(R.id.tvAvgOfTwelve)).setText("10.4");
    ((TextView) findViewById(R.id.tvAvgOfHundred)).setText("10.5");
    ((TextView) findViewById(R.id.tvAvgOfThousand)).setText("11.1");
    ((TextView) findViewById(R.id.tvBestOfFive)).setText("9.2");
    ((TextView) findViewById(R.id.tvBestOfTwelve)).setText("9.4");
    ((TextView) findViewById(R.id.tvBestOfHundred)).setText("9.5");
    ((TextView) findViewById(R.id.tvBestOfThousand)).setText("10.1");*/
  }

  @Override
  public void onBackPressed() {
    if (timerState == TimerState.STARTED) {
      stopTimer();
      resetTimer();
    } else {
      super.onBackPressed();
    }
  }

  private void startTimer() {
    timerStartTs = System.currentTimeMillis();
    timerState = TimerState.STARTED;
    updateTimerRunnable.run();
  }

  private void stopTimer() {
    timerState = TimerState.STOPPED;
    // update time once more to get the ms right
    // (as all ms do not necessarily appear when timing, some are skipped due to refresh interval)
    updateTimerText();
    timerHandler.removeCallbacks(updateTimerRunnable);
    generateScramble();
  }

  private void startInspectionTimer() {
    timerStartTs = System.currentTimeMillis();
    tvInspection.setText(getString(R.string.inspection) + ":");
    updateInspecTimerRunnable.run();
  }

  private void stopInspectionTimer() {
    timerHandler.removeCallbacks(updateInspecTimerRunnable);
    tvInspection.setText("");
  }

  private void resetTimer() {
    timerStartTs = 0;
    tvTimer.setText("0.00");
  }

  private synchronized void updateTimerText() {
    StringBuilder sb = new StringBuilder();
    long curTime = System.currentTimeMillis() - timerStartTs;
    int minutes = (int) curTime / 60000;
    int seconds = (int) (curTime / 1000) % 60;
    int hundreds = (int) (curTime / 10.0) % 100;
    if (minutes > 0) {
      sb.append(minutes).append(":");
      sb.append(String.format("%02d", seconds));
    } else {
      sb.append(seconds);
    }
    sb.append(".").append(String.format("%02d", hundreds));
    tvTimer.setText(sb.toString());
  }

  private synchronized void updateInspectionTimerText() {
    long curTime = System.currentTimeMillis() - timerStartTs;
    tvTimer.setText(String.valueOf((int) (curTime / 1000) % 60));
  }

  private void generateScramble() {
    if (cubeType != null) {
      String scramble = ScramblerFactory.getScrambler(cubeType).getNewScramble();
      tvScramble.setText(getColoredTextSpan(scramble));
    }
  }

  private Spannable getColoredTextSpan(String scramble) {
    Spannable span = new SpannableString(scramble);
    String alternateColor = "#C0B9F9";
    int prevLinesCharCount = 0;
    for (String line : scramble.split("\n")) {
      char[] cline = line.toCharArray();
      int index = line.indexOf(" ", 0);
      while (index != -1) {
        int startIndex = index;
        for (; index < cline.length && cline[index] == ' '; index++) ; // next non-space char
        index = line.indexOf(" ", index); // next space char
        if (index == -1) {
          index = cline.length - 1;
        }
        span.setSpan(new ForegroundColorSpan(Color.parseColor(alternateColor)),
            startIndex + prevLinesCharCount, index + prevLinesCharCount, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        for (; index < cline.length && cline[index] == ' '; index++) ; // next non-space char
        index = line.indexOf(" ", index);
      }
      prevLinesCharCount += line.length() + 1;
    }
    return span;
  }

}
