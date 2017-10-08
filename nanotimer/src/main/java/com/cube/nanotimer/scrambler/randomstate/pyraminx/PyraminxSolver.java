package com.cube.nanotimer.scrambler.randomstate.pyraminx;

import com.cube.nanotimer.scrambler.randomstate.WalterIndexMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class PyraminxSolver {
  private int minScrambleLength;

  private static final int N_TIPS_ORIENTATIONS = 81;
  private static final int N_VERTICES_ORIENTATIONS = 81;
  private static final int N_EDGES_PERMUTATIONS = 360;
  private static final int N_EDGES_ORIENTATIONS = 32;

  private static PyraminxState[] tipMoves;
  private static String[] tipMoveNames;
  private static PyraminxState[] moves;
  private static String[] moveNames;
  private static int[][] tipsOrientationMove;
  private static int[][] verticesOrientationMove;
  private static int[][] edgesPermutationMove;
  private static int[][] edgesOrientationMove;
  private static byte[] tipsOrientationDistance;
  private static byte[] verticesOrientationDistance;
  private static byte[] edgesPermutationDistance;
  private static byte[] edgesOrientationDistance;

  private boolean mustStop = false;

  public PyraminxSolver(int minScrambleLength) {
    this.minScrambleLength = minScrambleLength;
  }

  public static void genTables() {
    if (tipMoves != null) {
      return; // already initialized
    }

    PyraminxState moveu = new PyraminxState(new byte[] { 1, 0, 0, 0 }, new byte[] { 0, 0, 0, 0 }, new byte[] { 0, 1, 2, 3, 4, 5 }, new byte[] { 0, 0, 0, 0, 0, 0 });
    PyraminxState movel = new PyraminxState(new byte[] { 0, 1, 0, 0 }, new byte[] { 0, 0, 0, 0 }, new byte[] { 0, 1, 2, 3, 4, 5 }, new byte[] { 0, 0, 0, 0, 0, 0 });
    PyraminxState mover = new PyraminxState(new byte[] { 0, 0, 1, 0 }, new byte[] { 0, 0, 0, 0 }, new byte[] { 0, 1, 2, 3, 4, 5 }, new byte[] { 0, 0, 0, 0, 0, 0 });
    PyraminxState moveb = new PyraminxState(new byte[] { 0, 0, 0, 1 }, new byte[] { 0, 0, 0, 0 }, new byte[] { 0, 1, 2, 3, 4, 5 }, new byte[] { 0, 0, 0, 0, 0, 0 });

    tipMoves = new PyraminxState[] {
      moveu,
      moveu.multiply(moveu),
      movel,
      movel.multiply(movel),
      mover,
      mover.multiply(mover),
      moveb,
      moveb.multiply(moveb),
    };

    tipMoveNames = new String[] {
      "u", "u'",
      "l", "l'",
      "r", "r'",
      "b", "b'",
    };

    PyraminxState moveU = new PyraminxState(new byte[] { 1, 0, 0, 0 }, new byte[] { 1, 0, 0, 0 }, new byte[] { 2, 0, 1, 3, 4, 5 }, new byte[] { 0, 0, 0, 0, 0, 0 });
    PyraminxState moveL = new PyraminxState(new byte[] { 0, 1, 0, 0 }, new byte[] { 0, 1, 0, 0 }, new byte[] { 0, 1, 5, 3, 2, 4 }, new byte[] { 0, 0, 1, 0, 0, 1 });
    PyraminxState moveR = new PyraminxState(new byte[] { 0, 0, 1, 0 }, new byte[] { 0, 0, 1, 0 }, new byte[] { 0, 4, 2, 1, 3, 5 }, new byte[] { 0, 1, 0, 0, 1, 0 });
    PyraminxState moveB = new PyraminxState(new byte[] { 0, 0, 0, 1 }, new byte[] { 0, 0, 0, 1 }, new byte[] { 3, 1, 2, 5, 4, 0 }, new byte[] { 1, 0, 0, 1, 0, 0 });

    moves = new PyraminxState[] {
      moveU,
      moveU.multiply(moveU),
      moveL,
      moveL.multiply(moveL),
      moveR,
      moveR.multiply(moveR),
      moveB,
      moveB.multiply(moveB),
    };

    moveNames = new String[] {
      "U", "U'",
      "L", "L'",
      "R", "R'",
      "B", "B'",
    };

    // move tables
    tipsOrientationMove = new int[N_TIPS_ORIENTATIONS][tipMoveNames.length];
    for (int i = 0; i < tipsOrientationMove.length; i++) {
      PyraminxState state = new PyraminxState(WalterIndexMapping.indexToOrientation(i, 3, 4), new byte[4], new byte[6], new byte[6]);
      for (int j = 0; j < moves.length; j++) {
        tipsOrientationMove[i][j] = WalterIndexMapping.orientationToIndex(state.multiply(tipMoves[j]).tipsOrientation, 3);
      }
    }

    verticesOrientationMove = new int[N_VERTICES_ORIENTATIONS][moves.length];
    for (int i = 0; i < verticesOrientationMove.length; i++) {
      PyraminxState state = new PyraminxState(new byte[4], WalterIndexMapping.indexToOrientation(i, 3, 4), new byte[6], new byte[6]);
      for (int j = 0; j < moves.length; j++) {
        verticesOrientationMove[i][j] = WalterIndexMapping.orientationToIndex(state.multiply(moves[j]).verticesOrientation, 3);
      }
    }

    edgesPermutationMove = new int[N_EDGES_PERMUTATIONS][moves.length];
    for (int i = 0; i < edgesPermutationMove.length; i++) {
      PyraminxState state = new PyraminxState(new byte[4], new byte[4], WalterIndexMapping.indexToEvenPermutation(i, 6), new byte[6]);
      for (int j = 0; j < moves.length; j++) {
        edgesPermutationMove[i][j] = WalterIndexMapping.evenPermutationToIndex(state.multiply(moves[j]).edgesPermutation);
      }
    }

    edgesOrientationMove = new int[N_EDGES_ORIENTATIONS][moves.length];
    for (int i = 0; i < edgesOrientationMove.length; i++) {
      PyraminxState state = new PyraminxState(new byte[4], new byte[4], new byte[6], WalterIndexMapping.indexToZeroSumOrientation(i, 2, 6));
      for (int j = 0; j < moves.length; j++) {
        edgesOrientationMove[i][j] = WalterIndexMapping.zeroSumOrientationToIndex(state.multiply(moves[j]).edgesOrientation, 2);
      }
    }

    // prune tables
    tipsOrientationDistance = new byte[N_TIPS_ORIENTATIONS];
    for (int i = 0; i < tipsOrientationDistance.length; i++) {
      tipsOrientationDistance[i] = -1;
    }
    tipsOrientationDistance[0] = 0;

    int depth = 0;
    int nVisited;
    do {
      nVisited = 0;

      for (int i = 0; i < tipsOrientationDistance.length; i++) {
        if (tipsOrientationDistance[i] == depth) {
          for (int k = 0; k < tipMoves.length; k++) {
            int next = tipsOrientationMove[i][k];
            if (tipsOrientationDistance[next] < 0) {
              tipsOrientationDistance[next] = (byte) (depth + 1);
              nVisited++;
            }
          }
        }
      }

      depth++;
    } while (nVisited > 0);

    verticesOrientationDistance = new byte[N_VERTICES_ORIENTATIONS];
    for (int i = 0; i < verticesOrientationDistance.length; i++) {
      verticesOrientationDistance[i] = -1;
    }
    verticesOrientationDistance[0] = 0;

    depth = 0;
    do {
      nVisited = 0;

      for (int i = 0; i < verticesOrientationDistance.length; i++) {
        if (verticesOrientationDistance[i] == depth) {
          for (int k = 0; k < moves.length; k++) {
            int next = verticesOrientationMove[i][k];
            if (verticesOrientationDistance[next] < 0) {
              verticesOrientationDistance[next] = (byte) (depth + 1);
              nVisited++;
            }
          }
        }
      }

      depth++;
    } while (nVisited > 0);

    edgesPermutationDistance = new byte[N_EDGES_PERMUTATIONS];
    for (int i = 0; i < edgesPermutationDistance.length; i++) {
      edgesPermutationDistance[i] = -1;
    }
    edgesPermutationDistance[0] = 0;

    depth = 0;
    do {
      nVisited = 0;

      for (int i = 0; i < edgesPermutationDistance.length; i++) {
        if (edgesPermutationDistance[i] == depth) {
          for (int k = 0; k < moves.length; k++) {
            int next = edgesPermutationMove[i][k];
            if (edgesPermutationDistance[next] < 0) {
              edgesPermutationDistance[next] = (byte) (depth + 1);
              nVisited++;
            }
          }
        }
      }

      depth++;
    } while (nVisited > 0);

    edgesOrientationDistance = new byte[N_EDGES_ORIENTATIONS];
    for (int i = 0; i < edgesOrientationDistance.length; i++) {
      edgesOrientationDistance[i] = -1;
    }
    edgesOrientationDistance[0] = 0;

    depth = 0;
    do {
      nVisited = 0;

      for (int i = 0; i < edgesOrientationDistance.length; i++) {
        if (edgesOrientationDistance[i] == depth) {
          for (int k = 0; k < moves.length; k++) {
            int next = edgesOrientationMove[i][k];
            if (edgesOrientationDistance[next] < 0) {
              edgesOrientationDistance[next] = (byte) (depth + 1);
              nVisited++;
            }
          }
        }
      }

      depth++;
    } while (nVisited > 0);
  }

  private String[] solveTips(PyraminxState state) throws InterruptedException {
//        if (!initialized) {
//            genTables();
//        }

    int tipsOrientation =
      WalterIndexMapping.orientationToIndex(state.tipsOrientation, 3);

    for (int depth = 0; ; depth++) {
      ArrayList<String> solution = new ArrayList<String>();
      if (searchTips(tipsOrientation, depth, solution, -1)) {
        String[] sequence = new String[solution.size()];
        solution.toArray(sequence);

        return sequence;
      }
    }
  }

  private boolean searchTips(int tipsOrientation, int depth, ArrayList<String> solution, int lastVertex) throws InterruptedException {
    if (mustStop) {
      throw new InterruptedException("Scramble interruption requested.");
    }

    if (depth == 0) {
      return tipsOrientation == 0;
    }

    if (tipsOrientationDistance[tipsOrientation] <= depth) {
      for (int i = 0; i < tipMoves.length; i++) {
        if (i / 2 == lastVertex) {
          continue;
        }

        solution.add(tipMoveNames[i]);
        if (searchTips(
          tipsOrientationMove[tipsOrientation][i],
          depth - 1,
          solution,
          i / 2)) {
          return true;
        }
        solution.remove(solution.size() - 1);
      }
    }

    return false;
  }

  private String[] solve(PyraminxState state) throws InterruptedException {
//        if (!initialized) {
//            genTables();
//        }

    int verticesOrientation =
      WalterIndexMapping.orientationToIndex(state.verticesOrientation, 3);
    int edgesPermutation =
      WalterIndexMapping.evenPermutationToIndex(state.edgesPermutation);
    int edgesOrientation =
      WalterIndexMapping.zeroSumOrientationToIndex(state.edgesOrientation, 2);

    for (int depth = minScrambleLength; ; depth++) {
      ArrayList<String> solution = new ArrayList<String>();
      if (search(verticesOrientation, edgesPermutation, edgesOrientation, depth, solution, -1)) {
        String[] sequence = new String[solution.size()];
        solution.toArray(sequence);

        return sequence;
      }
    }
  }

  private boolean search(int verticesOrientation, int edgesPermutation, int edgesOrientation, int depth,
                         ArrayList<String> solution, int lastVertex) throws InterruptedException {
    if (depth == 0) {
      return verticesOrientation == 0 &&
        edgesPermutation == 0 &&
        edgesOrientation == 0;
    }

    if (mustStop) {
      throw new InterruptedException("Scramble interruption requested.");
    }

    if (verticesOrientationDistance[verticesOrientation] <= depth &&
      edgesPermutationDistance[edgesPermutation] <= depth &&
      edgesOrientationDistance[edgesOrientation] <= depth) {
      for (int i = 0; i < moves.length; i++) {
        if (i / 2 == lastVertex) {
          continue;
        }

        solution.add(moveNames[i]);
        if (search(
          verticesOrientationMove[verticesOrientation][i],
          edgesPermutationMove[edgesPermutation][i],
          edgesOrientationMove[edgesOrientation][i],
          depth - 1,
          solution,
          i / 2)) {
          return true;
        }
        solution.remove(solution.size() - 1);
      }
    }

    return false;
  }

  public String[] generate(PyraminxState state) {
    HashMap<String, String> inverseMoveNames = new HashMap<String, String>();
    inverseMoveNames.put("u", "u'");
    inverseMoveNames.put("u'", "u");
    inverseMoveNames.put("l", "l'");
    inverseMoveNames.put("l'", "l");
    inverseMoveNames.put("r", "r'");
    inverseMoveNames.put("r'", "r");
    inverseMoveNames.put("b", "b'");
    inverseMoveNames.put("b'", "b");
    inverseMoveNames.put("U", "U'");
    inverseMoveNames.put("U'", "U");
    inverseMoveNames.put("L", "L'");
    inverseMoveNames.put("L'", "L");
    inverseMoveNames.put("R", "R'");
    inverseMoveNames.put("R'", "R");
    inverseMoveNames.put("B", "B'");
    inverseMoveNames.put("B'", "B");

    String[] solution;
    try {
      solution = solve(state);
    } catch (InterruptedException e) {
      return null;
    }

    HashMap<String, PyraminxState> movesStringToState = new HashMap<String, PyraminxState>();
    movesStringToState.put("U", moves[0]);
    movesStringToState.put("U'", moves[1]);
    movesStringToState.put("L", moves[2]);
    movesStringToState.put("L'", moves[3]);
    movesStringToState.put("R", moves[4]);
    movesStringToState.put("R'", moves[5]);
    movesStringToState.put("B", moves[6]);
    movesStringToState.put("B'", moves[7]);

    for (String move : solution) {
      state = state.multiply(movesStringToState.get(move));
    }

    String[] tipsSolution;
    try {
      tipsSolution = solveTips(state);
    } catch (InterruptedException e) {
      return null;
    }

    String[] sequence = new String[tipsSolution.length + solution.length];
    for (int i = 0; i < solution.length; i++) {
      sequence[i] = inverseMoveNames.get(solution[solution.length - 1 - i]);
    }
    for (int i = 0; i < tipsSolution.length; i++) {
      sequence[solution.length + i] = inverseMoveNames.get(tipsSolution[tipsSolution.length - 1 - i]);
    }

    return sequence;
  }

  public PyraminxState getRandomState(Random random) {
    int tipsOrientation =
      random.nextInt(N_TIPS_ORIENTATIONS);
    int verticesOrientation =
      random.nextInt(N_VERTICES_ORIENTATIONS);
    int edgesPermutation =
      random.nextInt(N_EDGES_PERMUTATIONS);
    int edgesOrientation =
      random.nextInt(N_EDGES_ORIENTATIONS);

    return new PyraminxState(
      WalterIndexMapping.indexToOrientation(tipsOrientation, 3, 4),
      WalterIndexMapping.indexToOrientation(verticesOrientation, 3, 4),
      WalterIndexMapping.indexToEvenPermutation(edgesPermutation, 6),
      WalterIndexMapping.indexToZeroSumOrientation(edgesOrientation, 2, 6));
  }

  public void stop() {
    mustStop = true;
  }
}
