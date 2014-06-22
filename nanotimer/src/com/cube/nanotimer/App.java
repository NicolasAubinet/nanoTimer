package com.cube.nanotimer;

import android.content.Context;
import com.cube.nanotimer.services.Service;
import com.cube.nanotimer.services.ServiceImpl;

public class App {

  private static Context context;
  private static Service service;

  public static Context getContext() {
    return context;
  }

  public static void setContext(Context context) {
    App.context = context;
    App.service = ServiceImpl.getInstance(context);
  }

  public static Service getService() {
    return service;
  }

}
