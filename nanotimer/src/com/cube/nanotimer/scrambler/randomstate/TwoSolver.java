package com.cube.nanotimer.scrambler.randomstate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TwoSolver {

  // Cubies numbering (DBL is considered as solved):
  //
  //     U          D
  // #########  #########
  // # 0 # 3 #  #   # 6 #
  // #########  #########
  // # 1 # 2 #  # 4 # 5 #
  // #########  #########

  static class CubeState {

    public byte[] permutations = new byte[7];
    public byte[] orientations = new byte[7];

    public CubeState() {
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Corner permutations: ").append(Arrays.toString(permutations)).append("\n");
      sb.append("Corner orientations: ").append(Arrays.toString(orientations)).append("\n");
      return sb.toString();
    }
  }

  enum Move {
    U("U", new byte[] { 1, 2, 3, 0, 4, 5, 6 }, new byte[] { 0, 0, 0, 0, 0, 0, 0 }),
    UP("U'", new byte[] { }, new byte[] { }),
    U2("U2", new byte[] { }, new byte[] { }),
    R("R", new byte[] { 0, 1, 5, 2, 4, 6, 3 }, new byte[] { 0, 0, 2, 1, 0, 1, 2 }),
    RP("R'", new byte[] { }, new byte[] { }),
    R2("R2", new byte[] { }, new byte[] { }),
    F("F", new byte[] { 0, 4, 1, 3, 5, 2, 6 }, new byte[] { 0, 2, 1, 0, 1, 2, 0 }),
    FP("F'", new byte[] { }, new byte[] { }),
    F2("F2", new byte[] { }, new byte[] { });

    Move(String name, byte[] corPerm, byte[] corOrient) {
      this.name = name;
      this.corPerm = corPerm;
      this.corOrient = corOrient;
    }

    String name;
    byte[] corPerm;
    byte[] corOrient;
  }

  public static final int N_PERM = 5040;
  public static final int N_ORIENT = 729;

  private static final int SEARCH_TIME_MIN = 100; // time in ms during which to search for a better solution
  private static final int DEFAULT_MAX_SOLUTION_LENGTH = 11;

  private List<Byte> solution;
  private List<Byte> bestSolution;
  private long searchStartTs;
  private int maxSolutionLength;

  private final Object solutionSyncHelper = new Object();
  private int solutionSearchCount = 0;

  private static Move[] moves;
  private static Move[] allMoves;
  private static byte[] slices;

  // Transition tables
  static short[][] transitPerm;
  static short[][] transitOrient;

  // Pruning tables
  static byte[] pruningPerm;
  static byte[] pruningOrient;

  static {
    // Moves
    moves = new Move[] {
        Move.U, Move.R, Move.F
    };

    allMoves = new Move[] {
        Move.U, Move.U2, Move.UP,
        Move.R, Move.R2, Move.RP,
        Move.F, Move.F2, Move.FP,
    };

    // Slices
    slices = new byte[allMoves.length];
    for (int i = 0; i < allMoves.length; i++) {
      slices[i] = (byte) (i / 3);
    }
  }

  private boolean search(int perm, int orient, int depth, byte lastMove) {
    boolean foundSolution = false;
    if (depth == 0) {
      if (orient == 0 && perm == 0) {
        if (bestSolution == null || solution.size() < bestSolution.size()) {
          bestSolution = new ArrayList<Byte>(solution.size());
          for (Byte m : solution) {
            bestSolution.add(m);
          }
        }
        foundSolution = true;
      }
      return foundSolution;
    }
    if (bestSolution != null && System.currentTimeMillis() > searchStartTs + SEARCH_TIME_MIN) {
      return false;
    }

    if (pruningPerm[perm] <= depth && pruningOrient[orient] <= depth) {
      int curSolutionSize = solution.size();
      for (byte i = 0; i < moves.length; i++) {
        if (lastMove >= 0 && i == slices[lastMove]) { // same face twice in a row
          continue;
        }
        int corPerm = perm;
        int corOri = orient;
        for (int j = 0; j < 3; j++) {
          corPerm = transitPerm[corPerm][i];
          corOri = transitOrient[corOri][i];
          byte nextMove = (byte) (i * 3 + j);
          solution.add(nextMove);
          foundSolution |= search(corPerm, corOri, depth - 1, nextMove);
          solution.remove(curSolutionSize);
        }
      }
    }
    return foundSolution;
  }

  public String[] getSolution(CubeState cubeState) {
    return getSolution(cubeState, null);
  }

  public String[] getSolution(CubeState cubeState, ScrambleConfig config) {
    synchronized (solutionSyncHelper) {
      solutionSearchCount++;
      if (transitPerm == null) {
        genTables();
      }
    }
    if (config != null && config.getMaxLength() > 0) {
      maxSolutionLength = config.getMaxLength();
    } else {
      maxSolutionLength = DEFAULT_MAX_SOLUTION_LENGTH;
    }
    searchStartTs = System.currentTimeMillis();

    solution = new ArrayList<Byte>();
    bestSolution = null;

    int cornerPermutation = IndexConvertor.packPermutation(cubeState.permutations);
    int cornerOrientation = IndexConvertor.packOrientation(cubeState.orientations, 3);

    for (int i = 0; bestSolution == null || System.currentTimeMillis() < searchStartTs + SEARCH_TIME_MIN || bestSolution.size() > maxSolutionLength; i++) {
      if (search(cornerPermutation, cornerOrientation, i, (byte) -1)) {
        solution = new ArrayList<Byte>();
      }
    }

    String[] solution = null;
    if (bestSolution != null) {
      solution = new String[bestSolution.size()];
      int i = 0;
      for (Byte m : bestSolution) {
        solution[i++] = allMoves[m].name;
      }
    }
//    Log.i("[NanoTimer]", "solution time: " + (System.currentTimeMillis() - searchStartTs));

    synchronized (solutionSyncHelper) {
      solutionSearchCount--;
      solutionSyncHelper.notify();
    }

    return solution;
  }

  public static void genTables() {
    if (transitPerm != null) {
      return;
    }
    byte[] state7 = new byte[7];

    transitPerm = new short[N_PERM][moves.length];
    for (int i = 0; i < transitPerm.length; i++) {
      IndexConvertor.unpackPermutation(i, state7);
      for (int j = 0; j < moves.length; j++) {
        transitPerm[i][j] = (short) IndexConvertor.packPermMult(state7, moves[j].corPerm);
      }
    }

    transitOrient = new short[N_ORIENT][moves.length];
    for (int i = 0; i < transitOrient.length; i++) {
      IndexConvertor.unpackOrientation(i, state7, (byte) 3);
      for (int j = 0; j < moves.length; j++) {
        transitOrient[i][j] = (short) IndexConvertor.packOrientMult(state7, moves[j].corPerm, moves[j].corOrient, 3);
      }
    }

    pruningPerm = new byte[N_PERM];
    genPruning(pruningPerm, transitPerm);
    pruningOrient = new byte[N_ORIENT];
    genPruning(pruningOrient, transitOrient);
  }

  private static void genPruning(byte[] pruningTable, short[][] transit) {
    for (int i = 0; i < transit.length; i++) {
      pruningTable[i] = -1;
    }
    pruningTable[0] = 0;
    int done = 1;
    byte distance = 0;
    while (done < pruningTable.length) {
      for (int i = 0; i < transit.length; i++) {
        if (pruningTable[i] == distance) {
          for (int j = 0; j < moves.length; j++) {
            int res = i;
            for (int k = 0; k < 3; k++) {
              res = transit[res][j];
              if (pruningTable[res] < 0) {
                pruningTable[res] = (byte) (distance + 1);
                done++;
              }
            }
          }
        }
      }
      distance++;
    }
  }

  public void freeMemory() {
    synchronized (solutionSyncHelper) {
      while (solutionSearchCount > 0) {
        try {
          solutionSyncHelper.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      transitPerm = null;
      transitOrient = null;
      pruningOrient = null;
      pruningPerm = null;
    }
  }

}
