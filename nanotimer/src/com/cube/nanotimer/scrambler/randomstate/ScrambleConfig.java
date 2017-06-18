package com.cube.nanotimer.scrambler.randomstate;

import com.cube.nanotimer.vo.ThreeScrambleType;

public class ScrambleConfig {

  private int maxLength;
  private ThreeScrambleType threeScrambleType;

  public ScrambleConfig(int maxLength) {
    this(maxLength, ThreeScrambleType.RANDOM);
  }

  public ScrambleConfig(int maxLength, ThreeScrambleType threeScrambleType) {
    this.maxLength = maxLength;
    this.threeScrambleType = threeScrambleType;
  }

  public int getMaxLength() {
    return maxLength;
  }

  public ThreeScrambleType getThreeScrambleType() {
    return threeScrambleType;
  }

}
