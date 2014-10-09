package com.cube.nanotimer.scrambler.randomstate;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

public class StateTables {

  // TODO : make positions start at 0 (everywhere, starting from Move class, IndexConvertor, tests etc)

  private static final int N_CORNER_PERMUTATIONS = 40320;
  private static final int N_CORNER_ORIENTATIONS = 2187;
  private static final int N_E_EDGE_COMBINATIONS = 495;

  private static final int N_EDGE_PERMUTATIONS = 479001600;
  private static final int N_E_EDGE_PERMUTATIONS = 24;
  private static final int N_U_D_EDGE_PERMUTATIONS = 40320;
  private static final int N_EDGE_ORIENTATIONS = 2048;

  // TODO : transit optimization:
  //v       only have 1 move per face (so 2nd index max would be 6 instead of 18) and apply same moves multiple times (R R R in a row instead of R U2 R')
  //v         would pbly have a sub-loop during solution search when looping between moves
  //        change permutations (corner and UD edges) to relative positions (1 2 3 4 5 6 7 8 = 3 4 5 6 7 8 1 2)
  //          keep the 1st corner and 1st UD edge positions in variables and apply permutation[pos-1] after every move during solution search
  //          phase will be solved if permutationInd == 0 && cornerpos/udedgepos == 0
  //        should be able to change from int to short (b/c n_corner_permutations going down from 40320 to 5040)
  //          could pbly also do it for other tables (already now)

  // Transition tables
  static int[][] transitCornerPermutation;
  static int[][] transitCornerOrientation;
  static int[][] transitEEdgeCombination;
  static int[][] transitEEdgePermutation;
  static int[][] transitUDEdgePermutation;
  static int[][] transitEdgeOrientation;

  // TODO : pruning optimization:
  //        switch to backward search when getting close to the end (compare with / without)
  //        corner and UD edge perms will be reduced with transit optimization
  //        could probably also be able to only use 1 move per face (don't do 2 and ') (see if ok)
  //          could also maybe even reduce it to R U F and use symetric moves (R same effect than L', etc.)
  //        if need more, could also avoid filling in the last distance (stop at 11 for phase 1 and at 17 for phase 2). if -1, then we know it's 12 and 18

  // Pruning tables
  static byte[][] pruningCornerOrientation;
  static byte[][] pruningEdgeOrientation;
  static byte[][] pruningCornerPermutation;
  static byte[][] pruningUDEdgePermutation;

  public static void generateTables(Move[] moves1, Move[] moves2) {
    long ts = System.currentTimeMillis();

    // #####################
    // # Transition tables #
    // #####################

    // --> Phase 1
    long stepTs = System.currentTimeMillis();
    transitCornerOrientation = new int[N_CORNER_ORIENTATIONS][moves1.length];
    for (int i = 0; i < transitCornerOrientation.length; i++) {
      byte[] state = IndexConvertor.unpackOrientation(i, 3, 8);
      for (int j = 0; j < moves1.length; j++) {
        transitCornerOrientation[i][j] = IndexConvertor.packOrientation(getOrientResult(state, moves1[j].corPerm, moves1[j].corOrient, 3), 3);
      }
    }
    logTimeDifference(stepTs, "trCorOri");

    stepTs = System.currentTimeMillis();
    transitEdgeOrientation = new int[N_EDGE_ORIENTATIONS][moves1.length];
    for (int i = 0; i < transitEdgeOrientation.length; i++) {
      byte[] state = IndexConvertor.unpackOrientation(i, 2, 12);
      for (int j = 0; j < moves1.length; j++) {
        transitEdgeOrientation[i][j] = IndexConvertor.packOrientation(getOrientResult(state, moves1[j].edgPerm, moves1[j].edgOrient, 2), 2);
      }
    }
    logTimeDifference(stepTs, "trEdgOri");

    stepTs = System.currentTimeMillis();
    transitEEdgeCombination = new int[N_E_EDGE_COMBINATIONS][moves1.length];
    for (int i = 0; i < transitEEdgeCombination.length; i++) {
      boolean[] state = IndexConvertor.unpackCombination(i, 4, 12);
      for (int j = 0; j < moves1.length; j++) {
        transitEEdgeCombination[i][j] = IndexConvertor.packCombination(getPermResult(state, moves1[j].edgPerm), 4);
      }
    }
    logTimeDifference(stepTs, "trEEdgComb");

    // --> Phase 2
    stepTs = System.currentTimeMillis();
    transitCornerPermutation = new int[N_CORNER_PERMUTATIONS][moves2.length];
    for (int i = 0; i < transitCornerPermutation.length; i++) {
      byte[] state = IndexConvertor.unpackPermutation(i, 8);
      for (int j = 0; j < moves2.length; j++) {
        transitCornerPermutation[i][j] = IndexConvertor.packPermutation(getPermResult(state, moves2[j].corPerm));
      }
    }
    logTimeDifference(stepTs, "trCorPerm");

    // E and UD edges stay on the same layers as phase 2 moves can not interchange them
    stepTs = System.currentTimeMillis();
    transitEEdgePermutation = new int[N_E_EDGE_PERMUTATIONS][moves2.length];
    for (int i = 0; i < transitEEdgePermutation.length; i++) {
      byte[] state = IndexConvertor.unpackPermutation(i, 4);
      byte[] edges = new byte[12];
      for (byte j = 0; j < edges.length; j++) {
        edges[j] = (j < 4) ? state[j] : 0;
      }

      for (int j = 0; j < moves2.length; j++) {
        byte[] res = getPermResult(edges, moves2[j].edgPerm);
        byte[] eEdges = new byte[4];
        System.arraycopy(res, 0, eEdges, 0, eEdges.length);
        transitEEdgePermutation[i][j] = IndexConvertor.packPermutation(eEdges);
      }
    }
    logTimeDifference(stepTs, "trEEdgPerm");

    stepTs = System.currentTimeMillis();
    transitUDEdgePermutation = new int[N_U_D_EDGE_PERMUTATIONS][moves2.length];
    for (int i = 0; i < transitUDEdgePermutation.length; i++) {
      byte[] state = IndexConvertor.unpackPermutation(i, 8);
      byte[] edges = new byte[12];
      for (byte j = 0; j < edges.length; j++) {
        edges[j] = (j < 4) ? 0 : state[j - 4];
      }

      for (int j = 0; j < moves2.length; j++) {
        byte[] res = getPermResult(edges, moves2[j].edgPerm);
        byte[] udEdges = new byte[8];
        System.arraycopy(res, 4, udEdges, 0, udEdges.length);
        transitUDEdgePermutation[i][j] = IndexConvertor.packPermutation(udEdges);
      }
    }
    logTimeDifference(stepTs, "trUDEdgPerm");
    logTimeDifference(ts, "-> transit time");

    // ##################
    // # Pruning tables #
    // ##################

    long tsPrun = System.currentTimeMillis();

    // --> Phase 1
    stepTs = System.currentTimeMillis();
    pruningCornerOrientation = new byte[N_CORNER_ORIENTATIONS][N_E_EDGE_COMBINATIONS];
    genPruning(pruningCornerOrientation, transitCornerOrientation, transitEEdgeCombination, 1);
    logTimeDifference(stepTs, "prCorOri");

    stepTs = System.currentTimeMillis();
    pruningEdgeOrientation = new byte[N_EDGE_ORIENTATIONS][N_E_EDGE_COMBINATIONS];
    genPruning(pruningEdgeOrientation, transitEdgeOrientation, transitEEdgeCombination, 1);
    logTimeDifference(stepTs, "prEdgOri");

    // --> Phase 2
    stepTs = System.currentTimeMillis();
    pruningCornerPermutation = new byte[N_CORNER_PERMUTATIONS][N_E_EDGE_PERMUTATIONS];
    genPruning(pruningCornerPermutation, transitCornerPermutation, transitEEdgePermutation, 2);
    logTimeDifference(stepTs, "prCorPerm");

    stepTs = System.currentTimeMillis();
    pruningUDEdgePermutation = new byte[N_U_D_EDGE_PERMUTATIONS][N_E_EDGE_PERMUTATIONS];
    genPruning(pruningUDEdgePermutation, transitUDEdgePermutation, transitEEdgePermutation, 2);
    logTimeDifference(stepTs, "prEdgPerm");

    logTimeDifference(tsPrun, "-> pruning time");
    logTimeDifference(ts, "time to generate static stuff");
  }

  private static void genPruning(byte[][] pruningTable, int[][] transit1, int[][] transit2, int phase) {
    for (int i = 0; i < pruningTable.length; i++) {
      for (int j = 0; j < pruningTable[i].length; j++) {
        pruningTable[i][j] = -1;
      }
    }
    pruningTable[0][0] = 0;
    Queue<PruningState> prunList = new LinkedList<PruningState>();
    prunList.add(new PruningState(0, 0, (short) 1));
    while (!prunList.isEmpty()) {
      PruningState prunState = prunList.remove();
      for (int i = 0; i < 6; i++) {
        int res1 = prunState.ind1;
        int res2 = prunState.ind2;
        int nSubMoves = (phase == 1 || i < 2) ? 3 : 1;
        for (int j = 0; j < nSubMoves; j++) {
          res1 = transit1[res1][i];
          res2 = transit2[res2][i];
          if (pruningTable[res1][res2] < 0) {
            pruningTable[res1][res2] = (byte) prunState.distance;
            prunList.add(new PruningState(res1, res2, (short) (prunState.distance + 1)));
          }
        }
      }
    }
  }

  static byte[] getPermResult(byte[] state, byte[] permIndices) {
    // New way (speed-optimized and with 0-based index)
    /*byte permStart = -1;
    byte prevInd;
    byte tmp;
    for (byte i = 0; i < state.length; i++) {
      if (i != permIndices[i]) {
        if (permStart < 0) { // start of permutation
          permStart = i;
          prevInd = i;
          tmp = state[i];
        } else if (permIndices[i] == permStart) { // end of permutation
          state[i] = tmp;
          break;
        }
        state[prevInd] = state[permIndices[i];
        prevInd = permIndices[i];
      }
    }
    return state;*/
    // Old way (array creation, takes some time)
    byte[] result = new byte[state.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = state[permIndices[i] - 1];
    }
    return result;
  }

  static boolean[] getPermResult(boolean[] state, byte[] permIndices) {
    // TODO : use the need way (no array creation, see method above)
    boolean[] result = new boolean[state.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = state[permIndices[i] - 1];
    }
    return result;
  }

  static byte[] getOrientResult(byte[] state, byte[] permIndices, byte[] orientIndices, int nDifferentValues) {
    // TODO : avoid creating a new array
    byte[] result = new byte[state.length];
    for (int i = 0; i < state.length; i++) {
      result[i] = (byte) ((state[permIndices[i] - 1] + orientIndices[i]) % nDifferentValues);
    }
    return result;
  }

  private static void logTimeDifference(long startTs, String msg) {
    Log.i("[NanoTimer]", msg + ": " + (System.currentTimeMillis() - startTs));
  }

  static class PruningState {
    int ind1;
    int ind2;
    short distance;

    PruningState(int ind1, int ind2, short distance) {
      this.ind1 = ind1;
      this.ind2 = ind2;
      this.distance = distance;
    }
  }

}
