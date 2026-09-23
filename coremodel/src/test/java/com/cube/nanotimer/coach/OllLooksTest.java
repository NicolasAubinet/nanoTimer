package com.cube.nanotimer.coach;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.cube.nanotimer.session.CaseKnowledge;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(JUnit4.class)
public class OllLooksTest {

  private static final int TWO_LOOKED = 12;
  private static final int SOLVES = 30;

  /** The two sides are the solves the two-look count counted and the rest, read off the OLL step. */
  @Test
  public void testTheOllStepIsSplitByTheSolvesThatLookedTwice() {
    CoachPayload payload = build(SOLVES, false);

    Assert.assertEquals(TWO_LOOKED, payload.getTwoLookCount().intValue());
    Assert.assertEquals(TWO_LOOKED, payload.value("oll_looks.two_look.count").intValue());
    Assert.assertEquals(SOLVES - TWO_LOOKED, payload.value("oll_looks.one_look.count").intValue());
    Assert.assertEquals(6000, payload.value("oll_looks.two_look.mean_ms").doubleValue(), 30);
    Assert.assertEquals(4000, payload.value("oll_looks.one_look.mean_ms").doubleValue(), 30);
  }

  /** A skipped OLL took no look, and counted as one-look it would make one look cheaper than it is. */
  @Test
  public void testASkipIsInNeitherSide() {
    CoachPayload payload = build(SOLVES, true);

    Assert.assertEquals(SOLVES - TWO_LOOKED, payload.value("oll_looks.one_look.count").intValue());
    Assert.assertEquals(4000, payload.value("oll_looks.one_look.mean_ms").doubleValue(), 30);
  }

  /** A side under the step floor compares with nothing, so neither is sent. */
  @Test
  public void testBothSidesOrNeither() {
    CoachPayload payload = build(TWO_LOOKED + CoachPayloadBuilder.FAMILY_FLOOR - 1, false);

    Assert.assertTrue(payload.getOllLooks().isEmpty());
    Assert.assertNull(payload.value("oll_looks.one_look.mean_ms"));
  }

  @Test
  public void testTheSplitSurvivesTheWire() {
    CoachPayload payload = CoachPayload.parse(build(SOLVES, false).toJson());

    Assert.assertEquals(7, payload.getSchemaVersion());
    Assert.assertEquals(2, payload.getOllLooks().size());
    Assert.assertEquals(6000, payload.value("oll_looks.two_look.mean_ms").doubleValue(), 30);
  }

  /** Before version 7 the split was not measured, which no reader may take for a gap of nothing. */
  @Test
  public void testAnOlderPayloadHasNoSplit() throws Exception {
    JSONObject older = new JSONObject(build(SOLVES, false).toJson());
    older.remove("oll_looks");
    older.put("schema_version", 6);
    CoachPayload payload = CoachPayload.parse(older.toString());

    Assert.assertTrue(payload.getOllLooks().isEmpty());
    Assert.assertNull(payload.value("oll_looks.two_look.mean_ms"));
  }

  /**
   * The first {@link #TWO_LOOKED} solves orient in two algorithms and take 6 s over it, the rest in
   * one and take 4 s; with a skip, one more solve orients nothing at all in 300 ms.
   */
  private static CoachPayload build(int solves, boolean skip) {
    List<StepSample> samples = new ArrayList<StepSample>();
    List<Long> times = new ArrayList<Long>();
    for (int solve = 0; solve < solves; solve++) {
      long spread = (solve % 2 == 0 ? 1 : -1) * (solve + 1) * 10L;
      boolean two = solve < TWO_LOOKED;
      samples.add(new StepSample("cross", 3000 + spread, 0, false, solve));
      samples.add(new StepSample("f2l", 12000 + spread, 5000, false, solve));
      samples.add(new StepSample("oll", (two ? 6000 : 4000) + spread, 2000, false, solve));
      samples.add(new StepSample("pll", 4800 + spread, 2400, false, solve));
      samples.add(new StepSample("ollalg_45", 2000, 800, true, solve));
      if (two) {
        samples.add(new StepSample("ollalg_26", 2000, 800, true, solve));
      }
      times.add(Long.valueOf(25000 + spread));
    }
    if (skip) {
      samples.add(new StepSample("oll_skip", 300, 300, false, solves));
      times.add(Long.valueOf(24000));
    }
    return CoachPayloadBuilder.build("3x3", "cfop", times, samples, samples, times.size(),
        Collections.<StepSample>emptyList(), 0, Collections.<CaseKnowledge>emptyList());
  }
}
