package com.cube.nanotimer.scrambler.randomstate;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

public class StateTables {

  // TODO : make positions start at 0 (everywhere, starting from Move class, IndexConvertor, tests etc)

  private static final int N_CORNER_PERMUTATIONS = 40320;
//  private static final int N_CORNER_PERMUTATIONS = 5040;
  private static final int N_CORNER_ORIENTATIONS = 2187;
  private static final int N_E_EDGE_COMBINATIONS = 495;

  private static final int N_E_EDGE_PERMUTATIONS = 24;
  private static final int N_U_D_EDGE_PERMUTATIONS = 40320;
//  private static final int N_U_D_EDGE_PERMUTATIONS = 5040;
  private static final int N_EDGE_ORIENTATIONS = 2048;

  // RELATIVE PERMUTATIONS are wrooooong! Don't know how to do a R after a U...
  // TODO : see if there is a way to make it work
  // TODO : else, could see if possible to drop the last piece (have 7 corners and 7 UD edges and figure the last one out) to keep 5040 elements instead of 40320

  // TODO : transit optimization:
  //        use symmetries to reduce entries?
  //v       only have 1 move per face (so 2nd index max would be 6 instead of 18) and apply same moves multiple times (R R R in a row instead of R U2 R')
  //v         would pbly have a sub-loop during solution search when looping between moves
  //x       change permutations (corner and UD edges) to relative positions (1 2 3 4 5 6 7 8 = 3 4 5 6 7 8 1 2)    (not working, losing the position for side moves like R, L etc...)
  //          keep the 1st corner and 1st UD edge positions in variables and apply permutation[pos-1] after every move during solution search
  //          phase will be solved if permutationInd == 0 && cornerpos/udedgepos == 0
  //        should be able to change from int to short (b/c N_CORNER_PERMUTATIONS going down from 40320 to 5040)
  //          could pbly also do it for other tables (already now)

  // Transition tables
  static int[][] transitCornerPermutation;
  static short[][] transitCornerOrientation;
  static short[][] transitEEdgeCombination;
  static short[][] transitEEdgePermutation;
  static int[][] transitUDEdgePermutation;
  static short[][] transitEdgeOrientation;

  // TODO : pruning optimization:
  //        switch to backward search when getting close to the end (compare with / without)
  //        use symmetries to reduce entries (not necessarily all symmetries)
  //        could investigate possibility to include the move bringing to the position in addition to the distance for faster solutions (would make tables bigger though)
  //        corner and UD edge perms will be reduced with transit optimization
  //        could probably also be able to only use 1 move per face (don't do 2 and ') (see if ok)
  //          could also maybe even reduce it to R U F and use symmetric moves (R same effect than L', etc.)
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

    byte[] state8 = new byte[8];
    byte[] state12 = new byte[12];
    boolean[] bState12 = new boolean[12];

    // --> Phase 1
    long stepTs = System.currentTimeMillis();
    transitCornerOrientation = new short[N_CORNER_ORIENTATIONS][moves1.length];
    for (int i = 0; i < transitCornerOrientation.length; i++) {
      IndexConvertor.unpackOrientation(i, state8, (byte) 3);
      for (int j = 0; j < moves1.length; j++) {
        transitCornerOrientation[i][j] = (short) IndexConvertor.packOrientMult(state8, moves1[j].corPerm, moves1[j].corOrient, 3);
      }
    }
    logTimeDifference(stepTs, "trCorOri");

    stepTs = System.currentTimeMillis();
    transitEdgeOrientation = new short[N_EDGE_ORIENTATIONS][moves1.length];
    for (int i = 0; i < transitEdgeOrientation.length; i++) {
      IndexConvertor.unpackOrientation(i, state12, (byte) 2);
      for (int j = 0; j < moves1.length; j++) {
        transitEdgeOrientation[i][j] = (short) IndexConvertor.packOrientMult(state12, moves1[j].edgPerm, moves1[j].edgOrient, 2);
      }
    }
    logTimeDifference(stepTs, "trEdgOri");

    stepTs = System.currentTimeMillis();
    transitEEdgeCombination = new short[N_E_EDGE_COMBINATIONS][moves1.length];
    for (int i = 0; i < transitEEdgeCombination.length; i++) {
      IndexConvertor.unpackCombination(i, bState12, 4);
      for (int j = 0; j < moves1.length; j++) {
        transitEEdgeCombination[i][j] = (short) IndexConvertor.packCombPermMult(bState12, moves1[j].edgPerm, 4);
      }
    }
    logTimeDifference(stepTs, "trEEdgComb");

    // --> Phase 2
    stepTs = System.currentTimeMillis();
    transitCornerPermutation = new int[N_CORNER_PERMUTATIONS][moves2.length];
    for (int i = 0; i < transitCornerPermutation.length; i++) {
      IndexConvertor.unpackPermutation(i, state8);
      for (int j = 0; j < moves2.length; j++) {
        transitCornerPermutation[i][j] = IndexConvertor.packPermMult(state8, moves2[j].corPerm);
      }
    }
    logTimeDifference(stepTs, "trCorPerm");

    // E and UD edges stay on the same slice/layers as phase 2 moves can only interchange them
    stepTs = System.currentTimeMillis();
    transitEEdgePermutation = new short[N_E_EDGE_PERMUTATIONS][moves2.length];
    for (int i = 0; i < transitEEdgePermutation.length; i++) {
      byte[] state = new byte[4];
      byte[] edges = new byte[12];
      IndexConvertor.unpackPermutation(i, state);
      for (byte j = 0; j < edges.length; j++) {
        edges[j] = (j < 4) ? state[j] : 0;
      }

      for (int j = 0; j < moves2.length; j++) {
        byte[] res = getPermResult(edges, moves2[j].edgPerm);
        byte[] eEdges = new byte[4];
        System.arraycopy(res, 0, eEdges, 0, eEdges.length);
        transitEEdgePermutation[i][j] = (short) IndexConvertor.packPermutation(eEdges);
      }
    }
    logTimeDifference(stepTs, "trEEdgPerm");

    stepTs = System.currentTimeMillis();
    transitUDEdgePermutation = new int[N_U_D_EDGE_PERMUTATIONS][moves2.length];
    for (int i = 0; i < transitUDEdgePermutation.length; i++) {
      IndexConvertor.unpackPermutation(i, state8);
      byte[] edges = new byte[12];
      for (byte j = 0; j < edges.length; j++) {
        edges[j] = (j < 4) ? 0 : state8[j - 4];
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
    genPruning(pruningCornerOrientation, transitCornerOrientation, transitEEdgeCombination, moves1, 1);
    logTimeDifference(stepTs, "prCorOri");

    stepTs = System.currentTimeMillis();
    pruningEdgeOrientation = new byte[N_EDGE_ORIENTATIONS][N_E_EDGE_COMBINATIONS];
    genPruning(pruningEdgeOrientation, transitEdgeOrientation, transitEEdgeCombination, moves1, 1);
    logTimeDifference(stepTs, "prEdgOri");

    // --> Phase 2
    stepTs = System.currentTimeMillis();
    pruningCornerPermutation = new byte[N_CORNER_PERMUTATIONS][N_E_EDGE_PERMUTATIONS];
    genPruning(pruningCornerPermutation, transitCornerPermutation, transitEEdgePermutation, moves2, 2);
    logTimeDifference(stepTs, "prCorPerm");

    stepTs = System.currentTimeMillis();
    pruningUDEdgePermutation = new byte[N_U_D_EDGE_PERMUTATIONS][N_E_EDGE_PERMUTATIONS];
    genPruning(pruningUDEdgePermutation, transitUDEdgePermutation, transitEEdgePermutation, moves2, 2);
    logTimeDifference(stepTs, "prEdgPerm");

    logTimeDifference(tsPrun, "-> pruning time");
    logTimeDifference(ts, "time to generate tables");
  }

  private static void genPruning(byte[][] pruningTable, short[][] transit1, short[][] transit2, Move[] moves, int phase) {
    int[][] tr = new int[transit1.length][transit1[0].length];
    for (int i = 0; i < transit1.length; i++) {
      for (int j = 0; j < transit1[i].length; j++) {
        tr[i][j] = transit1[i][j];
      }
    }
    genPruning(pruningTable, tr, transit2, moves, phase);
  }

  private static void genPruning(byte[][] pruningTable, int[][] transit1, short[][] transit2, Move[] moves, int phase) {
    for (int i = 0; i < transit1.length; i++) {
      for (int j = 0; j < transit2.length; j++) {
        pruningTable[i][j] = -1;
      }
    }
    pruningTable[0][0] = 0;
    int totalLength = transit1.length * transit2.length;
//    int maxDistance = (phase == 1) ? 12 : 18;
    int done = 1;
    byte distance = 0;
    // Only fill up to a certain point, until when we start filling pruning in reverse order (better performance)
    while (done < totalLength) {
//    while (distance < maxDistance - 2) {
      for (int i = 0; i < transit1.length; i++) {
        for (int j = 0; j < transit2.length; j++) {
          if (pruningTable[i][j] == distance) {
            for (int k = 0; k < moves.length; k++) {
              int res1 = i;
              int res2 = j;
              int nSubMoves = (phase == 1 || k < 2) ? 3 : 1;
              for (int l = 0; l < nSubMoves; l++) {
                res1 = transit1[res1][k];
                res2 = transit2[res2][k];
                if (pruningTable[res1][res2] < 0) {
                  pruningTable[res1][res2] = (byte) (distance + 1);
                  done++;
                }
              }
            }
          }
        }
      }
      distance++;
    }
//    finishFillingPruning(pruningTable, transit1, transit2, moves, phase);
  }

  private static void finishFillingPruning(byte[][] pruningTable, int[][] transit1, short[][] transit2, Move[] moves, int phase) {
    int depthLimit = 2;
    for (int i = 0; i < transit1.length; i++) {
      for (int j = 0; j < transit2.length; j++) {
        if (pruningTable[i][j] < 0) {
          int curDepthSearch = 0;
          boolean found = false;
          Queue<PruningState> prunList = new LinkedList<PruningState>();
          prunList.add(new PruningState(i, j));
          while (curDepthSearch < depthLimit && !found) {
            PruningState state = prunList.remove();
            for (int k = 0; k < moves.length && !found; k++) {
              int res1 = state.ind1;
              int res2 = state.ind2;
              int nSubMoves = (phase == 1 || k < 2) ? 3 : 1;
              for (int l = 0; l < nSubMoves && !found; l++) {
                res1 = transit1[res1][k];
                res2 = transit2[res2][k];
                if (pruningTable[res1][res2] >= 0) {
                  pruningTable[i][j] = (byte) (pruningTable[res1][res2] + (curDepthSearch + 1));
                  found = true;
                }
                if (curDepthSearch < depthLimit - 1) {
                  prunList.add(new PruningState(res1, res2));
                }
              }
            }
            curDepthSearch++;
          }
        }
      }
    }
  }

  /*private static void genPruning(byte[][] pruningTable, int[][] transit1, short[][] transit2, int phase) {
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
  }*/

  static byte[] getPermResult(byte[] state, byte[] permIndices) {
    byte[] result = new byte[state.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = state[permIndices[i]];
    }
    return result;
  }

  static boolean[] getPermResult(boolean[] state, byte[] permIndices) {
    boolean[] result = new boolean[state.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = state[permIndices[i]];
    }
    return result;
  }

  static byte[] getOrientResult(byte[] state, byte[] permIndices, byte[] orientIndices, int nDifferentValues) {
    byte[] result = new byte[state.length];
    for (int i = 0; i < state.length; i++) {
      result[i] = (byte) ((state[permIndices[i]] + orientIndices[i]) % nDifferentValues);
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

    PruningState(int ind1, int ind2) {
      this.ind1 = ind1;
      this.ind2 = ind2;
    }

    PruningState(int ind1, int ind2, short distance) {
      this.ind1 = ind1;
      this.ind2 = ind2;
      this.distance = distance;
    }
  }

}
