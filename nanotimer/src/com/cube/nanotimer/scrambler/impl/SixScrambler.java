package com.cube.nanotimer.scrambler.impl;

public class SixScrambler extends AbstractCubeScrambler {

  @Override
  protected String[][] getMoves() {
    return new String[][]{
        {"U", "D", "2U", "2D", "3U", "3D"},
        {"F", "B", "2F", "2B", "3F", "3B"},
        {"R", "L", "2R", "2L", "3R", "3L"}
    };
  }

  @Override
  protected int getMoveCount() {
    return 80;
  }
}
