package com.cube.nanotimer.scrambler.randomstate;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import com.cube.nanotimer.scrambler.randomstate.ThreeSolver.CubeState;
import junit.framework.Assert;

import java.util.Arrays;

public class ThreeSolverTest extends AndroidTestCase {

  @SmallTest
  public void testPhase1VeryEasy() {
    CubeState cubeState = new CubeState();
    //  F
    cubeState.cornerPermutations = new byte[] { 1, 6, 2, 4, 5, 7, 3, 8 };
    cubeState.edgePermutations = new byte[] { 5, 2, 3, 9, 4, 6, 7, 8, 1, 10, 11, 12 };
    cubeState.cornerOrientations = new byte[] { 0, 2, 1, 0, 0, 1, 2, 0 };
    cubeState.edgeOrientations = new byte[] { 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0 };

    ThreeSolver solver = new ThreeSolver();
    String[] scramble = solver.getSolution(cubeState);
    Log.i("[NanoTimer]", "Scramble: " + Arrays.toString(scramble));
    Assert.assertTrue(scramble.length > 0);
  }

  @SmallTest
  public void testPhase1Easy() {
    CubeState cubeState = new CubeState();
    //  F L2 R' D2
    cubeState.cornerPermutations = new byte[] { 7, 5, 4, 8, 2, 3, 6, 1 };
    cubeState.edgePermutations = new byte[] { 6, 10, 9, 3, 4, 2, 7, 12, 11, 8, 1, 5 };
    cubeState.cornerOrientations = new byte[] { 1, 0, 2, 1, 2, 1, 2, 0 };
    cubeState.edgeOrientations = new byte[] { 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 1 };

    ThreeSolver solver = new ThreeSolver();
    String[] scramble = solver.getSolution(cubeState);
    Log.i("[NanoTimer]", "Scramble: " + Arrays.toString(scramble));
    Assert.assertTrue(scramble.length > 0);
  }

  @SmallTest
  public void testPhase1() {
    CubeState cubeState = new CubeState();
    //  F L2 R' D2 R2 D' U' R D' B2 U2 F' U2 L2 D L R' U' F2 L R D2 B R' U2
    cubeState.cornerPermutations = new byte[] { 4, 1, 5, 2, 7, 3, 6, 8 };
    cubeState.edgePermutations = new byte[] { 2, 10, 9, 1, 12, 8, 7, 11, 5, 6, 4, 3 };
    cubeState.cornerOrientations = new byte[] { 2, 2, 2, 1, 0, 1, 1, 0 };
    cubeState.edgeOrientations = new byte[] { 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1 };

    ThreeSolver solver = new ThreeSolver();
    String[] scramble = solver.getSolution(cubeState);
    Log.i("[NanoTimer]", "Scramble: " + Arrays.toString(scramble));
  }

}
