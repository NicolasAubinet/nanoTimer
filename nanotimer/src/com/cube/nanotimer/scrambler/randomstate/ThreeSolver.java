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
    U(new byte[] { 2, 3, 4, 1, 5, 6, 7 }, new byte[] { 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 2, 3, 4, 1, 5, 6, 7, 8, 9, 10, 11 },  new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    D(new byte[] { 1, 2, 3, 4, 8, 5, 6 }, new byte[] { 0, 0, 0, 0, 0, 0, 0 }, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 12, 9, 10 },  new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    R(new byte[] { 1, 2, 7, 3, 5, 6, 8 }, new byte[] { 0, 0, 2, 1, 0, 0, 1 }, new byte[] { 1, 5, 3, 4, 10, 2, 7, 8, 9, 6, 11 },  new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    L(new byte[] { 5, 1, 3, 4, 5, 2, 7 }, new byte[] { 2, 1, 0, 0, 1, 2, 0 }, new byte[] { 1, 2, 3, 7, 5, 6, 12, 4, 9, 10, 11 }, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }),
    F(new byte[] { 1, 6, 2, 4, 5, 7, 3 }, new byte[] { 0, 2, 1, 0, 0, 1, 2 }, new byte[] { 8, 2, 3, 4, 1, 6, 7, 9, 5, 10, 11 },  new byte[] { 1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0 }),
    B(new byte[] { 4, 2, 3, 8, 1, 6, 7 }, new byte[] { 1, 0, 0, 2, 2, 0, 0 }, new byte[] { 1, 2, 6, 4, 5, 11, 3, 8, 9, 10, 7 },  new byte[] { 0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1 });

    Move (byte[] corPerm, byte[] corOrient, byte[] edgPerm, byte[] edgOrient) {
      this.corPerm = corPerm;
      this.corOrient = corOrient;
      this.edgPerm = edgPerm;
      this.edgOrient = edgOrient;
    }

    byte[] corPerm;
    byte[] corOrient;
    byte[] edgPerm;
    byte[] edgOrient;
  }

  public static final int N_CORNER_PERMUTATIONS = 40320;
  public static final int N_CORNER_ORIENTATIONS = 2187;

  public static final int N_EDGE_PERMUTATIONS = 479001600;
  public static final int N_E_EDGE_PERMUTATIONS = 24;
  public static final int N_U_D_EDGE_PERMUTATIONS = 40320;
  public static final int N_EDGE_ORIENTATIONS = 2048;

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

  static class CubeState {
    public byte[] cornerPermutations = new byte[7];
    public byte[] cornerOrientations = new byte[7];
    public byte[] edgePermutations = new byte[11];
    public byte[] edgeOrientations = new byte[11];
  }

  static {
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

    Move[] moves = Move.values();
    // Phase 1
    transitCornerOrientation = new int[N_CORNER_ORIENTATIONS][moves.length];
    for (int i = 0; i < transitCornerOrientation.length; i++) {
      byte[] state = IndexConvertor.unpackCornerOrientation(i);
      for (int j = 0; j < moves.length; j++) {
        transitCornerOrientation[i][j] = IndexConvertor.packCornerOrientation(getOrientResult(state, moves[j].corOrient, (byte) 3));
      }
    }

    transitEdgeOrientation = new int[N_EDGE_ORIENTATIONS][moves.length];
    for (int i = 0; i < transitEdgeOrientation.length; i++) {
      byte[] state = IndexConvertor.unpackEdgeOrientation(i);
      for (int j = 0; j < moves.length; j++) {
        transitEdgeOrientation[i][j] = IndexConvertor.packEdgeOrientation(getOrientResult(state, moves[j].edgOrient, (byte) 2));
      }
    }

    // Phase 2
    transitCornerPermutation = new int[N_CORNER_PERMUTATIONS][moves.length];
    transitEEdgePermutation = new int[N_E_EDGE_PERMUTATIONS][moves.length];
    transitUDEdgePermutation = new int[N_U_D_EDGE_PERMUTATIONS][moves.length];
  }

  private static byte[] getPermResult(byte[] state, byte[] permIndices) {
    byte[] result = new byte[state.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = state[permIndices[i]];
    }
    return result;
  }

  private static byte[] getOrientResult(byte[] state, byte[] orientIndices, byte nDifferentValues) {
    for (int i = 0; i < state.length; i++) {
      state[i] += (orientIndices[i] % nDifferentValues);
    }
    return state;
  }

  public String[] getSolution(CubeState cubeState) {
    return null;
  }

}
