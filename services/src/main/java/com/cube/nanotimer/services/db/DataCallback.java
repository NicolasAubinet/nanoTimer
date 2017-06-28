package com.cube.nanotimer.services.db;

public abstract class DataCallback<T> {

  public DataCallback() {
  }

  public abstract void onData(T data);
}
