package com.cube.nanotimer.scrambler.impl;

import com.cube.nanotimer.scrambler.Scrambler;

import java.util.Random;

public class PyraminxScrambler implements Scrambler {

  private static final int MOVES_COUNT = 25;

  protected String[] vertexMoves = { "l", "r", "b", "u" };
  protected String[] mainMoves = { "L", "R", "B", "U" };

  private Random rand = new Random();

  @Override
  public String[] getNewScramble() {
    String[] scramble = new String[MOVES_COUNT];
    int prevSlice = -1;
    int i = 0;

    for (int corInd = 0; corInd < 4; corInd++) { // vertex (corner) moves
      if (rand.nextBoolean()) {
        scramble[i] = vertexMoves[corInd];
        scramble[i] += getRandDirection(scramble[i]);
        i++;
      }
    }
    if (i == 0) { // must have at least one corner turn
      scramble[i] = vertexMoves[rand.nextInt(4)];
      scramble[i] += getRandDirection(scramble[i]);
      i++;
    }

    for (; i < MOVES_COUNT; i++) {
      int sliceInd;
      do {
        sliceInd = rand.nextInt(mainMoves.length);
      } while (sliceInd == prevSlice);

      scramble[i] = mainMoves[sliceInd];
      scramble[i] += getRandDirection(scramble[i]);

      prevSlice = sliceInd;
    }

    return scramble;
  }

  private String getRandDirection(String move) {
    if (rand.nextBoolean()) {
      return "'";
    }
    return "";
  }

}
