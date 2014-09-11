package com.cube.nanotimer.scrambler.randomstate;

import com.cube.nanotimer.scrambler.Scrambler;
import com.cube.nanotimer.scrambler.randomstate.ThreeSolver.CubeState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RSThreeScrambler implements Scrambler {

  private ThreeSolver threeSolver = new ThreeSolver();

  @Override
  public String[] getNewScramble() {
    return threeSolver.getSolution(getRandomState());
  }

  private CubeState getRandomState() {
    CubeState cubeState = new CubeState();
    Random r = new Random();
    List<Byte> positions = new ArrayList<Byte>();

    // corners
    for (byte i = 1; i < 9; i++) {
      positions.add(i);
    }
    for (int i = 0; i < 7; i++) {
      cubeState.cornerPermutations[i] = positions.remove((byte) r.nextInt(positions.size()));
      cubeState.cornerOrientations[i] = (byte) r.nextInt(3);
    }

    // edges
    for (byte i = 1; i < 13; i++) {
      positions.add(i);
    }
    for (int i = 0; i < 11; i++) {
      cubeState.edgePermutations[i] = positions.remove((byte) r.nextInt(positions.size()));
      cubeState.edgeOrientations[i] = (byte) r.nextInt(2);
    }

    return cubeState;
  }

}
