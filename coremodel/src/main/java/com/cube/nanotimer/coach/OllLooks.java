package com.cube.nanotimer.coach;

import com.cube.nanotimer.session.MethodStatistics;
import com.cube.nanotimer.vo.StepStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The OLL step split by how many looks it took, so what a two-look costs this solver is measured
 * rather than assumed.
 *
 * <p>Whether to learn full OLL is usually settled by a rule of thumb (at sub-20, or sub-15), and a
 * solver below it can still be told to learn cases (owner, 2026-09-23). The gap between their own
 * one-look and two-look OLLs, times how often they two-look, is what learning the rest would buy, so
 * a coach can rank it against everything else instead of putting it first by rule.
 *
 * <p>A skipped OLL is neither: it took no look at all, and counted as one-look it would make one-look
 * look cheaper than it is. Both sides go through the outlier rule and are sent together or not at
 * all, the way the F2L groups are.
 */
final class OllLooks {

  /** The OLL steps of the solves that took the orientation in one algorithm. */
  static final String ONE_LOOK = "one_look";

  /** The OLL steps of the solves that took it in more than one. */
  static final String TWO_LOOK = "two_look";

  private static final String OLL_FAMILY = "oll";

  private OllLooks() {
  }

  /**
   * @param twoLooked the solves that looked more than once at the orientation
   * @return both sides above {@code floor}, one-look first, or nothing
   */
  static List<StepFigure> of(List<StepSample> samples, Set<Long> twoLooked, int floor) {
    List<StepSample> split = new ArrayList<StepSample>();
    for (StepSample sample : samples) {
      if (sample.isPart() || !OLL_FAMILY.equals(MethodStatistics.familyOf(sample.getCode()))
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
