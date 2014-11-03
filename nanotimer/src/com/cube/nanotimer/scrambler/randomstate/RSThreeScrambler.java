package com.cube.nanotimer.scrambler.randomstate;

import android.util.Log;
import com.cube.nanotimer.scrambler.randomstate.ThreeSolver.CubeState;
import com.cube.nanotimer.util.helper.Utils;

import java.util.Arrays;
import java.util.Random;

public class RSThreeScrambler implements RSScrambler {

  private ThreeSolver threeSolver = new ThreeSolver();

  @Override
  public String[] getNewScramble(ScrambleConfig config) {
    CubeState randomState = getRandomState();
//    Log.i("[NanoTimer]", "Random state:\n" + randomState.toString());
    String[] scramble = threeSolver.getSolution(randomState, config);
    Log.i("[NanoTimer]", "Scramble: " + Arrays.toString(scramble));
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

  private CubeState getRandomState() {
    CubeState cubeState;
    Random r = Utils.getRandom();

    byte[] state;

    do {
      cubeState = new CubeState();

      state = new byte[8];
      IndexConvertor.unpackPermutation(r.nextInt(StateTables.N_CORNER_PERMUTATIONS), state);
      cubeState.cornerPermutations = state;

      state = new byte[12];
      IndexConvertor.unpackPermutation(r.nextInt(StateTables.N_EDGE_PERMUTATIONS), state);
      cubeState.edgePermutations = state;

      state = new byte[8];
      IndexConvertor.unpackOrientation(r.nextInt(StateTables.N_CORNER_ORIENTATIONS), state, (byte) 3);
      cubeState.cornerOrientations = state;

      state = new byte[12];
      IndexConvertor.unpackOrientation(r.nextInt(StateTables.N_EDGE_ORIENTATIONS), state, (byte) 2);
      cubeState.edgeOrientations = state;
    } while (hasParity(cubeState.cornerPermutations) != hasParity(cubeState.edgePermutations));

    return cubeState;
  }

  static boolean hasParity(byte[] perm) {
    int inversion = 0;
    for (int i = 0; i < perm.length; i++) {
      for (int j = i + 1; j < perm.length; j++) {
        if (perm[i] > perm[j]) {
          inversion++;
        }
      }
    }
    return (inversion % 2 != 0);
  }

  private String[] invertMoves(String[] moves) {
    String[] inverted = new String[moves.length];
    for (int i = 0; i < moves.length; i++) {
      String m = moves[moves.length - 1 - i];
      if (m.endsWith("'")) {
        m = m.substring(0, m.length() - 1);
      } else if (!m.endsWith("2")) {
        m += "'";
      }
      inverted[i] = m;
    }
    return inverted;
  }

}
