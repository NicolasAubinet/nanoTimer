package com.cube.nanotimer.coach;

import com.cube.nanotimer.session.MethodStatistics;
import com.cube.nanotimer.vo.StepStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A last layer step, OLL or PLL, split by how many looks it took, so what a two-look costs this
 * solver is measured rather than assumed.
 *
 * <p>Whether to learn the full set is usually settled by a rule of thumb (full OLL at sub-20, or
 * sub-15), and a solver below it can still be told to learn cases (owner, 2026-09-23). The gap
 * between their own one-look and two-look steps, times how often they two-look, is what learning the
 * rest would buy, so a coach can rank it against everything else instead of putting it first by rule.
 *
 * <p>A skipped step is neither: it took no look at all, and counted as one-look it would make one-look
 * look cheaper than it is. Both sides go through the outlier rule and are sent together or not at
 * all, the way the F2L groups are.
 */
final class LastLayerLooks {

  /** The steps of the solves that took it in one algorithm. */
  static final String ONE_LOOK = "one_look";

  /** The steps of the solves that took it in more than one. */
  static final String TWO_LOOK = "two_look";

  private LastLayerLooks() {
  }

  /**
   * @param family the step split, "oll" or "pll"
   * @param twoLooked the solves that looked more than once at it
   * @return both sides above {@code floor}, one-look first, or nothing
   */
  static List<StepFigure> of(List<StepSample> samples, String family, Set<Long> twoLooked,
      int floor) {
    List<StepSample> split = new ArrayList<StepSample>();
    for (StepSample sample : samples) {
      if (sample.isPart() || !family.equals(MethodStatistics.familyOf(sample.getCode()))
          || MethodStatistics.SKIP.equals(MethodStatistics.caseOf(sample.getCode()))) {
        continue;
      }
      String side = twoLooked.contains(Long.valueOf(sample.getSolveId())) ? TWO_LOOK : ONE_LOOK;
      split.add(new StepSample(side, sample.getTimeMs(), sample.getRecognitionMs(), false,
          sample.getSolveId()));
    }
    StepTallies tallies = new StepTallies(split);
    StepStats one = tallies.get(ONE_LOOK);
    StepStats two = tallies.get(TWO_LOOK);
    List<StepFigure> figures = new ArrayList<StepFigure>();
    if (one == null || two == null || one.getCount() < floor || two.getCount() < floor) {
      return figures;
    }
    figures.add(StepFigure.group(one, tallies.getRejectionRate(ONE_LOOK), null));
    figures.add(StepFigure.group(two, tallies.getRejectionRate(TWO_LOOK), null));
    return figures;
  }
}
