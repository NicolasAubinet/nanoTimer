package com.cube.nanotimer.scrambler.randomstate;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

public class StateTables {

  private static final int N_CORNER_PERMUTATIONS = 40320;
  private static final int N_CORNER_ORIENTATIONS = 2187;
  private static final int N_E_EDGE_COMBINATIONS = 495;

  private static final int N_EDGE_PERMUTATIONS = 479001600;
  private static final int N_E_EDGE_PERMUTATIONS = 24;
  private static final int N_U_D_EDGE_PERMUTATIONS = 40320;
  private static final int N_EDGE_ORIENTATIONS = 2048;

  // Transition tables
  static int[][] transitCornerPermutation;
  static int[][] transitCornerOrientation;
  static int[][] transitEEdgeCombination;
  static int[][] transitEEdgePermutation;
  static int[][] transitUDEdgePermutation;
  static int[][] transitEdgeOrientation;

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
    transitCornerOrientation = new int[N_CORNER_ORIENTATIONS][moves1.length];
    for (int i = 0; i < transitCornerOrientation.length; i++) {
      byte[] state = IndexConvertor.unpackOrientation(i, 3, 8);
      for (int j = 0; j < moves1.length; j++) {
        transitCornerOrientation[i][j] = IndexConvertor.packOrientation(getOrientResult(state, moves1[j].corPerm, moves1[j].corOrient, 3), 3);
      }
    }

    transitEdgeOrientation = new int[N_EDGE_ORIENTATIONS][moves1.length];
    for (int i = 0; i < transitEdgeOrientation.length; i++) {
      byte[] state = IndexConvertor.unpackOrientation(i, 2, 12);
      for (int j = 0; j < moves1.length; j++) {
        transitEdgeOrientation[i][j] = IndexConvertor.packOrientation(getOrientResult(state, moves1[j].edgPerm, moves1[j].edgOrient, 2), 2);
      }
    }

    transitEEdgeCombination = new int[N_E_EDGE_COMBINATIONS][moves1.length];
    for (int i = 0; i < transitEEdgeCombination.length; i++) {
      boolean[] state = IndexConvertor.unpackCombination(i, 4, 12);
      for (int j = 0; j < moves1.length; j++) {
        transitEEdgeCombination[i][j] = IndexConvertor.packCombination(getPermResult(state, moves1[j].edgPerm), 4);
      }
    }

    // --> Phase 2
    transitCornerPermutation = new int[N_CORNER_PERMUTATIONS][moves2.length];
    for (int i = 0; i < transitCornerPermutation.length; i++) {
      byte[] state = IndexConvertor.unpackPermutation(i, 8);
      for (int j = 0; j < moves2.length; j++) {
        transitCornerPermutation[i][j] = IndexConvertor.packPermutation(getPermResult(state, moves2[j].corPerm));
      }
    }

    // E and UD edges stay on the same layers as phase 2 moves can not interchange them
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

    // ##################
    // # Pruning tables #
    // ##################

    long tsPrun = System.currentTimeMillis();

    // --> Phase 1
    pruningCornerOrientation = new byte[N_CORNER_ORIENTATIONS][N_E_EDGE_COMBINATIONS];
    for (int i = 0; i < N_CORNER_ORIENTATIONS; i++) {
      for (int j = 0; j < N_E_EDGE_COMBINATIONS; j++) {
        pruningCornerOrientation[i][j] = -1;
      }
    }

    pruningCornerOrientation[0][0] = 0;
    Queue<PruningState> prunList = new LinkedList<PruningState>();
    prunList.add(new PruningState(0, 0, (short) 1));
    while (!prunList.isEmpty()) {
      PruningState prunState = prunList.remove();
      for (int i = 0; i < moves1.length; i++) {
        int orientRes = transitCornerOrientation[prunState.ind1][i];
        int edgComb = transitEEdgeCombination[prunState.ind2][i];
        if (pruningCornerOrientation[orientRes][edgComb] < 0) {
          pruningCornerOrientation[orientRes][edgComb] = (byte) prunState.distance;
          prunList.add(new PruningState(orientRes, edgComb, (short) (prunState.distance + 1)));
        }
      }
    }

//    for (int i = 0; i < N_CORNER_ORIENTATIONS; i++) {
//      for (int j = 0; j < N_E_EDGE_COMBINATIONS; j++) {
//        int d = pruningCornerOrientation[i][j];
//        if (d >= 0) {
//          distCounts[d]++;
//        }
//      }
//    }
//    Log.i("[NanoTimer]", "distCounts: " + Arrays.toString(distCounts));

    pruningEdgeOrientation = new byte[N_EDGE_ORIENTATIONS][N_E_EDGE_COMBINATIONS];
    for (int i = 0; i < N_EDGE_ORIENTATIONS; i++) {
      for (int j = 0; j < N_E_EDGE_COMBINATIONS; j++) {
        pruningEdgeOrientation[i][j] = -1;
      }
    }
    pruningEdgeOrientation[0][0] = 0;
    prunList = new LinkedList<PruningState>();
    prunList.add(new PruningState(0, 0, (short) 1));
    while (!prunList.isEmpty()) {
      PruningState prunState = prunList.remove();
      for (int i = 0; i < moves1.length; i++) {
        int orientRes = transitEdgeOrientation[prunState.ind1][i];
        int edgComb = transitEEdgeCombination[prunState.ind2][i];
        if (pruningEdgeOrientation[orientRes][edgComb] < 0) {
          pruningEdgeOrientation[orientRes][edgComb] = (byte) prunState.distance;
          prunList.add(new PruningState(orientRes, edgComb, (short) (prunState.distance + 1)));
        }
      }
    }

    // --> Phase 2
    pruningCornerPermutation = new byte[N_CORNER_PERMUTATIONS][N_E_EDGE_PERMUTATIONS];
    for (int i = 0; i < N_CORNER_PERMUTATIONS; i++) {
      for (int j = 0; j < N_E_EDGE_PERMUTATIONS; j++) {
        pruningCornerPermutation[i][j] = -1;
      }
    }
    pruningCornerPermutation[0][0] = 0;
    prunList = new LinkedList<PruningState>();
    prunList.add(new PruningState(0, 0, (short) 1));
    while (!prunList.isEmpty()) {
      PruningState prunState = prunList.remove();
      for (int i = 0; i < moves2.length; i++) {
        int permRes = transitCornerPermutation[prunState.ind1][i];
        int edgPerm = transitEEdgePermutation[prunState.ind2][i];
        if (pruningCornerPermutation[permRes][edgPerm] < 0) {
          pruningCornerPermutation[permRes][edgPerm] = (byte) prunState.distance;
          prunList.add(new PruningState(permRes, edgPerm, (short) (prunState.distance + 1)));
        }
      }
    }

    pruningUDEdgePermutation = new byte[N_U_D_EDGE_PERMUTATIONS][N_E_EDGE_PERMUTATIONS];
    for (int i = 0; i < N_U_D_EDGE_PERMUTATIONS; i++) {
      for (int j = 0; j < N_E_EDGE_PERMUTATIONS; j++) {
        pruningUDEdgePermutation[i][j] = -1;
      }
    }
    pruningUDEdgePermutation[0][0] = 0;
    prunList = new LinkedList<PruningState>();
    prunList.add(new PruningState(0, 0, (short) 1));
    while (!prunList.isEmpty()) {
      PruningState prunState = prunList.remove();
      for (int i = 0; i < moves2.length; i++) {
        int permRes = transitUDEdgePermutation[prunState.ind1][i];
        int edgPerm = transitEEdgePermutation[prunState.ind2][i];
        if (pruningUDEdgePermutation[permRes][edgPerm] < 0) {
          pruningUDEdgePermutation[permRes][edgPerm] = (byte) prunState.distance;
          prunList.add(new PruningState(permRes, edgPerm, (short) (prunState.distance + 1)));
        }
      }
    }

    Log.i("[NanoTimer]", "pruning time: " + (System.currentTimeMillis() - tsPrun));
    Log.i("[NanoTimerPerf]", "time to generate static stuff: " + (System.currentTimeMillis() - ts));
  }

  static byte[] getPermResult(byte[] state, byte[] permIndices) {
    byte[] result = new byte[state.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = state[permIndices[i] - 1];
    }
    return result;
  }

  static boolean[] getPermResult(boolean[] state, byte[] permIndices) {
    boolean[] result = new boolean[state.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = state[permIndices[i] - 1];
    }
    return result;
  }

  static byte[] getOrientResult(byte[] state, byte[] permIndices, byte[] orientIndices, int nDifferentValues) {
    byte[] result = new byte[state.length];
    for (int i = 0; i < state.length; i++) {
      result[i] = (byte) ((state[permIndices[i] - 1] + orientIndices[i]) % nDifferentValues);
    }
    return result;
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
