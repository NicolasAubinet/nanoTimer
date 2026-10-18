package com.cube.nanotimer.coach;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.cube.nanotimer.session.CaseKnowledge;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(JUnit4.class)
public class LastLayerLooksTest {

  private static final int TWO_LOOKED = 12;
  private static final int TWO_PERMUTED = 11;
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

  /** A PLL in two algorithms is a two-look PLL, counted and split apart from the OLL's looks. */
  @Test
  public void testThePllStepIsSplitByTheSolvesThatPermutedTwice() {
    CoachPayload payload = build(SOLVES, false);

    Assert.assertEquals(TWO_PERMUTED, payload.getTwoLookPllCount().intValue());
    Assert.assertEquals(TWO_PERMUTED, payload.value("two_look_pll_count").intValue());
    Assert.assertEquals(TWO_PERMUTED, payload.value("pll_looks.two_look.count").intValue());
    Assert.assertEquals(SOLVES - TWO_PERMUTED,
        payload.value("pll_looks.one_look.count").intValue());
    Assert.assertEquals(7000, payload.value("pll_looks.two_look.mean_ms").doubleValue(), 30);
    Assert.assertEquals(4800, payload.value("pll_looks.one_look.mean_ms").doubleValue(), 30);
  }

  /** Moves turned and taken back are a restart, not a second look. */
  @Test
  public void testARestartIsNotASecondLook() {
    CoachPayload payload = build(SOLVES, false);

    Assert.assertEquals(TWO_PERMUTED, payload.getTwoLookPllCount().intValue());
  }

  /** A two-look card says how many of its own step's cases are left to learn, not the other's. */
  @Test
  public void testTheCasesToLearnAreCountedPerStep() {
    CoachPayload payload = new CoachPayload(CoachPayload.VERSION, "3x3", "cfop", 100, 500, 0, null,
        null, null, null, null, null, null, null, null,
        Arrays.asList("oll_1", "oll_2", "pll_ga", "oll_3"));

    Assert.assertEquals(4, payload.value("to_learn_cases.count").intValue());
    Assert.assertEquals(3, payload.value("to_learn_cases.oll.count").intValue());
    Assert.assertEquals(1, payload.value("to_learn_cases.pll.count").intValue());
    Assert.assertEquals(0, payload.value("known_cases.pll.count").intValue());
    Assert.assertNull(payload.value("to_learn_cases.pll.mean_ms"));
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

    Assert.assertEquals(CoachPayload.VERSION, payload.getSchemaVersion());
    Assert.assertEquals(2, payload.getOllLooks().size());
    Assert.assertEquals(6000, payload.value("oll_looks.two_look.mean_ms").doubleValue(), 30);
    Assert.assertEquals(TWO_PERMUTED, payload.getTwoLookPllCount().intValue());
    Assert.assertEquals(2, payload.getPllLooks().size());
    Assert.assertEquals(7000, payload.value("pll_looks.two_look.mean_ms").doubleValue(), 30);
  }

  /** Before version 9 two-look PLLs were not counted, which is not a count of none. */
  @Test
  public void testAVersion8PayloadHasNoPllCount() throws Exception {
    JSONObject older = new JSONObject(build(SOLVES, false).toJson());
    older.remove("two_look_pll_count");
    older.remove("pll_looks");
    older.put("schema_version", 8);
    CoachPayload payload = CoachPayload.parse(older.toString());

    Assert.assertNull(payload.getTwoLookPllCount());
    Assert.assertNull(payload.value("two_look_pll_count"));
    Assert.assertTrue(payload.getPllLooks().isEmpty());
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
   * one and take 4 s; with a skip, one more solve orients nothing at all in 300 ms. The last
   * {@link #TWO_PERMUTED} permute in two algorithms and take 7 s, the rest in one and 4.8 s, a few of
   * those after a restart.
   */
  private static CoachPayload build(int solves, boolean skip) {
    List<StepSample> samples = new ArrayList<StepSample>();
    List<Long> times = new ArrayList<Long>();
    for (int solve = 0; solve < solves; solve++) {
      long spread = (solve % 2 == 0 ? 1 : -1) * (solve + 1) * 10L;
      boolean two = solve < TWO_LOOKED;
      boolean twoPermuted = solve >= solves - TWO_PERMUTED;
      samples.add(new StepSample("cross", 3000 + spread, 0, false, solve));
      samples.add(new StepSample("f2l", 12000 + spread, 5000, false, solve));
      samples.add(new StepSample("oll", (two ? 6000 : 4000) + spread, 2000, false, solve));
      samples.add(new StepSample("pll", (twoPermuted ? 7000 : 4800) + spread, 2400, false, solve));
      samples.add(new StepSample("ollalg_45", 2000, 800, true, solve));
      if (two) {
        samples.add(new StepSample("ollalg_26", 2000, 800, true, solve));
      }
      if (!twoPermuted && solve % 5 == 0) {
        samples.add(new StepSample("pllrestart", 600, 300, true, solve));
      }
      samples.add(new StepSample("alg_ua", 2000, 800, true, solve));
      if (twoPermuted) {
        samples.add(new StepSample("alg_t", 2400, 800, true, solve));
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
