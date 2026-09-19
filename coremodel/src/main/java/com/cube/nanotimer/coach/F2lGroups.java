package com.cube.nanotimer.coach;

import com.cube.nanotimer.session.MethodStatistics;
import com.cube.nanotimer.vo.StepStats;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The F2L pairs gathered into groups a coach can say something about, where no single case can be.
 *
 * <p>Per-case F2L timing is not sent: a case's figure from one half of a history agrees with the
 * other half's at r = 0.34, so a ranking of cases would be noise. Groups pool enough pairs to stand,
 * and they ask two questions with a known fix each:
 *
 * <ul>
 *   <li><b>Stuck pieces</b>: the pairs handed with a piece trapped in another unsolved slot (the
 *       advanced cases, {@code pair_a*}) against the basic ones ({@code pair_<number>}). A placement
 *       neither set names, a skip, and a pair stored with only its slot are in neither group.
 *   <li><b>The first pair</b>: the pause before the first pair after the cross against the pause
 *       before each later one, which says whether the cross ends in a hunt for the first pair.
 * </ul>
 *
 * <p>Each group goes through the outlier rule as one code, and a comparison is sent whole or not at
 * all: a group above its floor beside one below it compares with nothing. No case number is in any
 * code here, so nothing downstream can show one.
 */
final class F2lGroups {

  /** The pairs handed with both pieces in the top layer or in their own slot. */
  static final String BASIC = "f2l_basic";

  /** The pairs handed with a piece trapped in another unsolved slot. */
  static final String STUCK = "f2l_stuck";

  /** The first pair each solve worked on after the cross. */
  static final String FIRST = "f2l_first";

  /** Every pair after the first. */
  static final String LATER = "f2l_later";

  private static final String PAIR_FAMILY = "pair";
  private static final Pattern BASIC_CASE = Pattern.compile("\\d+");
  private static final Pattern ADVANCED_CASE = Pattern.compile("a\\d+a?");

  private F2lGroups() {
  }

  /** The groups above {@code floor}, basic and stuck first, then first and later. */
  static List<StepFigure> of(List<StepSample> samples, int floor) {
    List<StepSample> byCase = new ArrayList<StepSample>();
    int basic = 0;
    int stuck = 0;
    for (StepSample pair : pairs(samples)) {
      String caseName = MethodStatistics.caseOf(pair.getCode());
      if (caseName == null) {
        continue;
      }
      if (BASIC_CASE.matcher(caseName).matches()) {
        byCase.add(relabel(pair, BASIC));
        basic++;
      } else if (ADVANCED_CASE.matcher(caseName).matches()) {
        byCase.add(relabel(pair, STUCK));
        stuck++;
      }
    }
    List<StepFigure> figures = new ArrayList<StepFigure>();
    Double share = basic + stuck == 0 ? null : Double.valueOf((double) stuck / (basic + stuck));
    figures.addAll(compared(byCase, BASIC, STUCK, share, floor));
    figures.addAll(compared(byPosition(samples), FIRST, LATER, null, floor));
    return figures;
  }

  /**
   * Every pair but a skip, first or later by where it came among its solve's pairs. A skip had no
   * pause of its own, so the first pair is the first one actually worked on; a pair whose place was
   * not read is left out rather than guessed.
   */
  private static List<StepSample> byPosition(List<StepSample> samples) {
    Map<Long, Integer> firstOf = new LinkedHashMap<Long, Integer>();
    List<StepSample> placed = new ArrayList<StepSample>();
    for (StepSample pair : pairs(samples)) {
      if (pair.getPartIndex() == null
          || MethodStatistics.SKIP.equals(MethodStatistics.caseOf(pair.getCode()))) {
        continue;
      }
      placed.add(pair);
      Long solve = Long.valueOf(pair.getSolveId());
      Integer first = firstOf.get(solve);
      if (first == null || pair.getPartIndex().intValue() < first.intValue()) {
        firstOf.put(solve, pair.getPartIndex());
      }
    }
    List<StepSample> grouped = new ArrayList<StepSample>();
    for (StepSample pair : placed) {
      boolean first = pair.getPartIndex().equals(firstOf.get(Long.valueOf(pair.getSolveId())));
      grouped.add(relabel(pair, first ? FIRST : LATER));
    }
    return grouped;
  }

  private static List<StepSample> pairs(List<StepSample> samples) {
    List<StepSample> pairs = new ArrayList<StepSample>();
    for (StepSample sample : samples) {
      if (sample.isPart() && PAIR_FAMILY.equals(MethodStatistics.familyOf(sample.getCode()))) {
        pairs.add(sample);
      }
    }
    return pairs;
  }

  /** Both groups of a comparison, or neither when either is under the floor. */
  private static List<StepFigure> compared(List<StepSample> grouped, String one, String other,
      Double otherShare, int floor) {
    StepTallies tallies = new StepTallies(grouped);
    StepStats a = tallies.get(one);
    StepStats b = tallies.get(other);
    List<StepFigure> figures = new ArrayList<StepFigure>();
    if (a == null || b == null || a.getCount() < floor || b.getCount() < floor) {
      return figures;
    }
    figures.add(StepFigure.group(a, tallies.getRejectionRate(one), null));
    figures.add(StepFigure.group(b, tallies.getRejectionRate(other), otherShare));
    return figures;
  }

  private static StepSample relabel(StepSample pair, String group) {
    return new StepSample(group, pair.getTimeMs(), pair.getRecognitionMs(), false,
        pair.getSolveId(), pair.getPartIndex());
  }
}
