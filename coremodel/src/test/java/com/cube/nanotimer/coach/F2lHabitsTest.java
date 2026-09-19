package com.cube.nanotimer.coach;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class F2lHabitsTest {

  private static final int SOLVE_FLOOR = CoachPayloadBuilder.FAMILY_FLOOR;
  private static final int CASE_FLOOR = CoachPayloadBuilder.CASE_FLOOR;

  /**
   * A listed algorithm costs nothing even where a shorter one is listed too: the recommended one is
   * often the longer (owner, 2026-09-19). Only what went in off the list is counted.
   */
  @Test
  public void testAListedAlgorithmCostsNoExtraMoves() {
    List<PairExecution> pairs = new ArrayList<PairExecution>();
    for (int solve = 0; solve < 12; solve++) {
      pairs.add(pair(solve, "pair_32", 11, 7, true));
      pairs.add(pair(solve, "pair_4", solve < 3 ? 10 : 7, 7, solve >= 3));
    }

    List<MoveFigure> cases = F2lHabits.cases(pairs, CASE_FLOOR);

    Assert.assertEquals("pair_4", cases.get(0).getCode());
    Assert.assertEquals(0.75, cases.get(0).getExtraMoves(), 1e-9);
    Assert.assertEquals(0.25, cases.get(0).getOffListRate().doubleValue(), 1e-9);
    Assert.assertEquals("pair_32", cases.get(1).getCode());
    Assert.assertEquals(0, cases.get(1).getExtraMoves(), 1e-9);
    Assert.assertEquals(0, cases.get(1).getOffListRate().doubleValue(), 1e-9);
  }

  /** Every occurrence counts, not the latest: one odd turn among the usual ones stays one. */
  @Test
  public void testACaseIsARateOverEveryOccurrence() {
    List<PairExecution> pairs = new ArrayList<PairExecution>();
    pairs.add(pair(0, "pair_27", 11, 7, false)); // the latest, newest first
    for (int solve = 1; solve < 8; solve++) {
      pairs.add(pair(solve, "pair_27", 7, 7, true));
    }

    MoveFigure figure = F2lHabits.cases(pairs, CASE_FLOOR).get(0);

    Assert.assertEquals(8, figure.getCount());
    Assert.assertEquals(1 / 8.0, figure.getOffListRate().doubleValue(), 1e-9);
    Assert.assertEquals(0.5, figure.getExtraMoves(), 1e-9);
  }

  @Test
  public void testACaseUnderItsFloorIsNotSent() {
    List<PairExecution> pairs = new ArrayList<PairExecution>();
    for (int solve = 0; solve < CASE_FLOOR - 1; solve++) {
      pairs.add(pair(solve, "pair_23", 11, 7, true));
    }

    Assert.assertTrue(F2lHabits.cases(pairs, CASE_FLOOR).isEmpty());
  }

  /** A knocked cube is thrown out by its time, and counted as thrown out. */
  @Test
  public void testTheOutlierRuleJudgesAnOccurrenceByItsTime() {
    List<PairExecution> pairs = new ArrayList<PairExecution>();
    for (int solve = 0; solve < 10; solve++) {
      pairs.add(PairExecution.of(solve, "pair_5", 3000 + solve * 40, 7, 7, true));
    }
    pairs.add(PairExecution.of(10, "pair_5", 40000, 25, 7, false));

    MoveFigure figure = F2lHabits.cases(pairs, CASE_FLOOR).get(0);

    Assert.assertEquals(10, figure.getCount());
    Assert.assertEquals(0, figure.getExtraMoves(), 1e-9);
    Assert.assertEquals(0, figure.getOffListRate().doubleValue(), 1e-9);
    Assert.assertEquals(1 / 11.0, figure.getRejectionRate(), 1e-9);
  }

  /**
   * Look-ahead: the first pair is built long while the second is half built with it, so each pair
   * alone reads 2 over and 2 under, and the solve's F2L is over by nothing.
   */
  @Test
  public void testTheWholeF2lLetsOnePairsSavingOffsetAnothers() {
    List<PairExecution> pairs = new ArrayList<PairExecution>();
    for (int solve = 0; solve < SOLVE_FLOOR; solve++) {
      pairs.add(pair(solve, "pair_12", 9, 7, false));
      pairs.add(pair(solve, "pair_3", 5, 7, false));
    }

    MoveFigure total = F2lHabits.total(pairs, SOLVE_FLOOR);

    Assert.assertEquals(SOLVE_FLOOR, total.getCount());
    Assert.assertEquals(14, total.getTurnedMoves().doubleValue(), 1e-9);
    Assert.assertEquals(14, total.getUsualMoves().doubleValue(), 1e-9);
    Assert.assertEquals(0, total.getExtraMoves(), 1e-9);
    Assert.assertNull(total.getOffListRate());
  }

  /** The three figures of the whole F2L are one reading of the same solves, so they add up. */
  @Test
  public void testTheWholeF2lAddsUpAndMayComeInUnder() {
    List<PairExecution> pairs = new ArrayList<PairExecution>();
    for (int solve = 0; solve < SOLVE_FLOOR; solve++) {
      pairs.add(pair(solve, "pair_12", solve % 2 == 0 ? 5 : 9, 7, false));
    }

    MoveFigure total = F2lHabits.total(pairs, SOLVE_FLOOR);

    Assert.assertEquals(7, total.getTurnedMoves().doubleValue(), 1e-9);
    Assert.assertEquals(7, total.getUsualMoves().doubleValue(), 1e-9);
    Assert.assertEquals(0, total.getExtraMoves(), 1e-9);
    Assert.assertEquals(1, F2lHabits.cases(pairs, CASE_FLOOR).get(0).getExtraMoves(), 1e-9);
  }

  /** A listed pair is its own usual, so only the pair off the list puts the solve over. */
  @Test
  public void testAListedPairAddsNothingToTheWhole() {
    List<PairExecution> pairs = new ArrayList<PairExecution>();
    for (int solve = 0; solve < SOLVE_FLOOR; solve++) {
      pairs.add(pair(solve, "pair_32", 11, 7, true));
      pairs.add(pair(solve, "pair_12", 9, 7, false));
    }

    MoveFigure total = F2lHabits.total(pairs, SOLVE_FLOOR);

    Assert.assertEquals(20, total.getTurnedMoves().doubleValue(), 1e-9);
    Assert.assertEquals(18, total.getUsualMoves().doubleValue(), 1e-9);
    Assert.assertEquals(2, total.getExtraMoves(), 1e-9);
  }

  /** A stuck piece leaves its solve's F2L with nothing to add up to, so the solve is left out. */
  @Test
  public void testASolveWithAPairThatCannotBeComparedIsLeftOutOfTheWhole() {
    List<PairExecution> pairs = new ArrayList<PairExecution>();
    for (int solve = 0; solve < SOLVE_FLOOR; solve++) {
      pairs.add(pair(solve, "pair_12", 9, 7, false));
    }
    pairs.add(pair(SOLVE_FLOOR, "pair_12", 30, 7, false));
    pairs.add(PairExecution.unlisted(SOLVE_FLOOR, 5000));

    MoveFigure total = F2lHabits.total(pairs, SOLVE_FLOOR);

    Assert.assertEquals(SOLVE_FLOOR, total.getCount());
    Assert.assertEquals(2, total.getExtraMoves(), 1e-9);
  }

  @Test
  public void testTheWholeF2lUnderItsFloorIsNotSent() {
    List<PairExecution> pairs = new ArrayList<PairExecution>();
    for (int solve = 0; solve < SOLVE_FLOOR - 1; solve++) {
      pairs.add(pair(solve, "pair_12", 9, 7, true));
    }

    Assert.assertNull(F2lHabits.total(pairs, SOLVE_FLOOR));
  }

  /** Occurrences spread a little in time, so the outlier rule has a spread to judge against. */
  private static PairExecution pair(int solve, String caseCode, int moves, int usualMoves,
      boolean listed) {
    return PairExecution.of(solve, caseCode, 3000 + (solve % 2 == 0 ? 1 : -1) * solve * 30L,
        moves, usualMoves, listed);
  }
}
