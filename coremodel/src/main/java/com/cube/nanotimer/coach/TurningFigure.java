package com.cube.nanotimer.coach;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * How a step, or the whole solve, is turned: moves a solve, and how often the solver stops between
 * two of them once they know what to turn.
 *
 * <p><b>The two ride together because neither reads alone.</b> A low turning speed is a solver
 * turning smoothly and slowly or one turning fast with freezes between, and the advice is opposite;
 * moves and time with nothing else would have a coach tell a stalling solver to turn faster. A stop
 * is a gap over {@code SolveAnalyzer.LOOKAHEAD_PAUSE_MS}, the app's own line for having stopped, so
 * a stall inside the cross is planning and one inside an algorithm is recall.
 *
 * <p><b>No turns per second.</b> It is the moves over a time already sent, and a field of its own
 * could be cited without either half clearing its floor. Moves count what was turned, including
 * what was taken back, since a mistake and its undo are the inefficiency being measured.
 */
public class TurningFigure {

  private final String code;
  private final int count;
  private final double moves;
  private final double stalls;
  private final long stallMs;
  private final double rejectionRate;

  TurningFigure(String code, int count, double moves, double stalls, long stallMs,
      double rejectionRate) {
    this.code = code;
    this.count = count;
    this.moves = moves;
    this.stalls = stalls;
    this.stallMs = stallMs;
    this.rejectionRate = rejectionRate;
  }

  /** The step's family, {@code cross}, or {@link CoachPayloadBuilder#SOLVE} for the whole solve. */
  public String getCode() {
    return code;
  }

  /** How many solves it is read from. */
  public int getCount() {
    return count;
  }

  /** Moves a solve. */
  public double getMoves() {
    return moves;
  }

  /** Stops a solve. */
  public double getStalls() {
    return stalls;
  }

  /** What the stops came to a solve. */
  public long getStallMs() {
    return stallMs;
  }

  public double getRejectionRate() {
    return rejectionRate;
  }

  Double field(String name) {
    if ("count".equals(name)) {
      return Double.valueOf(count);
    }
    if ("moves".equals(name)) {
      return Double.valueOf(moves);
    }
    if ("stalls".equals(name)) {
      return Double.valueOf(stalls);
    }
    if ("stall_ms".equals(name)) {
      return Double.valueOf(stallMs);
    }
    return "rejection_rate".equals(name) ? Double.valueOf(rejectionRate) : null;
  }

  JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("code", code);
    json.put("count", count);
    json.put("moves", tenths(moves));
    json.put("stalls", tenths(stalls));
    json.put("stall_ms", stallMs);
    json.put("rejection_rate", Math.round(rejectionRate * 100) / 100.0);
    return json;
  }

  static TurningFigure fromJson(JSONObject json) throws JSONException {
    return new TurningFigure(json.getString("code"), json.getInt("count"),
        json.getDouble("moves"), json.getDouble("stalls"), json.getLong("stall_ms"),
        json.getDouble("rejection_rate"));
  }

  /** A mean of whole counts is not finer than a tenth. */
  private static double tenths(double value) {
    return Math.round(value * 10) / 10.0;
  }
}
