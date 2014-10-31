package com.cube.nanotimer.scrambler.randomstate;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import com.cube.nanotimer.scrambler.randomstate.TwoSolver.CubeState;
import junit.framework.Assert;

import java.util.Arrays;

public class TwoSolverTest extends AndroidTestCase {

  @SmallTest
  public void testVeryEasy() {
    CubeState cubeState = new CubeState();
    //  F
    cubeState.permutations = new byte[] { 0, 4, 1, 3, 5, 2, 6 };
    cubeState.orientations = new byte[] { 0, 2, 1, 0, 1, 2, 0 };

    TwoSolver solver = new TwoSolver();
    String[] scramble = solver.getSolution(cubeState);
    Log.i("[NanoTimer]", "Scramble: " + Arrays.toString(scramble));
    Assert.assertTrue(scramble.length > 0);
  }

  @SmallTest
  public void testEasy() {
    CubeState cubeState = new CubeState();
    // F U2
    cubeState.permutations = new byte[] { 1, 3, 0, 4, 5, 2, 6 };
    cubeState.orientations = new byte[] { 1, 0, 0, 2, 1, 2, 0 };

    TwoSolver solver = new TwoSolver();
    String[] scramble = solver.getSolution(cubeState);
    Log.i("[NanoTimer]", "Scramble: " + Arrays.toString(scramble));
    Assert.assertTrue(scramble.length > 0);
    // Found:
  }

  @SmallTest
  public void testNormal() {
    CubeState cubeState = new CubeState();
    // U F2 R U2 R' F R F' U' R2
    cubeState.permutations = new byte[] { 4, 2, 0, 1, 3, 6, 5 };
    cubeState.orientations = new byte[] { 0, 2, 2, 1, 0, 2, 2 };

    TwoSolver solver = new TwoSolver();
    String[] scramble = solver.getSolution(cubeState);
    Log.i("[NanoTimer]", "Scramble: " + Arrays.toString(scramble));
    Assert.assertTrue(scramble.length > 0);
    // Found:
  }

  @SmallTest
  public void testCubeStateMoves() {
    CubeState state = new CubeState();
    byte[] state7 = new byte[7];
    IndexConvertor.unpackPermutation(0, state7);
    state.permutations = state7;
    state7 = new byte[7];
    IndexConvertor.unpackOrientation(0, state7, (byte) 3);
    state.orientations = state7;

    applyMove(state, TwoSolver.Move.F); // F2
    applyMove(state, TwoSolver.Move.F);
    applyMove(state, TwoSolver.Move.U); // U
    applyMove(state, TwoSolver.Move.R); // R
    applyMove(state, TwoSolver.Move.F); // F
    applyMove(state, TwoSolver.Move.R); // R2
    applyMove(state, TwoSolver.Move.R);
    applyMove(state, TwoSolver.Move.U); // U
    Assert.assertTrue(Arrays.equals(new byte[] { 2, 0, 1, 5, 6, 3, 4 }, state.permutations));
    Assert.assertTrue(Arrays.equals(new byte[] { 2, 2, 1, 0, 2, 1, 1 }, state.orientations));
  }

  @SmallTest
  public void testTransit() {
    TwoSolver.genTables();

    // Permutation
    short[][] transitPerm = TwoSolver.transitPerm;
    for (int i = 0; i < transitPerm.length; i++) {
      for (int j = 0; j < transitPerm[i].length; j++) {
        short val = transitPerm[i][j];
        Assert.assertTrue(val >= 0 && val < TwoSolver.N_PERM);
      }
    }

    Assert.assertEquals(IndexConvertor.packPermutation(new byte[] { 1, 2, 3, 0, 4, 5, 6 }), transitPerm[0][0]); // U
    Assert.assertEquals(IndexConvertor.packPermutation(new byte[] { 0, 1, 5, 2, 4, 6, 3 }), transitPerm[0][1]); // R
    Assert.assertEquals(IndexConvertor.packPermutation(new byte[] { 0, 4, 1, 3, 5, 2, 6 }), transitPerm[0][2]); // F
    Assert.assertEquals(0, transitPerm[IndexConvertor.packPermutation(new byte[] { 0, 2, 5, 3, 1, 4, 6 })][2]); // F', F
    Assert.assertEquals(IndexConvertor.packPermutation(new byte[] { 6, 5, 4, 1, 2, 3, 0 }),
        transitPerm[IndexConvertor.packPermutation(new byte[] { 1, 6, 5, 4, 2, 3, 0 })][0]); // after R U F2 R, U

    // Orientation
    short[][] transitOrient = TwoSolver.transitOrient;
    for (int i = 0; i < transitOrient.length; i++) {
      for (int j = 0; j < transitOrient[i].length; j++) {
        short val = transitOrient[i][j];
        Assert.assertTrue(val >= 0 && val < TwoSolver.N_ORIENT);
      }
    }
    Assert.assertEquals(0, transitOrient[0][0]);
    Assert.assertEquals(IndexConvertor.packOrientation(new byte[] { 0, 0, 2, 1, 0, 1, 2 }, 3), transitOrient[0][1]); // R
    Assert.assertEquals(IndexConvertor.packOrientation(new byte[] { 0, 2, 1, 0, 1, 2, 0 }, 3), transitOrient[0][2]); // F
    Assert.assertEquals(0, transitOrient[IndexConvertor.packOrientation(new byte[] { 0, 2, 1, 0, 1, 2, 0 }, 3)][2]); // F', F
    Assert.assertEquals(IndexConvertor.packOrientation(new byte[] { 1, 1, 1, 0, 1, 0, 2 }, 3),
        transitOrient[IndexConvertor.packOrientation(new byte[] { 0, 1, 1, 1, 1, 0, 2 }, 3)][0]); // after R U F2 R, U

  }

  @SmallTest
  public void testPruning() {
    TwoSolver.genTables();

    // Permutation
    byte[] pruningPerm = TwoSolver.pruningPerm;
    for (int i = 0; i < pruningPerm.length; i++) {
      Assert.assertTrue(pruningPerm[i] >= 0);
    }
    Assert.assertEquals(0, pruningPerm[0]);
    Assert.assertEquals(1, pruningPerm[IndexConvertor.packPermutation(new byte[] { 0, 1, 5, 2, 4, 6, 3 })]); // R
    Assert.assertEquals(1, pruningPerm[IndexConvertor.packPermutation(new byte[] { 0, 4, 1, 3, 5, 2, 6 })]); // F
    Assert.assertEquals(2, pruningPerm[IndexConvertor.packPermutation(new byte[] { 0, 4, 2, 1, 5, 6, 3 })]); // F R
    Assert.assertEquals(3, pruningPerm[IndexConvertor.packPermutation(new byte[] { 4, 2, 1, 0, 5, 6, 3 })]); // F R U
    Assert.assertEquals(3, pruningPerm[IndexConvertor.packPermutation(new byte[] { 2, 1, 0, 4, 5, 6, 3 })]); // F R U2

    // Orientation
    byte[] pruningOrient = TwoSolver.pruningOrient;
    for (int i = 0; i < pruningOrient.length; i++) {
      Assert.assertTrue(pruningOrient[i] >= 0);
    }
    Assert.assertEquals(0, pruningOrient[0]);
    Assert.assertEquals(1, pruningOrient[IndexConvertor.packOrientation(new byte[] { 0, 0, 2, 1, 0, 1, 2 }, 3)]); // R
    Assert.assertEquals(1, pruningOrient[IndexConvertor.packOrientation(new byte[] { 0, 2, 1, 0, 1, 2, 0 }, 3)]); // F
    Assert.assertEquals(2, pruningOrient[IndexConvertor.packOrientation(new byte[] { 0, 2, 1, 2, 1, 1, 2 }, 3)]); // F R
    Assert.assertEquals(3, pruningOrient[IndexConvertor.packOrientation(new byte[] { 2, 1, 2, 0, 1, 1, 2 }, 3)]); // F R U
  }

  @SmallTest
  public void testTableSizes() {
    // TODO
  }

  private void applyMove(CubeState state, TwoSolver.Move move) {
    state.permutations = StateTables.getPermResult(state.permutations, move.corPerm);
    state.orientations = StateTables.getOrientResult(state.orientations, move.corPerm, move.corOrient, 3);
  }

}
