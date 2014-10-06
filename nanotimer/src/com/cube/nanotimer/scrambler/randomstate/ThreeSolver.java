package com.cube.nanotimer.scrambler.randomstate;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ThreeSolver {

  static class CubeState {
    public byte[] cornerPermutations = new byte[8];
    public byte[] cornerOrientations = new byte[8];
    public byte[] edgePermutations = new byte[12];
    public byte[] edgeOrientations = new byte[12];

    public CubeState() {
    }

    public CubeState(CubeState cubeState) {
      System.arraycopy(cubeState.cornerPermutations, 0, this.cornerPermutations, 0, 8);
      System.arraycopy(cubeState.cornerOrientations, 0, this.cornerOrientations, 0, 8);
      System.arraycopy(cubeState.edgePermutations, 0, this.edgePermutations, 0, 12);
      System.arraycopy(cubeState.edgeOrientations, 0, this.edgeOrientations, 0, 12);
    }
  }

  private static final int PHASE1_MAX_DISTANCE = 12;
  private static final int PHASE2_MAX_DISTANCE = 18;

  private CubeState initialState;
  private List<Integer> solution1;
  private List<Integer> solution2;
  private List<Integer> bestSolution1;
  private List<Integer> bestSolution2;

  static Move[] moves1;
  static Move[] moves2;
  private static int[] slices1;
  private static int[] slices2;
  private static int[] opposites;

  // Transition tables
  private static int[][] transitCornerPermutation;
  private static int[][] transitCornerOrientation;
  private static int[][] transitEEdgeCombination;
  private static int[][] transitEEdgePermutation;
  private static int[][] transitUDEdgePermutation;
  private static int[][] transitEdgeOrientation;

  // Pruning tables
  private static byte[][] pruningCornerOrientation;
  private static byte[][] pruningEdgeOrientation;
  private static byte[][] pruningCornerPermutation;
  private static byte[][] pruningUDEdgePermutation;

  static {
    // Moves
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

    // Slices
    slices1 = new int[moves1.length];
    for (int i = 0; i < moves1.length; i++) {
      slices1[i] = i / 3;
    }
    slices2 = new int[moves2.length];
    for (int i = 0; i < 6; i++) {
      slices2[i] = i / 3;
    }
    slices2[6] = 2;
    slices2[7] = 3;
    slices2[8] = 4;
    slices2[9] = 5;

    // Opposites
    opposites = new int[6];
    opposites[0] = 1;
    opposites[1] = 0;
    opposites[2] = 3;
    opposites[3] = 2;
    opposites[4] = 5;
    opposites[5] = 4;
  }

  private void phase1(int cornerOrientation, int edgeOrientation, int eEdgeCombination, int depth) {
    if (depth == 0) {
      if (cornerOrientation == 0 && edgeOrientation == 0 && eEdgeCombination == 0) {
//      if (bestSolution1.isEmpty() || solution1.size() < bestSolution1.size()) {
        bestSolution1 = new ArrayList<Integer>(solution1.size()); // TODO : these lines will pbly move somewhere in phase2
        for (Integer i : solution1) {
          bestSolution1.add(i);
        }
//      }

      /*CubeState state = new CubeState(initialState);
      applyMoves(state, solution1);

      byte[] udEdgePermutation = new byte[8];
      byte[] eEdgePermutation = new byte[4];
      for (int i = 0; i < state.edgePermutations.length; i++) {
        if (i < 4) {
          eEdgePermutation[i] = state.edgePermutations[i];
        } else {
          udEdgePermutation[i - 4] = (byte) (state.edgePermutations[i] - 4);
        }
      }
      phase2(IndexConvertor.packPermutation(state.cornerPermutations),
          IndexConvertor.packPermutation(udEdgePermutation),
          IndexConvertor.packPermutation(eEdgePermutation));*/
      }
      return;
    }

    if (pruningCornerOrientation[cornerOrientation][eEdgeCombination] <= depth && // TODO : find correct value to compare to (change '0')
        pruningEdgeOrientation[edgeOrientation][eEdgeCombination] <= depth) {
    int curSolutionSize = solution1.size();
//    if (curSolutionSize < PHASE1_MAX_DISTANCE) {
      int[] lastMoves = new int[] {
          curSolutionSize > 0 ? solution1.get(curSolutionSize - 1) : -1,
          curSolutionSize > 1 ? solution1.get(curSolutionSize - 2) : -1
      };
      for (int i = 0; i < moves1.length; i++) {
        if ((curSolutionSize > 0 && slices1[i] == slices1[lastMoves[0]]) || // same face twice in a row
            (curSolutionSize > 1 && opposites[slices1[i]] == slices1[lastMoves[0]] && opposites[slices1[lastMoves[0]]] == slices1[lastMoves[1]])) { // opposite faces 3 times in a row
          continue;
        }
        solution1.add(i);
        phase1(transitCornerOrientation[cornerOrientation][i],
            transitEdgeOrientation[edgeOrientation][i],
            transitEEdgeCombination[eEdgeCombination][i], depth - 1);
        solution1.remove(curSolutionSize);
      }
//    }
    }
  }

  private void phase2(int cornerPermutation, int udEdgePermutation, int eEdgePermutation) {
  }

  public String[] getSolution(CubeState cubeState) {
    if (transitCornerPermutation == null) {
      // TODO : gen tables when app loads, if random-state option is enabled
      getTables();
    }
    long ts = System.currentTimeMillis();

    initialState = cubeState;
    solution1 = new ArrayList<Integer>();
    solution2 = new ArrayList<Integer>();
    bestSolution1 = new ArrayList<Integer>();
    bestSolution2 = new ArrayList<Integer>();

    int cornerOrientation = IndexConvertor.packOrientation(cubeState.cornerOrientations, 3);
    int edgeOrientation = IndexConvertor.packOrientation(cubeState.edgeOrientations, 2);
    boolean[] combinations = new boolean[cubeState.edgePermutations.length];
    for (int i = 0; i < cubeState.edgePermutations.length; i++) {
      combinations[i] = (cubeState.edgePermutations[i] <= 4);
    }
    int eEdgeCombination = IndexConvertor.packCombination(combinations, 4);

    for (int i = 0; i < PHASE1_MAX_DISTANCE; i++) {
      phase1(cornerOrientation, edgeOrientation, eEdgeCombination, i);
      if (!bestSolution1.isEmpty()) { // TODO : tmp for tests
        break;
      }
    }

    String[] solution = new String[bestSolution1.size() + bestSolution2.size()];
    int i = 0;
    for (Integer m : bestSolution1) {
      solution[i++] = moves1[m].name;
    }
    for (Integer m : bestSolution2) {
      solution[i++] = moves2[m].name;
    }
    Log.i("[NanoTimer]", "solution time: " + (System.currentTimeMillis() - ts));
    return solution;
  }

  private void getTables() {
    StateTables.generateTables(moves1, moves2);
    transitCornerPermutation = StateTables.transitCornerPermutation;
    transitCornerOrientation = StateTables.transitCornerOrientation;
    transitEEdgeCombination = StateTables.transitEEdgeCombination;
    transitEEdgePermutation = StateTables.transitEEdgePermutation;
    transitUDEdgePermutation = StateTables.transitUDEdgePermutation;
    transitEdgeOrientation = StateTables.transitEdgeOrientation;

    pruningCornerOrientation = StateTables.pruningCornerOrientation;
    pruningEdgeOrientation = StateTables.pruningEdgeOrientation;
    pruningCornerPermutation = StateTables.pruningCornerPermutation;
    pruningUDEdgePermutation = StateTables.pruningUDEdgePermutation;
  }

  private void applyMoves(CubeState state, List<Integer> moves) {
    for (Integer m : moves) {
      // TODO : handle moves2
      Move move = moves1[m];
      state.edgePermutations = StateTables.getPermResult(state.edgePermutations, move.edgPerm);
      state.cornerPermutations = StateTables.getPermResult(state.cornerPermutations, move.corPerm);
      state.edgeOrientations = StateTables.getOrientResult(state.edgeOrientations, move.edgPerm, move.edgOrient, 2);
      state.cornerOrientations = StateTables.getOrientResult(state.cornerOrientations, move.corPerm, move.corOrient, 3);
    }
  }

}
