package com.cube.nanotimer.coach;

import com.cube.nanotimer.session.AlgorithmFigures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * How many moves F2L pairs take over the algorithms their cases are usually taught with, per case and
 * for the whole F2L, so a coach can name a case the solver does the long way time after time.
 *
 * <p><b>A rate over every occurrence</b>, unlike {@link AlgorithmFigures}, which counts each case
 * once as it was last turned: that one asks what the solver turns now, this one how often. A case
 * whose latest turn was the odd one out would otherwise read as a habit, and a habit whose latest
 * turn happened to be the usual one would not read at all.
 *
 * <p><b>The whole F2L goes beside the cases, never without them</b>, because moves are charged to
 * the pair that finishes next: a solver building the next pair during this one looks wasteful here
 * and cheap there, and only the total says whether anything was lost. It is read from the solves
 * whose every pair can be set against a list, since one stuck piece leaves a solve's F2L with
 * nothing to add up to. A pair turned with a listed algorithm is measured against that algorithm, so
 * it adds nothing, and what is left is what the pairs put in off the list cost.
 *
 * <p><b>The whole F2L is the plain difference</b>, turned less usual, and may come out under zero
 * where pairs went in shorter than the shortest algorithm their case is listed with, which is what
 * look-ahead looks like. It is not floored the way an occurrence is: the three figures are one
 * reading of the same solves and have to add up, or a card would say a solver is over by two moves
 * above two numbers that are equal.
 *
 * <p><b>The outlier rule judges an occurrence by its time</b>, as it does everywhere else. Judged
 * by its moves it would do nothing, a case's executions mostly being the same count, and where it
 * did anything it would throw out exactly the long executions this is here to count.
 */
final class F2lHabits {

  private F2lHabits() {
  }

  /** The whole F2L, or null when fewer than {@code floor} solves can be set against the lists. */
  static MoveFigure total(List<PairExecution> pairs, int floor) {
    Map<Long, List<PairExecution>> bySolve = new LinkedHashMap<Long, List<PairExecution>>();
    for (PairExecution pair : pairs) {
      List<PairExecution> ofSolve = bySolve.get(Long.valueOf(pair.getSolveId()));
      if (ofSolve == null) {
        ofSolve = new ArrayList<PairExecution>();
        bySolve.put(Long.valueOf(pair.getSolveId()), ofSolve);
      }
      ofSolve.add(pair);
    }
    List<int[]> moves = new ArrayList<int[]>();
    List<Long> times = new ArrayList<Long>();
    for (List<PairExecution> ofSolve : bySolve.values()) {
      int turned = 0;
      int usual = 0;
      long timeMs = 0;
      boolean comparable = true;
      for (PairExecution pair : ofSolve) {
        comparable &= pair.isComparable();
        turned += pair.getMoves();
        // A listed algorithm is its own usual, so only the other pairs can put the solve over.
        usual += pair.isListed() ? pair.getMoves() : pair.getUsualMoves();
        timeMs += pair.getTimeMs();
      }
      if (comparable) {
        moves.add(new int[] { turned, usual });
        times.add(Long.valueOf(timeMs));
      }
    }
    boolean[] kept = StepTallies.kept(asArray(times));
    int count = 0;
    double turned = 0;
    double usual = 0;
    for (int i = 0; i < kept.length; i++) {
      if (kept[i]) {
        count++;
        turned += moves.get(i)[0];
        usual += moves.get(i)[1];
      }
    }
    if (count < floor || count == 0) {
      return null;
    }
    return MoveFigure.total(count, turned / count, usual / count, (turned - usual) / count,
        (double) (kept.length - count) / kept.length);
  }

  /** Each case with at least {@code floor} occurrences kept, most extra moves first. */
  static List<MoveFigure> cases(List<PairExecution> pairs, int floor) {
    Map<String, List<PairExecution>> byCase = new LinkedHashMap<String, List<PairExecution>>();
    for (PairExecution pair : pairs) {
      if (!pair.isComparable()) {
        continue;
      }
      List<PairExecution> ofCase = byCase.get(pair.getCaseCode());
      if (ofCase == null) {
        ofCase = new ArrayList<PairExecution>();
        byCase.put(pair.getCaseCode(), ofCase);
      }
      ofCase.add(pair);
    }
    List<MoveFigure> figures = new ArrayList<MoveFigure>();
    for (Map.Entry<String, List<PairExecution>> entry : byCase.entrySet()) {
      MoveFigure figure = pairCase(entry.getKey(), entry.getValue(), floor);
      if (figure != null) {
        figures.add(figure);
      }
    }
    Collections.sort(figures, new Comparator<MoveFigure>() {
      @Override
      public int compare(MoveFigure a, MoveFigure b) {
        int byExtra = Double.compare(b.getExtraMoves(), a.getExtraMoves());
        if (byExtra != 0) {
          return byExtra;
        }
        int byOffList = Double.compare(b.getOffListRate().doubleValue(),
            a.getOffListRate().doubleValue());
        return byOffList != 0 ? byOffList : a.getCode().compareTo(b.getCode());
      }
    });
    return figures;
  }

  private static MoveFigure pairCase(String code, List<PairExecution> ofCase, int floor) {
    double[] times = new double[ofCase.size()];
    for (int i = 0; i < times.length; i++) {
      times[i] = ofCase.get(i).getTimeMs();
    }
    boolean[] kept = StepTallies.kept(times);
    int count = 0;
    int offList = 0;
    double extra = 0;
    for (int i = 0; i < kept.length; i++) {
      if (kept[i]) {
        PairExecution pair = ofCase.get(i);
        count++;
        offList += pair.isListed() ? 0 : 1;
        extra += AlgorithmFigures.extraMoves(pair.isListed(), pair.getMoves(),
            pair.getUsualMoves());
      }
    }
    if (count < floor || count == 0) {
      return null;
    }
    return MoveFigure.pairCase(code, count, extra / count, (double) offList / count,
        (double) (kept.length - count) / kept.length);
  }

  private static double[] asArray(List<Long> values) {
    double[] array = new double[values.size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = values.get(i).longValue();
    }
    return array;
  }
}
