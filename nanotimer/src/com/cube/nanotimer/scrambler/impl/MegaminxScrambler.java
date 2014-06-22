package com.cube.nanotimer.scrambler.impl;

import com.cube.nanotimer.scrambler.Scrambler;

import java.util.Random;

public class MegaminxScrambler implements Scrambler {

  private final int MOVES_PER_LINE = 10;
  private final int LINES_COUNT = 7;

  private String[] mainMoves = {"R", "D"};
  private String endMove = "U";

  @Override
  public String getNewScramble() {
    StringBuilder scramble = new StringBuilder();
    Random rand = new Random();
    for (int i = 0; i < LINES_COUNT; i++) {
      int firstMove = rand.nextInt(2);
      for (int j = 0; j < MOVES_PER_LINE; j++) {
        scramble.append(mainMoves[(firstMove + j) % mainMoves.length]);
        if (rand.nextBoolean()) {
          scramble.append("++ ");
        } else {
          scramble.append("-- ");
        }
      }
      scramble.append(endMove);
      if (rand.nextBoolean()) {
        scramble.append("'");
      } else {
        scramble.append(" ");
      }
      scramble.append("\n");
    }
    return scramble.toString();
  }

}
