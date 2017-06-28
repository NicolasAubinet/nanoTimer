package com.cube.nanotimer.scrambler.randomstate;

import com.cube.nanotimer.vo.ScrambleType;

public class ScrambleConfig {

  private int maxLength;
  private ScrambleType scrambleType;

  public ScrambleConfig(int maxLength) {
    this(maxLength, null);
  }

  public ScrambleConfig(int maxLength, ScrambleType scrambleType) {
    this.maxLength = maxLength;
    this.scrambleType = scrambleType;
  }

  public int getMaxLength() {
    return maxLength;
  }

  public ScrambleType getScrambleType() {
    return scrambleType;
  }

}
