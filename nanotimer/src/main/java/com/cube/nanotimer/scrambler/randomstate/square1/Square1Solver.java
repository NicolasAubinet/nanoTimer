package com.cube.nanotimer.scrambler.randomstate.square1;

import android.util.Log;
import com.cube.nanotimer.scrambler.randomstate.WalterIndexMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Square1Solver {

  // phase 1
  private static ArrayList<Square1State> shapes;
  private static HashMap<Integer, Integer> evenShapeDistance;
  private static HashMap<Integer, Integer> oddShapeDistance;

  // phase 2
  public static final int N_CORNERS_PERMUTATIONS = 40320;
  public static final int N_EDGES_PERMUTATIONS = 40320;
  public static final int N_CORNERS_COMBINATIONS = 70;
  public static final int N_EDGES_COMBINATIONS = 70;

  private static Square1State[] moves1;
  private static Square1CubeState[] moves2;
  private static int[][] cornersPermutationMove;
  private static int[][] cornersCombinationMove;
  private static int[][] edgesPermutationMove;
  private static int[][] edgesCombinationMove;
  private static byte[][] cornersDistance;
  private static byte[][] edgesDistance;

  private volatile boolean mustStop = false;
  private static final Object solutionSyncHelper = new Object();
  private static volatile int solutionSearchCount = 0;

  private static final int MAX_DEPTH = 17;

  public static void genTables() {
    if (isInitialised()) {
      return; // already initialized
    }

    long genTablesStartTs = System.currentTimeMillis();

    // -- phase 1 --

    // moves
    moves1 = new Square1State[23];

    Square1State move10 = new Square1State(new byte[] {
      11,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10,
      12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
    });

    Square1State move = move10;
    for (int i = 0; i < 11; i++) {
      moves1[i] = move;
      move = move.multiply(move10);
    }

    Square1State move01 = new Square1State(new byte[] {
      0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11,
      13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 12,
    });

    move = move01;
    for (int i = 0; i < 11; i++) {
      moves1[11 + i] = move;
      move = move.multiply(move01);
    }

    Square1State moveTwist = new Square1State(new byte[] {
      0,  1, 19, 18, 17, 16, 15, 14,  8,  9, 10, 11,
      12, 13,  7,  6,  5,  4,  3,  2, 20, 21, 22, 23,
    });

    moves1[22] = moveTwist;

    // shape tables

    Thread shapesThread = null;
    if (shapes == null || evenShapeDistance == null || oddShapeDistance == null) {
      shapesThread = new Thread() {
        @Override
        public void run() {
          long ts = System.currentTimeMillis();
          shapes = new ArrayList<Square1State>();

          evenShapeDistance = new HashMap<Integer, Integer>();
          oddShapeDistance = new HashMap<Integer, Integer>();
          evenShapeDistance.put(Square1State.id.getShapeIndex(), 0);

          ArrayList<Square1State> fringe = new ArrayList<Square1State>();
          fringe.add(Square1State.id);

          int depth = 0;

          while (fringe.size() > 0) {
            ArrayList<Square1State> newFringe = new ArrayList<Square1State>();
            for (Square1State state : fringe) {
              if (state.isTwistable()) {
                shapes.add(state);
              }

              for (int i = 0; i < moves1.length; i++) {
                if (i == 22 && !state.isTwistable()) {
                  continue;
                }

                Square1State next = state.multiply(moves1[i]);

                HashMap<Integer, Integer> distanceTable =
                  isEvenPermutation(next.getPiecesPermutation()) ?
                    evenShapeDistance : oddShapeDistance;

                if (!distanceTable.containsKey(next.getShapeIndex())) {
                  distanceTable.put(next.getShapeIndex(), depth + 1);
                  newFringe.add(next);
                }
              }
            }

            fringe = newFringe;
            depth++;
          }
          logTimeDifference(ts, "shapes thread");
        }
      };
      shapesThread.start();
    }

    // -- phase 2 --

    // moves
    final Square1CubeState move30 =          new Square1CubeState(new byte[] { 3, 0, 1, 2, 4, 5, 6, 7 }, new byte[] { 3, 0, 1, 2, 4, 5, 6, 7 });
    final Square1CubeState move03 =          new Square1CubeState(new byte[] { 0, 1, 2, 3, 5, 6, 7, 4 }, new byte[] { 0, 1, 2, 3, 5, 6, 7, 4 });
    final Square1CubeState moveTwistTop =    new Square1CubeState(new byte[] { 0, 6, 5, 3, 4, 2, 1, 7 }, new byte[] { 6, 5, 2, 3, 4, 1, 0, 7 });
    final Square1CubeState moveTwistBottom = new Square1CubeState(new byte[] { 0, 6, 5, 3, 4, 2, 1, 7 }, new byte[] { 0, 5, 4, 3, 2, 1, 6, 7 });

    moves2 = new Square1CubeState[] {
      move30,
      move30.multiply(move30),
      move30.multiply(move30).multiply(move30),
      move03,
      move03.multiply(move03),
      move03.multiply(move03).multiply(move03),
      moveTwistTop,
      moveTwistBottom,
    };

    // move tables
    Thread cornersPermThread = new Thread() {
      @Override
      public void run() {
        long ts = System.currentTimeMillis();
        cornersPermutationMove = new int[N_CORNERS_PERMUTATIONS][moves2.length];
        for (int i = 0; i < cornersPermutationMove.length; i++) {
          Square1CubeState state = new Square1CubeState(WalterIndexMapping.indexToPermutation(i, 8), new byte[8]);
          for (int j = 0; j < cornersPermutationMove[i].length; j++) {
            cornersPermutationMove[i][j] =
              WalterIndexMapping.permutationToIndex(
                state.multiply(moves2[j]).cornersPermutation);
          }
        }
        logTimeDifference(ts, "corners perm");
      }
    };
    cornersPermThread.start();

    Thread cornersCombThread = new Thread() {
      @Override
      public void run() {
        long ts = System.currentTimeMillis();
        cornersCombinationMove = new int[N_CORNERS_COMBINATIONS][moves2.length];
        for (int i = 0; i < cornersCombinationMove.length; i++) {
          boolean[] combination = WalterIndexMapping.indexToCombination(i, 4, 8);

          byte[] corners = new byte[8];
          byte nextTop = 0;
          byte nextBottom = 4;

          for (int j = 0; j < corners.length; j++) {
            if (combination[j]) {
              corners[j] = nextTop++;
            } else {
              corners[j] = nextBottom++;
            }
          }

          Square1CubeState state = new Square1CubeState(corners, new byte[8]);
          for (int j = 0; j < cornersCombinationMove[i].length; j++) {
            Square1CubeState result = state.multiply(moves2[j]);

            boolean[] isTopCorner = new boolean[8];
            for (int k = 0; k < isTopCorner.length; k++) {
              isTopCorner[k] = result.cornersPermutation[k] < 4;
            }

            cornersCombinationMove[i][j] =
              WalterIndexMapping.combinationToIndex(isTopCorner, 4);
          }
        }
        logTimeDifference(ts, "corners comb");
      }
    };
    cornersCombThread.start();

    Thread edgesPermThread = new Thread() {
      @Override
      public void run() {
        long ts = System.currentTimeMillis();
        edgesPermutationMove = new int[N_EDGES_PERMUTATIONS][moves2.length];
        for (int i = 0; i < edgesPermutationMove.length; i++) {
          Square1CubeState state = new Square1CubeState(new byte[8], WalterIndexMapping.indexToPermutation(i, 8));
          for (int j = 0; j < edgesPermutationMove[i].length; j++) {
            edgesPermutationMove[i][j] =
              WalterIndexMapping.permutationToIndex(
                state.multiply(moves2[j]).edgesPermutation);
          }
        }
        logTimeDifference(ts, "edges perm");
      }
    };
    edgesPermThread.start();

    Thread edgesCombThread = new Thread() {
      @Override
      public void run() {
        long ts = System.currentTimeMillis();
        edgesCombinationMove = new int[N_EDGES_COMBINATIONS][moves2.length];
        for (int i = 0; i < edgesCombinationMove.length; i++) {
          boolean[] combination = WalterIndexMapping.indexToCombination(i, 4, 8);

          byte[] edges = new byte[8];
          byte nextTop = 0;
          byte nextBottom = 4;

          for (int j = 0; j < edges.length; j++) {
            if (combination[j]) {
              edges[j] = nextTop++;
            } else {
              edges[j] = nextBottom++;
            }
          }

          Square1CubeState state = new Square1CubeState(new byte[8], edges);
          for (int j = 0; j < edgesCombinationMove[i].length; j++) {
            Square1CubeState result = state.multiply(moves2[j]);

            boolean[] isTopEdge = new boolean[8];
            for (int k = 0; k < isTopEdge.length; k++) {
              isTopEdge[k] = result.edgesPermutation[k] < 4;
            }

            edgesCombinationMove[i][j] =
              WalterIndexMapping.combinationToIndex(isTopEdge, 4);
          }
        }
        logTimeDifference(ts, "edges comb");
      }
    };
    edgesCombThread.start();

    try {
      edgesCombThread.join();
      edgesPermThread.join();
      cornersCombThread.join();
      cornersPermThread.join();
    } catch (InterruptedException e) {
      Log.e("[NanoTimer]", "Square-1 transit tables generation interrupted");
    }

    // prune tables

    Thread cornersDistanceThread = null;
    if (cornersDistance == null) {
      cornersDistanceThread = new Thread() {
        @Override
        public void run() {
          long ts = System.currentTimeMillis();
          cornersDistance = new byte[N_CORNERS_PERMUTATIONS][N_EDGES_COMBINATIONS];
          for (int i = 0; i < cornersDistance.length; i++) {
            for (int j = 0; j < cornersDistance[i].length; j++) {
              cornersDistance[i][j] = -1;
            }
          }
          cornersDistance[0][0] = 0;

          int depth = 0;
          int nVisited;
          do {
            nVisited = 0;

            for (int i = 0; i < cornersDistance.length; i++) {
              for (int j = 0; j < cornersDistance[i].length; j++) {
                if (cornersDistance[i][j] == depth) {
                  for (int k = 0; k < moves2.length; k++) {
                    int nextCornerPermutation = cornersPermutationMove[i][k];
                    int nextEdgeCombination = edgesCombinationMove[j][k];
                    if (cornersDistance[nextCornerPermutation][nextEdgeCombination] < 0) {
                      cornersDistance[nextCornerPermutation][nextEdgeCombination] = (byte) (depth + 1);
                      nVisited++;
                    }
                  }
                }
              }
            }

            depth++;
          } while (nVisited > 0);
          logTimeDifference(ts, "corners distance");
        }
      };
      cornersDistanceThread.start();
    }

    Thread edgesDistanceThread = null;
    if (edgesDistance == null) {
      edgesDistanceThread = new Thread() {
        @Override
        public void run() {
          long ts = System.currentTimeMillis();
          edgesDistance = new byte[N_EDGES_PERMUTATIONS][N_CORNERS_COMBINATIONS];
          for (int i = 0; i < edgesDistance.length; i++) {
            for (int j = 0; j < edgesDistance[i].length; j++) {
              edgesDistance[i][j] = -1;
            }
          }
          edgesDistance[0][0] = 0;

          int depth = 0;
          int nVisited;
          do {
            nVisited = 0;

            for (int i = 0; i < edgesDistance.length; i++) {
              for (int j = 0; j < edgesDistance[i].length; j++) {
                if (edgesDistance[i][j] == depth) {
                  for (int k = 0; k < moves2.length; k++) {
                    int nextEdgesPermutation = edgesPermutationMove[i][k];
                    int nextCornersCombination = cornersCombinationMove[j][k];
                    if (edgesDistance[nextEdgesPermutation][nextCornersCombination] < 0) {
                      edgesDistance[nextEdgesPermutation][nextCornersCombination] = (byte) (depth + 1);
                      nVisited++;
                    }
                  }
                }
              }
            }

            depth++;
          } while (nVisited > 0);
          logTimeDifference(ts, "edges distance");
        }
      };
      edgesDistanceThread.start();
    }

    try {
      if (shapesThread != null) {
        shapesThread.join();
      }
      if (cornersDistanceThread != null) {
        cornersDistanceThread.join();
      }
      if (edgesDistanceThread != null) {
        edgesDistanceThread.join();
      }
    } catch (InterruptedException e) {
      Log.e("[NanoTimer]", "Square-1 pruning tables generation interrupted");
    }

    logTimeDifference(genTablesStartTs, "Total square 1 tables generation");
  }

  private static boolean isEvenPermutation(byte[] permutation) {
    int nInversions = 0;
    for (int i = 0; i < permutation.length; i++) {
      for (int j = i + 1; j < permutation.length; j++) {
        if (permutation[i] > permutation[j]) {
          nInversions++;
        }
      }
    }

    return nInversions % 2 == 0;
  }

  public String[] generate(Square1State state) {
    synchronized (solutionSyncHelper) {
      solutionSearchCount++;
    }

    ArrayList<String> sequence = new ArrayList<String>();

    int top = 0;
    int bottom = 0;

    int[] solution;
    try {
      solution = solution(state);
    } catch (InterruptedException e) {
      // user requested stop
      return null;
    }

    for (int i = solution.length - 1; i >= 0; i--) {
      if (solution[i] < 11) {
        top += 12 - (solution[i] + 1);
        top %= 12;
      } else if (solution[i] < 22) {
        bottom += 12 - ((solution[i] - 11) + 1);
        bottom %= 12;
      } else {
        if (top != 0 || bottom != 0) {
          if (top > 6) {
            top = -(12 - top);
          }

          if (bottom > 6) {
            bottom = -(12 - bottom);
          }

          String move = String.format("(%2d,%2d)", top, bottom);
          sequence.add(move);
          top = 0;
          bottom = 0;
        }

//        sequence.add("/");
      }
    }

    if (top != 0 || bottom != 0) {
      if (top > 6) {
        top = -(12 - top);
      }

      if (bottom > 6) {
        bottom = -(12 - bottom);
      }

      String move = String.format("(%2d,%2d)", top, bottom);
      sequence.add(move);
    }

    String[] sequenceArray = new String[sequence.size()];
    sequence.toArray(sequenceArray);

    synchronized (solutionSyncHelper) {
      solutionSearchCount--;
      solutionSyncHelper.notify();
    }
    mustStop = false;

    return sequenceArray;
  }

  private int[] solution(Square1State state) throws InterruptedException {
//    if (!initialized) {
//      genTables();
//    }

    for (int depth = 0;; depth++) {
      ArrayList<Integer> solution1 = new ArrayList<Integer>();
      ArrayList<Integer> solution2 = new ArrayList<Integer>();
      if (search(state, isEvenPermutation(state.getPiecesPermutation()), depth, solution1, solution2)) {
        ArrayList<Integer> sequence = new ArrayList<Integer>();

        for (int moveIndex : solution1) {
          sequence.add(moveIndex);
        }

        int[][] phase2MoveMapping = {
          {  2 },
          {  5 },
          {  8 },
          { 13 },
          { 16 },
          { 19 },
          {  0, 22, 10 },
          { 21, 22, 11 },
        };

        for (int moveIndex : solution2) {
          for (int phase1MoveIndex : phase2MoveMapping[moveIndex]) {
            sequence.add(phase1MoveIndex);
          }
        }

        int[] sequenceArray = new int[sequence.size()];
        for (int i = 0; i < sequenceArray.length; i++) {
          sequenceArray[i] = sequence.get(i);
        }

        return sequenceArray;
      }
    }
  }

  private boolean search(Square1State state, boolean isEvenPermutation, int depth, ArrayList<Integer> solution1, ArrayList<Integer> solution2) throws InterruptedException {
    if (mustStop) {
      throw new InterruptedException("Scramble interruption requested.");
    }

    if (depth == 0) {
      if (isEvenPermutation && state.getShapeIndex() == Square1State.id.getShapeIndex()) {
        int[] sequence2 = solution2(state.toCubeState(), MAX_DEPTH);
        if (sequence2 != null) {
          for (int m : sequence2) {
            solution2.add(m);
          }

          return true;
        }
      }

      return false;
    }

    int distance =
      isEvenPermutation ?
        evenShapeDistance.get(state.getShapeIndex()) :
        oddShapeDistance.get(state.getShapeIndex());
    if (distance <= depth) {
      for (int i = 0; i < moves1.length; i++) {
        if (i == 22 && !state.isTwistable()) {
          continue;
        }

        Square1State next = state.multiply(moves1[i]);

        solution1.add(i);
        if (search(
          next,
          isEvenPermutation(next.getPiecesPermutation()),
          depth - 1,
          solution1,
          solution2)) {
          return true;
        }
        solution1.remove(solution1.size() - 1);
      }
    }

    return false;
  }

  private int[] solution2(Square1CubeState state, int maxDepth) throws InterruptedException {
    if (mustStop) {
      throw new InterruptedException("Scramble interruption requested.");
    }

    int cornersPermutation = WalterIndexMapping.permutationToIndex(state.cornersPermutation);

    boolean[] isTopCorner= new boolean[8];
    for (int k = 0; k < isTopCorner.length; k++) {
      isTopCorner[k] = state.cornersPermutation[k] < 4;
    }
    int cornersCombination = WalterIndexMapping.combinationToIndex(isTopCorner, 4);

    int edgesPermutation = WalterIndexMapping.permutationToIndex(state.edgesPermutation);

    boolean[] isTopEdge = new boolean[8];
    for (int k = 0; k < isTopEdge.length; k++) {
      isTopEdge[k] = state.edgesPermutation[k] < 4;
    }

    int edgesCombination = WalterIndexMapping.combinationToIndex(isTopEdge, 4);

    for (int depth = 0; depth <= maxDepth; depth++) {
      int[] solution = new int[depth];
      if (search2(cornersPermutation, cornersCombination, edgesPermutation, edgesCombination, depth, solution)) {
        return solution;
      }
    }

    return null;
  }

  private boolean search2(int cornersPermutation, int cornersCombination, int edgesPermutation, int edgesCombination, int depth, int[] solution) throws InterruptedException {
    if (mustStop) {
      throw new InterruptedException("Scramble interruption requested.");
    }

    if (depth == 0) {
      return cornersPermutation == 0 && edgesPermutation == 0;
    }

    if (cornersDistance[cornersPermutation][edgesCombination] <= depth &&
      edgesDistance[edgesPermutation][cornersCombination] <= depth) {
      for (int i = 0; i < moves2.length; i++) {
        if (solution.length - depth - 1 >= 0 && solution[solution.length - depth - 1] / 3 == i / 3) {
          continue;
        }

        solution[solution.length - depth] = i;
        if (search2(
          cornersPermutationMove[cornersPermutation][i],
          cornersCombinationMove[cornersCombination][i],
          edgesPermutationMove[edgesPermutation][i],
          edgesCombinationMove[edgesCombination][i],
          depth - 1,
          solution)) {
          return true;
        }
      }
    }

    return false;
  }

  public Square1State getRandomState(Square1State shape, Random random) {
    byte[] cornersPermutation =
      WalterIndexMapping.indexToPermutation(
        random.nextInt(N_CORNERS_PERMUTATIONS), 8);

    byte[] edgesPermutation =
      WalterIndexMapping.indexToPermutation(
        random.nextInt(N_EDGES_PERMUTATIONS), 8);

    byte[] permutation = new byte[shape.permutation.length];
    for (int i = 0; i < permutation.length; i++) {
      if (shape.permutation[i] < 8) {
        permutation[i] = cornersPermutation[shape.permutation[i]];
      } else {
        permutation[i] = (byte) (8 + edgesPermutation[shape.permutation[i] - 8]);
      }
    }

    return new Square1State(permutation);
  }

  public Square1State getRandomState(Random random) {
//    if (!initialized) {
//      genTables();
//    }

    return getRandomState(
      shapes.get(random.nextInt(shapes.size())),
      random);
  }

  public void stop() {
    if (solutionSearchCount > 0) {
      mustStop = true;
    }
  }

  public static void setShapes(ArrayList<Square1State> shapes) {
    Square1Solver.shapes = shapes;
  }

  public static void setEvenShapeDistance(HashMap<Integer, Integer> evenShapeDistance) {
    Square1Solver.evenShapeDistance = evenShapeDistance;
  }

  public static void setOddShapeDistance(HashMap<Integer, Integer> oddShapeDistance) {
    Square1Solver.oddShapeDistance = oddShapeDistance;
  }

  public static void setCornersDistance(byte[][] cornersDistance) {
    Square1Solver.cornersDistance = cornersDistance;
  }

  public static void setEdgesDistance(byte[][] edgesDistance) {
    Square1Solver.edgesDistance = edgesDistance;
  }

  private static void logTimeDifference(long startTs, String msg) {
//    Log.i("[NanoTimer]", msg + ": " + (System.currentTimeMillis() - startTs));
//    System.out.println(msg + ": " + (System.currentTimeMillis() - startTs));
  }

  public static boolean isInitialised() {
    return moves1 != null;
  }

}
