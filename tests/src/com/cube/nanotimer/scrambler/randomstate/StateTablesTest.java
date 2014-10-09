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
import java.util.Random;

public class StateTablesTest extends AndroidTestCase {

  @SmallTest
  public void testCubeStateMoves() {
    CubeState state = new CubeState();
    state.edgePermutations = IndexConvertor.unpackPermutation(0, 12);
    state.cornerPermutations = IndexConvertor.unpackPermutation(0, 8);
    state.edgeOrientations = IndexConvertor.unpackOrientation(0, 2, 12);
    state.cornerOrientations = IndexConvertor.unpackOrientation(0, 3, 8);

    applyMove(state, Move.F); // F2
    applyMove(state, Move.F);
    applyMove(state, Move.U);
    applyMove(state, Move.R);
    applyMove(state, Move.D);
    applyMove(state, Move.L); // L2
    applyMove(state, Move.L);
    applyMove(state, Move.B);
    Assert.assertTrue(Arrays.equals(new byte[] { 3, 0, 1, 7, 4, 6, 2, 5 }, state.cornerPermutations));
    Assert.assertTrue(Arrays.equals(new byte[] { 9, 1, 7, 2, 5, 3, 6, 10, 11, 4, 0, 8 }, state.edgePermutations));
    Assert.assertTrue(Arrays.equals(new byte[] { 2, 2, 2, 0, 2, 0, 0, 1 }, state.cornerOrientations));
    Assert.assertTrue(Arrays.equals(new byte[] { 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0 }, state.edgeOrientations));

    applyMove(state, Move.R); // R'
    applyMove(state, Move.R);
    applyMove(state, Move.R);
    applyMove(state, Move.U); // U'
    applyMove(state, Move.U);
    applyMove(state, Move.U);
    applyMove(state, Move.L);
    applyMove(state, Move.D); // D'
    applyMove(state, Move.D);
    applyMove(state, Move.D);
    applyMove(state, Move.B); // B
    applyMove(state, Move.B);
    applyMove(state, Move.F);
    Assert.assertTrue(Arrays.equals(new byte[] { 6, 1, 5, 3, 7, 2, 0, 4 }, state.cornerPermutations));
    Assert.assertTrue(Arrays.equals(new byte[] { 10, 8, 4, 9, 6, 5, 2, 7, 3, 0, 1, 11 }, state.edgePermutations));
    Assert.assertTrue(Arrays.equals(new byte[] { 1, 2, 1, 1, 2, 0, 1, 1 }, state.cornerOrientations));
    Assert.assertTrue(Arrays.equals(new byte[] { 1, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1, 0 }, state.edgeOrientations));

    applyMove(state, Move.L); // L'
    applyMove(state, Move.L);
    applyMove(state, Move.L);
    applyMove(state, Move.U); // U2
    applyMove(state, Move.U);
    applyMove(state, Move.R); // R2
    applyMove(state, Move.R);
    applyMove(state, Move.B); // B'
    applyMove(state, Move.B);
    applyMove(state, Move.B);
    applyMove(state, Move.F); // F'
    applyMove(state, Move.F);
    applyMove(state, Move.F);
    applyMove(state, Move.D); // D2
    applyMove(state, Move.D);
    Assert.assertTrue(Arrays.equals(new byte[] { 6, 4, 2, 5, 7, 0, 1, 3 }, state.cornerPermutations));
    Assert.assertTrue(Arrays.equals(new byte[] { 3, 6, 1, 2, 8, 0, 7, 5, 10, 4, 11, 9 }, state.edgePermutations));
    Assert.assertTrue(Arrays.equals(new byte[] { 0, 0, 2, 0, 0, 2, 0, 2 }, state.cornerOrientations));
    Assert.assertTrue(Arrays.equals(new byte[] { 0, 1, 0, 1, 1, 1, 0, 0, 0, 0, 1, 1 }, state.edgeOrientations));
  }

  /*@SmallTest
  public void testTransit() {
    Move[] moves1 = new Move[] {
        Move.U,
        Move.D,
        Move.R,
        Move.L,
        Move.F,
        Move.B
    };

    short[][] transitCornerOrientation = new short[2187][6];
    for (int i = 0; i < transitCornerOrientation.length; i++) {
      for (int j = 0; j < moves1.length; j++) {
        byte[] state = IndexConvertor.unpackOrientation(i, 3, 8);
        transitCornerOrientation[i][j] = (short) IndexConvertor.packOrientation(StateTables.getOrientResult(state, moves1[j].corPerm, moves1[j].corOrient, 3), 3);
      }
    }
  }*/

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
        [IndexConvertor.packRel8Permutation(new byte[] { 0, 6, 5, 3, 4, 2, 1, 7 })]
        [IndexConvertor.packPermutation(new byte[] { 3, 1, 2, 0 })]); // F2
    Assert.assertEquals(2, pruningCornerPermutation
        [IndexConvertor.packRel8Permutation(new byte[] { 1, 7, 6, 0, 4, 5, 3, 2 })]
        [IndexConvertor.packPermutation(new byte[] { 1, 0, 2, 3 })]); // R2 U
    Assert.assertEquals(3, pruningCornerPermutation
        [IndexConvertor.packRel8Permutation(new byte[] { 1, 4, 2, 0, 3, 6, 7, 5 })]
        [IndexConvertor.packPermutation(new byte[] { 0, 3, 1, 2 })]); // L2 D' B2
    Assert.assertEquals(4, pruningCornerPermutation
        [IndexConvertor.packRel8Permutation(new byte[] { 0, 1, 7, 6, 3, 2, 4, 5 })]
        [IndexConvertor.packPermutation(new byte[] { 1, 0, 3, 2 })]); // U2 R2 D2 L2

    byte[][] pruningUDEdgePermutation = StateTables.pruningUDEdgePermutation;
    for (int i = 0; i < pruningUDEdgePermutation.length; i++) {
      for (int j = 0; j < pruningUDEdgePermutation[i].length; j++) {
        Assert.assertTrue(pruningUDEdgePermutation[i][j] >= 0);
      }
    }
    Assert.assertEquals(0, pruningUDEdgePermutation[0][0]);
    Assert.assertEquals(1, pruningUDEdgePermutation
        [IndexConvertor.packRel8Permutation(new byte[] { 4, 1, 2, 3, 0, 5, 6, 7 })]
        [IndexConvertor.packPermutation(new byte[] { 3, 1, 2, 0 })]); // F1
    Assert.assertEquals(1, pruningUDEdgePermutation
        [IndexConvertor.packRel8Permutation(new byte[] { 5, 2, 3, 0, 4, 1, 6, 7 })]
        [IndexConvertor.packPermutation(new byte[] { 1, 0, 2, 3 })]); // R1 U
    Assert.assertEquals(3, pruningUDEdgePermutation
        [IndexConvertor.packRel8Permutation(new byte[] { 0, 1, 3, 7, 5, 6, 2, 4 })]
        [IndexConvertor.packPermutation(new byte[] { 0, 3, 1, 2 })]); // L1 D' B1
    Assert.assertEquals(3, pruningUDEdgePermutation
        [IndexConvertor.packRel8Permutation(new byte[] { 2, 5, 0, 3, 6, 7, 4, 1 })]
        [IndexConvertor.packPermutation(new byte[] { 1, 0, 3, 2 })]); // U1 R1 D1 L1
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

  @SmallTest
  public void testPermPerformance() {
    Random r = new Random();
    byte[] state;
    byte[] perms;
    long tsTotal = 0;
    for (int i = 0; i < 50000; i++) {
      int n = r.nextInt(40320);
      state = IndexConvertor.unpackPermutation(n, 8);
      perms = Move.values()[r.nextInt(18)].corPerm;
      long ts = System.currentTimeMillis();
      StateTables.getPermResult(state, perms);
      tsTotal += (System.currentTimeMillis() - ts);
    }
    Log.i("[NanoTimer]", "corner perm time: " + tsTotal);
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
