package com.cube.nanotimer.scrambler.randomstate;

public class ScrambleConfig {

  private int maxLength;

  public ScrambleConfig(int maxLength) {
    this.maxLength = maxLength;
  }

  public int getMaxLength() {
    return maxLength;
  }

  public void setMaxLength(int maxLength) {
    this.maxLength = maxLength;
  }

}
