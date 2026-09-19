package com.cube.nanotimer.cube;

import com.cube.nanotimer.coach.PairExecution;
import com.cube.nanotimer.vo.SolveStep;
import com.cube.nanotimer.vo.SolveTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Every F2L pair of a window's solves, replayed and set against the algorithms its case is usually
 * taught with, for the coach's F2L moves. Read here because only the app can replay a solve.
 *
 * <p>Standard is what {@link AlgorithmFiguresReader} counts as standard, a pair turned with one of
 * its case's listed algorithms, and its extra moves are counted the same way. Unlike those figures
 * every occurrence is kept, not the latest of each case. A pair of a stuck piece, a placement no case
 * names, or moves that cannot be read comes back {@link PairExecution#unlisted}, so its solve is not
 * set against the lists as a whole.
 */
public final class F2lHabitReader {

  private static final String F2L = "f2l";

  private F2lHabitReader() {
  }

  /** @param solves the window's solves, whatever they were solved as: only CFOP is read */
  public static List<PairExecution> readFrom(List<SolveTime> solves) {
    final List<PairExecution> pairs = new ArrayList<PairExecution>();
    for (final SolveTime solve : solves) {
      CaseExecutions.readSteps(solve, new CaseExecutions.StepReader() {
        @Override
        public void read(SolveStep step, SolveSolution.Step turned) {
          if (F2L.equals(step.getName())) {
            readPairs(solve.getId(), step, turned, pairs);
          }
        }
      });
    }
    return pairs;
  }

  private static void readPairs(long solveId, SolveStep step, SolveSolution.Step turned,
      List<PairExecution> pairs) {
    List<SolveStep> parts = step.getSubSteps();
    for (int i = 0; i < parts.size(); i++) {
      long timeMs = parts.get(i).getTotalMs();
      if (i >= turned.getGroups().size()) {
        pairs.add(PairExecution.unlisted(solveId, timeMs)); // no moves to set against anything
        continue;
      }
      String moves = turned.getPartMoves(i);
      if (moves.isEmpty()) {
        continue; // a skip: nothing turned, so nothing over the usual either
      }
      CaseExecutions.Pair pair = CaseExecutions.pairOf(parts.get(i), moves);
      pairs.add(pair == null || pair.getExecution().getUsualMoves() <= 0
          ? PairExecution.unlisted(solveId, timeMs)
          : PairExecution.of(solveId, pair.getCaseCode(), timeMs, pair.getExecution().getMoves(),
              pair.getExecution().getUsualMoves(), pair.isListed()));
    }
  }
}
