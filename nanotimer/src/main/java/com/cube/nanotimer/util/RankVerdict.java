package com.cube.nanotimer.util;

import android.content.Context;

import com.cube.nanotimer.R;

/**
 * The "5th best ever" line a solve earns by its lifetime rank. One rule for every screen that
 * quotes it, so the timer and the solve details never say different things about the same solve.
 */
public final class RankVerdict {

  /** A rank is worth naming when it is in the all-time top ten, and in the top quarter of them. */
  private static final int TOP_RANK = 10;
  private static final int TOP_PART = 4;

  private RankVerdict() {
  }

  /**
   * Whether a rank below the record is worth a line. The record itself (rank 1) is not: it is
   * announced as a personal best, in its own words.
   *
   * @param rankedCount how many solves the rank was taken among
   */
  public static boolean isNotable(int rank, int rankedCount) {
    return rank > 1 && rank <= TOP_RANK && rank * TOP_PART <= rankedCount;
  }

  /** The line for a notable rank, e.g. "5th best ever". */
  public static String text(Context context, int rank) {
    return context.getResources().getStringArray(R.array.verdict_ranks)[rank - 2];
  }

}
