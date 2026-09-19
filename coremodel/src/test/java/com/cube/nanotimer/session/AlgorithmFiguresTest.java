package com.cube.nanotimer.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AlgorithmFiguresTest {

  @Test
  public void extraMovesAreTheTurnsOverTheUsualOnes() {
    assertEquals(2, AlgorithmFigures.extraMoves(false, 16, 14));
    assertEquals(0, AlgorithmFigures.extraMoves(false, 14, 14));
  }

  /** A recommended algorithm longer than another listed one is still no waste. */
  @Test
  public void aListedAlgorithmCostsNothing() {
    assertEquals(0, AlgorithmFigures.extraMoves(true, 11, 7));
  }

  @Test
  public void aShorterExecutionCostsNothing() {
    assertEquals(0, AlgorithmFigures.extraMoves(false, 12, 15));
  }

  @Test
  public void nothingToCompareWithCostsNothing() {
    assertEquals(0, AlgorithmFigures.extraMoves(false, 9, 0));
  }

  @Test
  public void theLatestExecutionOfACaseDecides() {
    AlgorithmFigures figures = new AlgorithmFigures(21);
    figures.add("pll_ja", true, 10, 10);
    figures.add("pll_ja", false, 13, 10); // older, so ignored
    assertEquals(1, figures.getStandard());
    assertEquals(0, figures.getNotStandard());
    assertEquals(0, figures.getExtraMoves());
  }

  @Test
  public void theThreeGroupsAddUpToTheSet() {
    AlgorithmFigures figures = new AlgorithmFigures(21);
    figures.add("pll_v", true, 16, 14);
    figures.add("pll_gc", false, 12, 15);
    figures.add("pll_ua", false, 11, 9);
    assertEquals(1, figures.getStandard());
    assertEquals(2, figures.getNotStandard());
    assertEquals(18, figures.getNotSeen());
    assertEquals(2, figures.getExtraMoves()); // the V is listed, so its two over count nothing
  }

  @Test
  public void aFullSetTakesNoMore() {
    AlgorithmFigures figures = new AlgorithmFigures(2);
    figures.add("a", true, 5, 5);
    assertFalse(figures.isComplete());
    figures.add("b", true, 5, 5);
    figures.add("c", false, 9, 5);
    assertTrue(figures.isComplete());
    assertEquals(0, figures.getNotSeen());
    assertEquals(0, figures.getExtraMoves());
  }
}
