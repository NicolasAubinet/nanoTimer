package com.cube.nanotimer.scrambler.randomstate;

import com.cube.nanotimer.scrambler.randomstate.ThreeSolver.CubeState;
import com.cube.nanotimer.util.helper.Utils;

public class RSThreeScrambler implements RSScrambler {

  private ThreeSolver threeSolver = new ThreeSolver();

  @Override
  public String[] getNewScramble(ScrambleConfig config) {
    String[] scramble;
    ThreeScrambleStyle threeScrambleStyle = config.getThreeScrambleStyle();

    do {
      CubeState randomState = threeScrambleStyle.getRandomState();
//      Log.i("[NanoTimer]", "Random state:\n" + randomState.toString());
      scramble = Utils.invertMoves(threeSolver.getSolution(randomState, config));
//      Log.i("[NanoTimer]", "Scramble: " + Arrays.toString(scramble));
    } while (scramble != null && scramble.length < 12 && threeScrambleStyle == ThreeScrambleStyle.RANDOM);

    scramble = threeScrambleStyle.finalizeScramble(scramble);
    return scramble;
  }

  @Override
  public void freeMemory() {
    threeSolver.freeMemory();
  }

  @Override
  public void genTables() {
    threeSolver.genTables();
  }

  @Override
  public void stop() {
    threeSolver.stop();
  }

}
