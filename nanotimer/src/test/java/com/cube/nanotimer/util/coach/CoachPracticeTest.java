package com.cube.nanotimer.util.coach;

import com.cube.nanotimer.drill.DrillSpec;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/** The practice sets the coach service answers with, read without a network. */
public class CoachPracticeTest {

  private static final String PLL_DRILL = "{\"spec_version\":3,\"id\":\"coach-practice-pll\","
      + "\"puzzle\":\"3x3\",\"method\":\"cfop\",\"type\":\"case_execution\","
      + "\"delivery\":\"virtual\",\"cases\":[\"pll_rb\",\"pll_v\"],\"selection\":\"weighted\","
      + "\"reps\":40,\"target_ms\":4300,\"case_targets_ms\":{\"pll_rb\":4800,\"pll_v\":4700},"
      + "\"retire_after_wins\":3,\"min_showings\":5}";

  @Test
  public void eachSetCarriesItsFamilyCasesAndARunnableDrill() {
    List<CoachPractice.Set> sets = CoachPractice.parse("{\"sets\":[{\"family\":\"pll\","
        + "\"cases\":[\"pll_rb\",\"pll_v\"],\"cost_ms\":120,\"drill\":" + PLL_DRILL + "}]}");

    Assert.assertEquals(1, sets.size());
    Assert.assertEquals("pll", sets.get(0).getFamily());
    Assert.assertEquals(Arrays.asList("pll_rb", "pll_v"), sets.get(0).getCases());
    Assert.assertTrue(sets.get(0).getDrill().retiresCases());
    Assert.assertEquals(4800, sets.get(0).getDrill().getTargetMs("pll_rb"));
  }

  /** A drill from a newer service is one set this app cannot run, not a section that cannot draw. */
  @Test
  public void aSetWhoseDrillThisAppCannotRunIsLeftOut() {
    String tooNew = PLL_DRILL.replace("\"spec_version\":3",
        "\"spec_version\":" + (DrillSpec.VERSION + 1));
    List<CoachPractice.Set> sets = CoachPractice.parse("{\"sets\":[{\"family\":\"oll\","
        + "\"drill\":" + tooNew + "},{\"family\":\"pll\",\"drill\":" + PLL_DRILL + "}]}");

    Assert.assertEquals(1, sets.size());
    Assert.assertEquals("pll", sets.get(0).getFamily());
  }

  @Test
  public void nothingKeptOrTextThatIsNoAnswerReadsAsNoSets() {
    Assert.assertTrue(CoachPractice.parse(null).isEmpty());
    Assert.assertTrue(CoachPractice.parse("<html>502</html>").isEmpty());
  }
}
