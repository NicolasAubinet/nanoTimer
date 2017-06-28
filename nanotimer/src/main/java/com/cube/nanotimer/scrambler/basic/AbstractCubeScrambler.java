package com.cube.nanotimer.scrambler.basic;

import com.cube.nanotimer.scrambler.Scrambler;

import java.util.Random;

public abstract class AbstractCubeScrambler implements Scrambler {

  protected String[] directions = {"", "'", "2"};

  @Override
  public String[] getNewScramble() {
    String[] scramble = new String[getMoveCount()];
    String[][] moves = getMoves();
    Random rand = new Random();
    int prevSlice = -1;

    for (int i = 0; i < getMoveCount(); i++) {
      int sliceInd, sliceMoveInd;
      String m;
      do {
        sliceInd = rand.nextInt(moves.length);
        sliceMoveInd = rand.nextInt(moves[sliceInd].length);
        m = moves[sliceInd][sliceMoveInd];
      } while (sliceInd == prevSlice);

      String direction = directions[rand.nextInt(directions.length)];
      scramble[i] = (m + direction);

      prevSlice = sliceInd;
    }

    return scramble;
  }

  protected abstract String[][] getMoves(); // double array to group by slice

  protected abstract int getMoveCount();
}
