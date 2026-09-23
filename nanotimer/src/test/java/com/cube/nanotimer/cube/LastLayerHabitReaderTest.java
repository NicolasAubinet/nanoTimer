package com.cube.nanotimer.cube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.cube.nanotimer.coach.PairExecution;
import com.cube.nanotimer.vo.CubeMethod;
import com.cube.nanotimer.vo.SolveTime;

import java.util.List;

import org.junit.Test;

/** The last layer steps the coach's moves are read from, off solves built as {@link CaseExecutionsTest} builds them. */
public class LastLayerHabitReaderTest {

  private static final String SUNE = "R U R' U R U U R'";
  private static final String TPERM = "R U R' U' R' F R R U' R' U' R U R' F'";
  /** F2L case 4 into each slot in turn, front right first. */
  private static final String PAIRS = "R U R' B U B' L U L' F U F'";
  /** Edges then corners: a two-look OLL of a case one algorithm would have done. */
  private static final String TWO_LOOK = "F R U R' U' F' " + SUNE;

  /** The OLL and the PLL, and none of the pairs before them. */
  @Test
  public void readsTheLastLayerOfTheSolve() {
    List<PairExecution> executions = LastLayerHabitReader.readFrom(
        CaseExecutionsTest.solves(PAIRS + " " + SUNE + " " + TPERM), SolveReadings.none());

    assertEquals(2, executions.size());
    assertTrue(executions.get(0).getCaseCode().startsWith("oll_"));
    assertEquals("pll_t", executions.get(1).getCaseCode());
    for (PairExecution execution : executions) {
      assertTrue(execution.isComparable());
      assertTrue(execution.isListed());
      assertEquals(execution.getUsualMoves(), execution.getMoves());
    }
  }

  /** A two-look is its own card, and set against the one-look algorithm it would read as long. */
  @Test
  public void leavesOutAStepTakenInTwoAlgorithms() {
    List<PairExecution> executions = LastLayerHabitReader.readFrom(
        CaseExecutionsTest.solves(TWO_LOOK + " " + TPERM), SolveReadings.none());

    assertEquals(1, executions.size());
    assertEquals("pll_t", executions.get(0).getCaseCode());
  }

  @Test
  public void readsNothingOutOfASolveThatIsNotCfop() {
    List<SolveTime> solves = CaseExecutionsTest.solves(SUNE + " " + TPERM);
    solves.get(0).getSolveType().setMethod(CubeMethod.ROUX);

    assertTrue(LastLayerHabitReader.readFrom(solves, SolveReadings.none()).isEmpty());
  }
}
