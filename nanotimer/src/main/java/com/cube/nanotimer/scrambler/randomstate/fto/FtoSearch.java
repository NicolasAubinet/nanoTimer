package com.cube.nanotimer.scrambler.randomstate.fto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic move-table / pruning-table / IDA* search engine.
 *
 * Ported (JS -> Java) from the subset of cstimer's "mathlib.js" (GPL-3.0) used by
 * the FTO solver: {@code createMoveHash}, {@code createPrun} and {@code Searcher}.
 * cstimer is by Shuang Chen (cs0x7f): https://github.com/cs0x7f/cstimer
 * This port is GPL-3.0 (same as NanoTimer).
 */
public final class FtoSearch {

  private FtoSearch() {
  }

  /** Hashes an opaque state to a map key (Integer / Long / String, depending on phase). */
  public interface HashFunc {
    Object hash(Object state);
  }

  /** Applies move {@code move} to an opaque state, returning the new state (or null if illegal). */
  public interface MoveFunc {
    Object move(Object state, int move);
  }

  /** Applies move {@code move} to a coordinate index, returning the new index (or < 0 if illegal). */
  public interface DoMove {
    int move(int idx, int move);
  }

  public interface SearchMove {
    Object move(Object idx, int axis);
  }

  public interface SearchPrun {
    int prun(Object idx);
  }

  public interface SearchSolved {
    boolean solved(Object idx);
  }

  public interface SearchCallback {
    boolean found(int[][] sol, int sidx);
  }

  /** Result of {@link #createMoveHash}: per-move transition tables + a hash->index map. */
  public static final class MoveHash {
    public final int[][] moveTable; // [moveIndex][stateIndex] -> next stateIndex (or -1)
    public final Map<Object, Integer> hash2idx;

    MoveHash(int[][] moveTable, Map<Object, Integer> hash2idx) {
      this.moveTable = moveTable;
      this.hash2idx = hash2idx;
    }

    public int nStates() {
      return moveTable[0].length;
    }
  }

  /**
   * BFS the reachable state graph from {@code initState}, building per-move
   * transition tables indexed by a compact state id (assigned in discovery order).
   */
  public static MoveHash createMoveHash(Object initState, int[] validMoves, HashFunc hashFunc, MoveFunc moveFunc) {
    List<Object> states = new ArrayList<>();
    states.add(initState);
    Map<Object, Integer> hash2idx = new HashMap<>();
    hash2idx.put(hashFunc.hash(initState), 0);

    List<List<Integer>> moveCols = new ArrayList<>();
    for (int m = 0; m < validMoves.length; m++) {
      moveCols.add(new ArrayList<Integer>());
    }

    for (int i = 0; i < states.size(); i++) {
      Object curState = states.get(i);
      for (int m = 0; m < validMoves.length; m++) {
        Object newState = moveFunc.move(curState, validMoves[m]);
        if (newState == null) {
          moveCols.get(m).add(-1);
          continue;
        }
        Object newHash = hashFunc.hash(newState);
        Integer idx = hash2idx.get(newHash);
        if (idx == null) {
          idx = states.size();
          hash2idx.put(newHash, idx);
          states.add(newState);
        }
        moveCols.get(m).add(idx);
      }
    }

    int n = states.size();
    int[][] moveTable = new int[validMoves.length][n];
    for (int m = 0; m < validMoves.length; m++) {
      List<Integer> col = moveCols.get(m);
      for (int i = 0; i < n; i++) {
        moveTable[m][i] = col.get(i);
      }
    }
    return new MoveHash(moveTable, hash2idx);
  }

  /** BFS pruning table (4 bits per state) from a single solved index. */
  public static int[] createPrun(int init, int size, int maxd, DoMove doMove, int nMoves, int nPower) {
    return createPrun(new int[] {init}, size, maxd, doMove, nMoves, nPower, 256);
  }

  /** BFS pruning table from a single solved index, using a move table as the transition. */
  public static int[] createPrun(int init, int size, int maxd, int[][] moveTable, int nMoves, int nPower) {
    return createPrun(new int[] {init}, size, maxd, new DoMove() {
      @Override
      public int move(int idx, int m) {
        return moveTable[m][idx];
      }
    }, nMoves, nPower, 256);
  }

  public static int[] createPrun(int[] init, int size, int maxd, DoMove doMove, int nMoves, int nPower, int nInv) {
    int len = (size + 7) >>> 3;
    int[] prun = new int[len];
    for (int i = 0; i < len; i++) {
      prun[i] = -1;
    }
    for (int i = 0; i < init.length; i++) {
      prun[init[i] >> 3] ^= 15 << ((init[i] & 7) << 2);
    }
    for (int l = 0; l <= maxd; l++) {
      int done = 0;
      boolean inv = l >= nInv;
      int fill = (l + 1) ^ 15;
      int find = inv ? 0xf : l;
      int check = inv ? l : 0xf;

      int val = 0;
      outer:
      for (int p = 0; p < size; p++, val >>= 4) {
        if ((p & 7) == 0) {
          val = prun[p >> 3];
          if (!inv && val == -1) {
            p += 7;
            continue;
          }
        }
        if ((val & 0xf) != find) {
          continue;
        }
        for (int m = 0; m < nMoves; m++) {
          int q = p;
          for (int c = 0; c < nPower; c++) {
            q = doMove.move(q, m);
            if (q < 0) {
              break;
            }
            if (FtoMath.getPruning(prun, q) != check) {
              continue;
            }
            done++;
            if (inv) {
              prun[p >> 3] ^= fill << ((p & 7) << 2);
              continue outer;
            }
            prun[q >> 3] ^= fill << ((q & 7) << 2);
          }
        }
      }
      if (done == 0) {
        break;
      }
    }
    return prun;
  }

  /**
   * IDA* searcher over an opaque state with an admissible pruning estimate.
   * Note: this port supports finding the first (and, via a callback, multiple)
   * solution(s) within a single search, which is all the FTO solver needs; it
   * does not implement cstimer's cross-call {@code next()} enumeration.
   */
  public static final class Searcher {
    private final SearchSolved isSolved;
    private final SearchPrun getPrun;
    private final SearchMove doMove;
    private final int nAxis;
    private final int nPower;
    private final int[] ckmv;

    private int sidx;
    private List<int[]> sol;
    private int length;
    private Object[] idxs;
    private long cost;
    private SearchCallback callback;

    /** Set asynchronously to abort an in-flight search. */
    public volatile boolean aborted;

    public Searcher(SearchSolved isSolved, SearchPrun getPrun, SearchMove doMove, int nAxis, int nPower, int[] ckmv) {
      this.isSolved = (isSolved != null) ? isSolved : new SearchSolved() {
        @Override
        public boolean solved(Object idx) {
          return true;
        }
      };
      this.getPrun = getPrun;
      this.doMove = doMove;
      this.nAxis = nAxis;
      this.nPower = nPower;
      this.ckmv = ckmv;
    }

    public int[][] solve(Object idx, int minl, int maxl) {
      Object[] sols = solveMulti(new Object[] {idx}, minl, maxl, null);
      return sols == null ? null : (int[][]) sols[0];
    }

    public Object[] solveMulti(Object[] idxs, int minl, int maxl, SearchCallback callback) {
      this.sidx = 0;
      this.sol = new ArrayList<>();
      this.length = minl;
      this.idxs = idxs;
      this.cost = 1000000000L + 1;
      this.callback = (callback != null) ? callback : new SearchCallback() {
        @Override
        public boolean found(int[][] s, int si) {
          return true;
        }
      };
      for (; this.length <= maxl; this.length++) {
        for (; this.sidx < this.idxs.length; this.sidx++) {
          if (idaSearch(this.idxs[this.sidx], this.length, 0, -1) == 0) {
            return this.cost <= 0 ? null : new Object[] {toArray(this.sol), this.sidx};
          }
        }
        this.sidx = 0;
      }
      return null;
    }

    private int idaSearch(Object idx, int maxl, int depth, int lm) {
      if (--cost <= 0 || aborted) {
        return 0;
      }
      int prun = getPrun.prun(idx);
      if (prun > maxl) {
        return prun > maxl + 1 ? 2 : 1;
      } else if (maxl == 0) {
        return (isSolved.solved(idx) && callback.found(toArray(sol), sidx)) ? 0 : 1;
      } else if (prun == 0 && maxl == 1 && isSolved.solved(idx)) {
        return 1;
      }
      for (int axis = 0; axis < nAxis; axis++) {
        if (lm >= 0 && ((ckmv[lm] >> axis) & 1) != 0) {
          continue;
        }
        Object idx1 = idx;
        for (int pow = 0; pow < nPower; pow++) {
          idx1 = doMove.move(idx1, axis);
          if (idx1 == null) {
            break;
          }
          sol.add(new int[] {axis, pow});
          int ret = idaSearch(idx1, maxl - 1, depth + 1, axis);
          if (ret == 0) {
            return 0;
          }
          sol.remove(sol.size() - 1);
          if (ret == 2) {
            break;
          }
        }
      }
      return 1;
    }

    private static int[][] toArray(List<int[]> sol) {
      int[][] arr = new int[sol.size()][];
      for (int i = 0; i < sol.size(); i++) {
        arr[i] = new int[] {sol.get(i)[0], sol.get(i)[1]};
      }
      return arr;
    }
  }
}
