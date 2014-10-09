package com.cube.nanotimer.scrambler.randomstate;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import com.cube.nanotimer.scrambler.randomstate.ThreeSolver.CubeState;
import junit.framework.Assert;

import java.util.Arrays;

public class ThreeSolverTest extends AndroidTestCase {

  // TODO : test with a solved cube (scramble should be empty, but not null)
  // TODO : could also test a non-solvable configuration to see how long it takes to return null

  @SmallTest
  public void testVeryEasy() {
    CubeState cubeState = new CubeState();
    //  F
    cubeState.cornerPermutations = new byte[] { 0, 5, 1, 3, 4, 6, 2, 7 };
    cubeState.edgePermutations = new byte[] { 4, 1, 2, 8, 3, 5, 6, 7, 0, 9, 10, 11 };
    cubeState.cornerOrientations = new byte[] { 0, 2, 1, 0, 0, 1, 2, 0 };
    cubeState.edgeOrientations = new byte[] { 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0 };

    ThreeSolver solver = new ThreeSolver();
    String[] scramble = solver.getSolution(cubeState);
    Log.i("[NanoTimer]", "Scramble: " + Arrays.toString(scramble));
    Assert.assertTrue(scramble.length > 0);
  }

  // TODO : pass something that's already orientated and combined

  @SmallTest
  public void testEasy() {
    CubeState cubeState = new CubeState();
    //  F L2 R' D2
    cubeState.cornerPermutations = new byte[] { 6, 4, 3, 7, 1, 2, 5, 0 };
    cubeState.edgePermutations = new byte[] { 5, 9, 8, 2, 3, 1, 6, 11, 10, 7, 0, 4 };
    cubeState.cornerOrientations = new byte[] { 1, 0, 2, 1, 2, 1, 2, 0 };
    cubeState.edgeOrientations = new byte[] { 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 1 };

    ThreeSolver solver = new ThreeSolver();
    String[] scramble = solver.getSolution(cubeState);
    Log.i("[NanoTimer]", "Scramble: " + Arrays.toString(scramble));
    Assert.assertTrue(scramble.length > 0);
  }

  @SmallTest
  public void testEasyWithLastPhase1Move() {
    CubeState cubeState = new CubeState();
    //  D2 R B2 F
//    cubeState.cornerPermutations = new byte[] { 6, 4, 3, 7, 1, 2, 5, 0 };
//    cubeState.edgePermutations = new byte[] { 5, 9, 8, 2, 3, 1, 6, 11, 10, 7, 0, 4 };
//    cubeState.cornerOrientations = new byte[] { 1, 0, 2, 1, 2, 1, 2, 0 };
//    cubeState.edgeOrientations = new byte[] { 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 1 };

    ThreeSolver solver = new ThreeSolver();
    String[] scramble = solver.getSolution(cubeState);
    Log.i("[NanoTimer]", "Scramble: " + Arrays.toString(scramble));
    Assert.assertTrue(scramble.length > 0);
  }

  @SmallTest
  public void testRealScramble() {
    CubeState cubeState = new CubeState();
    //  F L2 R' D2 R2 D' U' R D' B2 U2 F' U2 L2 D L R' U' F2 L R D2 B R' U2
    cubeState.cornerPermutations = new byte[] { 3, 0, 4, 1, 6, 2, 5, 7 };
    cubeState.edgePermutations = new byte[] { 1, 9, 8, 0, 11, 7, 6, 10, 4, 5, 3, 2 };
    cubeState.cornerOrientations = new byte[] { 2, 2, 2, 1, 0, 1, 1, 0 };
    cubeState.edgeOrientations = new byte[] { 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1 };

    ThreeSolver solver = new ThreeSolver();
    String[] scramble = solver.getSolution(cubeState);
    Log.i("[NanoTimer]", "Scramble: " + Arrays.toString(scramble) + " (length: " + scramble.length + ")");
  }

}
