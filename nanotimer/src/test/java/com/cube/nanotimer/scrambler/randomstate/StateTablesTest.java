package com.cube.nanotimer.scrambler.randomstate;

import com.cube.nanotimer.vo.ThreeCubeState;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Random;

@RunWith(JUnit4.class)
public class StateTablesTest {

  @Test
  public void testCubeStateMoves() {
    ThreeCubeState state = new ThreeCubeState();
    byte[] state12 = new byte[12];
    IndexConvertor.unpackPermutation(0, state12);
    state.edgePermutations = state12;
    byte[] state8 = new byte[8];
    IndexConvertor.unpackPermutation(0, state8);
    state.cornerPermutations = state8;
    state12 = new byte[12];
    IndexConvertor.unpackOrientation(0, state12, (byte) 2);
    state.edgeOrientations = state12;
    state8 = new byte[8];
    IndexConvertor.unpackOrientation(0, state8, (byte) 3);
    state.cornerOrientations = state8;

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

  @Test
  public void testTransit() {
    StateTables.generateTables(ThreeSolver.moves1, ThreeSolver.moves2);

    // Edge combinations

    short[][] edgeCombinations = StateTables.transitEEdgeCombination;
    for (int i = 0; i < edgeCombinations.length; i++) {
      for (int j = 0; j < edgeCombinations[i].length; j++) {
        short val = edgeCombinations[i][j];
        Assert.assertTrue(val >= 0 && val < 495);
      }
    }
    Assert.assertEquals(0, edgeCombinations[0][0]); // U
    Assert.assertEquals(0, edgeCombinations[0][1]); // D
    Assert.assertEquals(0, edgeCombinations[365][3]);

    // Test combinations after F L2 R' D2
    Assert.assertEquals(IndexConvertor.packCombination(new boolean[] { true, true, true, true, false, false, false, false, false, false, false, false }, 4), edgeCombinations[0][0]);

    int ind = IndexConvertor.packCombination(new boolean[] { false, false, false, true, true, true, false, false, false, false, true, false }, 4);
    byte[] edgePermutations = new byte[] { 5, 9, 8, 2, 3, 1, 6, 11, 10, 7, 0, 4 };
    boolean[] combinations = new boolean[edgePermutations.length];
    for (int i = 0; i < edgePermutations.length; i++) {
      combinations[i] = (edgePermutations[i] < 4);
    }
    Assert.assertEquals(ind, IndexConvertor.packCombination(combinations, 4));

    // From ThreeSolverTest testEasy() state
    // 11 4  6 14 11 5 9
    // L' D2 R F' L' D' L
    ind = edgeCombinations[ind][3]; // L (same than L')
    Assert.assertEquals(IndexConvertor.packCombination(new boolean[] { false, false, false, false, true, true, false, false, false, false, true, true }, 4), ind);

    ind = edgeCombinations[ind][1]; // D
    Assert.assertEquals(IndexConvertor.packCombination(new boolean[] { false, false, false, false, true, true, false, false, true, false, false, true }, 4), ind);
    ind = edgeCombinations[ind][1]; // D (to make D2)
    Assert.assertEquals(IndexConvertor.packCombination(new boolean[] { false, false, false, false, true, true, false, false, true, true, false, false }, 4), ind);

    ind = edgeCombinations[ind][2]; // R
    Assert.assertEquals(IndexConvertor.packCombination(new boolean[] { true, true, false, false, true, false, false, false, true, false, false, false }, 4), ind);

    // Corner permutations
    int[][] cornerPermutations = StateTables.transitCornerPermutation;
    ind = cornerPermutations[0][0]; // U
    int state1 = ind;
    ind = cornerPermutations[ind][2]; // R2
    ind = cornerPermutations[ind][2]; // R2
    Assert.assertEquals(state1, ind);
    ind = cornerPermutations[ind][0]; // U
    ind = cornerPermutations[ind][0]; // U
    ind = cornerPermutations[ind][0]; // U
    Assert.assertEquals(0, ind); // back to solved state

    ind = cornerPermutations[0][2]; // R2
    ind = cornerPermutations[ind][0]; // U
    ind = cornerPermutations[ind][0]; // U
    ind = cornerPermutations[ind][0]; // U
    ind = cornerPermutations[ind][0]; // U
    ind = cornerPermutations[ind][2]; // R2
    Assert.assertEquals(0, ind); // back to solved state

    // R U R' U'
    ind = 0;
    for (int i = 0; i < 6; i++) {
      ind = cornerPermutations[ind][2]; // R2
      ind = cornerPermutations[ind][0]; // U
      ind = cornerPermutations[ind][0]; // U
    }
    Assert.assertEquals(0, ind); // back to solved state

    ind = cornerPermutations[0][2]; // R2
    ind = cornerPermutations[ind][1]; // D
    ind = cornerPermutations[ind][1]; // D
    ind = cornerPermutations[ind][3]; // L2
    ind = cornerPermutations[ind][0]; // U
    ind = cornerPermutations[ind][0]; // U
    ind = cornerPermutations[ind][2]; // R2
    ind = cornerPermutations[ind][4]; // F2
    ind = cornerPermutations[ind][4]; // F2
    ind = cornerPermutations[ind][2]; // R2
    ind = cornerPermutations[ind][5]; // B2
    ind = cornerPermutations[ind][5]; // B2
    ind = cornerPermutations[ind][4]; // F2
    ind = cornerPermutations[ind][4]; // F2
    ind = cornerPermutations[ind][0]; // U
    ind = cornerPermutations[ind][0]; // U
    ind = cornerPermutations[ind][3]; // L2
    ind = cornerPermutations[ind][1]; // D
    ind = cornerPermutations[ind][1]; // D
    ind = cornerPermutations[ind][2]; // R2
    Assert.assertEquals(0, ind); // back to solved state
  }

  @Test
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
//    Assert.assertEquals(12, pruningEdgeOrientation // 8?
//        [IndexConvertor.packOrientation(new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, 2)]
//        [IndexConvertor.packCombination(new boolean[] { true, true, true, true, false, false, false, false, false, false, false, false }, 4)]);

    // PHASE 2

    byte[][] pruningCornerPermutation = StateTables.pruningCornerPermutation;
    for (int i = 0; i < pruningCornerPermutation.length; i++) {
      for (int j = 0; j < pruningCornerPermutation[i].length; j++) {
        Assert.assertTrue(pruningCornerPermutation[i][j] >= 0);
      }
    }
    Assert.assertEquals(0, pruningCornerPermutation[0][0]);
    Assert.assertEquals(1, pruningCornerPermutation
        [IndexConvertor.packPermutation(new byte[] { 0, 6, 5, 3, 4, 2, 1, 7 })]
        [IndexConvertor.packPermutation(new byte[] { 3, 1, 2, 0 })]); // F2
    Assert.assertEquals(1, pruningCornerPermutation
        [IndexConvertor.packPermutation(new byte[] { 1, 2, 3, 0, 4, 5, 6, 7 })]
        [IndexConvertor.packPermutation(new byte[] { 0, 1, 2, 3 })]); // U
    Assert.assertEquals(1, pruningCornerPermutation
        [IndexConvertor.packPermutation(new byte[] { 0, 1, 2, 3, 5, 6, 7, 4 })]
        [IndexConvertor.packPermutation(new byte[] { 0, 1, 2, 3 })]); // D'
    Assert.assertEquals(1, pruningCornerPermutation
        [IndexConvertor.packPermutation(new byte[] { 2, 3, 0, 1, 4, 5, 6, 7 })]
        [IndexConvertor.packPermutation(new byte[] { 0, 1, 2, 3 })]); // U2
    Assert.assertEquals(2, pruningCornerPermutation
        [IndexConvertor.packPermutation(new byte[] { 2, 3, 7, 6, 4, 5, 1, 0 })]
        [IndexConvertor.packPermutation(new byte[] { 1, 0, 2, 3 })]); // U2 R2
    Assert.assertEquals(2, pruningCornerPermutation
        [IndexConvertor.packPermutation(new byte[] { 1, 7, 6, 0, 4, 5, 3, 2 })]
        [IndexConvertor.packPermutation(new byte[] { 1, 0, 2, 3 })]); // R2 U
    Assert.assertEquals(3, pruningCornerPermutation
        [IndexConvertor.packPermutation(new byte[] { 1, 4, 2, 0, 3, 6, 7, 5 })]
        [IndexConvertor.packPermutation(new byte[] { 0, 3, 1, 2 })]); // L2 D' B2
    Assert.assertEquals(3, pruningCornerPermutation
        [IndexConvertor.packPermutation(new byte[] { 2, 3, 7, 6, 1, 0, 4, 5 })]
        [IndexConvertor.packPermutation(new byte[] { 1, 0, 2, 3 })]); // U2 R2 D2
    Assert.assertEquals(4, pruningCornerPermutation
        [IndexConvertor.packPermutation(new byte[] { 0, 1, 7, 6, 3, 2, 4, 5 })]
        [IndexConvertor.packPermutation(new byte[] { 1, 0, 3, 2 })]); // U2 R2 D2 L2

    byte[][] pruningUDEdgePermutation = StateTables.pruningUDEdgePermutation;
    for (int i = 0; i < pruningUDEdgePermutation.length; i++) {
      for (int j = 0; j < pruningUDEdgePermutation[i].length; j++) {
        Assert.assertTrue(pruningUDEdgePermutation[i][j] >= 0);
      }
    }
    Assert.assertEquals(0, pruningUDEdgePermutation[0][0]);
    Assert.assertEquals(1, pruningUDEdgePermutation
        [IndexConvertor.packPermutation(new byte[] { 4, 1, 2, 3, 0, 5, 6, 7 })]
        [IndexConvertor.packPermutation(new byte[] { 3, 1, 2, 0 })]); // F2
    Assert.assertEquals(1, pruningUDEdgePermutation
        [IndexConvertor.packPermutation(new byte[] { 2, 3, 0, 1, 4, 5, 6, 7 })]
        [IndexConvertor.packPermutation(new byte[] { 0, 1, 2, 3 })]); // U2
    Assert.assertEquals(2, pruningUDEdgePermutation
        [IndexConvertor.packPermutation(new byte[] { 5, 2, 3, 0, 4, 1, 6, 7 })]
        [IndexConvertor.packPermutation(new byte[] { 1, 0, 2, 3 })]); // R2 U
    Assert.assertEquals(2, pruningUDEdgePermutation
        [IndexConvertor.packPermutation(new byte[] { 2, 5, 0, 1, 4, 3, 6, 7 })]
        [IndexConvertor.packPermutation(new byte[] { 1, 0, 2, 3 })]); // U2 R2
    Assert.assertEquals(2, pruningUDEdgePermutation
        [IndexConvertor.packPermutation(new byte[] { 2, 3, 0, 7, 4, 5, 6, 1 })]
        [IndexConvertor.packPermutation(new byte[] { 0, 1, 3, 2 })]); // U2 L2
    Assert.assertEquals(3, pruningUDEdgePermutation
        [IndexConvertor.packPermutation(new byte[] { 0, 1, 3, 7, 5, 6, 2, 4 })]
        [IndexConvertor.packPermutation(new byte[] { 0, 3, 1, 2 })]); // L2 D' B2
    Assert.assertEquals(4, pruningUDEdgePermutation
        [IndexConvertor.packPermutation(new byte[] { 2, 5, 0, 3, 6, 7, 4, 1 })]
        [IndexConvertor.packPermutation(new byte[] { 1, 0, 3, 2 })]); // U2 R2 D2 L2
  }

  /*public void testSaveTables() throws IOException {
    StateTables.generateTables(ThreeSolver.moves1, ThreeSolver.moves2);

    FileOutputStream fos = getContext().openFileOutput("randomstatetables", Context.MODE_PRIVATE);
    ObjectOutputStream oos = new ObjectOutputStream(fos);

    long ts = System.currentTimeMillis();
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
    System.out.println("Time to write tables: " + (System.currentTimeMillis() - ts));
  }

  public void testReadTables() throws IOException, ClassNotFoundException {
    FileInputStream fis = getContext().openFileInput("randomstatetables");
    ObjectInputStream ois = new ObjectInputStream(fis);

    long ts = System.currentTimeMillis();
    int[][] iTrTable = (int[][]) ois.readObject();
    short[][] trTable = (short[][]) ois.readObject();
    trTable = (short[][]) ois.readObject();
    trTable = (short[][]) ois.readObject();
    iTrTable = (int[][]) ois.readObject();
    trTable = (short[][]) ois.readObject();

    byte[][] prTable = (byte[][]) ois.readObject();
    prTable = (byte[][]) ois.readObject();
    prTable = (byte[][]) ois.readObject();
    prTable = (byte[][]) ois.readObject();

    ois.close();
    fis.close();
    System.out.println("Time to read tables: " + (System.currentTimeMillis() - ts));
  }*/

  @Test
  public void testTableSizes() {
    StateTables.generateTables(ThreeSolver.moves1, ThreeSolver.moves2);

    System.out.println("trCorPerm size: " + getSize(StateTables.transitCornerPermutation));
    System.out.println("trCorOri size: " + getSize(StateTables.transitCornerOrientation));
    System.out.println("trEEComb size: " + getSize(StateTables.transitEEdgeCombination));
    System.out.println("trEEPerm size: " + getSize(StateTables.transitEEdgePermutation));
    System.out.println("trUDEPerm size: " + getSize(StateTables.transitUDEdgePermutation));
    System.out.println("trEOri size: " + getSize(StateTables.transitEdgeOrientation));

    System.out.println("prCorOri size: " + getSize(StateTables.pruningCornerOrientation));
    System.out.println("prEOri size: " + getSize(StateTables.pruningEdgeOrientation));
    System.out.println("prCorPerm size: " + getSize(StateTables.pruningCornerPermutation));
    System.out.println("prUDEPerm size: " + getSize(StateTables.pruningUDEdgePermutation));
  }

  @Test
  public void testPermPerformance() {
    Random r = new Random();
    byte[] perms;
    byte[] dest = new byte[8];
    for (int i = 0; i < 50000; i++) {
      int n = r.nextInt(40320);
      IndexConvertor.unpackPermutation(n, dest);
    }
    long ts = System.currentTimeMillis();
    for (int i = 0; i < 50000; i++) {
      perms = Move.values()[r.nextInt(18)].corPerm;
      StateTables.getPermResult(dest, perms);
    }
    System.out.println("corner perm time: " + (System.currentTimeMillis() - ts));
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

  private void applyMove(ThreeCubeState state, Move move) {
    state.edgePermutations = StateTables.getPermResult(state.edgePermutations, move.edgPerm);
    state.cornerPermutations = StateTables.getPermResult(state.cornerPermutations, move.corPerm);
    state.edgeOrientations = StateTables.getOrientResult(state.edgeOrientations, move.edgPerm, move.edgOrient, 2);
    state.cornerOrientations = StateTables.getOrientResult(state.cornerOrientations, move.corPerm, move.corOrient, 3);
  }

}
