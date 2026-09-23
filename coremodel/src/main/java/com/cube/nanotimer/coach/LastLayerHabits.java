package com.cube.nanotimer.coach;

import com.cube.nanotimer.session.AlgorithmFigures;
import com.cube.nanotimer.session.MethodStatistics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * How many moves OLL and PLL take over the algorithms their cases are usually taught with, per step
 * and per case, so a coach can tell a case whose algorithm is long from one whose turning is slow.
 *
 * <p><b>Only a step taken in one algorithm is here.</b> A two-look is already its own card and its
 * own count, and set against the one-look algorithm it would read as a long algorithm, which is the
 * wrong advice for a case that simply has not been learnt. The app leaves those out before they
 * arrive.
 *
 * <p>A case is read exactly as an F2L case is ({@link F2lHabits#cases}), a rate over every
 * occurrence with the outlier rule judging by time. A step's total is the mean of the same extra
 * moves over its executions, so it never comes out under zero the way a whole F2L can, and its three
 * figures add up: a last layer has no look-ahead to buy a move back with.
 */
final class LastLayerHabits {

  private LastLayerHabits() {
  }

  /** Each last layer step with at least {@code floor} executions kept, in solving order. */
  static List<MoveFigure> steps(List<PairExecution> executions, int floor) {
    Map<String, List<PairExecution>> byStep = new LinkedHashMap<String, List<PairExecution>>();
    for (String step : CoachPayloadBuilder.CASE_FAMILIES) {
      byStep.put(step, new ArrayList<PairExecution>());
    }
    for (PairExecution execution : executions) {
      if (!execution.isComparable()) {
        continue;
      }
      List<PairExecution> ofStep = byStep.get(MethodStatistics.familyOf(execution.getCaseCode()));
      if (ofStep != null) {
        ofStep.add(execution);
      }
    }
    List<MoveFigure> figures = new ArrayList<MoveFigure>();
    for (Map.Entry<String, List<PairExecution>> step : byStep.entrySet()) {
      MoveFigure figure = step(step.getKey(), step.getValue(), floor);
      if (figure != null) {
        figures.add(figure);
      }
    }
    return figures;
  }

  /** Each last layer case with at least {@code floor} executions kept, most extra moves first. */
  static List<MoveFigure> cases(List<PairExecution> executions, int floor) {
    return F2lHabits.cases(of(executions), floor);
  }

  /** The executions of a last layer case, which is every one whose case names a case family. */
  static List<PairExecution> of(List<PairExecution> executions) {
    List<PairExecution> lastLayer = new ArrayList<PairExecution>();
    for (PairExecution execution : executions) {
      if (execution.getCaseCode() != null && CoachPayloadBuilder.CASE_FAMILIES.contains(
          MethodStatistics.familyOf(execution.getCaseCode()))) {
        lastLayer.add(execution);
      }
    }
    return lastLayer;
  }

  private static MoveFigure step(String code, List<PairExecution> ofStep, int floor) {
    double[] times = new double[ofStep.size()];
    for (int i = 0; i < times.length; i++) {
      times[i] = ofStep.get(i).getTimeMs();
    }
    boolean[] kept = StepTallies.kept(times);
    int count = 0;
    double turned = 0;
    double usual = 0;
    for (int i = 0; i < kept.length; i++) {
      if (kept[i]) {
        PairExecution execution = ofStep.get(i);
        count++;
        turned += execution.getMoves();
        usual += execution.getMoves() - AlgorithmFigures.extraMoves(execution.isListed(),
            execution.getMoves(), execution.getUsualMoves());
      }
    }
    if (count < floor || count == 0) {
      return null;
    }
    return MoveFigure.step(code, count, turned / count, usual / count, (turned - usual) / count,
        (double) (kept.length - count) / kept.length);
  }
}
