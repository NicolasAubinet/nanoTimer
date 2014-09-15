package com.cube.nanotimer.scrambler.basic;

import com.cube.nanotimer.Options;
import com.cube.nanotimer.Options.BigCubesNotation;

public class FiveScrambler extends AbstractCubeScrambler {

  @Override
  protected String[][] getMoves() {
    if (Options.INSTANCE.getBigCubesNotation() == BigCubesNotation.RUF) {
      return new String[][] {
          { "U", "D", "u", "d" },
          { "F", "B", "f", "b" },
          { "R", "L", "r", "l" }
      };
    } else {
      return new String[][] {
          { "U", "D", "Uw", "Dw" },
          { "F", "B", "Fw", "Bw" },
          { "R", "L", "Rw", "Lw" }
      };
    }
  }

  @Override
  protected int getMoveCount() {
    return 60;
  }
}
