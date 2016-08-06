package com.cube.nanotimer.scrambler.basic;

import com.cube.nanotimer.scrambler.Scrambler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClockUUdUxxScrambler implements Scrambler {

  // Style: (UUdU, 2, -4) (UUdd, 1, 4) (dUdU, 3, -4) ...

  private static final int MOVES_COUNT = 12;

  @Override
  public String[] getNewScramble() {
    List<ClockMove> clockMoves = new ArrayList<ClockMove>();
    String[] moves = new String[MOVES_COUNT];
    for (int i = 0; i < moves.length; ) {
      ClockMove cm = ClockMove.getRandomMove();
      boolean cancelMove = false;
      for (int j = 0; j < i; j++) {
        if (cm.equalsCornerPos(clockMoves.get(j))) {
          cancelMove = true;
        }
      }
      if (cancelMove) {
        continue;
      }
      clockMoves.add(cm);
      moves[i] = cm.toString();
      i++;
    }
    return moves;
  }

  private static class ClockMove {
    boolean buttons[] = new boolean[4];
    int corner; // 1 to 4
    int hours;  // 1 to 6 and -1 to -5
    private static Random r = new Random();

    public static ClockMove getRandomMove() {
      ClockMove cm = new ClockMove();
      for (int i = 0; i < cm.buttons.length; i++) {
        cm.buttons[i] = r.nextBoolean();
      }
      cm.corner = r.nextInt(4) + 1;
      cm.hours = r.nextInt(11);
      if (cm.hours <= 4) {
        cm.hours -= 5; // -1 to -5
      } else {
        cm.hours -= 4; // 1 to 6
      }
      return cm;
    }

    public boolean equalsCornerPos(ClockMove m) {
      for (int i = 0; i < buttons.length; i++) {
        if (this.buttons[i] != m.buttons[i]) {
          return false;
        }
      }
      return this.corner == m.corner;
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("(");
      for (boolean b : buttons) {
        if (b) {
          sb.append("U");
        } else {
          sb.append("d");
        }
      }
      sb.append(",").append(String.format("%2d", corner)).append(",");
      sb.append(String.format("%2d", hours)).append(")");
      return sb.toString();
    }
  }

}
