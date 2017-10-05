package com.cube.nanotimer.gui;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayout;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.Options.AdsStyle;
import com.cube.nanotimer.Options.BigCubesNotation;
import com.cube.nanotimer.Options.InspectionMode;
import com.cube.nanotimer.R;
import com.cube.nanotimer.SoundManager;
import com.cube.nanotimer.gui.widget.ResultListener;
import com.cube.nanotimer.gui.widget.SessionDetailDialog;
import com.cube.nanotimer.gui.widget.ads.AdProvider;
import com.cube.nanotimer.gui.widget.dialog.AddNewTimeDialog;
import com.cube.nanotimer.scrambler.ScramblerService;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent.State;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenListener;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.session.CubeSession;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.util.ScrambleFormatterService;
import com.cube.nanotimer.util.YesNoListener;
import com.cube.nanotimer.util.helper.DialogUtils;
import com.cube.nanotimer.util.helper.GUIUtils;
import com.cube.nanotimer.util.helper.ScreenUtils;
import com.cube.nanotimer.util.helper.Utils;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;
import com.cube.nanotimer.vo.SolveTypeStep;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimerActivity extends NanoTimerActivity implements ResultListener {

  enum TimerState {STOPPED, RUNNING, INSPECTING}

  private TextView tvTimer;
  private TextView tvScramble;
  private TextView tvSolvesCount;
  private TextView tvAccuracy;
  private TextView tvTitle;
  private ViewGroup layout;
  private GridLayout sessionTimesLayout;
  private TableLayout timerStepsLayout;

  private CubeType cubeType;
  private SolveType solveType;
  private String[] currentScramble;
  private SolveTime lastSolveTime;
  private CubeSession cubeSession;
  private SolveAverages solveAverages;
  private SolveAverages prevSolveAverages;
  private int currentOrientation;
  private List<Long> stepsTimes = new ArrayList<Long>();
  private long stepStartTs;
  private List<Animation> animations = new ArrayList<Animation>();
  private boolean hasNewSession;
  private SolveAverageCallback solveAverageCallback = new SolveAverageCallback();

  private int solvesCount; // session solves count (or history solves count if no session exists)
  private int historySolvesCount;
  private ColorStateList defaultTextColor;
  private static final int MIN_TIMES_FOR_RECORD_NOTIFICATION = 12;

  private final long REFRESH_INTERVAL = 30;
  private Timer timer;
  private Timer holdToStartTimer;
  private Handler timerHandler = new Handler();
  private final Object holdToStartTimerSync = new Object();
  private final Object timerSync = new Object();
  private long timerStartTs;
  private volatile long holdToStartTs;
  private final long HOLD_TO_START_MIN_DURATION = 500;
  private volatile TimerState timerState = TimerState.STOPPED;
  private boolean showMenu = true;
  private boolean oversteppedInspection = false;

  private long lastTimerStartTs;
  private long lastTimerStopTs;
  private boolean ignoreActionUp;
  private final long START_STOP_DELAY = 150; // to avoid stopping timer too quickly after a start
  private final long STOP_START_DELAY = 500; // to avoid starting timer too quickly after a stop

  private int inspectionTime;
  private InspectionMode inspectionMode;
  private boolean soundsEnabled;
  private boolean keepScreenOnWhenTimerOff;

  private int defaultBackgroundColor = R.color.graybg;
  private int pushedBackgroundColor = R.color.pushedbg;

  private RandomStateGenListener randomStateGenListener = new RandomStateGenListener() {
    @Override
    public void onStateUpdate(RandomStateGenEvent event) {
      if (event.getState() == State.GENERATED) {
        boolean foundScramble = getAndDisplayNewScramble();
        if (foundScramble) {
          ScramblerService.INSTANCE.removeRandomStateGenListener(this);
        }
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    App.INSTANCE.setContext(this);
    setContentView(R.layout.timer_screen);
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    currentOrientation = getResources().getConfiguration().orientation;

    cubeType = (CubeType) getIntent().getSerializableExtra("cubeType");
    solveType = (SolveType) getIntent().getSerializableExtra("solveType");
    historySolvesCount = getIntent().getIntExtra("solvesCount", 0);
    if (cubeType == null || solveType == null) {
      finish();
    }
    cubeSession = new CubeSession();
    App.INSTANCE.getService().getSolveAverages(solveType, solveAverageCallback);

    initActionBar();

    inspectionTime = Options.INSTANCE.getInspectionTime();
    inspectionMode = Options.INSTANCE.getInspectionMode();
    soundsEnabled = Options.INSTANCE.isInspectionSoundsEnabled();
    keepScreenOnWhenTimerOff = Options.INSTANCE.isKeepTimerScreenOnWhenTimerOff();

    initViews();

    defaultTextColor = tvSolvesCount.getTextColors();
    resetTimer();
    setDefaultBannerText();

    if (!solveType.hasSteps()) {
      App.INSTANCE.getService().getSessionTimes(solveType, new DataCallback<List<Long>>() {
        @Override
        public void onData(List<Long> data) {
          cubeSession = new CubeSession(data);
          refreshSessionFields();
        }
      });
      App.INSTANCE.getService().getSessionStart(solveType, new DataCallback<Long>() {
        @Override
        public void onData(Long data) {
          hasNewSession = (data != null && data > 0);
        }
      });
      App.INSTANCE.getService().getSolvesCount(solveType, new DataCallback<Integer>() {
        @Override
        public void onData(final Integer data) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              setSolvesCount(data);
            }
          });
        }
      });
    }

    generateScramble();
  }

  @Override
  protected void onResume() {
    super.onResume();
    App.INSTANCE.setContext(this);
    App.INSTANCE.onResume();
  }

  private void initActionBar() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    actionBar.setCustomView(R.layout.textcentered_actionbar);
    actionBar.setDisplayHomeAsUpEnabled(true);
  }

  private void initViews() {
    tvTimer = (TextView) findViewById(R.id.tvTimer);
    tvScramble = (TextView) findViewById(R.id.tvScramble);
    tvSolvesCount = (TextView) findViewById(R.id.tvSolvesCount);
    tvAccuracy = (TextView) findViewById(R.id.tvLifetimeAccuracy);
    tvTitle = (TextView) findViewById(R.id.tvTitle);
    sessionTimesLayout = (GridLayout) findViewById(R.id.sessionTimesLayout);
    TableLayout averagesLayout = (TableLayout) findViewById(R.id.averagesLayout);
    timerStepsLayout = (TableLayout) findViewById(R.id.timerStepsLayout);

    if (currentOrientation == Configuration.ORIENTATION_PORTRAIT && cubeType == CubeType.SEVEN_BY_SEVEN) {
      tvTimer.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvTimer.getTextSize() - 5);
    }

    Float scrambleTextSize = getCubeTypeScrambleTextSize();
    if (scrambleTextSize != null) {
      tvScramble.setTextSize(TypedValue.COMPLEX_UNIT_PX, scrambleTextSize);
    }

    if (solveType.isBlind()) {
      findViewById(R.id.trAvgOfFive).setVisibility(View.GONE);
      findViewById(R.id.trLifetimeAccuracy).setVisibility(View.VISIBLE);
      findViewById(R.id.trBestMeanOfThree).setVisibility(View.VISIBLE);
      ((TextView) findViewById(R.id.tvAvgOf)).setText(R.string.success_avg);
      ((TextView) findViewById(R.id.tvBestOf)).setText(R.string.accuracy);
    } else if (solveType.hasSteps()) {
      findViewById(R.id.sessionLayout).setVisibility(View.GONE);
      averagesLayout.setColumnCollapsed(2, true);
      timerStepsLayout.setVisibility(View.VISIBLE);
      if (solveType.getSteps().length <= 4) {
        timerStepsLayout.setColumnCollapsed(2, true);
        timerStepsLayout.setColumnCollapsed(3, true);
      }
      findViewById(R.id.trAvgOfLife).setVisibility(View.VISIBLE);
      hideUnneededStepFields();
    } else {
      timerStepsLayout.setVisibility(View.GONE);
      findViewById(R.id.trAvgOfLife).setVisibility(View.GONE);
    }

    updateAveragesTableBackgroundColors();

    View actionBarLayout = findViewById(R.id.actionbarLayout);
    actionBarLayout.setOnTouchListener(layoutTouchListener);

    layout = (ViewGroup) findViewById(R.id.mainLayout);
    layout.setOnTouchListener(layoutTouchListener);
    if (timerState == TimerState.STOPPED) {
      setKeepScreenOn(keepScreenOnWhenTimerOff);
    } else {
      setKeepScreenOn(true);
    }
  }

  private void updateAveragesTableBackgroundColors() {
    TableLayout averagesTable = (TableLayout) findViewById(R.id.averagesLayout);

    int visibleTableRowsCount = 0;
    for (int i = 1; i < averagesTable.getChildCount(); i++) {
      TableRow tableRow = (TableRow) averagesTable.getChildAt(i);

      if (tableRow.getVisibility() == View.VISIBLE) {
        int backgroundColorId;
        if (visibleTableRowsCount % 2 == 0) {
          backgroundColorId = R.color.grid_background_1;
        } else {
          backgroundColorId = R.color.grid_background_2;
        }
        tableRow.setBackgroundResource(backgroundColorId);

        visibleTableRowsCount++;
      }
    }
  }

  private Float getCubeTypeScrambleTextSize() {
    Float size;
    switch (cubeType) {
      case TWO_BY_TWO:
        size = 24f;
        break;
      case THREE_BY_THREE:
      case PYRAMINX:
      case SKEWB:
        size = 22f;
        break;
      case FOUR_BY_FOUR:
      case FIVE_BY_FIVE:
      case SQUARE1:
        size = 21f;
        break;
      case CLOCK:
        size = 20f;
        break;
      case SIX_BY_SIX:
      case MEGAMINX:
        size = 18f;
        break;
      case SEVEN_BY_SEVEN:
        size = 15.5f;
        break;
      default:
        size = null;
        break;
    }
    if (Options.INSTANCE.getBigCubesNotation() == BigCubesNotation.RWUWFW) {
      // adjust size otherwise it is too large, and causes a bug when going from landscape mode to portrait mode
      switch (cubeType) {
        case FOUR_BY_FOUR:
        case FIVE_BY_FIVE:
        case SIX_BY_SIX:
        case SEVEN_BY_SEVEN:
          size -= 2;
      }
    }
    return size;
  }

  private void setDefaultBannerText() {
    StringBuilder sb = new StringBuilder();
    sb.append(cubeType.getName());

    if (!Utils.isDefaultSolveTypeName(solveType.getName())) {
      String localizedSolveTypeName = Utils.toSolveTypeLocalizedName(this, solveType.getName());
      sb.append(" (").append(localizedSolveTypeName).append(")");
    }
    setTitle(sb.toString(), defaultTextColor.getDefaultColor());
  }

  public void setTitle(String s) {
    tvTitle.setText(s);
  }

  public void setTitle(int res) {
    tvTitle.setText(res);
  }

  public synchronized void setTitle(String s, int textColor) {
    setTitle(s);
    setTitleColor(textColor);
  }

  @Override
  public void setTitleColor(int textColor) {
    tvTitle.setTextColor(textColor);
  }

  @Override
  public void onBackPressed() {
    if (timerState == TimerState.RUNNING) {
      stopTimer(false);
      resetTimer();
    } else if (timerState == TimerState.INSPECTING) { // for automatic inspection mode
      stopInspectionTimer();
      resetTimer();
    } else {
      if (timer != null) {
        timer.cancel();
        timer.purge();
      }
      AdsStyle adsStyle = Options.INSTANCE.getAdsStyle();
      if (Options.INSTANCE.isAdsEnabled() && (adsStyle == AdsStyle.INTERSTITIAL || adsStyle == AdsStyle.MIXED)) {
        AdProvider.showInterstitial();
      }
      super.onBackPressed();
    }
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.findItem(R.id.itShareTime).setVisible(showMenu && lastSolveTime != null);
    menu.findItem(R.id.itSessionDetails).setVisible(showMenu && hasNewSession);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.timer_menu, menu);
    for (int i = 0; i < menu.size(); i++) {
      menu.getItem(i).setVisible(showMenu);
    }
    if (solveType.hasSteps()) {
      menu.findItem(R.id.itSessionDetails).setVisible(false);
      menu.findItem(R.id.itNewSession).setVisible(false);
      menu.findItem(R.id.itAddTime).setVisible(false);
    }
    return true;
  }

  private void showMenuButton(boolean show) {
    if (this.showMenu != show) {
      this.showMenu = show;
      supportInvalidateOptionsMenu();

      getSupportActionBar().setDisplayHomeAsUpEnabled(show);

      // adjust action bar layout so that the text is always centered
      // and to allow to stop the timer by pushing anywhere on the action bar
      View actionBarLayout = findViewById(R.id.actionbarLayout);
      ViewGroup.LayoutParams actionBarLayoutParams = actionBarLayout.getLayoutParams();

      if (show) {
        actionBarLayoutParams.width = LayoutParams.WRAP_CONTENT;
      } else {
        actionBarLayoutParams.width = LayoutParams.MATCH_PARENT;
      }
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (timerState == TimerState.STOPPED) {
      switch (item.getItemId()) {
        case R.id.itPlusTwo:
          if (lastSolveTime != null && !lastSolveTime.isDNF() && !lastSolveTime.isPlusTwo()) {
            lastSolveTime.plusTwo();
            App.INSTANCE.getService().saveTime(lastSolveTime, solveAverageCallback);
            tvTimer.setText(FormatterService.INSTANCE.formatSolveTime(lastSolveTime.getTime()));
            cubeSession.setLastAsPlusTwo();
            refreshSessionFields();
          }
          break;
        case R.id.itDNF:
          if (lastSolveTime != null && !lastSolveTime.isDNF()) {
            lastSolveTime.setTime(-1);
            App.INSTANCE.getService().saveTime(lastSolveTime, solveAverageCallback);
            tvTimer.setText(FormatterService.INSTANCE.formatSolveTime(lastSolveTime.getTime()));
            cubeSession.setLastAsDNF();
            refreshSessionFields();
          }
          break;
        case R.id.itDelete:
          if (lastSolveTime != null) {
            App.INSTANCE.getService().deleteTime(lastSolveTime, solveAverageCallback);
            cubeSession.deleteLast();
            historySolvesCount--;
            setSolvesCount(solvesCount - 1);
            refreshSessionFields();
            resetTimer();
          }
          break;
        case R.id.itSessionDetails:
          DialogUtils.showFragment(this, SessionDetailDialog.newInstance(solveType));
          break;
        case R.id.itNewSession:
          DialogUtils.showYesNoConfirmation(this, getString(R.string.new_session_confirmation), new YesNoListener() {
            @Override
            public void onYes() {
              App.INSTANCE.getService().startNewSession(solveType, System.currentTimeMillis(), null);
              cubeSession.clearSession();
              setSolvesCount(0);
              refreshSessionFields();
              if (!hasNewSession) {
                hasNewSession = true;
              }
            }
          });
          break;
        case R.id.itAddTime:
          if (Utils.checkProFeature(this)) {
            if (currentScramble != null) {
              String scramble = ScrambleFormatterService.INSTANCE.formatScrambleAsSingleLine(currentScramble, cubeType);
              AddNewTimeDialog dialog = AddNewTimeDialog.newInstance(this, solveType, scramble);
              DialogUtils.showFragment(this, dialog);
            } else {
              DialogUtils.showShortInfoMessage(this, R.string.can_not_add_time_while_generating);
            }
          }
          break;
        case R.id.itShareTime:
          if (lastSolveTime != null) {
            DialogUtils.shareTime(this, lastSolveTime, cubeType);
          }
          break;
      }
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (newConfig.orientation != currentOrientation) {
      currentOrientation = newConfig.orientation;
      String timerText = tvTimer.getText().toString();

      List<String> stepsText = new ArrayList<String>();
      if (solveType.hasSteps()) {
        for (int i = 0; i < timerStepsLayout.getChildCount(); i++) {
          TableRow tr = (TableRow) timerStepsLayout.getChildAt(i);
          for (int j = 0; j < tr.getChildCount(); j++) {
            stepsText.add(((TextView) tr.getChildAt(j)).getText().toString());
          }
        }
      }

      setContentView(R.layout.timer_screen);
      initViews();

      if (timerState == TimerState.STOPPED) {
        tvTimer.setText(timerText);
      }
      if (currentScramble != null) {
        tvScramble.setText(ScrambleFormatterService.INSTANCE.formatToColoredScramble(currentScramble, cubeType, currentOrientation));
      } else {
        tvScramble.setText(R.string.scramble_generating);
      }
      tvSolvesCount.setText(String.valueOf(solvesCount));

      refreshSessionFields();
      refreshAvgFields(false);

      if (solveType.hasSteps()) {
        Iterator<String> it = stepsText.iterator();
        for (int i = 0; i < timerStepsLayout.getChildCount(); i++) {
          TableRow tr = (TableRow) timerStepsLayout.getChildAt(i);
          for (int j = 0; j < tr.getChildCount(); j++) {
            ((TextView) tr.getChildAt(j)).setText(it.next());
          }
        }
      }
    }
  }

  @Override
  public void onResult(Object... params) {
    final SolveAverages solveAverages = (SolveAverages) params[0];
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        addTimeToUI(solveAverages.getSolveTime().getTime());
        generateScramble();
      }
    });
    solveAverageCallback.onData(solveAverages);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_SPACE && event.getRepeatCount() == 0) {
      onTouchEvent(MotionEvent.ACTION_DOWN);
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_SPACE && event.getRepeatCount() == 0) {
      onTouchEvent(MotionEvent.ACTION_UP);
      return true;
    }
    return super.onKeyUp(keyCode, event);
  }

  private void refreshSessionFields() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        clearSessionTextViews();
        if (cubeSession == null) {
          return;
        }
        List<Long> sessionTimes = cubeSession.getTimes();
        if (!sessionTimes.isEmpty()) {
          int bestInd = cubeSession.getBestTimeInd(solveType.isBlind());
          int worstInd = cubeSession.getWorstTimeInd(solveType.isBlind());
          for (int i = 0; i < sessionTimes.size(); i++) {
            TextView tv = getSessionTextView(i);
            GUIUtils.setSessionTimeCellText(tv, sessionTimes.get(i), i, bestInd, worstInd, solveType.isBlind());
          }
        }
      }
    });
  }

  private void clearSessionTextViews() {
    for (int i = 0; i < sessionTimesLayout.getChildCount(); i++) {
      TextView tr = (TextView) sessionTimesLayout.getChildAt(i);
      tr.setText("");
    }
  }

  private TextView getSessionTextView(int i) {
    View v = sessionTimesLayout.getChildAt(i);
    return (TextView) v;
  }

  private void startTimer() {
    long curTime = System.currentTimeMillis();
    lastTimerStartTs = curTime;
    if (curTime - lastTimerStopTs < STOP_START_DELAY) {
      return;
    }
    timerStartTs = curTime;
    if (solveType.hasSteps()) {
      stepsTimes.clear();
      stepStartTs = timerStartTs;
    }
    timerStarted();
    timer = new Timer();
    if (Options.INSTANCE.isShowTimeWhenRunning()) {
      TimerTask timerTask = new TimerTask() {
        public void run() {
          timerHandler.post(new Runnable() {
            public void run() {
              synchronized (timerSync) {
                if (timerState == TimerState.RUNNING) {
                  updateTimerText(System.currentTimeMillis() - timerStartTs);
                }
              }
            }
          });
        }
      };
      timer.schedule(timerTask, 1, REFRESH_INTERVAL);
    } else {
      tvTimer.setText("--:--");
    }
    timerState = TimerState.RUNNING;
  }

  private void stopTimer(boolean save) {
    long curTime = System.currentTimeMillis();
    if (curTime - lastTimerStartTs < START_STOP_DELAY) {
      return;
    }
    lastTimerStopTs = curTime;
    long time = (curTime - timerStartTs);
    timerState = TimerState.STOPPED;
    if (timer != null) {
      timer.cancel();
      timer.purge();
    }
    timerStopped();
    if (oversteppedInspection) {
      time += 2000; // add 2s to time if started solve after inspection time ended (for official inspection mode)
      oversteppedInspection = false;
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
    long curTime = System.currentTimeMillis();
    if (curTime - lastTimerStopTs < STOP_START_DELAY) {
      return;
    }
    timerStartTs = curTime;
    oversteppedInspection = false;
    enableScreenRotationChanges(false);
    timerStarted();
    resetTimerText();
    timerState = TimerState.INSPECTING;
    setTitle(R.string.inspection);
    clearAvgRecordStyle();
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
    setDefaultBannerText();
    timerState = TimerState.STOPPED;
    enableScreenRotationChanges(true);
    timerStopped();
  }

  /**
   * Used to fix a problem in the way android handles its views, when the layout is clicked during "Hold and release" inspection.
   * When the orientation changes, the views are re-created and the layout is no longer considered as clicked.
   * This is the reason why the orientation should not be allowed to change during inspection.
   */
  private void enableScreenRotationChanges(boolean enable) {
    ScreenUtils.enableScreenRotationChanges(this, enable);
  }

  private void nextSolveStep() {
    long ts = System.currentTimeMillis();
    if (stepsTimes.size() < solveType.getSteps().length) {
      long time = ts - stepStartTs;
      stepsTimes.add(time);
      updateStepTimeText(stepsTimes.size() - 1, FormatterService.INSTANCE.formatSolveTime(time));
    }
    stepStartTs = ts;
  }

  private void resetTimer() {
    synchronized (timerSync) {
      if (timer != null) {
        timer.cancel();
        timer.purge();
      }
      lastSolveTime = null;
      timerStartTs = 0;
      resetTimerText();
    }
  }

  private void resetTimerText() {
    String defaultText = "0.00";
    tvTimer.setText(defaultText);
    if (solveType.hasSteps()) {
      for (int i = 0; i < Options.INSTANCE.getMaxStepsCount(); i++) {
        int rowInd = i % 4;
        int colInd = (i < 4) ? 0 : 2;
        if (i < solveType.getSteps().length) {
          SolveTypeStep sts = solveType.getSteps()[i];
          ((TextView) ((TableRow) timerStepsLayout.getChildAt(rowInd)).getChildAt(colInd)).setText(sts.getName() + ":");
          updateStepTimeText(i, defaultText);
        }
      }
    }
  }

  private void hideUnneededStepFields() {
    // hide fields to center steps vertically
    if (solveType.getSteps().length < 4) {
      for (int i = solveType.getSteps().length; i < Options.INSTANCE.getMaxStepsCount(); i++) {
        int rowInd = i % 4;
        int colInd = (i < 4) ? 0 : 2;
        (((TableRow) timerStepsLayout.getChildAt(rowInd)).getChildAt(colInd)).setVisibility(View.GONE);
        (((TableRow) timerStepsLayout.getChildAt(rowInd)).getChildAt(colInd + 1)).setVisibility(View.GONE);
      }
    }
  }

  private void saveTime(long time) {
    SolveTime solveTime = new SolveTime();
    solveTime.setTime(time);
    solveTime.setTimestamp(System.currentTimeMillis());
    solveTime.setSolveType(solveType);

    String scramble = "";
    if (currentScramble != null) { // should never be null here, but let's make sure
      scramble = ScrambleFormatterService.INSTANCE.formatScrambleAsSingleLine(currentScramble, cubeType);
    }

    solveTime.setScramble(scramble);
    if (solveType.hasSteps()) {
      solveTime.setStepsTimes(stepsTimes.toArray(new Long[0]));
    }

    addTimeToUI(time);
    App.INSTANCE.getService().saveTime(solveTime, solveAverageCallback);
  }

  private void addTimeToUI(long time) {
    if (cubeSession != null) {
      cubeSession.addTime(time);
      historySolvesCount++;
      setSolvesCount(solvesCount + 1);
      refreshSessionFields();
    }
  }

  private void updateStepTimeText(int id, String time) {
    if (id >= 0 && id < solveType.getSteps().length) {
      int rowInd = id % 4;
      int colInd = (id < 4) ? 1 : 3;
      ((TextView) ((TableRow) timerStepsLayout.getChildAt(rowInd)).getChildAt(colInd)).setText(time);
    }
  }

  private synchronized void updateTimerText(long curTime) {
    tvTimer.setText(FormatterService.INSTANCE.formatSolveTime(curTime));
    if (solveType.hasSteps()) {
      updateStepTimeText(stepsTimes.size(),
        FormatterService.INSTANCE.formatSolveTime(System.currentTimeMillis() - stepStartTs));
    }
  }

  private synchronized void updateInspectionTimerText() {
    long curTime = System.currentTimeMillis() - timerStartTs;
    final int officialInspectionDnfTime = 2;
    int seconds = (int) (curTime / 1000);
    tvTimer.setText(String.valueOf(seconds));
    boolean automaticMode = (inspectionMode == InspectionMode.AUTOMATIC);
    SoundManager soundManager = App.INSTANCE.getSoundManager();

    if (soundsEnabled) {
      if (Options.INSTANCE.getInspectionSoundsType() == Options.InspectionSoundsType.CLASSIC) {
        if (inspectionTime > 0 && seconds > 0 && seconds >= inspectionTime - 3
        && (seconds < inspectionTime || (automaticMode && seconds == inspectionTime) || (inspectionMode == InspectionMode.OFFICIAL && seconds < inspectionTime + officialInspectionDnfTime))) {
          soundManager.playSound(this, R.raw.beep);
        }
      } else if (Options.INSTANCE.getInspectionSoundsType() == Options.InspectionSoundsType.OFFICIAL) {
        if (seconds == 8) {
          soundManager.playSound(this, R.raw.eight);
        } else if (seconds == 12) {
          soundManager.playSound(this, R.raw.twelve);
        } else if (automaticMode && seconds == inspectionTime) {
          soundManager.playSound(this, R.raw.beep);
        }
      }
    }

    boolean mustDnfTime = false;
    if (seconds >= inspectionTime) {
      if (automaticMode) {
        stopInspectionTimer();
        startTimer();
      } else if (inspectionMode == InspectionMode.OFFICIAL) {
        if (seconds == inspectionTime + officialInspectionDnfTime) {
          mustDnfTime = true;
        } else if (seconds >= inspectionTime) {
          tvTimer.setText(R.string.plus_two);
          oversteppedInspection = true;
        }
      } else {
        if (inspectionTime > 0) {
          mustDnfTime = true;
        }
      }
    }

    if (mustDnfTime) {
      stopInspectionTimer();
      layout.setBackgroundResource(defaultBackgroundColor);
      if (inspectionMode == InspectionMode.OFFICIAL) {
        synchronized (holdToStartTimerSync) {
          stopHoldToStartTimer();
          holdToStartTs = 0;
        }
        ignoreActionUp = true;
      }
      updateTimerText(-1); // DNF
      App.INSTANCE.getSoundManager().playSound(this, R.raw.error);
      saveTime(-1);
      generateScramble();
    }
  }

  private void startHoldToStartTimer() {
    holdToStartTs = System.currentTimeMillis();
    holdToStartTimer = new Timer();
    final Handler timerHandler = new Handler();
    TimerTask timerTask = new TimerTask() {
      public void run() {
        timerHandler.post(new Runnable() {
          @Override
          public void run() {
            synchronized (holdToStartTimerSync) {
              if (holdToStartTs > 0) {
                long remainingHoldTime = Math.max(0, HOLD_TO_START_MIN_DURATION - (System.currentTimeMillis() - holdToStartTs));
                setTitle(String.format("%.1f", ((float) remainingHoldTime / 1000)));
                if (remainingHoldTime == 0) {
                  stopHoldToStartTimer();
                  setTitle(getString(R.string.ready), getResources().getColor(R.color.green));
                }
              }
            }
          }
        });
      }
    };
    holdToStartTimer.schedule(timerTask, 1, REFRESH_INTERVAL);
  }

  private void stopHoldToStartTimer() {
    if (holdToStartTimer != null) {
      holdToStartTimer.cancel();
      holdToStartTimer.purge();
      holdToStartTimer = null;
    }
  }

  private void generateScramble() {
    if (cubeType != null) {
      boolean foundScramble = getAndDisplayNewScramble();
      if (!foundScramble) {
        tvScramble.setText(R.string.scramble_generating);
        // couldn't find scramble in cache (for special scrambles like f2l, edges only etc), wait for a GENERATED event to check again
        ScramblerService.INSTANCE.addRandomStateGenListener(randomStateGenListener);
      }
    }
  }

  private boolean getAndDisplayNewScramble() {
    boolean foundScramble = false;
    String[] scramble = ScramblerService.INSTANCE.getScramble(cubeType, solveType.getScrambleType());
    if (scramble != null) {
      currentScramble = scramble;
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          tvScramble.setText(ScrambleFormatterService.INSTANCE.formatToColoredScramble(currentScramble, cubeType, currentOrientation));
        }
      });
      foundScramble = true;
    }
    return foundScramble;
  }

  private void timerStarted() {
    setKeepScreenOn(true);
    showMenuButton(false);
  }

  private void timerStopped() {
    setKeepScreenOn(keepScreenOnWhenTimerOff);
    showMenuButton(true);
  }

  private void setKeepScreenOn(boolean keepOn) {
    layout.setKeepScreenOn(keepOn);
  }

  private void setSolvesCount(int solvesCount) {
    this.solvesCount = Math.max(0, solvesCount);
    tvSolvesCount.setText(String.valueOf(solvesCount));
  }

  private void refreshAvgFields(boolean showNotifications) {
    for (Animation a : animations) {
      a.cancel();
    }
    animations = new ArrayList<Animation>();

    if (solveType.isBlind()) {
      tvAccuracy.setText(FormatterService.INSTANCE.formatPercentage(solveAverages.getLifetimeAccuracy()));
      refreshAvgField(R.id.tvMeanOfThree, solveAverages.getMeanOf3(), getString(R.string.NA));
      refreshAvgFieldWithRecord(R.id.tvBestMeanOfThree, solveAverages.getBestOf3(),
          (prevSolveAverages != null ? prevSolveAverages.getBestOf3() : null), getString(R.string.NA), showNotifications, false);
      refreshAvgFieldWithRecord(R.id.tvLifetimeBest, solveAverages.getBestOfLifetime(),
          (prevSolveAverages != null ? prevSolveAverages.getBestOfLifetime() : null), getString(R.string.NA), showNotifications, true);

      refreshAvgField(R.id.tvLifetimeAvg, solveAverages.getAvgOfLifetime(), getString(R.string.NA));
      refreshAvgField(R.id.tvAvgOfTwelve, solveAverages.getAvgOf12(), "-");
      refreshAvgField(R.id.tvAvgOfFifty, solveAverages.getAvgOf50(), "-");
      refreshAvgField(R.id.tvAvgOfHundred, solveAverages.getAvgOf100(), "-");
      ((TextView) findViewById(R.id.tvBestOfTwelve)).setText(
          FormatterService.INSTANCE.formatPercentage(solveAverages.getAccuracyOf12(), "-"));
      ((TextView) findViewById(R.id.tvBestOfFifty)).setText(
          FormatterService.INSTANCE.formatPercentage(solveAverages.getAccuracyOf50(), "-"));
      ((TextView) findViewById(R.id.tvBestOfHundred)).setText(
          FormatterService.INSTANCE.formatPercentage(solveAverages.getAccuracyOf100(), "-"));
    } else if (solveType.hasSteps()) {
      ((TextView) findViewById(R.id.tvAvgOfFive)).setText(
          FormatterService.INSTANCE.formatStepsTimes(solveAverages.getStepsAvgOf5()));
      ((TextView) findViewById(R.id.tvAvgOfTwelve)).setText(
          FormatterService.INSTANCE.formatStepsTimes(solveAverages.getStepsAvgOf12()));
      ((TextView) findViewById(R.id.tvAvgOfFifty)).setText(
          FormatterService.INSTANCE.formatStepsTimes(solveAverages.getStepsAvgOf50()));
      ((TextView) findViewById(R.id.tvAvgOfHundred)).setText(
          FormatterService.INSTANCE.formatStepsTimes(solveAverages.getStepsAvgOf100()));
      ((TextView) findViewById(R.id.tvAvgOfLife)).setText(
          FormatterService.INSTANCE.formatStepsTimes(solveAverages.getStepsAvgOfLifetime()));
    } else {
      refreshAvgField(R.id.tvAvgOfFive, solveAverages.getAvgOf5(), "-");
      refreshAvgField(R.id.tvAvgOfTwelve, solveAverages.getAvgOf12(), "-");
      refreshAvgField(R.id.tvAvgOfFifty, solveAverages.getAvgOf50(), "-");
      refreshAvgField(R.id.tvAvgOfHundred, solveAverages.getAvgOf100(), "-");
      refreshAvgField(R.id.tvLifetimeAvg, solveAverages.getAvgOfLifetime(), getString(R.string.NA));
      refreshAvgField(R.id.tvMeanOfThree, solveAverages.getMeanOf3(), getString(R.string.NA));

      refreshAvgFieldWithRecord(R.id.tvBestOfFive, solveAverages.getBestOf5(),
          (prevSolveAverages != null ? prevSolveAverages.getBestOf5() : null), "-", showNotifications, false);
      refreshAvgFieldWithRecord(R.id.tvBestOfTwelve, solveAverages.getBestOf12(),
          (prevSolveAverages != null ? prevSolveAverages.getBestOf12() : null), "-", showNotifications, false);
      refreshAvgFieldWithRecord(R.id.tvBestOfFifty, solveAverages.getBestOf50(),
          (prevSolveAverages != null ? prevSolveAverages.getBestOf50() : null), "-", showNotifications, false);
      refreshAvgFieldWithRecord(R.id.tvBestOfHundred, solveAverages.getBestOf100(),
          (prevSolveAverages != null ? prevSolveAverages.getBestOf100() : null), "-", showNotifications, false);
      refreshAvgFieldWithRecord(R.id.tvLifetimeBest, solveAverages.getBestOfLifetime(),
          (prevSolveAverages != null ? prevSolveAverages.getBestOfLifetime() : null), getString(R.string.NA), showNotifications, true);
    }
  }

  private String formatAvgField(Long f, String defaultValue) {
    return FormatterService.INSTANCE.formatSolveTime(f, defaultValue);
  }

  private void refreshAvgField(int fieldId, Long value, String defaultValue) {
    TextView tv = (TextView) findViewById(fieldId);
    tv.setText(formatAvgField(value, defaultValue));
    tv.setTextColor(defaultTextColor);
    tv.setTypeface(null, Typeface.NORMAL);
  }

  private void clearAvgRecordStyle() {
    prevSolveAverages = null;
    List<TextView> tvs = new ArrayList<TextView>();
    tvs.add((TextView) findViewById(R.id.tvBestOfFive));
    tvs.add((TextView) findViewById(R.id.tvBestOfTwelve));
    tvs.add((TextView) findViewById(R.id.tvBestOfFifty));
    tvs.add((TextView) findViewById(R.id.tvBestOfHundred));
    tvs.add((TextView) findViewById(R.id.tvLifetimeBest));
    for (TextView tv : tvs) {
      tv.setTextColor(defaultTextColor);
      tv.setTypeface(null, Typeface.NORMAL);
    }
    if (animations != null) {
      for (Animation a : animations) {
        a.cancel();
      }
    }
  }

  private void refreshAvgFieldWithRecord(int fieldId, Long value, Long previousValue, String defaultValue,
                                         boolean showNotifications, boolean showBanner) {
    refreshAvgField(fieldId, value, defaultValue);
    if (historySolvesCount > MIN_TIMES_FOR_RECORD_NOTIFICATION && previousValue != null && value != null && value < previousValue && !solveType.hasSteps()) {
      final int recordColor = getResources().getColor(R.color.new_record);
      final TextView tv = (TextView) findViewById(fieldId);
      tv.setTypeface(null, Typeface.BOLD);

      if (showNotifications) {
        final int defaultColor = defaultTextColor.getDefaultColor();
        if (showBanner) {
          setTitle(getString(R.string.new_record), recordColor);
          final Handler bannerHandler = new Handler();
          Timer bannerTimer = new Timer();
          TimerTask bannerTimerTask = new TimerTask() {
            public void run() {
              bannerHandler.post(new Runnable() {
                public void run() {
                  setDefaultBannerText();
                }
              });
            }
          };
          bannerTimer.schedule(bannerTimerTask, 3000);
        }

        // animate text view color
        Animation a = new Animation() {
          private int animationTimes = 3;
          private int animationStepsCounts = (animationTimes * 2) - 1;
          private int stepDurationMs = 1000 / animationStepsCounts;

          @Override
          protected void applyTransformation(float interpolatedTime, Transformation t) {
            int interpolatedTimeMs = (int) (interpolatedTime * 1000);
            int curStep = interpolatedTimeMs / stepDurationMs;
            int timeInStepMs = interpolatedTimeMs % stepDurationMs;
            float stepProgression = (float) timeInStepMs / stepDurationMs; // the +1 is to avoid division by 0
            if (curStep % 2 == 0) { // default to yellow
              tv.setTextColor(GUIUtils.getColorCodeBetween(defaultColor, recordColor, stepProgression));
            } else { // yellow to default
              tv.setTextColor(GUIUtils.getColorCodeBetween(recordColor, defaultColor, stepProgression));
            }
          }
        };
        a.setDuration(5000);
        tv.startAnimation(a);
        animations.add(a);
      } else {
        tv.setTextColor(recordColor);
      }
    }
  }

  private boolean onTouchEvent(int parMotionEventAction) {
    if (currentScramble == null) {
      // don't allow to do anything if there is no scramble (can happen for special scramble types when scrambles are not yet generated)
      return false;
    }

    // change bg color
    if (parMotionEventAction == MotionEvent.ACTION_DOWN) {
      if (System.currentTimeMillis() - lastTimerStopTs >= STOP_START_DELAY) {
        layout.setBackgroundResource(pushedBackgroundColor);
      } else {
        return false; // to avoid receiving the ACTION_UP
      }
    } else if (parMotionEventAction == MotionEvent.ACTION_UP) {
      layout.setBackgroundResource(defaultBackgroundColor);
      if (ignoreActionUp) {
        ignoreActionUp = false;
        return true;
      }
    }
    // handle timer start/stop
    if (timerState == TimerState.RUNNING && parMotionEventAction == MotionEvent.ACTION_DOWN) {
      if (solveType.hasSteps()) {
        nextSolveStep();
        if (stepsTimes.size() == solveType.getSteps().length) {
          stopTimer(true);
        }
      } else {
        stopTimer(true);
      }
      ignoreActionUp = true; // to avoid starting timer again when releasing
    } else if (solveType.isBlind()) {
      if (parMotionEventAction == MotionEvent.ACTION_UP) {
        // no inspection for blind solve types
        startTimer();
      }
    } else if (inspectionMode == InspectionMode.HOLD_AND_RELEASE) {
      if (timerState == TimerState.STOPPED && parMotionEventAction == MotionEvent.ACTION_DOWN) {
        startInspectionTimer();
      } else if (timerState == TimerState.INSPECTING && parMotionEventAction == MotionEvent.ACTION_UP) {
        stopInspectionTimer();
        startTimer();
      }
    } else if (inspectionMode == InspectionMode.AUTOMATIC) {
      if (timerState == TimerState.STOPPED && parMotionEventAction == MotionEvent.ACTION_UP) {
        startInspectionTimer();
      } else if (timerState == TimerState.INSPECTING && parMotionEventAction == MotionEvent.ACTION_UP) {
        stopInspectionTimer();
        startTimer();
      }
    } else if (inspectionMode == InspectionMode.OFFICIAL) {
      if (parMotionEventAction == MotionEvent.ACTION_DOWN && ignoreActionUp) {
        ignoreActionUp = false;
      }
      if (timerState == TimerState.STOPPED && parMotionEventAction == MotionEvent.ACTION_UP && inspectionTime > 0) {
        startInspectionTimer();
      } else if (timerState == TimerState.INSPECTING || inspectionTime == 0) {
        synchronized (holdToStartTimerSync) {
          if (parMotionEventAction == KeyEvent.ACTION_DOWN) {
            startHoldToStartTimer();
          } else if (parMotionEventAction == MotionEvent.ACTION_UP) {
            stopHoldToStartTimer();
            if (System.currentTimeMillis() - holdToStartTs > HOLD_TO_START_MIN_DURATION) { // if screen pushed for long enough
              stopInspectionTimer();
              startTimer();
              setDefaultBannerText();
            } else {
              if (inspectionTime > 0) {
                setTitle(R.string.inspection);
              } else {
                setDefaultBannerText();
              }
            }
            holdToStartTs = 0;
          }
        }
      }
    }
    return true;
  }

  private class SolveAverageCallback extends DataCallback<SolveAverages> {
    @Override
    public synchronized void onData(final SolveAverages data) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          prevSolveAverages = solveAverages;
          solveAverages = data;
          lastSolveTime = data.getSolveTime();
          refreshAvgFields(true);
        }
      });
    }
  }

  private OnTouchListener layoutTouchListener = new OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
      return onTouchEvent(motionEvent.getAction());
    }
  };

}
