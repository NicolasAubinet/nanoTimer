package com.cube.nanotimer.scrambler.randomstate.skewb;

import android.content.Context;
import com.cube.nanotimer.scrambler.randomstate.RSScrambler;
import com.cube.nanotimer.scrambler.randomstate.ScrambleConfig;

import java.util.Random;

public class RSSkewbScrambler implements RSScrambler {
  private SkewbSolver solver = new SkewbSolver();

  public RSSkewbScrambler() {
  }

  @Override
  public String[] getNewScramble(ScrambleConfig config) {
    SkewbState randomState = solver.getRandomState(new Random());
    return solver.generate(randomState);
  }

  @Override
  public void prepareGenTables(Context context) {
  }

  @Override
  public void genTables() {
    SkewbSolver.genTables();
  }

  @Override
  public void stop() {
    solver.stop();
  }
}
