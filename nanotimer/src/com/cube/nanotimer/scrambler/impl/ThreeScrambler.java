package com.cube.nanotimer.scrambler.impl;

public class ThreeScrambler extends AbstractCubeScrambler {

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
    return 25;
  }

  @Override
  protected int getMovesPerLine() {
    return 5;
  }
}
