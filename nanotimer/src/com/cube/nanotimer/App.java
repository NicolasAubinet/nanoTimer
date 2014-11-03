package com.cube.nanotimer;

import android.content.Context;
import com.cube.nanotimer.gui.MainScreenActivity;
import com.cube.nanotimer.gui.widget.AppRater;
import com.cube.nanotimer.gui.widget.ReleaseNotes;
import com.cube.nanotimer.scrambler.ScramblerService;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent.State;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenListener;
import com.cube.nanotimer.services.Service;
import com.cube.nanotimer.services.ServiceImpl;
import com.cube.nanotimer.util.helper.GUIUtils;

public enum App {
  INSTANCE;

  private Context context;
  private Service service;

  private static final int SCRAMBLE_NOTIF_ID = 1;
  private RandomStateGenListener randomStateGenListener;

  public Context getContext() {
    return context;
  }

  public void setContext(Context context) {
    if (this.context == null) { // the app is starting
      this.service = ServiceImpl.getInstance(context);
      Options.INSTANCE.setContext(context);
      ScramblerService.INSTANCE.init(context);
      AppRater.appLaunched(context);
      ReleaseNotes.appLaunched(context);
      initRandomStateGenListener(context);
    }
    this.context = context;
  }

  private void initRandomStateGenListener(final Context context) {
    randomStateGenListener = new RandomStateGenListener() {
      @Override
      public void onStateUpdate(RandomStateGenEvent event) {
        String title = context.getString(R.string.scrambles_being_generated);
        if (event.getState() == State.PREPARING) {
          GUIUtils.showNotification(context, SCRAMBLE_NOTIF_ID, title, "", MainScreenActivity.class);
        } else if (event.getState() == State.GENERATING) {
          GUIUtils.showNotification(context, SCRAMBLE_NOTIF_ID, title,
              context.getString(R.string.generating_scramble, event.getCurScramble(), event.getTotalToGenerate()), MainScreenActivity.class);
        } else if (event.getState() == State.IDLE) {
          GUIUtils.hideNotification(context, SCRAMBLE_NOTIF_ID);
        }
      }
    };
    ScramblerService.INSTANCE.addRandomStateGenListener(randomStateGenListener);
  }

  public Service getService() {
    return service;
  }

}
