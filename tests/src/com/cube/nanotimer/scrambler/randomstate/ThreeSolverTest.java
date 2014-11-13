package com.cube.nanotimer.scrambler.randomstate;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import com.cube.nanotimer.scrambler.randomstate.ThreeSolver.CubeState;
import com.cube.nanotimer.util.helper.Utils;
import junit.framework.Assert;

import java.util.Arrays;

public class ThreeSolverTest extends AndroidTestCase {

  @SmallTest
  public void testSolvedCubeSolving() {
    CubeState cubeState = new CubeState();
    cubeState.cornerPermutations = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 };
    cubeState.edgePermutations = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    cubeState.cornerOrientations = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };
    cubeState.edgeOrientations = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    ThreeSolver solver = new ThreeSolver();
    String[] scramble = solver.getSolution(cubeState);
    Assert.assertEquals(0, scramble.length);
  }

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

  @SmallTest
  public void testOrientedAndCombined() {
    CubeState cubeState = new CubeState();
    // R2 U F2 D' B2 F2 U2 D L2 D
    cubeState.cornerPermutations = new byte[] { 0, 1, 4, 2, 3, 6, 7, 5 };
    cubeState.edgePermutations = new byte[] { 1, 2, 3, 0, 11, 4, 5, 7, 6, 9, 8, 10 };
    cubeState.cornerOrientations = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };
    cubeState.edgeOrientations = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    ThreeSolver solver = new ThreeSolver();
    String[] scramble = solver.getSolution(cubeState);
    Log.i("[NanoTimer]", "Scramble: " + Arrays.toString(scramble));
    Assert.assertTrue(scramble.length > 0);
    // Found: [D', L2, U2, D', F2, B2, D, F2, U', R2]
  }

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
    //  found: [U, D', L, F, R', B', R2, F2, D2, R', F', ., D, F2, U', L2, D', R2, L2, F2, D2, F2, D2]
  }

  @SmallTest
  public void testInvertedScramble() {
    CubeState cubeState = new CubeState();
    //  F L2 R' D2 R2 D' U' R D' B2 U2 F' U2 L2 D L R' U' F2 L R D2 B R' U2
    cubeState.cornerPermutations = new byte[] { 3, 0, 4, 1, 6, 2, 5, 7 };
    cubeState.edgePermutations = new byte[] { 1, 9, 8, 0, 11, 7, 6, 10, 4, 5, 3, 2 };
    cubeState.cornerOrientations = new byte[] { 2, 2, 2, 1, 0, 1, 1, 0 };
    cubeState.edgeOrientations = new byte[] { 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1 };

    ThreeSolver solver = new ThreeSolver();
    String[] scramble = Utils.invertMoves(solver.getSolution(cubeState));
    Log.i("[NanoTimer]", "Inverted scramble: " + Arrays.toString(scramble) + " (length: " + scramble.length + ")");
    //  Original non-inverted solution: [U, D', L, F, R', B', R2, F2, D2, R', F', ., D, F2, U', L2, D', R2, L2, F2, D2, F2, D2]
    String[] expectedScramble = new String[] { "D2", "F2", "D2", "F2", "L2", "R2", "D", "L2", "U", "F2", "D'",
        "F", "R", "D2", "F2", "R2", "B", "R", "F'", "L'", "D", "U'" };
    Assert.assertEquals(expectedScramble.length, scramble.length);
    for (int i = 0; i < expectedScramble.length; i++) {
      Assert.assertEquals(expectedScramble[i], scramble[i]);
    }
  }

  @SmallTest
  public void testRealScramble2() {
    CubeState cubeState = new CubeState();
    //  L2 R D L D2 B' U2 R F' D' U' L' R' D2 F L D2 B F' L' F2 L' R D' L2
    cubeState.cornerPermutations = new byte[] { 2, 4, 6, 1, 0, 3, 5, 7 };
    cubeState.edgePermutations = new byte[] { 8, 3, 9, 11, 10, 7, 5, 0, 2, 1, 4, 6 };
    cubeState.cornerOrientations = new byte[] { 1, 0, 0, 1, 1, 1, 1, 1 };
    cubeState.edgeOrientations = new byte[] { 1, 0, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0 };

    ThreeSolver solver = new ThreeSolver();
    String[] scramble = solver.getSolution(cubeState);
    Log.i("[NanoTimer]", "Scramble: " + Arrays.toString(scramble) + " (length: " + scramble.length + ")");
    //  found: [U, R2, B2, R2, F', B', D2, L, U', R2, F, D', F2, B2, U', L2, U, B2, U', L2, D2, F2, R2]
  }

}
