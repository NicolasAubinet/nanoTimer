package com.cube.nanotimer.scrambler.cross;

import com.cube.nanotimer.scrambler.randomstate.Move;
import com.cube.nanotimer.scrambler.randomstate.StateTables;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

/**
 * Solves the CFOP "cross" for one face of a 3x3: the 4 edges of that face placed home with correct
 * orientation, ignoring corners and the other 8 edges.
 *
 * <p>The cross is a tiny sub-problem &mdash; 4 distinguishable edges among 12 slots
 * ({@code 12*11*10*9 = 11880}) each flipped or not ({@code 2^4 = 16}) gives {@value #N_STATES}
 * states, always solvable in &le; 8 HTM. We therefore precompute a full BFS distance-to-solved
 * table once, then "solve" a scramble by gradient descent: from the scrambled state, follow every
 * move that strictly decreases the distance. This yields <em>all</em> optimal solutions instantly.
 *
 * <p>Each instance handles one face (its 4 home edges); per-face tables avoid whole-cube rotation
 * and move-remapping. The goal test is uniform: those 4 edges home with orientation 0.
 *
 * <p>Pure synchronous analysis &mdash; no Service/DB, no caching, no generation pool.
 */
public class CrossSolver {

  /** {@code 12*11*10*9 * 16} = ordered placements of 4 edges among 12 slots, times 2^4 orientations. */
  static final int N_STATES = 190080;

  /** All 18 face turns, in {@link Move} declaration order. */
  private static final Move[] MOVES = Move.values();

  /** Home indices of the 4 edges making up this cross. */
  private final byte[] crossPieces;
  /** {@code crossIndex[homeIndex]} = 0..3 for a cross edge, -1 otherwise. */
  private final int[] crossIndex;
  /** Distance-to-solved for every cross coordinate. */
  private final byte[] distance;

  public CrossSolver(byte[] crossPieces) {
    if (crossPieces.length != 4) {
      throw new IllegalArgumentException("A cross is defined by exactly 4 edges");
    }
    this.crossPieces = crossPieces.clone();
    this.crossIndex = new int[12];
    Arrays.fill(crossIndex, -1);
    for (int k = 0; k < 4; k++) {
      crossIndex[crossPieces[k]] = k;
    }
    this.distance = buildTable();
  }

  /** Solver for the standard D (bottom) cross. */
  public static CrossSolver dCross() {
    return new CrossSolver(facePieces(Move.D));
  }

  /**
   * The 4 edge home indices belonging to a face, derived from its quarter turn (the slots it moves).
   * E.g. {@code Move.D -> {8,9,10,11}}, {@code Move.R -> {0,1,5,9}}.
   */
  public static byte[] facePieces(Move quarterTurn) {
    byte[] pieces = new byte[4];
    int n = 0;
    for (byte i = 0; i < 12; i++) {
      if (quarterTurn.edgPerm[i] != i) {
        pieces[n++] = i;
      }
    }
    if (n != 4) {
      throw new IllegalArgumentException("Move " + quarterTurn.name + " does not move exactly 4 edges");
    }
    return pieces;
  }

  /** All optimal solutions to this cross for the given scramble string. */
  public List<String[]> solve(String scramble) {
    byte[][] state = ScrambleParser.toEdgeState(scramble);
    return solve(state[0], state[1]);
  }

  /** All optimal solutions to this cross for the given edge state ({@code perm}, {@code orient}). */
  public List<String[]> solve(byte[] perm, byte[] orient) {
    int startIndex = encode(perm, orient);
    int depth = distance[startIndex];
    List<String[]> solutions = new ArrayList<>();
    descend(perm, orient, depth, new String[depth], 0, solutions);
    return solutions;
  }

  /** Optimal cross length (HTM) for the given scramble. */
  public int optimalLength(String scramble) {
    byte[][] state = ScrambleParser.toEdgeState(scramble);
    return distance[encode(state[0], state[1])];
  }

  // Collects every move sequence of the minimal length that solves the cross, by following the
  // distance gradient (each move strictly decreases distance-to-solved).
  private void descend(byte[] perm, byte[] orient, int remaining, String[] path, int depth, List<String[]> out) {
    if (remaining == 0) {
      out.add(path.clone());
      return;
    }
    for (Move m : MOVES) {
      byte[] nextPerm = StateTables.getPermResult(perm, m.edgPerm);
      byte[] nextOrient = StateTables.getOrientResult(orient, m.edgPerm, m.edgOrient, 2);
      if (distance[encode(nextPerm, nextOrient)] == remaining - 1) {
        path[depth] = m.name;
        descend(nextPerm, nextOrient, remaining - 1, path, depth + 1, out);
      }
    }
  }

  // One BFS from the solved cross over the whole sub-space, deduped by cross coordinate.
  private byte[] buildTable() {
    byte[] dist = new byte[N_STATES];
    Arrays.fill(dist, (byte) -1);

    byte[] perm = new byte[12];
    byte[] orient = new byte[12];
    for (byte i = 0; i < 12; i++) {
      perm[i] = i;
    }
    dist[encode(perm, orient)] = 0;

    Queue<byte[]> queue = new ArrayDeque<>();
    queue.add(pack(perm, orient));
    while (!queue.isEmpty()) {
      byte[] cur = queue.poll();
      byte[] curPerm = Arrays.copyOfRange(cur, 0, 12);
      byte[] curOrient = Arrays.copyOfRange(cur, 12, 24);
      byte d = dist[encode(curPerm, curOrient)];
      for (Move m : MOVES) {
        byte[] nextPerm = StateTables.getPermResult(curPerm, m.edgPerm);
        byte[] nextOrient = StateTables.getOrientResult(curOrient, m.edgPerm, m.edgOrient, 2);
        int idx = encode(nextPerm, nextOrient);
        if (dist[idx] < 0) {
          dist[idx] = (byte) (d + 1);
          queue.add(pack(nextPerm, nextOrient));
        }
      }
    }
    return dist;
  }

  // Maps an edge state to a unique index in [0, N_STATES), considering only this cross's 4 edges.
  // index = placementRank * 16 + orientationBits, where placementRank is the rank of the ordered
  // assignment of the 4 cross edges to slots (mixed radix 12,11,10,9) and orientationBits packs
  // their 4 orientation flips.
  private int encode(byte[] perm, byte[] orient) {
    int[] slotOf = new int[4];
    int[] orientOf = new int[4];
    for (int s = 0; s < 12; s++) {
      int k = crossIndex[perm[s]];
      if (k >= 0) {
        slotOf[k] = s;
        orientOf[k] = orient[s];
      }
    }

    int rank = 0;
    int orientBits = 0;
    boolean[] used = new boolean[12];
    for (int k = 0; k < 4; k++) {
      int slot = slotOf[k];
      int r = 0;
      for (int s = 0; s < slot; s++) {
        if (!used[s]) {
          r++;
        }
      }
      rank = rank * (12 - k) + r;
      used[slot] = true;
      orientBits = orientBits * 2 + orientOf[k];
    }
    return rank * 16 + orientBits;
  }

  private static byte[] pack(byte[] perm, byte[] orient) {
    byte[] packed = new byte[24];
    System.arraycopy(perm, 0, packed, 0, 12);
    System.arraycopy(orient, 0, packed, 12, 12);
    return packed;
  }
}
