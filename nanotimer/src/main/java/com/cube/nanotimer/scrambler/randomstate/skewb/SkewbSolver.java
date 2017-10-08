package com.cube.nanotimer.scrambler.randomstate.skewb;

import android.util.Log;
import com.cube.nanotimer.scrambler.randomstate.WalterIndexMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SkewbSolver {
  private static final int N_FACES_PERMUTATIONS = 360;
  private static final int N_FREE_CORNERS_PERMUTATION = 12;
  private static final int N_FREE_CORNERS_ORIENTATION = 27;
  private static final int N_FIXED_CORNERS_ORIENTATION = 81;

  private static SkewbState[] moves;
  private static int[][] facesPermutationMove;
  private static int[][] freeCornersPermutationMove;
  private static int[][] freeCornersOrientationMove;
  private static int[][] fixedCornersOrientationMove;
  private static int[][][][] distance;

  public SkewbSolver() {
  }

  public static void genTables() {
    if (moves != null) {
      return; // already initialized
    }

    long startTs = System.currentTimeMillis();

    // moves
    SkewbState moveL = new SkewbState(new byte[] { 1, 4, 2, 3, 0, 5 }, new byte[] { 2, 0, 1, 3 }, new byte[] { 2, 2, 2, 0 }, new byte[] { 1, 0, 0, 0 });
    SkewbState moveR = new SkewbState(new byte[] { 3, 1, 0, 2, 4, 5 }, new byte[] { 1, 3, 2, 0 }, new byte[] { 2, 2, 0, 2 }, new byte[] { 0, 1, 0, 0 });
    SkewbState moveD = new SkewbState(new byte[] { 0, 1, 2, 4, 5, 3 }, new byte[] { 0, 2, 3, 1 }, new byte[] { 0, 2, 2, 2 }, new byte[] { 0, 0, 0, 1 });
    SkewbState moveB = new SkewbState(new byte[] { 0, 2, 5, 3, 4, 1 }, new byte[] { 3, 1, 0, 2 }, new byte[] { 2, 0, 2, 2 }, new byte[] { 0, 0, 1, 0 });

    moves = new SkewbState[] {
      moveL,
      moveL.multiply(moveL),
      moveR,
      moveR.multiply(moveR),
      moveD,
      moveD.multiply(moveD),
      moveB,
      moveB.multiply(moveB),
    };

    long ts = System.currentTimeMillis();

    // move tables
    facesPermutationMove = new int[N_FACES_PERMUTATIONS][moves.length];
    for (int i = 0; i < facesPermutationMove.length; i++) {
      SkewbState state = new SkewbState(
        WalterIndexMapping.indexToEvenPermutation(i, 6),
        new byte[4],
        new byte[4],
        new byte[4]);
      for (int j = 0; j < moves.length; j++) {
        facesPermutationMove[i][j] =
          WalterIndexMapping.evenPermutationToIndex(
            state.multiply(moves[j]).facesPermutation);
      }
    }

    freeCornersPermutationMove = new int[N_FREE_CORNERS_PERMUTATION][moves.length];
    for (int i = 0; i < freeCornersPermutationMove.length; i++) {
      SkewbState state = new SkewbState(
        new byte[6],
        WalterIndexMapping.indexToEvenPermutation(i, 4),
        new byte[4],
        new byte[4]);
      for (int j = 0; j < moves.length; j++) {
        freeCornersPermutationMove[i][j] =
          WalterIndexMapping.evenPermutationToIndex(
            state.multiply(moves[j]).freeCornersPermutation);
      }
    }

    freeCornersOrientationMove = new int[N_FREE_CORNERS_ORIENTATION][moves.length];
    for (int i = 0; i < freeCornersOrientationMove.length; i++) {
      SkewbState state = new SkewbState(
        new byte[6],
        new byte[4],
        WalterIndexMapping.indexToZeroSumOrientation(i, 3, 4),
        new byte[4]);
      for (int j = 0; j < moves.length; j++) {
        freeCornersOrientationMove[i][j] =
          WalterIndexMapping.zeroSumOrientationToIndex(
            state.multiply(moves[j]).freeCornersOrientation, 3);
      }
    }

    fixedCornersOrientationMove = new int[N_FIXED_CORNERS_ORIENTATION][moves.length];
    for (int i = 0; i < fixedCornersOrientationMove.length; i++) {
      SkewbState state = new SkewbState(
        new byte[6],
        new byte[4],
        new byte[4],
        WalterIndexMapping.indexToOrientation(i, 3, 4));
      for (int j = 0; j < moves.length; j++) {
        fixedCornersOrientationMove[i][j] =
          WalterIndexMapping.orientationToIndex(
            state.multiply(moves[j]).fixedCornersOrientation, 3);
      }
    }

    logTimeDifference(ts, "skewb move tables");

    ts = System.currentTimeMillis();

    // distance table
    distance = new int[N_FACES_PERMUTATIONS]
      [N_FREE_CORNERS_PERMUTATION]
      [N_FREE_CORNERS_ORIENTATION]
      [N_FIXED_CORNERS_ORIENTATION];
    for (int i = 0; i < distance.length; i++) {
      for (int j = 0; j < distance[i].length; j++) {
        for (int k = 0; k < distance[i][j].length; k++) {
          for (int m = 0; m < distance[i][j][k].length; m++) {
            distance[i][j][k][m] = -1;
          }
        }
      }
    }

    distance[0][0][0][0] = 0;

    int nVisited;
    int depth = 0;
    do {
      nVisited = 0;

      for (int i = 0; i < distance.length; i++) {
        for (int j = 0; j < distance[i].length; j++) {
          for (int k = 0; k < distance[i][j].length; k++) {
            for (int m = 0; m < distance[i][j][k].length; m++) {
              if (distance[i][j][k][m] == depth) {
                for (int moveIndex = 0; moveIndex < moves.length; moveIndex++) {
                  int nextFacesPermutation = facesPermutationMove[i][moveIndex];
                  int nextFreeCornersPemutation = freeCornersPermutationMove[j][moveIndex];
                  int nextFreeCornersOrientation = freeCornersOrientationMove[k][moveIndex];
                  int nextFixedCornersOrientation = fixedCornersOrientationMove[m][moveIndex];

                  if (distance[nextFacesPermutation]
                    [nextFreeCornersPemutation]
                    [nextFreeCornersOrientation]
                    [nextFixedCornersOrientation] == -1) {
                    distance[nextFacesPermutation]
                      [nextFreeCornersPemutation]
                      [nextFreeCornersOrientation]
                      [nextFixedCornersOrientation] = depth + 1;
                    nVisited++;
                  }
                }
              }
            }
          }
        }
      }

      depth++;
    } while (nVisited > 0);

    logTimeDifference(ts, "skewb distance table");

    logTimeDifference(startTs, "Skewb tables generation");
  }

  public String[] solve(SkewbState state) {
    String[] moveNames = { "L", "L'", "R", "R'", "D", "D'", "B", "B'" };

    ArrayList<String> sequence = new ArrayList<String>();

    int facesPermutation =
      WalterIndexMapping.evenPermutationToIndex(
        state.facesPermutation);
    int freeCornersPermutation =
      WalterIndexMapping.evenPermutationToIndex(
        state.freeCornersPermutation);
    int freeCornersOrientation =
      WalterIndexMapping.zeroSumOrientationToIndex(
        state.freeCornersOrientation, 3);
    int fixedCornersOrientation =
      WalterIndexMapping.orientationToIndex(
        state.fixedCornersOrientation, 3);

    for (; ; ) {
      if (distance[facesPermutation]
        [freeCornersPermutation]
        [freeCornersOrientation]
        [fixedCornersOrientation] == 0) {
        break;
      }

      for (int k = 0; k < moves.length; k++) {
        int nextFacesPermutation = facesPermutationMove[facesPermutation][k];
        int nextFreeCornersPemutation = freeCornersPermutationMove[freeCornersPermutation][k];
        int nextFreeCornersOrientation = freeCornersOrientationMove[freeCornersOrientation][k];
        int nextFixedCornersOrientation = fixedCornersOrientationMove[fixedCornersOrientation][k];

        if (distance[nextFacesPermutation]
          [nextFreeCornersPemutation]
          [nextFreeCornersOrientation]
          [nextFixedCornersOrientation] ==
          distance[facesPermutation]
            [freeCornersPermutation]
            [freeCornersOrientation]
            [fixedCornersOrientation] - 1) {
          sequence.add(moveNames[k]);
          facesPermutation = nextFacesPermutation;
          freeCornersPermutation = nextFreeCornersPemutation;
          freeCornersOrientation = nextFreeCornersOrientation;
          fixedCornersOrientation = nextFixedCornersOrientation;
          break;
        }
      }
    }

    String[] sequenceArray = new String[sequence.size()];
    sequence.toArray(sequenceArray);

    return sequenceArray;
  }

  public String[] generate(SkewbState state) {
    String[] solution = solve(state);

    HashMap<String, String> inverseMoves = new HashMap<String, String>();
    inverseMoves.put("L", "L'");
    inverseMoves.put("L'", "L");
    inverseMoves.put("R", "R'");
    inverseMoves.put("R'", "R");
    inverseMoves.put("D", "D'");
    inverseMoves.put("D'", "D");
    inverseMoves.put("B", "B'");
    inverseMoves.put("B'", "B");

    String[] sequence = new String[solution.length];
    for (int i = 0; i < sequence.length; i++) {
      sequence[i] = inverseMoves.get(solution[solution.length - 1 - i]);
    }

    return sequence;
  }

  public SkewbState getRandomState(Random random) {
    for (; ; ) {
      int indexFacesPermutation =
        random.nextInt(N_FACES_PERMUTATIONS);
      int indexFreeCornersPermutation =
        random.nextInt(N_FREE_CORNERS_PERMUTATION);
      int indexFreeCornersOrientation =
        random.nextInt(N_FREE_CORNERS_ORIENTATION);
      int indexFixedCornersOrientation =
        random.nextInt(N_FIXED_CORNERS_ORIENTATION);

      if (distance[indexFacesPermutation]
        [indexFreeCornersPermutation]
        [indexFreeCornersOrientation]
        [indexFixedCornersOrientation] == -1) {
        continue;
      }

      return new SkewbState(
        WalterIndexMapping.indexToEvenPermutation(indexFacesPermutation, 6),
        WalterIndexMapping.indexToEvenPermutation(indexFreeCornersPermutation, 4),
        WalterIndexMapping.indexToZeroSumOrientation(indexFreeCornersOrientation, 3, 4),
        WalterIndexMapping.indexToOrientation(indexFixedCornersOrientation, 3, 4));
    }
  }

  public void stop() {
    // no need to cancel (fast enough)
  }

  private static void logTimeDifference(long startTs, String msg) {
    Log.i("[NanoTimer]", msg + ": " + (System.currentTimeMillis() - startTs));
  }
}
