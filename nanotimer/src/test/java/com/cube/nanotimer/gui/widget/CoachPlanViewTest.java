package com.cube.nanotimer.gui.widget;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/** What went well is split off a plan's headline only where the headline was written with it. */
public class CoachPlanViewTest {

  @Test
  public void splitsTheFirstOfTwoSentences() {
    assertArrayEquals(new String[] {"Your F2L pairs are 0.6 seconds faster than last week.",
        "This week the time is in your last layer."},
        CoachPlanView.splitWin("Your F2L pairs are 0.6 seconds faster than last week. "
            + "This week the time is in your last layer."));
  }

  /** Every plan written before the opening sentence was asked for is one sentence. */
  @Test
  public void keepsASingleSentenceWhole() {
    assertArrayEquals(new String[] {null, "Your Gb perm is losing 2.35 seconds, so drill it."},
        CoachPlanView.splitWin("Your Gb perm is losing 2.35 seconds, so drill it."));
  }

  @Test
  public void hasNothingToSplitWithoutAHeadline() {
    assertArrayEquals(new String[] {null, null}, CoachPlanView.splitWin(null));
    assertArrayEquals(new String[] {null, null}, CoachPlanView.splitWin("  "));
  }
}
