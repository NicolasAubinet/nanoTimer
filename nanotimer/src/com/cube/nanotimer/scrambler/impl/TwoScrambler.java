package com.cube.nanotimer.scrambler.impl;

public class TwoScrambler extends AbstractCubeScrambler {

  @Override
  protected String[][] getMoves() {
    return new String[][]{
        {"U", "D"},
        {"F", "B"},
        {"R", "L"}
    };
  }

  @Override
  protected int getMoveCount() {
    return 9;
  }
}
