package com.cube.nanotimer;

import android.content.Context;
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
    this.context = context;
    this.service = ServiceImpl.getInstance(context);
  }

  public Service getService() {
    return service;
  }

}
