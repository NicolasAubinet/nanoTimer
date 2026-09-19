package com.cube.nanotimer.coach;

/**
 * One F2L pair as the solver turned it, set against the algorithms its case is usually taught with.
 *
 * <p>Nothing stores this: a solve keeps its moves, and which of them went into which pair is found
 * by replaying it, which only the app can do. So it arrives here ready read, and the payload builder
 * stays a thing that counts rather than a thing that replays.
 */
public final class PairExecution {

  private final long solveId;
  private final String caseCode;
  private final long timeMs;
  private final int moves;
  private final int usualMoves;
  private final boolean listed;

  private PairExecution(long solveId, String caseCode, long timeMs, int moves, int usualMoves,
      boolean listed) {
    this.solveId = solveId;
    this.caseCode = caseCode;
    this.timeMs = timeMs;
    this.moves = moves;
    this.usualMoves = usualMoves;
    this.listed = listed;
  }

  /**
   * A pair of a case with a list of usual algorithms.
   *
   * @param caseCode the case, {@code pair_27}
   * @param timeMs the pair's time, recognition included, which is what the outlier rule judges
   * @param moves the turns it took, and {@code usualMoves} those of the shortest algorithm its case
   *     is listed with, both counted as the tables count them
   * @param listed whether what was turned is one of the case's listed algorithms
   */
  public static PairExecution of(long solveId, String caseCode, long timeMs, int moves,
      int usualMoves, boolean listed) {
    return new PairExecution(solveId, caseCode, timeMs, moves, usualMoves, listed);
  }

  /**
   * A pair turned with nothing to compare it with: a piece stuck in another slot, a placement no
   * case names, moves that cannot be read. It says only that its solve's F2L cannot be set against
   * the usual algorithms as a whole.
   */
  public static PairExecution unlisted(long solveId, long timeMs) {
    return new PairExecution(solveId, null, timeMs, 0, 0, false);
  }

  public long getSolveId() {
    return solveId;
  }

  /** The case, or null for a pair {@link #unlisted} with nothing to compare it with. */
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

  /** Whether there is anything to set it against. */
  public boolean isComparable() {
    return caseCode != null && usualMoves > 0;
  }
}
