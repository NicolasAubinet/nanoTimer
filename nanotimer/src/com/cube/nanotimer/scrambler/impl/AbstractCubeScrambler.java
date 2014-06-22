package com.cube.nanotimer.scrambler.impl;

import com.cube.nanotimer.scrambler.Scrambler;

import java.util.Random;

public abstract class AbstractCubeScrambler implements Scrambler {

  protected String[] directions = {"", "'", "2"};

  @Override
  public String getNewScramble() {
    String[] scramble = new String[getMoveCount()];
    String[][] moves = getMoves();
    Random rand = new Random();
    String prevMove = "";
    int prevSlice = -1;
    int consecSlice = -1; // to avoid turning three times the same slice

    for (int i = 0; i < getMoveCount(); i++) {
      int sliceInd, sliceMoveInd;
      String m;
      do {
        sliceInd = rand.nextInt(moves.length);
        sliceMoveInd = rand.nextInt(moves[sliceInd].length);
        m = moves[sliceInd][sliceMoveInd];
      } while (m.equals(prevMove) || sliceInd == consecSlice);

      String direction = directions[rand.nextInt(directions.length)];
      scramble[i] = (m + direction);

      if (sliceInd == prevSlice) {
        consecSlice = sliceInd;
      } else {
        consecSlice = -1;
      }
      prevMove = m;
      prevSlice = sliceInd;
    }

    return formatScramble(scramble);
  }

  /**
   * Format the scramble to add enough spaces to align the moves vertically
   * and to split it among multiple lines.
   *
   * @param scramble the scramble in array form
   * @return the formatted scramble
   */
  protected String formatScramble(String[] scramble) {
    int maxMoveLength = 1;
    for (String[] sliceMoves : getMoves()) {
      for (String m : sliceMoves) {
        if (m.length() > maxMoveLength) {
          maxMoveLength = m.length();
        }
      }
    }

    StringBuilder s = new StringBuilder();
    for (int i = 0; i < scramble.length; i++) {
      s.append(scramble[i]);
      for (int j = scramble[i].length(); j < maxMoveLength + 2; j++) {
        s.append(" ");
      }
      if (getMovesPerLine() > 0 && (i + 1) % getMovesPerLine() == 0 && i > 0 && i < getMoveCount() - 1) {
        s.append("\n");
      }
    }
    return s.toString();
  }

  protected int getMovesPerLine() {
    return 10;
  }

  protected abstract String[][] getMoves(); // double array to group by slice

  protected abstract int getMoveCount();
}
