package com.cube.nanotimer.cube;

import com.cube.nanotimer.coach.PairExecution;
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

  private F2lHabitReader() {
  }

  /**
   * @param solves the window's solves, whatever they were solved as: only CFOP is read
   * @param readings where each solve's executions come from, read there only where none was kept
   */
  public static List<PairExecution> readFrom(List<SolveTime> solves, SolveReadings readings) {
    List<PairExecution> pairs = new ArrayList<PairExecution>();
    for (SolveTime solve : solves) {
      for (SolveReading.Execution execution : readings.of(solve)) {
        if (!SolveReading.F2L.equals(execution.getFamily())) {
          continue;
        }
        pairs.add(execution.isRead()
            ? PairExecution.of(solve.getId(), execution.getCaseCode(), execution.getTimeMs(),
                execution.getMoves(), execution.getUsualMoves(), execution.isListed())
            : PairExecution.unlisted(solve.getId(), execution.getTimeMs()));
      }
    }
    return pairs;
  }

  /** As above, reading every solve and keeping nothing. */
  public static List<PairExecution> readFrom(List<SolveTime> solves) {
    return readFrom(solves, SolveReadings.none());
  }
}
