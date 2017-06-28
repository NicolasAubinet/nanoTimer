package com.cube.nanotimer.scrambler.basic;

import com.cube.nanotimer.scrambler.Scrambler;

import java.util.Random;

public class PyraminxScrambler implements Scrambler {

  private static final int MOVES_COUNT = 15;

  protected String[] vertexMoves = { "l", "r", "b", "u" };
  protected String[] mainMoves = { "L", "R", "B", "U" };

  private Random rand = new Random();

  @Override
  public String[] getNewScramble() {
    String[] scramble = new String[MOVES_COUNT];
    int prevSlice = -1;
    int vMovesCount = 0; // vertex moves count

    for (int vInd = vertexMoves.length - 1; vInd >= 0; vInd--) { // vertex (corner) moves
      // fill vertex moves from the end (should be at the end of the scramble)
      if (rand.nextBoolean()) {
        scramble[scramble.length - 1 - vMovesCount] = vertexMoves[vInd];
        scramble[scramble.length - 1 - vMovesCount] += getRandDirection();
        vMovesCount++;
      }
    }
    if (vMovesCount == 0) { // must have at least one corner turn
      scramble[scramble.length - 1] = vertexMoves[rand.nextInt(vertexMoves.length)];
      scramble[scramble.length - 1] += getRandDirection();
      vMovesCount++;
    }

    // fill in main moves
    for (int i = 0; i < MOVES_COUNT - vMovesCount; i++) {
      int sliceInd;
      do {
        sliceInd = rand.nextInt(mainMoves.length);
      } while (sliceInd == prevSlice);

      scramble[i] = mainMoves[sliceInd];
      scramble[i] += getRandDirection();

      prevSlice = sliceInd;
    }

    return scramble;
  }

  private String getRandDirection() {
    if (rand.nextBoolean()) {
      return "'";
    }
    return "";
  }

}
