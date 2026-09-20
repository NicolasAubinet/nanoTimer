package com.cube.nanotimer.cube;

import com.cube.nanotimer.session.MethodStatistics;
import com.cube.nanotimer.smartcube.step.AlgorithmExecution;
import com.cube.nanotimer.smartcube.step.AlgorithmForm;
import com.cube.nanotimer.smartcube.step.CaseAlgorithms;
import com.cube.nanotimer.vo.SolveStep;
import com.cube.nanotimer.vo.SolveTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * What one solve turned for each of its cases, read out of the solve and cheap to keep.
 *
 * <p>Reading it is not cheap: nothing stores a case's moves, so the scramble has to be solved again
 * and every execution set against the algorithms its case is taught with. Measured over 148 solves,
 * the replay is a fifth of that and the matching the other four fifths, and the same window is read
 * again on every Analysis open and every plan. The answer for a solve that has happened cannot
 * change, so it is read once and kept ({@code SolveReadingStore}).
 *
 * <p><b>Versioned, because the reading can change and the rows cannot.</b> A change to how a solve
 * is stepped, to the algorithm lists, or to what counts as one of them makes every stored row a lie,
 * and rows written by another version are ignored rather than trusted. Bump {@link #VERSION} for any
 * of those.
 *
 * <p>What is kept is deliberately not one screen's answer. Both readers want the same executions
 * and fold them differently, so the fold stays with them: a last layer step keeps whether it was
 * one of the usual algorithms <em>and</em> how many algorithms it took, rather than the one figure
 * {@link AlgorithmFiguresReader} makes of the two.
 */
public final class SolveReading {

  /**
   * Bump on any change to how a solve is read: the stepping, the algorithm lists, or what counts
   * as one of them. Rows written by another version are read again rather than believed.
   */
  public static final int VERSION = 1;

  /** The F2L step, whose parts are read a pair at a time where a last layer step is read whole. */
  public static final String F2L = "f2l";

  private SolveReading() {
  }

  /** One case execution of a solve. */
  public static final class Execution {

    private final String family;
    private final String caseCode;
    private final long timeMs;
    private final int moves;
    private final int usualMoves;
    private final boolean listed;
    private final int algorithms;

    /**
     * @param family the step it belongs to, {@code f2l} / {@code oll} / {@code pll}
     * @param caseCode the case turned, or null for an F2L pair whose placement names no case, whose
     *     piece was stuck elsewhere, or whose moves no algorithm can be told from: it still counts
     *     as a pair that happened, which is why it is kept rather than dropped
     * @param moves how many were turned, {@code usualMoves} how many the shortest listed algorithm
     *     takes, both 0 where there was nothing to compare
     * @param listed whether it went in with one of the case's listed algorithms
     * @param algorithms how many algorithms the step took, 1 for a pair and 2 for a two-looked last
     *     layer; kept apart from {@code listed} because only some readers count a two-look against
     *     the solver
     */
    public Execution(String family, String caseCode, long timeMs, int moves, int usualMoves,
        boolean listed, int algorithms) {
      this.family = family;
      this.caseCode = caseCode;
      this.timeMs = timeMs;
      this.moves = moves;
      this.usualMoves = usualMoves;
      this.listed = listed;
      this.algorithms = algorithms;
    }

    public String getFamily() {
      return family;
    }

    public String getCaseCode() {
      return caseCode;
    }

    public long getTimeMs() {
      return timeMs;
    }

    public int getMoves() {
      return moves;
    }

    public int getUsualMoves() {
      return usualMoves;
    }

    public boolean isListed() {
      return listed;
    }

    public int getAlgorithms() {
      return algorithms;
    }

    /** Whether there is anything to set against the case's algorithms at all. */
    public boolean isRead() {
      return caseCode != null && usualMoves > 0;
    }
  }

  /**
   * Reads a solve, which means solving its scramble again. The expensive path, and the one a kept
   * reading exists to skip.
   *
   * @return its executions in solving order, empty for a solve that is not a readable CFOP one
   */
  public static List<Execution> read(SolveTime solve) {
    final List<Execution> executions = new ArrayList<Execution>();
    CaseExecutions.readSteps(solve, new CaseExecutions.StepReader() {
      @Override
      public void read(SolveStep step, SolveSolution.Step turned) {
        if (F2L.equals(step.getName())) {
          readPairs(step, turned, executions);
        } else {
          readLastLayer(step, turned, executions);
        }
      }
    });
    return executions;
  }

  private static void readPairs(SolveStep step, SolveSolution.Step turned,
      List<Execution> executions) {
    List<SolveStep> parts = step.getSubSteps();
    for (int i = 0; i < parts.size(); i++) {
      long timeMs = parts.get(i).getTotalMs();
      if (i >= turned.getGroups().size()) {
        executions.add(unread(F2L, timeMs)); // no moves to set against
        continue;
      }
      String moves = turned.getPartMoves(i);
      if (moves.isEmpty()) {
        continue; // a skip: nothing turned, so nothing over the usual either
      }
      CaseExecutions.Pair pair = CaseExecutions.pairOf(parts.get(i), moves);
      executions.add(pair == null ? unread(F2L, timeMs)
          : new Execution(F2L, pair.getCaseCode(), timeMs,
              pair.getExecution().getMoves(), pair.getExecution().getUsualMoves(),
              pair.isListed(), 1));
    }
  }

  private static void readLastLayer(SolveStep step, SolveSolution.Step turned,
      List<Execution> executions) {
    String caseCode = CaseExecutions.caseOfStep(step.getName());
    if (caseCode == null) {
      return;
    }
    // Every algorithm the step took, run together, so a two-look can be measured against the one.
    StringBuilder moves = new StringBuilder();
    for (int i = 0; i < turned.getGroups().size(); i++) {
      moves.append(' ').append(turned.getPartMoves(i));
    }
    String turnedMoves = moves.toString().trim();
    if (turnedMoves.isEmpty() || AlgorithmForm.key(turnedMoves) == null) {
      return; // nothing turned, or nothing that can be read: an older execution may say more
    }
    AlgorithmExecution read = CaseAlgorithms.read(caseCode, turnedMoves);
    executions.add(new Execution(MethodStatistics.familyOf(caseCode),
        caseCode, step.getTotalMs(), read.getMoves(), read.getUsualMoves(), !read.isUnusual(),
        Math.max(1, step.getSubSteps().size())));
  }

  /** A pair that happened but can be set against nothing: it still counts as a pair. */
  private static Execution unread(String family, long timeMs) {
    return new Execution(family, null, timeMs, 0, 0, false, 1);
  }

  public static String toJson(List<Execution> executions) {
    JSONArray array = new JSONArray();
    try {
      for (Execution execution : executions) {
        JSONObject json = new JSONObject();
        json.put("family", execution.family);
        if (execution.caseCode != null) {
          json.put("case", execution.caseCode);
        }
        json.put("time_ms", execution.timeMs);
        json.put("moves", execution.moves);
        json.put("usual_moves", execution.usualMoves);
        json.put("listed", execution.listed);
        json.put("algorithms", execution.algorithms);
        array.put(json);
      }
    } catch (JSONException e) {
      throw new IllegalStateException("Cannot write a solve reading", e);
    }
    return array.toString();
  }

  /**
   * @return what was kept, or null where the text cannot be read, which is answered by reading the
   *     solve again rather than by failing: a reading is a cache and never the only copy
   */
  public static List<Execution> parse(String text) {
    List<Execution> executions = new ArrayList<Execution>();
    try {
      JSONArray array = new JSONArray(text);
      for (int i = 0; i < array.length(); i++) {
        JSONObject json = array.getJSONObject(i);
        executions.add(new Execution(json.getString("family"), json.optString("case", null),
            json.getLong("time_ms"), json.getInt("moves"), json.getInt("usual_moves"),
            json.getBoolean("listed"), json.getInt("algorithms")));
      }
    } catch (JSONException e) {
      return null;
    }
    return executions;
  }
}
