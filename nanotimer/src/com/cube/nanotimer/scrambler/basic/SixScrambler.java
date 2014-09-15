package com.cube.nanotimer.scrambler.basic;

import com.cube.nanotimer.Options;
import com.cube.nanotimer.Options.BigCubesNotation;

public class SixScrambler extends AbstractCubeScrambler {

  @Override
  protected String[][] getMoves() {
    if (Options.INSTANCE.getBigCubesNotation() == BigCubesNotation.RUF) {
      return new String[][] {
          { "U", "D", "2U", "2D", "3U", "3D" },
          { "F", "B", "2F", "2B", "3F", "3B" },
          { "R", "L", "2R", "2L", "3R", "3L" }
      };
    } else {
      return new String[][] {
          { "U", "D", "Uw", "Dw", "3Uw", "3Dw" },
          { "F", "B", "Fw", "Bw", "3Fw", "3Bw" },
          { "R", "L", "Rw", "Lw", "3Rw", "3Lw" }
      };
    }
  }

  @Override
  protected int getMoveCount() {
    return 80;
  }
}
