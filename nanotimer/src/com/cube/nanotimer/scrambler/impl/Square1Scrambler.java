package com.cube.nanotimer.scrambler.impl;

import com.cube.nanotimer.scrambler.Scrambler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Square1Scrambler implements Scrambler {

  // There are 12 positions on both top and bottom layer
  // On the top layer, position 0 indicates the back position at the right (the back one closest to the middle, affected by a R2 move)
  //                   counts clockwise to the 11th position that is back-left
  // The bottom layer starts with position 12 at the front-right position (the front one closest to the middle, affected by a R2 move)
  //                   counts clockwise (when facing the face) to the 23th position that is front-middle

  public static final int MOVES_COUNT_MIN = 16;
  public static final int MOVES_COUNT_DELTA = 4;

  @Override
  public String[] getNewScramble() {
    Random r = new Random();
    int movesCount = MOVES_COUNT_MIN + r.nextInt(MOVES_COUNT_DELTA);
    List<String> moves = new ArrayList<String>();
    Square1 s = new Square1();

    for (int i = 0; i < movesCount;) {
      Move move = new Move((short) (r.nextInt(12) - 5), (short) (r.nextInt(12) - 5));
      if (move.top == 0 && move.bottom == 0) {
        continue;
      }
      Square1 afterMove = new Square1(s.corners); // tmp copy to check if the move is valid
      afterMove.executeMove(move);
      if (afterMove.canTurn()) {
        moves.add(move.toString() + " ");
        s = afterMove;
        s.turn();
        i++;
        if (i % 4 == 0) {
          moves.add("\n");
        }
      }
    }
    return moves.toArray(new String[0]);
  }

  private class Square1 {
    private Corner[] corners = new Corner[8];

    public Square1() {
      initCorners();
    }

    public Square1(Corner[] corners) {
      for (int i = 0; i < corners.length; i++) {
        this.corners[i] = new Corner(corners[i]);
      }
    }

    private void initCorners() {
      // top corners (when solved)
      corners[0] = new Corner(0, 1);
      corners[1] = new Corner(3, 4);
      corners[2] = new Corner(6, 7);
      corners[3] = new Corner(9, 10);
      // bottom corners (when solved)
      corners[4] = new Corner(13, 14);
      corners[5] = new Corner(16, 17);
      corners[6] = new Corner(19, 20);
      corners[7] = new Corner(22, 23);
    }

    public void executeMove(Move m) {
      for (Corner c : corners) {
        c.move(m);
      }
    }

    public void turn() {
      for (Corner c : corners) {
        c.turn();
      }
    }

    /**
     * Indicates whether the corners are blocking the R2/L2 move
     * @return true if a R2/L2 move can be done, false if at least one corner is blocking
     */
    public boolean canTurn() {
      for (Corner c : corners) {
        if (c.isBlocking()) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Represents a corner
   * A corner spans over two positions (60°)
   * Corners are represented to keep track of their positions
   * because they can prevent from turning if they are on the R2 line.
   */
  private class Corner {
    int pos1;
    int pos2;

    public Corner(int pos1, int pos2) {
      this.pos1 = pos1;
      this.pos2 = pos2;
    }

    public Corner(Corner corner) {
      this.pos1 = corner.pos1;
      this.pos2 = corner.pos2;
    }

    /**
     * Move this corner (turn the face)
     * @param m the move to apply
     */
    public void move(Move m) {
      if (isTopLayer()) {
        pos1 = (pos1 + m.top + 12) % 12; // + 12 to handle positions like 0 with a negative move
        pos2 = (pos2 + m.top + 12) % 12;
      } else {
        pos1 = ((pos1 + m.bottom) % 12) + 12;
        pos2 = ((pos2 + m.bottom) % 12) + 12;
      }
    }

    /**
     * Execute a R2 move (move corner of the R side to the opposite layer)
     */
    public void turn() {
      if (isOnRightSide()) {
        if (isTopLayer()) {
          pos1 += 12;
          pos2 += 12;
        } else {
          pos1 -= 12;
          pos2 -= 12;
        }
      }
    }

    /**
     * Indicates whether this corner is located in a position that prevents from doing R2/L2
     * @return true if the corner is blocking a R2/L2 move
     */
    public boolean isBlocking() {
      return (pos1 == 11 && pos2 == 0) || (pos1 == 5 && pos2 == 6) // top
          || (pos1 == 23 && pos2 == 12) || (pos1 == 17 && pos2 == 18) ;// bottom
    }

    public boolean isTopLayer() {
      return pos2 < 12;
    }

    public boolean isOnRightSide() {
      return pos2 < 6 || (pos1 >= 12 && pos2 < 18);
    }
  }

  private class Move {
    short top;
    short bottom;

    public Move(short top, short bottom) {
      this.top = top;
      this.bottom = bottom;
    }

    @Override
    public String toString() {
      return "(" + String.format("%2d", top) + "," + String.format("%2d", bottom) + ")";
    }
  }

}
