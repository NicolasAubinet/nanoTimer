package com.cube.nanotimer.scrambler.randomstate;

import android.util.Log;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class ThreeSolver {

  // Cubies numbering:
  //
  //       U              D        E (mid-layer)
  // #############  #############  #############
  // # 1 # 7 # 4 #  # 6 # 9 # 7 #  # 3 #   # 2 #
  // #############  #############  #############
  // # 8 #   # 6 #  # 12#   # 10#  #   #   #   #
  // #############  #############  #############
  // # 2 # 5 # 3 #  # 5 # 11# 8 #  # 4 #   # 1 #
  // #############  #############  #############

  static enum Move {
    U ("U",  new byte[] { 2, 3, 4, 1, 5, 6, 7, 8 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 1, 2, 3, 4, 6, 7, 8, 5, 9, 10, 11, 12 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    U2("U2", new byte[] { 3, 4, 1, 2, 5, 6, 7, 8 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 1, 2, 3, 4, 7, 8, 5, 6, 9, 10, 11, 12 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    UP("U'", new byte[] { 4, 1, 2, 3, 5, 6, 7, 8 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 1, 2, 3, 4, 8, 5, 6, 7, 9, 10, 11, 12 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    D ("D",  new byte[] { 1, 2, 3, 4, 8, 5, 6, 7 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 12, 9, 10, 11 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    D2("D2", new byte[] { 1, 2, 3, 4, 7, 8, 5, 6 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 11, 12, 9, 10 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    DP("D'", new byte[] { 1, 2, 3, 4, 6, 7, 8, 5 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 9 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    R ("R",  new byte[] { 1, 2, 7, 3, 5, 6, 8, 4 }, new byte[] { 0, 0, 2, 1, 0, 0, 1, 2 }, new byte[] { 10, 6, 3, 4, 5, 1, 7, 8, 9, 2, 11, 12 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    R2("R2", new byte[] { 1, 2, 8, 7, 5, 6, 4, 3 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 2, 1, 3, 4, 5, 10, 7, 8, 9, 6, 11, 12 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    RP("R'", new byte[] { 1, 2, 4, 8, 5, 6, 3, 7 }, new byte[] { 0, 0, 2, 1, 0, 0, 1, 2 }, new byte[] { 6, 10, 3, 4, 5, 2, 7, 8, 9, 1, 11, 12 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    L ("L",  new byte[] { 5, 1, 3, 4, 6, 2, 7, 8 }, new byte[] { 2, 1, 0, 0, 1, 2, 0, 0 }, new byte[] { 1, 2, 12, 8, 5, 6, 7, 3, 9, 10, 11, 4 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    L2("L2", new byte[] { 6, 5, 3, 4, 2, 1, 7, 8 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 1, 2, 4, 3, 5, 6, 7, 12, 9, 10, 11, 8 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    LP("L'", new byte[] { 2, 6, 3, 4, 1, 5, 7, 8 }, new byte[] { 2, 1, 0, 0, 1, 2, 0, 0 }, new byte[] { 1, 2, 8, 12, 5, 6, 7, 4, 9, 10, 11, 3 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    F ("F",  new byte[] { 1, 6, 2, 4, 5, 7, 3, 8 }, new byte[] { 0, 2, 1, 0, 0, 1, 2, 0 }, new byte[] { 5, 2, 3, 9, 4, 6, 7, 8, 1, 10, 11, 12 }, new byte[] { 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0 }),
    F2("F2", new byte[] { 1, 7, 6, 4, 5, 3, 2, 8 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 4, 2, 3, 1, 9, 6, 7, 8, 5, 10, 11, 12 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    FP("F'", new byte[] { 1, 3, 7, 4, 5, 2, 6, 8 }, new byte[] { 0, 2, 1, 0, 0, 1, 2, 0 }, new byte[] { 9, 2, 3, 5, 1, 6, 7, 8, 4, 10, 11, 12 }, new byte[] { 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0 }),
    B ("B",  new byte[] { 4, 2, 3, 8, 1, 6, 7, 5 }, new byte[] { 1, 0, 0, 2, 2, 0, 0, 1 }, new byte[] { 1, 11, 7, 4, 5, 6, 2, 8, 9, 10, 3, 12 }, new byte[] { 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0 }),
    B2("B2", new byte[] { 8, 2, 3, 5, 4, 6, 7, 1 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 1, 3, 2, 4, 5, 6, 11, 8, 9, 10, 7, 12 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    BP("B'", new byte[] { 5, 2, 3, 1, 8, 6, 7, 4 }, new byte[] { 1, 0, 0, 2, 2, 0, 0, 1 }, new byte[] { 1, 7, 11, 4, 5, 6, 3, 8, 9, 10, 2, 12 }, new byte[] { 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0 });

    Move (String name, byte[] corPerm, byte[] corOrient, byte[] edgPerm, byte[] edgOrient) {
      this.name = name;
      this.corPerm = corPerm;
      this.corOrient = corOrient;
      this.edgPerm = edgPerm;
      this.edgOrient = edgOrient;
    }

    String name;
    byte[] corPerm;
    byte[] corOrient;
    byte[] edgPerm;
    byte[] edgOrient;
  }

  static class CubeState {
    public byte[] cornerPermutations = new byte[8];
    public byte[] cornerOrientations = new byte[8];
    public byte[] edgePermutations = new byte[12];
    public byte[] edgeOrientations = new byte[12];
  }

  private static final int N_CORNER_PERMUTATIONS = 40320;
  private static final int N_CORNER_ORIENTATIONS = 2187;
  private static final int N_E_EDGE_COMBINATIONS = 495;

  private static final int N_EDGE_PERMUTATIONS = 479001600;
  private static final int N_E_EDGE_PERMUTATIONS = 24;
  private static final int N_U_D_EDGE_PERMUTATIONS = 40320;
  private static final int N_EDGE_ORIENTATIONS = 2048;

  private static Move[] moves1;
  private static Move[] moves2;

  // Transition tables
  private static int[][] transitCornerPermutation;
  private static int[][] transitCornerOrientation;
  private static int[][] transitEEdgeCombination;
  private static int[][] transitEEdgePermutation;
  private static int[][] transitUDEdgePermutation;
  private static int[][] transitEdgeOrientation;

  // Pruning tables
//  static byte[][] pruningCornerOrientation; // TODO : put back to private (used for unit test)
  static byte[] pruningCornerOrientation; // TODO : put back to private (used for unit test)
  private static byte[][] pruningEdgeOrientation;
  private static byte[][] pruningCornerPermutation;
  private static byte[][] pruningUDEdgePermutation;

  // TODO : remove following variables (test):
  static int maxDistance = 0;
  static int nSmallest = 0;
  static int maxListSize = 0;
  static int[] distCounts;
  public static void initTables() {
    if (moves1 != null) {
      return;
    }
    long ts = System.currentTimeMillis();
    moves1 = new Move[] {
        Move.U, Move.U2, Move.UP,
        Move.D, Move.D2, Move.DP,
        Move.R, Move.R2, Move.RP,
        Move.L, Move.L2, Move.LP,
        Move.F, Move.F2, Move.FP,
        Move.B, Move.B2, Move.BP
    };
    moves2 = new Move[] {
        Move.U, Move.U2, Move.UP,
        Move.D, Move.D2, Move.DP,
        Move.R2,
        Move.L2,
        Move.F2,
        Move.B2
    };

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

    // --> Phase 1
//    int distance = 0;
//    int visited = 1;
//    distCounts = new int[13];
//    Arrays.fill(distCounts, 0);
    // TODO : couldn't have two separate arrays?
//    pruningCornerOrientation = new byte[N_CORNER_ORIENTATIONS][N_E_EDGE_COMBINATIONS];
    pruningCornerOrientation = new byte[N_CORNER_ORIENTATIONS];
//    for (int i = 0; i < N_CORNER_ORIENTATIONS; i++) {
//      for (int j = 0; j < N_E_EDGE_COMBINATIONS; j++) {
//        pruningCornerOrientation[i][j] = -1;
//      }
//    }
    Arrays.fill(pruningCornerOrientation, (byte) -1);
//    pruningCornerOrientation[0][0] = 0;
//    pruningCornerOrientation[0] = 0;
    long tsPrun = System.currentTimeMillis();
//    genPruningCornerOrientation(0, 0, -1, 1);
//    try {
//      ByteArrayOutputStream baos = new ByteArrayOutputStream();
//      ObjectOutputStream oos = new ObjectOutputStream(baos);
//      oos.writeObject(new PruningState(1, 1, (short) 1));
//      oos.close();
//      Log.i("[NanoTimer]", "pruningstate size: " + baos.size());
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//    Log.i("[NanoTimer]", "total memory: " + Runtime.getRuntime().totalMemory() + " free: " + Runtime.getRuntime().freeMemory());
    genPruningCornerOrientation();
    Log.i("[NanoTimer]", "nSmallest: " + nSmallest + " maxListSize: " + maxListSize);
    Log.i("[NanoTimer]", "pruning time: " + (System.currentTimeMillis() - tsPrun));
//    while (visited < N_CORNER_ORIENTATIONS * N_E_EDGE_COMBINATIONS && distance < 12) {
//      for (int i = 0; i < N_CORNER_ORIENTATIONS; i++) {
//        for (int j = 0; j < N_E_EDGE_COMBINATIONS; j++) {
//          if (pruningCornerOrientation[i][j] == distance) {
//            for (int k = 0; k < moves1.length; k++) {
//              int orientRes = transitCornerOrientation[i][k];
//              int edgComb = transitEEdgeCombination[j][k];
//              if (pruningCornerOrientation[orientRes][edgComb] < 0) {
//                pruningCornerOrientation[orientRes][edgComb] = (byte) (distance + 1);
//                visited++;
//              }
//            }
//          }
//        }
//      }
//      distance++;
//    }
//    Log.i("[NanoTimer]", "distance: " + distance);
//    Log.i("[NanoTimer]", "visited: " + visited + ". pruning time: " + (System.currentTimeMillis() - tsPrun) + " distCounts: " + Arrays.toString(distCounts));

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

    // --> Phase 2
    pruningCornerPermutation = new byte[N_CORNER_PERMUTATIONS][N_E_EDGE_PERMUTATIONS];
    pruningUDEdgePermutation = new byte[N_U_D_EDGE_PERMUTATIONS][N_E_EDGE_PERMUTATIONS];
    Log.i("[NanoTimerPerf]", "time to generate static stuff: " + (System.currentTimeMillis() - ts));
  }

  /*private static void genPruningCornerOrientation(int corInd, int edgInd, int lastMoveInd, int distance) {
    for (int i = 0; i < moves1.length; i++) {
      if (i / 3 == lastMoveInd / 3) {
        continue;
      }
      int orientRes = transitCornerOrientation[corInd][i];
      int edgComb = transitEEdgeCombination[edgInd][i];
      if (pruningCornerOrientation[orientRes][edgComb] < 0 || distance < pruningCornerOrientation[orientRes][edgComb]) {
//        if (distance < pruningCornerOrientation[orientRes][edgComb]) {
//          nSmallest++;
//        }
        pruningCornerOrientation[orientRes][edgComb] = (byte) distance;
        if (distance < 12) {
          genPruningCornerOrientation(orientRes, edgComb, i, distance + 1);
        }
      }
    }
  }

  private static void genPruningCornerOrientation() {
    Queue<PruningState> corList = new LinkedList<PruningState>();
    corList.add(new PruningState(0, 0, (short) 1));
    // TODO : check memory usage. list size can reach 593789. Up to 50Mb??
    while (!corList.isEmpty()) {
      PruningState prunState = corList.remove();
      for (int i = 0; i < moves1.length; i++) {
        int orientRes = transitCornerOrientation[prunState.ind1][i];
        int edgComb = transitEEdgeCombination[prunState.ind2][i];
        if (pruningCornerOrientation[orientRes][edgComb] < 0) {
          pruningCornerOrientation[orientRes][edgComb] = (byte) prunState.distance;
          corList.add(new PruningState(orientRes, edgComb, (short) (prunState.distance + 1)));
        }
      }
    }
  }*/

  private static void genPruningCornerOrientation() {
    Queue<PruningState> corList = new LinkedList<PruningState>();
    corList.add(new PruningState(0, (short) 1));
    while (!corList.isEmpty()) {
      PruningState prunState = corList.remove();
      for (int i = 0; i < moves1.length; i++) {
        int orientRes = transitCornerOrientation[prunState.ind][i];
        if (pruningCornerOrientation[orientRes] < 0) {
          pruningCornerOrientation[orientRes] = (byte) prunState.distance;
          corList.add(new PruningState(orientRes, (short) (prunState.distance + 1)));
        }
      }
    }
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

  public String[] getSolution(CubeState cubeState) {
    return null;
  }

  static class PruningState {
    int ind;
//    int ind1;
//    int ind2;
    short distance;

    PruningState(int ind, short distance) {
      this.ind = ind;
      this.distance = distance;
    }

//    PruningState(int ind1, int ind2, short distance) {
//      this.ind1 = ind1;
//      this.ind2 = ind2;
//      this.distance = distance;
//    }
  }

}
