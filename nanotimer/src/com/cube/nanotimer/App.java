package com.cube.nanotimer;

import android.content.Context;
import com.cube.nanotimer.gui.widget.AppRater;
import com.cube.nanotimer.gui.widget.ReleaseNotes;
import com.cube.nanotimer.scrambler.ScramblerService;
import com.cube.nanotimer.services.Service;
import com.cube.nanotimer.services.ServiceImpl;

public enum App {
  INSTANCE;

  private Context context;
  private Service service;

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
    }
    this.context = context;
  }

  public Service getService() {
    return service;
  }

}
