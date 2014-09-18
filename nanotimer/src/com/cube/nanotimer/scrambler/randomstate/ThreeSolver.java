package com.cube.nanotimer.scrambler.randomstate;

public class ThreeSolver {

  // Cubies numbering:
  //
  //       U              D        U (mid-layer)
  // #############  #############  #############
  // # 1 # 3 # 4 #  # 6 # 9 # 7 #  # 7 #   # 6 #
  // #############  #############  #############
  // # 4 #   # 2 #  # 12#   # 10#  #   #   #   #
  // #############  #############  #############
  // # 2 # 1 # 3 #  # 5 # 11# 8 #  # 8 #   # 5 #
  // #############  #############  #############

  static enum Move {
    U ("U",  new byte[] { 2, 3, 4, 1, 5, 6, 7, 8 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 2, 3, 4, 1, 5, 6, 7, 8, 9, 10, 11, 12 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    U2("U2", new byte[] { 3, 4, 1, 2, 5, 6, 7, 8 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 3, 4, 1, 2, 5, 6, 7, 8, 9, 10, 11, 12 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    UP("U'", new byte[] { 4, 1, 2, 3, 5, 6, 7, 8 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 4, 1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    D ("D",  new byte[] { 1, 2, 3, 4, 8, 5, 6, 7 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 12, 9, 10, 11 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    D2("D2", new byte[] { 1, 2, 3, 4, 7, 8, 5, 6 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 11, 12, 9, 10 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    DP("D'", new byte[] { 1, 2, 3, 4, 6, 7, 8, 5 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 9 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    R ("R",  new byte[] { 1, 2, 7, 3, 5, 6, 8, 4 }, new byte[] { 0, 0, 2, 1, 0, 0, 1, 2 }, new byte[] { 1, 5, 3, 4, 10, 2, 7, 8, 9, 6, 11, 12 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    R2("R2", new byte[] { 1, 2, 8, 7, 5, 6, 4, 3 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 1, 10, 3, 4, 6, 5, 7, 8, 9, 2, 11, 12 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    RP("R'", new byte[] { 1, 2, 4, 8, 5, 6, 3, 7 }, new byte[] { 0, 0, 2, 1, 0, 0, 1, 2 }, new byte[] { 1, 6, 3, 4, 2, 10, 7, 8, 9, 5, 11, 12 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    L ("L",  new byte[] { 5, 1, 3, 4, 5, 2, 7, 8 }, new byte[] { 2, 1, 0, 0, 1, 2, 0, 0 }, new byte[] { 1, 2, 3, 7, 5, 6, 12, 4, 9, 10, 11, 8 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    L2("L2", new byte[] { 6, 5, 3, 4, 2, 1, 7, 8 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 1, 2, 3, 12, 5, 6, 8, 7, 9, 10, 11, 4 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    LP("L'", new byte[] { 2, 6, 3, 4, 1, 5, 7, 8 }, new byte[] { 2, 1, 0, 0, 1, 2, 0, 0 }, new byte[] { 1, 2, 3, 8, 5, 6, 4, 12, 9, 10, 11, 7 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    F ("F",  new byte[] { 1, 6, 2, 4, 5, 7, 3, 8 }, new byte[] { 0, 2, 1, 0, 0, 1, 2, 0 }, new byte[] { 8, 2, 3, 4, 1, 6, 7, 9, 5, 10, 11, 12 }, new byte[] { 1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 0 }),
    F2("F2", new byte[] { 1, 7, 6, 4, 5, 3, 2, 8 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 9, 2, 3, 4, 8, 6, 7, 5, 1, 10, 11, 12 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    FP("F'", new byte[] { 1, 3, 7, 4, 5, 2, 6, 8 }, new byte[] { 0, 2, 1, 0, 0, 1, 2, 0 }, new byte[] { 5, 2, 3, 4, 9, 6, 7, 1, 8, 10, 11, 12 }, new byte[] { 1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 0 }),
    B ("B",  new byte[] { 4, 2, 3, 8, 1, 6, 7, 5 }, new byte[] { 1, 0, 0, 2, 2, 0, 0, 1 }, new byte[] { 1, 2, 6, 4, 5, 11, 3, 8, 9, 10, 7, 12 }, new byte[] { 0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0 }),
    B2("B2", new byte[] { 8, 2, 3, 5, 4, 6, 7, 1 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 1, 2, 11, 4, 5, 7, 6, 8, 9, 10, 3, 12 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    BP("B'", new byte[] { 5, 2, 3, 1, 8, 6, 7, 4 }, new byte[] { 1, 0, 0, 2, 2, 0, 0, 1 }, new byte[] { 1, 2, 7, 4, 5, 3, 11, 8, 9, 10, 6, 12 }, new byte[] { 0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0 });

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

  private static final int N_EDGE_PERMUTATIONS = 479001600;
  private static final int N_E_EDGE_PERMUTATIONS = 24;
  private static final int N_U_D_EDGE_PERMUTATIONS = 40320;
  private static final int N_EDGE_ORIENTATIONS = 2048;

  private static Move[] moves1;
  private static Move[] moves2;

  // Pruning tables
  private static int[] pruningCornerPermutation;
  private static int[] pruningCornerOrientation;
  private static int[] pruningEEdgePermutation;
  private static int[] pruningUDEdgePermutation;
  private static int[] pruningEdgeOrientation;

  // Transition tables
  private static int[][] transitCornerPermutation;
  private static int[][] transitCornerOrientation;
  private static int[][] transitEEdgePermutation;
  private static int[][] transitUDEdgePermutation;
  private static int[][] transitEdgeOrientation;

  static {
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
        Move.R,
        Move.L,
        Move.F,
        Move.B
    };

    // ##################
    // # Pruning tables #
    // ##################

    // Phase 1
    pruningCornerOrientation = new int[N_CORNER_ORIENTATIONS];
    for (int i = 0; i < pruningCornerOrientation.length; i++) {
    }

    pruningEdgeOrientation = new int[N_EDGE_ORIENTATIONS];

    // Phase 2
    pruningCornerPermutation = new int[N_CORNER_PERMUTATIONS];
    pruningEEdgePermutation = new int[N_E_EDGE_PERMUTATIONS];
    pruningUDEdgePermutation = new int[N_U_D_EDGE_PERMUTATIONS];

    // #####################
    // # Transition tables #
    // #####################

    // Phase 1
    transitCornerOrientation = new int[N_CORNER_ORIENTATIONS][moves1.length];
    for (int i = 0; i < transitCornerOrientation.length; i++) {
      byte[] state = IndexConvertor.unpackCornerOrientation(i);
      for (int j = 0; j < moves1.length; j++) {
        transitCornerOrientation[i][j] = IndexConvertor.packCornerOrientation(getOrientResult(state, moves1[j].corOrient, (byte) 3));
      }
    }

    transitEdgeOrientation = new int[N_EDGE_ORIENTATIONS][moves1.length];
    for (int i = 0; i < transitEdgeOrientation.length; i++) {
      byte[] state = IndexConvertor.unpackEdgeOrientation(i);
      for (int j = 0; j < moves1.length; j++) {
        transitEdgeOrientation[i][j] = IndexConvertor.packEdgeOrientation(getOrientResult(state, moves1[j].edgOrient, (byte) 2));
      }
    }

    // TODO : new table to keep the E edges positions (they should all be on the E layer for phase 1 to complete)

    // Phase 2
    transitCornerPermutation = new int[N_CORNER_PERMUTATIONS][moves2.length];
    for (int i = 0; i < transitCornerPermutation.length; i++) {
      byte[] state = IndexConvertor.unpackCornerPermutation(i);
      for (int j = 0; j < moves2.length; j++) {
        transitCornerPermutation[i][j] = IndexConvertor.packCornerPermutation(getPermResult(state, moves2[j].corPerm));
      }
    }

    transitEEdgePermutation = new int[N_E_EDGE_PERMUTATIONS][moves2.length];
    for (int i = 0; i < transitEEdgePermutation.length; i++) {
      byte[] state = IndexConvertor.unpackEEdgePermutation(i);
      byte[] edges = new byte[12];
      for (byte j = 0; j < edges.length; j++) {
        edges[j] = (j >= 4 && j < 8) ? state[j-4] : 0;
      }

      for (int j = 0; j < moves2.length; j++) {
        byte[] res = getPermResult(state, moves2[j].edgPerm);
        byte[] eEdges = new byte[4];
        for (int k = 0; k < eEdges.length; k++) {
          eEdges[k] = res[k + 4];
        }
        transitEEdgePermutation[i][j] = IndexConvertor.packEdgePermutation(eEdges);
      }
    }

    transitUDEdgePermutation = new int[N_U_D_EDGE_PERMUTATIONS][moves2.length];
    for (int i = 0; i < transitUDEdgePermutation.length; i++) {
      byte[] state = IndexConvertor.unpackUDEdgePermutation(i);
      byte[] edges = new byte[12];
      for (byte j = 0; j < edges.length; j++) {
        if (j < 4) {
          edges[j] = state[j];
        } else if (j >= 8) {
          edges[j] = state[j-4];
        } else {
          edges[j] = 0;
        }
      }

      for (int j = 0; j < moves2.length; j++) {
        byte[] res = getPermResult(state, moves2[j].edgPerm);
        byte[] udEdges = new byte[8];
        for (byte k = 0; k < udEdges.length; k++) {
          if (k < 4) {
            udEdges[k] = res[k];
          } else {
            udEdges[k] = res[k + 4];
          }
        }
        transitEEdgePermutation[i][j] = IndexConvertor.packEdgePermutation(udEdges);
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

  static byte[] getOrientResult(byte[] state, byte[] orientIndices, byte nDifferentValues) {
    for (int i = 0; i < state.length; i++) {
      state[i] += (orientIndices[i] % nDifferentValues);
    }
    return state;
  }

  public String[] getSolution(CubeState cubeState) {
    return null;
  }

}
