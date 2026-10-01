package com.cube.nanotimer.util.coach;

import org.junit.Assert;
import org.junit.Test;

/** How long an unanswered request is still the one to send again. */
public class CoachAskTest {

  private static final long NOW = 1_000_000_000L;

  @Test
  public void aRequestKeptWithinTheHourIsSentAgainAsItWas() {
    Assert.assertFalse(CoachAsk.stale(NOW, NOW));
    Assert.assertFalse(CoachAsk.stale(NOW - CoachAsk.PENDING_MAX_AGE_MS, NOW));
  }

  @Test
  public void anOlderOneIsRebuilt() {
    Assert.assertTrue(CoachAsk.stale(NOW - CoachAsk.PENDING_MAX_AGE_MS - 1, NOW));
  }

  /** A request kept by a build that stored no time reads as kept at 0. */
  @Test
  public void oneWithNoTimeIsRebuilt() {
    Assert.assertTrue(CoachAsk.stale(0, NOW));
  }

  @Test
  public void oneDatedAheadOfTheClockIsRebuilt() {
    Assert.assertTrue(CoachAsk.stale(NOW + 1, NOW));
  }
}
