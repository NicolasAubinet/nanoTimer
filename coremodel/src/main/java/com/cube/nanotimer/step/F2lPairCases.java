package com.cube.nanotimer.step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The F2L cases a pair drill can be dealt, under the codes a solve records them with: the 41 basic
 * ones ({@code pair_1} to {@code pair_41}) and the 51 with a piece trapped in another slot
 * ({@code pair_a1} to {@code pair_a42}, and {@code pair_a1a} to {@code pair_a9a}).
 *
 * <p>Here so that a plan can be checked for a dead drill button without the case reader, which lives
 * in {@code smartcube}; {@code DrillCaseVocabularyTest} pins the two lists to each other.
 */
public final class F2lPairCases {

  private static final String PREFIX = "pair_";
  private static final int BASIC = 41;
  private static final int ADVANCED = 42;
  private static final int ADVANCED_TWINS = 9;

  private static final List<String> STUCK = stuck();
  private static final List<String> ALL = all();

  private F2lPairCases() {
  }

  /** Every case, basic then advanced. */
  public static List<String> codes() {
    return ALL;
  }

  /** The cases with neither piece in another slot. */
  public static List<String> basicCodes() {
    return ALL.subList(0, BASIC);
  }

  /** The cases with a piece trapped in another slot. */
  public static List<String> stuckCodes() {
    return STUCK;
  }

  public static boolean isCase(String code) {
    return ALL.contains(code);
  }

  private static List<String> stuck() {
    List<String> codes = new ArrayList<String>();
    for (int i = 1; i <= ADVANCED; i++) {
      codes.add(PREFIX + "a" + i);
      if (i <= ADVANCED_TWINS) {
        codes.add(PREFIX + "a" + i + "a");
      }
    }
    return Collections.unmodifiableList(codes);
  }

  private static List<String> all() {
    List<String> codes = new ArrayList<String>();
    for (int i = 1; i <= BASIC; i++) {
      codes.add(PREFIX + i);
    }
    codes.addAll(STUCK);
    return Collections.unmodifiableList(codes);
  }
}
