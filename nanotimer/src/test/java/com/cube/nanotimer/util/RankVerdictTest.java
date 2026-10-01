package com.cube.nanotimer.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RankVerdictTest {

  @Test
  public void topTenOfALongHistoryIsNotable() {
    assertTrue(RankVerdict.isNotable(2, 500));
    assertTrue(RankVerdict.isNotable(5, 500));
    assertTrue(RankVerdict.isNotable(10, 500));
    assertFalse(RankVerdict.isNotable(11, 500));
  }

  @Test
  public void theRecordAndAnUnrankedSolveAreLeftToOtherLines() {
    assertFalse(RankVerdict.isNotable(1, 500));
    assertFalse(RankVerdict.isNotable(0, 500));
  }

  @Test
  public void rankMustSitInTheTopQuarter() {
    assertTrue(RankVerdict.isNotable(5, 20));
    assertFalse(RankVerdict.isNotable(5, 19));
    assertFalse(RankVerdict.isNotable(2, 7));
  }

}
