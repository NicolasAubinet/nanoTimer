package com.cube.nanotimer.scrambler.basic;

import com.cube.nanotimer.scrambler.Scrambler;

import java.util.Random;

public class FTOScrambler implements Scrambler {

  private static final int MOVES_COUNT = 30;

  // The 8 faces of the octahedron. Each face turns +/-120 degrees.
  protected String[] moves = { "U", "F", "BR", "BL", "D", "B", "R", "L" };

  private Random rand = new Random();

  @Override
  public String[] getNewScramble() {
    String[] scramble = new String[MOVES_COUNT];
    int prevFace = -1;

    for (int i = 0; i < MOVES_COUNT; i++) {
      int faceInd;
      do {
        faceInd = rand.nextInt(moves.length);
      } while (faceInd == prevFace);

      scramble[i] = moves[faceInd];
      if (rand.nextBoolean()) {
        scramble[i] += "'";
      }

      prevFace = faceInd;
    }

    return scramble;
  }

}
