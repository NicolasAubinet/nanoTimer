package com.cube.nanotimer.scrambler.randomstate;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import com.cube.nanotimer.scrambler.randomstate.ThreeSolver.CubeState;
import junit.framework.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public class StateTablesTest extends AndroidTestCase {

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

    applyMove(state, Move.RP);
    applyMove(state, Move.UP);
    applyMove(state, Move.L);
    applyMove(state, Move.DP);
    applyMove(state, Move.B2);
    applyMove(state, Move.F);
    Assert.assertTrue(Arrays.equals(new byte[] { 7, 2, 6, 4, 8, 3, 1, 5 }, state.cornerPermutations));
    Assert.assertTrue(Arrays.equals(new byte[] { 11, 9, 5, 10, 7, 6, 3, 8, 4, 1, 2, 12 }, state.edgePermutations));
    Assert.assertTrue(Arrays.equals(new byte[] { 1, 2, 1, 1, 2, 0, 1, 1 }, state.cornerOrientations));
    Assert.assertTrue(Arrays.equals(new byte[] { 1, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1, 0 }, state.edgeOrientations));

    applyMove(state, Move.LP);
    applyMove(state, Move.U2);
    applyMove(state, Move.R2);
    applyMove(state, Move.BP);
    applyMove(state, Move.FP);
    applyMove(state, Move.D2);
    Assert.assertTrue(Arrays.equals(new byte[] { 7, 5, 3, 6, 8, 1, 2, 4 }, state.cornerPermutations));
    Assert.assertTrue(Arrays.equals(new byte[] { 4, 7, 2, 3, 9, 1, 8, 6, 11, 5, 12, 10 }, state.edgePermutations));
    Assert.assertTrue(Arrays.equals(new byte[] { 0, 0, 2, 0, 0, 2, 0, 2 }, state.cornerOrientations));
    Assert.assertTrue(Arrays.equals(new byte[] { 0, 1, 0, 1, 1, 1, 0, 0, 0, 0, 1, 1 }, state.edgeOrientations));
  }

  @SmallTest
  public void testPrunings() {
    StateTables.generateTables(ThreeSolver.moves1, ThreeSolver.moves2);

    // PHASE 1

    // Corner orientation
    byte[][] pruningCornerOrientation = StateTables.pruningCornerOrientation;
    for (int i = 0; i < pruningCornerOrientation.length; i++) {
      for (int j = 0; j < pruningCornerOrientation[i].length; j++) {
        Assert.assertTrue(pruningCornerOrientation[i][j] >= 0);
      }
    }
    Assert.assertEquals(0, pruningCornerOrientation[0][0]);
    Assert.assertEquals(0, pruningCornerOrientation
        [IndexConvertor.packOrientation(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, 3)]
        [IndexConvertor.packCombination(new boolean[] { true, true, true, true, false, false, false, false, false, false, false, false }, 4)]);
    Assert.assertEquals(1, pruningCornerOrientation
        [IndexConvertor.packOrientation(new byte[] { 0, 0, 2, 1, 0, 0, 1, 2 }, 3)]
        [IndexConvertor.packCombination(new boolean[] { false, false, true, true, false, true, false, false, false, true, false, false }, 4)]); // R
    Assert.assertEquals(1, pruningCornerOrientation
        [IndexConvertor.packOrientation(new byte[] { 0, 2, 1, 0, 0, 1, 2, 0 }, 3)]
        [IndexConvertor.packCombination(new boolean[] { false, true, true, false, true, false, false, false, true, false, false, false }, 4)]); // F
    Assert.assertEquals(0, pruningCornerOrientation
        [IndexConvertor.packOrientation(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, 3)]
        [IndexConvertor.packCombination(new boolean[] { true, true, true, true, false, false, false, false, false, false, false, false }, 4)]);
    Assert.assertEquals(2, pruningCornerOrientation
        [IndexConvertor.packOrientation(new byte[] { 1, 2, 1, 2, 2, 1, 2, 1 }, 3)]
        [IndexConvertor.packCombination(new boolean[] { false, false, false, false, true, false, true, false, true, false, true, false }, 4)]); // F B
    Assert.assertEquals(4, pruningCornerOrientation
        [IndexConvertor.packOrientation(new byte[] { 1, 1, 1, 1, 2, 1, 1, 1 }, 3)]
        [IndexConvertor.packCombination(new boolean[] { false, false, false, false, true, false, false, false, true, true, true, false }, 4)]); // F R B L
    Assert.assertEquals(4, pruningCornerOrientation
        [IndexConvertor.packOrientation(new byte[] { 2, 2, 1, 1, 2, 2, 1, 1 }, 3)]
        [IndexConvertor.packCombination(new boolean[] { false, false, false, false, false, true, true, false, true, false, false, true }, 4)]); // F U2 R' L'
    Assert.assertEquals(5, pruningCornerOrientation
        [IndexConvertor.packOrientation(new byte[] { 1, 1, 1, 1, 2, 1, 1, 1 }, 3)]
        [IndexConvertor.packCombination(new boolean[] { false, false, false, false, false, false, false, true, true, true, true, false }, 4)]); // F R B L U
    Assert.assertEquals(7, pruningCornerOrientation
        [IndexConvertor.packOrientation(new byte[] { 0, 1, 1, 1, 2, 0, 0, 1 }, 3)]
        [IndexConvertor.packCombination(new boolean[] { true, false, false, true, true, false, false, false, false, true, false, false }, 4)]);

    // Edge orientation
    byte[][] pruningEdgeOrientation = StateTables.pruningEdgeOrientation;
    for (int i = 0; i < pruningEdgeOrientation.length; i++) {
      for (int j = 0; j < pruningEdgeOrientation[i].length; j++) {
        Assert.assertTrue(pruningEdgeOrientation[i][j] >= 0);
      }
    }
    Assert.assertEquals(0, pruningEdgeOrientation[0][0]);
    Assert.assertEquals(0, pruningEdgeOrientation
        [IndexConvertor.packOrientation(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 2)]
        [IndexConvertor.packCombination(new boolean[] { true, true, true, true, false, false, false, false, false, false, false, false }, 4)]);
    Assert.assertEquals(1, pruningEdgeOrientation
        [IndexConvertor.packOrientation(new byte[] { 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0 }, 2)]
        [IndexConvertor.packCombination(new boolean[] { false, true, true, false, true, false, false, false, true, false, false, false }, 4)]); // F
    Assert.assertEquals(1, pruningEdgeOrientation
        [IndexConvertor.packOrientation(new byte[] { 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0 }, 2)]
        [IndexConvertor.packCombination(new boolean[] { true, false, false, true, false, false, true, false, false, false, true, false }, 4)]); // B
    Assert.assertEquals(4, pruningEdgeOrientation
        [IndexConvertor.packOrientation(new byte[] { 0, 1, 0, 0, 1, 1, 1, 1, 1, 0, 1, 1 }, 2)]
        [IndexConvertor.packCombination(new boolean[] { false, false, false, false, true, false, false, false, true, true, true, false }, 4)]); // F R B L
    Assert.assertEquals(4, pruningEdgeOrientation
        [IndexConvertor.packOrientation(new byte[] { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0 }, 2)]
        [IndexConvertor.packCombination(new boolean[] { false, false, false, false, false, true, true, false, true, false, false, true }, 4)]); // F U2 R' L'
    Assert.assertEquals(7, pruningEdgeOrientation
        [IndexConvertor.packOrientation(new byte[] { 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 1 }, 2)]
        [IndexConvertor.packCombination(new boolean[] { true, false, false, true, true, false, false, false, false, true, false, false }, 4)]);

    // PHASE 2

    byte[][] pruningCornerPermutation = StateTables.pruningCornerPermutation;
    for (int i = 0; i < pruningCornerPermutation.length; i++) {
      for (int j = 0; j < pruningCornerPermutation[i].length; j++) {
        Assert.assertTrue(pruningCornerPermutation[i][j] >= 0);
      }
    }
    Assert.assertEquals(0, pruningCornerPermutation[0][0]);
    Assert.assertEquals(1, pruningCornerPermutation
        [IndexConvertor.packPermutation(new byte[] { 1, 7, 6, 4, 5, 3, 2, 8 })]
        [IndexConvertor.packPermutation(new byte[] { 4, 2, 3, 1 })]); // F2
    Assert.assertEquals(2, pruningCornerPermutation
        [IndexConvertor.packPermutation(new byte[] { 2, 8, 7, 1, 5, 6, 4, 3 })]
        [IndexConvertor.packPermutation(new byte[] { 2, 1, 3, 4 })]); // R2 U
    Assert.assertEquals(3, pruningCornerPermutation
        [IndexConvertor.packPermutation(new byte[] { 2, 5, 3, 1, 4, 7, 8, 6 })]
        [IndexConvertor.packPermutation(new byte[] { 1, 4, 2, 3 })]); // L2 D' B2
    Assert.assertEquals(4, pruningCornerPermutation
        [IndexConvertor.packPermutation(new byte[] { 1, 2, 8, 7, 4, 3, 5, 6 })]
        [IndexConvertor.packPermutation(new byte[] { 2, 1, 4, 3 })]); // U2 R2 D2 L2

    byte[][] pruningUDEdgePermutation = StateTables.pruningUDEdgePermutation;
    for (int i = 0; i < pruningUDEdgePermutation.length; i++) {
      for (int j = 0; j < pruningUDEdgePermutation[i].length; j++) {
        Assert.assertTrue(pruningUDEdgePermutation[i][j] >= 0);
      }
    }
    Assert.assertEquals(0, pruningUDEdgePermutation[0][0]);
    Assert.assertEquals(1, pruningUDEdgePermutation
        [IndexConvertor.packPermutation(new byte[] { 5, 2, 3, 4, 1, 6, 7, 8 })]
        [IndexConvertor.packPermutation(new byte[] { 4, 2, 3, 1 })]); // F2
    Assert.assertEquals(2, pruningUDEdgePermutation
        [IndexConvertor.packPermutation(new byte[] { 6, 3, 4, 1, 5, 2, 7, 8 })]
        [IndexConvertor.packPermutation(new byte[] { 2, 1, 3, 4 })]); // R2 U
    Assert.assertEquals(3, pruningUDEdgePermutation
        [IndexConvertor.packPermutation(new byte[] { 1, 2, 4, 8, 6, 7, 3, 5 })]
        [IndexConvertor.packPermutation(new byte[] { 1, 4, 2, 3 })]); // L2 D' B2
    Assert.assertEquals(4, pruningUDEdgePermutation
        [IndexConvertor.packPermutation(new byte[] { 3, 6, 1, 4, 7, 8, 5, 2 })]
        [IndexConvertor.packPermutation(new byte[] { 2, 1, 4, 3 })]); // U2 R2 D2 L2
  }

  /*public void testCreateFile() throws IOException {
    StateTables.generateTables();

    FileOutputStream fos = new FileOutputStream(new File("/sdcard/docs/randomstatetables"));
    ObjectOutputStream oos = new ObjectOutputStream(fos);

    oos.writeObject(StateTables.transitCornerPermutation);
    oos.writeObject(StateTables.transitCornerOrientation);
    oos.writeObject(StateTables.transitEEdgeCombination);
    oos.writeObject(StateTables.transitEEdgePermutation);
    oos.writeObject(StateTables.transitUDEdgePermutation);
    oos.writeObject(StateTables.transitEdgeOrientation);

    oos.writeObject(StateTables.pruningCornerOrientation);
    oos.writeObject(StateTables.pruningEdgeOrientation);
    oos.writeObject(StateTables.pruningCornerPermutation);
    oos.writeObject(StateTables.pruningUDEdgePermutation);

    oos.flush();
    oos.close();
    fos.close();
  }*/

  @SmallTest
  public void testTableSizes() {
    StateTables.generateTables(ThreeSolver.moves1, ThreeSolver.moves2);

    Log.i("[NanoTimer]", "trCorPerm size: " + getSize(StateTables.transitCornerPermutation));
    Log.i("[NanoTimer]", "trCorOri size: " + getSize(StateTables.transitCornerOrientation));
    Log.i("[NanoTimer]", "trEEComb size: " + getSize(StateTables.transitEEdgeCombination));
    Log.i("[NanoTimer]", "trEEPerm size: " + getSize(StateTables.transitEEdgePermutation));
    Log.i("[NanoTimer]", "trUDEPerm size: " + getSize(StateTables.transitUDEdgePermutation));
    Log.i("[NanoTimer]", "trEOri size: " + getSize(StateTables.transitEdgeOrientation));

    Log.i("[NanoTimer]", "prCorOri size: " + getSize(StateTables.pruningCornerOrientation));
    Log.i("[NanoTimer]", "prEOri size: " + getSize(StateTables.pruningEdgeOrientation));
    Log.i("[NanoTimer]", "prCorPerm size: " + getSize(StateTables.pruningCornerPermutation));
    Log.i("[NanoTimer]", "prUDEPerm size: " + getSize(StateTables.pruningUDEdgePermutation));
  }

  private int getSize(Object obj) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(obj);
      oos.close();
      return baos.size();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail();
    }
    return -1;
  }

  private void applyMove(CubeState state, Move move) {
    state.edgePermutations = StateTables.getPermResult(state.edgePermutations, move.edgPerm);
    state.cornerPermutations = StateTables.getPermResult(state.cornerPermutations, move.corPerm);
    state.edgeOrientations = StateTables.getOrientResult(state.edgeOrientations, move.edgPerm, move.edgOrient, 2);
    state.cornerOrientations = StateTables.getOrientResult(state.cornerOrientations, move.corPerm, move.corOrient, 3);
  }

}
