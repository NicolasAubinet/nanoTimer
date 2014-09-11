package com.cube.nanotimer.scrambler.randomstate;

public class ThreeSolver {

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

  private static String[][] moves1; // phase 1 moves
  private static String[][] moves2; // phase 2 moves

  static class CubeState {
    public byte[] cornerPermutations = new byte[7];
    public byte[] cornerOrientations = new byte[7];
    public byte[] edgePermutations = new byte[11];
    public byte[] edgeOrientations = new byte[11];
  }

  static {
    moves1 = new String[][] {
        { "U", "U2", "U'" },
        { "D", "D2", "D'" },
        { "R", "R2", "R'" },
        { "L", "L2", "L'" },
        { "F", "F2", "F'" },
        { "B", "B2", "B'" },
    };

    moves2 = new String[][] {
        { "U", "U2", "U'" },
        { "D", "D2", "D'" },
        { "R2" },
        { "L2" },
        { "F2" },
        { "B2" },
    };

    // ###########
    // # Pruning #
    // ###########

    // Phase 1
    pruningCornerOrientation = new int[N_CORNER_ORIENTATIONS];
    for (int i = 0; i < pruningCornerOrientation.length; i++) {
    }

    pruningEdgeOrientation = new int[N_EDGE_ORIENTATIONS];

    // Phase 2
    pruningCornerPermutation = new int[N_CORNER_PERMUTATIONS];
    pruningEEdgePermutation = new int[N_E_EDGE_PERMUTATIONS];
    pruningUDEdgePermutation = new int[N_U_D_EDGE_PERMUTATIONS];

    // ##############
    // # Transition #
    // ##############

    // Phase 1
    transitCornerOrientation = new int[N_CORNER_ORIENTATIONS][moves1.length]; // TODO : see if best to have moves1 size (18) or to just have the first or each move (size 6) and apply moves twice or trice to do "2" and "'"
    transitEdgeOrientation = new int[N_EDGE_ORIENTATIONS][moves1.length];

    // Phase 2
    transitCornerPermutation = new int[N_CORNER_PERMUTATIONS][moves2.length];
    transitEEdgePermutation = new int[N_E_EDGE_PERMUTATIONS][moves2.length];
    transitUDEdgePermutation = new int[N_U_D_EDGE_PERMUTATIONS][moves2.length];
  }

  public String[] getSolution(CubeState cubeState) {
    return null;
  }

}
