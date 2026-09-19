package com.cube.nanotimer.cube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.cube.nanotimer.coach.PairExecution;
import com.cube.nanotimer.vo.CubeMethod;
import com.cube.nanotimer.vo.SolveTime;

import java.util.List;

import org.junit.Test;

/** The F2L pairs the coach's moves are read from, off solves built as {@link CaseExecutionsTest} builds them. */
public class F2lHabitReaderTest {

  private static final String SUNE = "R U R' U R U U R'";
  private static final String TPERM = "R U R' U' R' F R R U' R' U' R U R' F'";
  /** F2L case 4 into each slot in turn, front right first. */
  private static final String PAIRS = "R U R' B U B' L U L' F U F'";

  /** Every occurrence comes back, not the latest of each case. */
  @Test
  public void readsEveryPairOfTheSolve() {
    List<PairExecution> pairs =
        F2lHabitReader.readFrom(CaseExecutionsTest.solves(PAIRS + " " + SUNE + " " + TPERM));

    assertEquals(4, pairs.size());
    for (PairExecution pair : pairs) {
      assertEquals("pair_4", pair.getCaseCode());
      assertEquals(1, pair.getSolveId());
      assertTrue(pair.isComparable());
      assertTrue(pair.isListed());
      assertEquals(pair.getUsualMoves(), pair.getMoves());
    }
  }

  @Test
  public void readsNothingOutOfASolveThatIsNotCfop() {
    List<SolveTime> solves = CaseExecutionsTest.solves(PAIRS + " " + SUNE + " " + TPERM);
    solves.get(0).getSolveType().setMethod(CubeMethod.ROUX);

    assertTrue(F2lHabitReader.readFrom(solves).isEmpty());
  }
}
