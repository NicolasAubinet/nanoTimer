package com.cube.nanotimer.scrambler.randomstate.square1;

import com.cube.nanotimer.scrambler.randomstate.RSScrambler;
import com.cube.nanotimer.scrambler.randomstate.ScrambleConfig;

import java.util.Random;

public class Square1RandomScrambler implements RSScrambler {
  private Square1Solver solver;
  private Random random;

  public Square1RandomScrambler() {
    this.solver = new Square1Solver();
    this.random = new Random();
  }

  @Override
  public String[] getNewScramble(ScrambleConfig config) {
    Square1State randomState = solver.getRandomState(random);
    return solver.generate(randomState);
  }

  @Override
  public void freeMemory() {
    Square1Solver.freeMemory();
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
