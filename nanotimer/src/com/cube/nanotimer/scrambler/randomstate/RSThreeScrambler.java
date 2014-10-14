package com.cube.nanotimer.scrambler.randomstate;

import android.util.Log;
import com.cube.nanotimer.scrambler.Scrambler;
import com.cube.nanotimer.scrambler.randomstate.ThreeSolver.CubeState;
import com.cube.nanotimer.util.helper.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RSThreeScrambler implements Scrambler {

  private ThreeSolver threeSolver = new ThreeSolver();

  @Override
  public String[] getNewScramble() {
    CubeState randomState = getRandomState();
    Log.i("[NanoTimer]", "Random state: " + randomState.toString());
    String[] solution = threeSolver.getSolution(randomState);
    Log.i("[NanoTimer]", "Solution: " + solution.toString());
    String[] scramble = invertMoves(solution);
    Log.i("[NanoTimer]", "Scramble: " + scramble.toString());
    return scramble;
  }

  private CubeState getRandomState() {
    CubeState cubeState;
    Random r = Utils.getRandom();

    // TODO : could also generate a simple random int for each, and unpack it!
    //   (base index is now 0)

    do {
      cubeState = new CubeState();
      // corners
      List<Byte> positions = new ArrayList<Byte>(8);
      for (byte i = 1; i <= 8; i++) {
        positions.add(i);
      }
      Collections.shuffle(positions, r);
      for (int i = 0; i < 8; i++) {
        cubeState.cornerPermutations[i] = positions.get(i);
        if (i < 7) {
          cubeState.cornerOrientations[i] = (byte) r.nextInt(3);
          cubeState.cornerOrientations[7] += cubeState.cornerOrientations[i];
        }
      }
      cubeState.cornerOrientations[7] = (byte) ((3 - cubeState.cornerOrientations[7] % 3) % 3);

      // edges
      // TODO : make sure that edge position is possible (based on corner positions) and that all positions still have equal probabilities (parities etc)
      positions = new ArrayList<Byte>(12);
      for (byte i = 1; i <= 12; i++) {
        positions.add(i);
      }
      Collections.shuffle(positions, r);
      for (int i = 0; i < 12; i++) {
        cubeState.edgePermutations[i] = positions.get(i);
        if (i < 11) {
          cubeState.cornerOrientations[i] = (byte) r.nextInt(2);
          cubeState.cornerOrientations[11] = cubeState.cornerOrientations[i];
        }
      }
      cubeState.cornerOrientations[11] = (byte) ((2 - cubeState.cornerOrientations[11] % 2) % 2);
    } while (hasParity(cubeState.cornerPermutations) != hasParity(cubeState.edgePermutations));

    return cubeState;
  }

  boolean hasParity(byte[] permutation) {
    List<Byte> available = new ArrayList<Byte>(permutation.length);
    for (byte i = 0; i < permutation.length; i++) {
      available.add(i);
    }
    int nextInd = 0;
    available.remove(0);
    while (available.size() > 2) {
      if (!available.contains((byte) nextInd)) {
        nextInd = available.get(0);
      }
      available.remove((byte) nextInd);
      nextInd = permutation[nextInd];

      if (!available.contains((byte) nextInd)) {
        nextInd = available.get(0);
      }
      available.remove((byte) nextInd);
      nextInd = permutation[nextInd];
    }
    return available.size() == 1;
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
