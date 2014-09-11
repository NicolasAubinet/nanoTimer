package com.cube.nanotimer.scrambler.basic;

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
