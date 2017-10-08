package com.cube.nanotimer.scrambler.randomstate;

import com.cube.nanotimer.util.helper.Utils;
import com.cube.nanotimer.vo.TwoCubeState;

import java.util.Random;

public class RSTwoScrambler implements RSScrambler {

  private TwoSolver twoSolver = new TwoSolver();

  @Override
  public String[] getNewScramble(ScrambleConfig config) {
    String[] scramble;
    do {
      TwoCubeState randomState = getRandomState();
//      Log.i("[NanoTimer]", "Random state:\n" + randomState.toString());
      scramble = Utils.invertMoves(twoSolver.getSolution(randomState, config));
//      Log.i("[NanoTimer]", "Scramble: " + Arrays.toString(scramble));
    } while (scramble != null && scramble.length < 4);
    return scramble;
  }

  @Override
  public void genTables() {
    TwoSolver.genTables();
  }

  @Override
  public void stop() {
    twoSolver.stop();
  }

  private TwoCubeState getRandomState() {
    TwoCubeState cubeState;
    Random r = Utils.getRandom();

    byte[] state;

    cubeState = new TwoCubeState();

    state = new byte[7];
    IndexConvertor.unpackPermutation(r.nextInt(TwoSolver.N_PERM), state);
    cubeState.permutations = state;

    state = new byte[7];
    IndexConvertor.unpackOrientation(r.nextInt(TwoSolver.N_ORIENT), state, (byte) 3);
    cubeState.orientations = state;

    return cubeState;
  }

}
