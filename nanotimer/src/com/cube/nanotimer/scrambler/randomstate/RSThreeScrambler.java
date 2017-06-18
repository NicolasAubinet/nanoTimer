package com.cube.nanotimer.scrambler.randomstate;

import com.cube.nanotimer.util.helper.Utils;
import com.cube.nanotimer.vo.ThreeCubeState;
import com.cube.nanotimer.vo.ThreeScrambleType;

import java.util.Random;

public class RSThreeScrambler implements RSScrambler {

  private ThreeSolver threeSolver = new ThreeSolver();

  @Override
  public String[] getNewScramble(ScrambleConfig config) {
    String[] scramble;
    ThreeScrambleType threeScrambleType = config.getThreeScrambleType();

    do {
      ThreeCubeState randomState;
      if (threeScrambleType == null || threeScrambleType == ThreeScrambleType.RANDOM) {
        Random r = new Random();
        randomState = new ThreeCubeState();
        randomState.cornerPermutations = IndexConvertor.unpackPermutation(r.nextInt(StateTables.N_CORNER_PERMUTATIONS), new byte[8]);
        randomState.edgePermutations = IndexConvertor.unpackPermutation(r.nextInt(StateTables.N_EDGE_PERMUTATIONS), new byte[12]);
        randomState.cornerOrientations = IndexConvertor.unpackOrientation(r.nextInt(StateTables.N_CORNER_ORIENTATIONS), new byte[8], (byte) 3);
        randomState.edgeOrientations = IndexConvertor.unpackOrientation(r.nextInt(StateTables.N_EDGE_ORIENTATIONS), new byte[12], (byte) 2);
      } else {
        randomState = threeScrambleType.getRandomState();
      }
//      Log.i("[NanoTimer]", "Random state:\n" + randomState.toString());
      scramble = Utils.invertMoves(threeSolver.getSolution(randomState, config));
//      Log.i("[NanoTimer]", "Scramble: " + Arrays.toString(scramble));
    } while (scramble != null && scramble.length < 12 && threeScrambleType == ThreeScrambleType.RANDOM);

    scramble = threeScrambleType.finalizeScramble(scramble);
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
