package com.cube.nanotimer.util.coach;

import com.cube.nanotimer.coach.PlanSchedule;

import org.junit.Assert;
import org.junit.Test;

/** What the weekly job does with an ask that brought no plan. */
public class CoachPlanJobTest {

  private static final long NOW = 1_000_000_000_000L;

  private static CoachReply refusal(String code, int retryAfterSeconds) {
    return CoachReply.parse(429, "{\"error\":{\"code\":\"" + code + "\",\"message\":\"no\","
        + "\"retry_after_s\":" + retryAfterSeconds + "}}");
  }

  @Test
  public void anUnreachableCoachIsTriedAgainSoon() {
    Assert.assertEquals(0, CoachPlanJob.waitAfter(CoachReply.unreachable()));
  }

  /** Even when it names a wait: nothing would bring the job back once that wait is over. */
  @Test
  public void aBusyCoachIsTriedAgainSoon() {
    Assert.assertEquals(0, CoachPlanJob.waitAfter(refusal("rate_limited", 60)));
    Assert.assertEquals(0, CoachPlanJob.waitAfter(refusal("upstream_unavailable", 0)));
    Assert.assertEquals(0, CoachPlanJob.waitAfter(refusal("store_unavailable", 0)));
  }

  /** Play not answering the service says nothing about the subscription. */
  @Test
  public void aPurchasePlayCouldNotBeAskedAboutIsTriedAgainSoon() {
    Assert.assertEquals(0, CoachPlanJob.waitAfter(refusal("purchase_unavailable", 60)));
  }

  @Test
  public void aRefusalIsLeftAloneForAsLongAsItSays() {
    Assert.assertEquals(3_600_000L, CoachPlanJob.waitAfter(refusal("weekly_cap_reached", 3600)));
  }

  @Test
  public void aRefusalThatNamesNoWaitIsLeftAloneForADay() {
    Assert.assertEquals(CoachPlanJob.REFUSED_WAIT_MS,
        CoachPlanJob.waitAfter(refusal("plan_rejected", 0)));
    Assert.assertEquals(CoachPlanJob.REFUSED_WAIT_MS,
        CoachPlanJob.waitAfter(CoachReply.unreadable()));
  }

  @Test
  public void aRefusalHoldsTheJobUntilItsTime() {
    Assert.assertTrue(CoachPlanJob.held(NOW + 1, NOW));
    Assert.assertFalse(CoachPlanJob.held(NOW, NOW));
    Assert.assertFalse(CoachPlanJob.held(0, NOW));
  }

  /** No wait is longer than a week, so a longer one means the clock was set back. */
  @Test
  public void aHoldFurtherThanAWeekAheadIsDropped() {
    Assert.assertTrue(CoachPlanJob.held(NOW + PlanSchedule.PERIOD_MS, NOW));
    Assert.assertFalse(CoachPlanJob.held(NOW + PlanSchedule.PERIOD_MS + 1, NOW));
  }
}
