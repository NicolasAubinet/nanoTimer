package com.cube.nanotimer.scrambler.basic;

import com.cube.nanotimer.scrambler.Scrambler;

import java.util.Random;

public class SkewbScrambler implements Scrambler {

  private static final int MOVES_COUNT = 15;

  protected String[] moves = { "U", "R", "B", "L" };

  private Random rand = new Random();

  @Override
  public String[] getNewScramble() {
    String[] scramble = new String[MOVES_COUNT];
    int prevSlice = -1;

    for (int i = 0; i < MOVES_COUNT; i++) {
      int sliceInd;
      do {
        sliceInd = rand.nextInt(moves.length);
      } while (sliceInd == prevSlice);

      scramble[i] = moves[sliceInd];
      if (rand.nextBoolean()) {
        scramble[i] += "'";
      }

      prevSlice = sliceInd;
    }

    return scramble;
  }

}
