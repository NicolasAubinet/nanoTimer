package com.cube.nanotimer.scrambler.randomstate.square1;

import com.cube.nanotimer.scrambler.randomstate.RSScrambler;
import com.cube.nanotimer.scrambler.randomstate.ScrambleConfig;

import java.util.Random;

public class RSSquare1Scrambler implements RSScrambler {
  private Square1Solver solver = new Square1Solver();

  public RSSquare1Scrambler() {
  }

  @Override
  public String[] getNewScramble(ScrambleConfig config) {
    Square1State randomState = solver.getRandomState(new Random());
    return solver.generate(randomState);
  }

  @Override
  public void genTables() {
    Square1Solver.genTables();
  }

  @Override
  public void stop() {
    solver.stop();
  }
}
