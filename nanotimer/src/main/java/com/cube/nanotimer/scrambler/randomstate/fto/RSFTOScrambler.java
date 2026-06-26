package com.cube.nanotimer.scrambler.randomstate.fto;

import android.content.Context;

import com.cube.nanotimer.scrambler.randomstate.RSScrambler;
import com.cube.nanotimer.scrambler.randomstate.ScrambleConfig;

import java.util.Random;

/**
 * Random-state scrambler for the Face-Turning Octahedron: samples a uniformly
 * random legal state and returns the scramble that produces it (the inverse of
 * its solution).
 */
public class RSFTOScrambler implements RSScrambler {

  private FTOSolver solver;

  @Override
  public String[] getNewScramble(ScrambleConfig config) {
    solver = new FTOSolver();

    Random random = new Random();
    FtoCubie fc = new FtoCubie();
    fc.ep = FtoMath.rndPerm(12, true, random);
    fc.uf = FtoMath.rndPerm(12, true, random);
    fc.rl = FtoMath.rndPerm(12, true, random);
    fc.cp = FtoMath.rndPerm(6, true, random);
    fc.co = FtoMath.setNOri(new int[6], random.nextInt(32), 6, -2);

    return solver.solveFacelet(fc.toFaceCube(), true);
  }

  @Override
  public void prepareGenTables(Context context) {
  }

  @Override
  public void genTables() {
    FTOSolver.genTables();
  }

  @Override
  public void stop() {
    if (solver != null) {
      solver.stop();
    }
  }
}
