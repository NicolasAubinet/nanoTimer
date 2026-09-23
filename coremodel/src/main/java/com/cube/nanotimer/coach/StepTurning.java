package com.cube.nanotimer.coach;

/**
 * One step of one solve as the solver turned it: how many moves, and how often they stopped
 * between two of them once the step's recognition was over.
 *
 * <p>Like {@link PairExecution} this is found by replaying the solve, which only the app can do, so
 * it arrives here ready read and the payload builder only counts.
 */
public final class StepTurning {

  private final long solveId;
  private final String code;
  private final long timeMs;
  private final int moves;
  private final int stalls;
  private final long stallMs;
  private final boolean wholeSolve;

  /**
   * @param code the step as the breakdown names it, {@code cross}, {@code f2l}, {@code pll_ja}
   * @param timeMs the step's time, recognition included, which is what the outlier rule judges
   * @param moves the turns it took, counted as a reconstruction counts them
   * @param stalls how many gaps between two of its turns, after recognition, were long enough to
   *     be the solver stopping, and {@code stallMs} what they came to
   * @param wholeSolve whether every step of this solve was read, so its steps add up to the solve
   */
  public StepTurning(long solveId, String code, long timeMs, int moves, int stalls, long stallMs,
      boolean wholeSolve) {
    this.solveId = solveId;
    this.code = code;
    this.timeMs = timeMs;
    this.moves = moves;
    this.stalls = stalls;
    this.stallMs = stallMs;
    this.wholeSolve = wholeSolve;
  }

  public long getSolveId() {
    return solveId;
  }

  public String getCode() {
    return code;
  }

  public long getTimeMs() {
    return timeMs;
  }

  public int getMoves() {
    return moves;
  }

  public int getStalls() {
    return stalls;
  }

  public long getStallMs() {
    return stallMs;
  }

  public boolean isWholeSolve() {
    return wholeSolve;
  }
}
