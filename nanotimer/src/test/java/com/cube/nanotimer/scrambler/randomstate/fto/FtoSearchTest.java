package com.cube.nanotimer.scrambler.randomstate.fto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Exercises the generic search engine (createMoveHash / createPrun / Searcher) on
 * a tiny decoupled puzzle: a 12-position clock with a single "+1" move.
 */
public class FtoSearchTest {

  private static final int N = 12;

  private FtoSearch.MoveHash buildClock() {
    return FtoSearch.createMoveHash(0, new int[] {0},
      new FtoSearch.HashFunc() {
        @Override
        public Object hash(Object state) {
          return state; // Integer is its own key
        }
      },
      new FtoSearch.MoveFunc() {
        @Override
        public Object move(Object state, int m) {
          return ((Integer) state + 1) % N;
        }
      });
  }

  @Test
  public void moveHashDiscoversAllStates() {
    FtoSearch.MoveHash mh = buildClock();
    assertEquals(N, mh.nStates());
    for (int i = 0; i < N; i++) {
      assertEquals((i + 1) % N, mh.moveTable[0][i]);
    }
  }

  @Test
  public void prunAndSearcherSolveTheClock() {
    final FtoSearch.MoveHash mh = buildClock();
    // nPower = 11: one move-step can be applied up to 11 times.
    final int[] prun = FtoSearch.createPrun(0, N, 20, mh.moveTable, 1, 11);
    assertEquals(0, FtoMath.getPruning(prun, 0));
    for (int i = 1; i < N; i++) {
      assertEquals(1, FtoMath.getPruning(prun, i));
    }

    FtoSearch.Searcher searcher = new FtoSearch.Searcher(
      new FtoSearch.SearchSolved() {
        @Override
        public boolean solved(Object idx) {
          return (Integer) idx == 0;
        }
      },
      new FtoSearch.SearchPrun() {
        @Override
        public int prun(Object idx) {
          return FtoMath.getPruning(prun, (Integer) idx);
        }
      },
      new FtoSearch.SearchMove() {
        @Override
        public Object move(Object idx, int axis) {
          return mh.moveTable[axis][(Integer) idx];
        }
      },
      1, 11, new int[] {1});

    // Already solved -> empty solution.
    int[][] solved = searcher.solve(0, 0, 5);
    assertNotNull(solved);
    assertEquals(0, solved.length);

    for (int start = 1; start < N; start++) {
      int[][] sol = searcher.solve(start, 0, 5);
      assertNotNull("no solution for start=" + start, sol);
      int pos = start;
      for (int[] step : sol) {
        int axis = step[0];
        int pow = step[1];
        for (int c = 0; c <= pow; c++) {
          pos = mh.moveTable[axis][pos];
        }
      }
      assertEquals("solution must solve start=" + start, 0, pos);
    }
  }
}
