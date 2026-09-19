package com.cube.nanotimer.coach;

import com.cube.nanotimer.session.AlgorithmFigures;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * What F2L pairs cost in moves against the algorithms their cases are usually taught with: one case
 * over every time it came up, or the whole F2L over the solves that can be set against them.
 *
 * <p>Extra moves are {@link AlgorithmFigures#extraMoves}, the one definition every screen shares:
 * none for a listed algorithm, never below zero, so a case's figure is the mean of its occurrences'
 * extra moves. Like {@link StepFigure} it carries its count, and anything that does not apply is
 * absent rather than zero.
 */
public class MoveFigure {

  private final String code;
  private final int count;
  private final double extraMoves;
  private final double rejectionRate;
  private final Double offListRate;
  private final Double turnedMoves;
  private final Double usualMoves;

  private MoveFigure(String code, int count, double extraMoves, double rejectionRate,
      Double offListRate, Double turnedMoves, Double usualMoves) {
    this.code = code;
    this.count = count;
    this.extraMoves = extraMoves;
    this.rejectionRate = rejectionRate;
    this.offListRate = offListRate;
    this.turnedMoves = turnedMoves;
    this.usualMoves = usualMoves;
  }

  /**
   * One case over every occurrence kept.
   *
   * @param offListRate how many of them were turned with none of the case's listed algorithms
   */
  static MoveFigure pairCase(String code, int count, double extraMoves, double offListRate,
      double rejectionRate) {
    return new MoveFigure(code, count, extraMoves, rejectionRate, Double.valueOf(offListRate),
        null, null);
  }

  /**
   * The whole F2L, a mean per solve, where {@code extraMoves} is {@code turnedMoves} less
   * {@code usualMoves} and may be under zero.
   */
  static MoveFigure total(int count, double turnedMoves, double usualMoves, double extraMoves,
      double rejectionRate) {
    return new MoveFigure(null, count, extraMoves, rejectionRate, null,
        Double.valueOf(turnedMoves), Double.valueOf(usualMoves));
  }

  /** The case, or null for the whole F2L. */
  public String getCode() {
    return code;
  }

  /** How many occurrences, or for the whole F2L how many solves, the figure is read from. */
  public int getCount() {
    return count;
  }

  /** For a case never under zero; for the whole F2L the plain difference, which may be. */
  public double getExtraMoves() {
    return extraMoves;
  }

  public double getRejectionRate() {
    return rejectionRate;
  }

  /** How often a case went in with none of its listed algorithms; null for the whole F2L. */
  public Double getOffListRate() {
    return offListRate;
  }

  /** The moves the whole F2L took a solve; null for a case. */
  public Double getTurnedMoves() {
    return turnedMoves;
  }

  /**
   * The moves its pairs' usual algorithms add up to a solve, a pair turned with a listed algorithm
   * counting as that one; null for a case.
   */
  public Double getUsualMoves() {
    return usualMoves;
  }

  Double field(String name) {
    if ("count".equals(name)) {
      return Double.valueOf(count);
    }
    if ("extra_moves".equals(name)) {
      return Double.valueOf(extraMoves);
    }
    if ("rejection_rate".equals(name)) {
      return Double.valueOf(rejectionRate);
    }
    if ("off_list_rate".equals(name)) {
      return offListRate;
    }
    if ("turned_moves".equals(name)) {
      return turnedMoves;
    }
    return "usual_moves".equals(name) ? usualMoves : null;
  }

  JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    if (code != null) {
      json.put("code", code);
    }
    json.put("count", count);
    json.put("extra_moves", tenths(extraMoves));
    json.put("rejection_rate", hundredths(rejectionRate));
    if (offListRate != null) {
      json.put("off_list_rate", hundredths(offListRate.doubleValue()));
    }
    if (turnedMoves != null) {
      json.put("turned_moves", tenths(turnedMoves.doubleValue()));
    }
    if (usualMoves != null) {
      json.put("usual_moves", tenths(usualMoves.doubleValue()));
    }
    return json;
  }

  static MoveFigure fromJson(JSONObject json) throws JSONException {
    return new MoveFigure(json.has("code") ? json.getString("code") : null, json.getInt("count"),
        json.getDouble("extra_moves"), json.getDouble("rejection_rate"),
        json.has("off_list_rate") ? Double.valueOf(json.getDouble("off_list_rate")) : null,
        json.has("turned_moves") ? Double.valueOf(json.getDouble("turned_moves")) : null,
        json.has("usual_moves") ? Double.valueOf(json.getDouble("usual_moves")) : null);
  }

  /** Moves go out to a tenth: a mean of whole turns is not finer than that. */
  private static double tenths(double moves) {
    return Math.round(moves * 10) / 10.0;
  }

  private static double hundredths(double rate) {
    return Math.round(rate * 100) / 100.0;
  }
}
