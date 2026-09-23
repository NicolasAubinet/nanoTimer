package com.cube.nanotimer.coach;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LastLayerHabitsTest {

  private static final int STEP_FLOOR = CoachPayloadBuilder.FAMILY_FLOOR;
  private static final int CASE_FLOOR = CoachPayloadBuilder.CASE_FLOOR;

  /** Each step reads its own cases only, OLL before PLL, and an F2L pair is neither. */
  @Test
  public void testEachStepReadsItsOwnCases() {
    List<PairExecution> executions = new ArrayList<PairExecution>();
    for (int solve = 0; solve < 12; solve++) {
      executions.add(PairExecution.of(solve, "pll_t", 2000, 14, 14, true));
      executions.add(PairExecution.of(solve, "oll_27", 1500, solve < 4 ? 11 : 7, 7, solve >= 4));
      executions.add(PairExecution.of(solve, "pair_4", 3000, 30, 7, false));
    }

    List<MoveFigure> steps = LastLayerHabits.steps(executions, STEP_FLOOR);

    Assert.assertEquals(2, steps.size());
    Assert.assertEquals("oll", steps.get(0).getCode());
    Assert.assertEquals(12, steps.get(0).getCount());
    Assert.assertEquals((4 * 11 + 8 * 7) / 12.0, steps.get(0).getTurnedMoves(), 1e-9);
    Assert.assertEquals(7, steps.get(0).getUsualMoves(), 1e-9);
    Assert.assertEquals(4 * 4 / 12.0, steps.get(0).getExtraMoves(), 1e-9);
    Assert.assertEquals("pll", steps.get(1).getCode());
    Assert.assertEquals(0, steps.get(1).getExtraMoves(), 1e-9);
  }

  /**
   * An algorithm of the solver's own that is shorter than the listed ones buys nothing back, the
   * way a case's figure never goes under zero, so the step's three figures still add up.
   */
  @Test
  public void testAShorterAlgorithmOffTheListIsNotAGain() {
    List<PairExecution> executions = new ArrayList<PairExecution>();
    for (int solve = 0; solve < 10; solve++) {
      executions.add(PairExecution.of(solve, "pll_ra", 2200, 11, 13, false));
    }

    MoveFigure pll = LastLayerHabits.steps(executions, STEP_FLOOR).get(0);

    Assert.assertEquals(0, pll.getExtraMoves(), 1e-9);
    Assert.assertEquals(pll.getTurnedMoves(), pll.getUsualMoves(), 1e-9);
  }

  @Test
  public void testAStepUnderItsFloorIsNotSent() {
    List<PairExecution> executions = new ArrayList<PairExecution>();
    for (int solve = 0; solve < STEP_FLOOR - 1; solve++) {
      executions.add(PairExecution.of(solve, "pll_t", 2000, 14, 14, true));
    }

    Assert.assertTrue(LastLayerHabits.steps(executions, STEP_FLOOR).isEmpty());
  }

  /** A case is read as an F2L case is, and the pairs are not among them. */
  @Test
  public void testCasesAreTheLastLayerOnes() {
    List<PairExecution> executions = new ArrayList<PairExecution>();
    for (int solve = 0; solve < 8; solve++) {
      executions.add(PairExecution.of(solve, "pll_gb", 2600, solve < 2 ? 16 : 12, 12,
          solve >= 2));
      executions.add(PairExecution.of(solve, "pair_4", 3000, 11, 7, false));
    }

    List<MoveFigure> cases = LastLayerHabits.cases(executions, CASE_FLOOR);

    Assert.assertEquals(1, cases.size());
    Assert.assertEquals("pll_gb", cases.get(0).getCode());
    Assert.assertEquals(1, cases.get(0).getExtraMoves(), 1e-9);
    Assert.assertEquals(0.25, cases.get(0).getOffListRate().doubleValue(), 1e-9);
  }
}
