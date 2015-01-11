package com.cube.nanotimer;

import android.content.Context;
import android.content.pm.PackageManager;
import com.cube.nanotimer.Options.ScrambleNotificationMode;
import com.cube.nanotimer.gui.MainScreenActivity;
import com.cube.nanotimer.gui.widget.AppRater;
import com.cube.nanotimer.gui.widget.ProVersionAd;
import com.cube.nanotimer.gui.widget.ProVersionWelcome;
import com.cube.nanotimer.gui.widget.ReleaseNotes;
import com.cube.nanotimer.scrambler.ScramblerService;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent.GenerationLaunch;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent.State;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenListener;
import com.cube.nanotimer.services.Service;
import com.cube.nanotimer.services.ServiceImpl;
import com.cube.nanotimer.util.helper.GUIUtils;

public enum App {
  INSTANCE;

  private Context context;
  private Service service;
  private boolean appGUILaunched;

  public static final String PRO_PACKAGE_NAME = "com.cube.nanotimerpro";

  private static final int SCRAMBLE_NOTIF_ID = 1;

  public Context getContext() {
    return context;
  }

  public void setContext(Context context) {
    init(context, false);
  }

  public void setApplicationContext(Context context) { // called from broadcast receiver
    init(context, true);
  }

  public void init(Context context, boolean fromService) {
    boolean appStarted = (this.context == null);
    if (appStarted || !fromService) {
      this.context = context;
    }
    if (appStarted) { // app started (either from GUI or from service)
      service = ServiceImpl.getInstance(context);
      Options.INSTANCE.setContext(context);
      ScramblerService.INSTANCE.init(context);
      initRandomStateGenListener(context);
    }
    if (!appGUILaunched && !fromService) { // app GUI started
      appGUILaunched(context);
    }
  }

  private void appGUILaunched(Context context) {
    appGUILaunched = true;
    AppLaunchStats.appLaunched(context);
    AppRater.appLaunched(context);
    ReleaseNotes.appLaunched(context);
    ProVersionAd.appLaunched(context);
  }

  private void initRandomStateGenListener(final Context context) {
    RandomStateGenListener randomStateGenListener = new RandomStateGenListener() {
      @Override
      public void onStateUpdate(RandomStateGenEvent event) {
        String title = context.getString(R.string.scrambles_being_generated, event.getCubeTypeName());
        ScrambleNotificationMode scrambleNotificationMode = Options.INSTANCE.getGenScrambleNotificationMode();
        boolean showNotif = (scrambleNotificationMode == ScrambleNotificationMode.ALWAYS ||
            (scrambleNotificationMode == ScrambleNotificationMode.MANUAL &&
                (event.getGenerationLaunch() == GenerationLaunch.MANUAL || event.getGenerationLaunch() == GenerationLaunch.PLUGGED)));
        if (event.getState() == State.PREPARING && showNotif) {
          GUIUtils.showNotification(context, SCRAMBLE_NOTIF_ID, title, "", MainScreenActivity.class);
        } else if (event.getState() == State.GENERATING && showNotif) {
          GUIUtils.showNotification(context, SCRAMBLE_NOTIF_ID, title,
              context.getString(R.string.generating_scramble, event.getCurScramble(), event.getTotalToGenerate()), MainScreenActivity.class);
        } else if (event.getState() == State.IDLE || !showNotif) {
          GUIUtils.hideNotification(context, SCRAMBLE_NOTIF_ID);
        }
      }
    };
    ScramblerService.INSTANCE.addRandomStateGenListener(randomStateGenListener);
  }

  public Service getService() {
    return service;
  }

  public boolean isProEnabled() {
    int sigMatch = context.getPackageManager().checkSignatures(context.getPackageName(), PRO_PACKAGE_NAME);
    return (sigMatch == PackageManager.SIGNATURE_MATCH);
  }

  public void onResume() {
    ProVersionWelcome.onResume(context, isProEnabled());
  }

}
