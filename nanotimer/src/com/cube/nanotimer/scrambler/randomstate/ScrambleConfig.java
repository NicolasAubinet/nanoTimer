package com.cube.nanotimer.scrambler.randomstate;

public class ScrambleConfig {

  private int maxLength;
  private ThreeScrambleStyle threeScrambleStyle;

  public ScrambleConfig(int maxLength) {
    this(maxLength, ThreeScrambleStyle.RANDOM);
  }

  public ScrambleConfig(int maxLength, ThreeScrambleStyle threeScrambleStyle) {
    this.maxLength = maxLength;
    this.threeScrambleStyle = threeScrambleStyle;
  }

  public int getMaxLength() {
    return maxLength;
  }

  public void setMaxLength(int maxLength) {
    this.maxLength = maxLength;
  }

  public ThreeScrambleStyle getThreeScrambleStyle() {
    return threeScrambleStyle;
  }

}
