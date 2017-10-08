package com.cube.nanotimer.scrambler.randomstate.pyraminx;

import com.cube.nanotimer.scrambler.randomstate.RSScrambler;
import com.cube.nanotimer.scrambler.randomstate.ScrambleConfig;

import java.util.Random;

public class RSPyraminxScrambler implements RSScrambler {
  private PyraminxSolver solver;

  public RSPyraminxScrambler() {
  }

  @Override
  public String[] getNewScramble(ScrambleConfig config) {
    solver = new PyraminxSolver(config.getMaxLength());

    PyraminxState randomState = solver.getRandomState(new Random());
    return solver.generate(randomState);
  }

  @Override
  public void genTables() {
    PyraminxSolver.genTables();
  }

  @Override
  public void stop() {
    solver.stop();
  }
}
