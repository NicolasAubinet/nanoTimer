package com.cube.nanotimer.util.coach;

import com.cube.nanotimer.drill.DrillSpec;

import java.util.Arrays;
import java.util.Collections;
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

  /** The solver picks how many of the next OLLs to see; one cleared since is skipped. */
  @Test
  public void aLearningSetShowsItsFirstCasesWithTheirAlgorithms() {
    DrillSpec learning = DrillSpec.learning("coach-learn-oll", DrillSpec.Type.CASE_EXECUTION,
        Arrays.asList("oll_26", "oll_27", "oll_21", "oll_45"), DrillSpec.Selection.ROUND_ROBIN, 40,
        5, Collections.singletonMap("oll_26", "R U2 R' U' R U' R'"), null);
    CoachPractice.Set set = CoachPractice.parse("{\"sets\":[{\"family\":\"oll\",\"drill\":"
        + learning.toJson() + "}]}").get(0);

    CoachPractice.Set shown = set.firstToLearn(2, Collections.singleton("oll_27"));

    Assert.assertTrue(set.isLearning());
    Assert.assertEquals(3, set.countToLearn(Collections.singleton("oll_27")));
    Assert.assertEquals(Arrays.asList("oll_26", "oll_21"), shown.getCases());
    Assert.assertTrue(shown.getDrill().isLearning());
    Assert.assertEquals(5, shown.getDrill().getWinsToRetire());
    Assert.assertEquals("R U2 R' U' R U' R'", shown.getDrill().getCaseAlgorithm("oll_26"));
    Assert.assertNull(set.firstToLearn(3, set.getCases()));
  }

  @Test
  public void nothingKeptOrTextThatIsNoAnswerReadsAsNoSets() {
    Assert.assertTrue(CoachPractice.parse(null).isEmpty());
    Assert.assertTrue(CoachPractice.parse("<html>502</html>").isEmpty());
  }
}
