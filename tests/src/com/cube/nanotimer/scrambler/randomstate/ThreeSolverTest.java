package com.cube.nanotimer.scrambler.randomstate;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import com.cube.nanotimer.scrambler.randomstate.ThreeSolver.CubeState;
import com.cube.nanotimer.scrambler.randomstate.ThreeSolver.Move;
import junit.framework.Assert;

import java.util.Arrays;

public class ThreeSolverTest extends AndroidTestCase {

  @SmallTest
  public void testCubeStateMoves() {
    CubeState state = new CubeState();
    state.edgePermutations = IndexConvertor.unpackPermutation(0, 12);
    state.cornerPermutations = IndexConvertor.unpackPermutation(0, 8);
    state.edgeOrientations = IndexConvertor.unpackOrientation(0, 2, 12);
    state.cornerOrientations = IndexConvertor.unpackOrientation(0, 3, 8);

    applyMove(state, Move.F2);
    applyMove(state, Move.U);
    applyMove(state, Move.R);
    applyMove(state, Move.D);
    applyMove(state, Move.L2);
    applyMove(state, Move.B);
    Assert.assertTrue(Arrays.equals(new byte[] { 4, 1, 2, 8, 5, 7, 3, 6 }, state.cornerPermutations));
    Assert.assertTrue(Arrays.equals(new byte[] { 10, 2, 8, 3, 6, 4, 7, 11, 12, 5, 1, 9 }, state.edgePermutations));
    Assert.assertTrue(Arrays.equals(new byte[] { 2, 2, 2, 0, 2, 0, 0, 1 }, state.cornerOrientations));
    Assert.assertTrue(Arrays.equals(new byte[] { 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0 }, state.edgeOrientations));
  }

  @SmallTest
  public void testCornerOrientPruning() {
    byte[][] pruningCornerOrientation = ThreeSolver.pruningCornerOrientation;
    for (int i = 0; i < pruningCornerOrientation.length; i++) {
      for (int j = 0; j < pruningCornerOrientation[i].length; j++) {
        Assert.assertTrue(pruningCornerOrientation[i][j] >= 0);
      }
    }
    Assert.assertEquals(0, pruningCornerOrientation[0][0]);
    // R
    Assert.assertEquals(1, pruningCornerOrientation
        [IndexConvertor.packOrientation(new byte[] { 0, 0, 2, 1, 0, 0, 1, 2 }, 8)]
        [IndexConvertor.packCombination(new boolean[] { false, false, true, true, false, true, false, false, false, true, false, false }, 4)]);
    // F
    Assert.assertEquals(1, pruningCornerOrientation
        [IndexConvertor.packOrientation(new byte[] { 0, 2, 1, 0, 0, 1, 2, 0 }, 8)]
        [IndexConvertor.packCombination(new boolean[] { false, true, true, false, true, false, false, false, true, false, false, false }, 4)]);
  }

  private void applyMove(CubeState state, Move move) {
    state.edgePermutations = ThreeSolver.getPermResult(state.edgePermutations, move.edgPerm);
    state.cornerPermutations = ThreeSolver.getPermResult(state.cornerPermutations, move.corPerm);
//    state.edgeOrientations = ThreeSolver.getPermResult(state.edgeOrientations, move.edgPerm);
//    state.cornerOrientations = ThreeSolver.getPermResult(state.cornerOrientations, move.corPerm);
    // TODO : F and B moves also change the orientation... see how to handle that
    state.edgeOrientations = ThreeSolver.getOrientResult(state.edgeOrientations, move.edgOrient, 2);
    state.cornerOrientations = ThreeSolver.getOrientResult(state.cornerOrientations, move.corOrient, 3);
  }

}
