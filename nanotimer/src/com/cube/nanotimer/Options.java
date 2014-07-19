package com.cube.nanotimer;

public enum Options {
  INSTANCE;

  private static final int MAX_STEPS_COUNT = 8;

  public int getMaxStepsCount() {
    return MAX_STEPS_COUNT;
  }

}
