package com.cube.nanotimer.step;

import java.util.HashMap;
import java.util.Map;

/**
 * How often a solve is dealt each last-layer case: the states that read as it, out of the 288 the
 * permutation can be left in or the 216 of the orientation. LastLayerCasesTest recounts them.
 */
public final class LastLayerOdds {

  public static final int PERMUTATION_STATES = 288;
  public static final int ORIENTATION_STATES = 216;

  /** Most PLLs are 16 states (4 AUFs either side), most OLLs 4; these are the symmetric ones. */
  private static final Map<String, Integer> SYMMETRIC = new HashMap<String, Integer>();

  static {
    for (String name : new String[] {"e", "z"}) {
      SYMMETRIC.put("pll_" + name, 8);
    }
    for (String name : new String[] {"h", "na", "nb"}) {
      SYMMETRIC.put("pll_" + name, 4);
    }
    SYMMETRIC.put("oll_20", 1);
    for (String name : new String[] {"1", "21", "55", "56", "57"}) {
      SYMMETRIC.put("oll_" + name, 2);
    }
  }

  private LastLayerOdds() {
  }

  /** How many states read as this case, or 0 for a code that names no last-layer case. */
  public static int states(String caseCode) {
    if (!LastLayerAlgorithms.dealsCase(caseCode)) {
      return 0;
    }
    Integer symmetric = SYMMETRIC.get(caseCode);
    if (symmetric != null) {
      return symmetric.intValue();
    }
    return caseCode.startsWith("pll_") ? 16 : 4;
  }

  /** The share of solves that deal this case, a T perm being 16 in 288, or null if it is no case. */
  public static Double share(String caseCode) {
    int states = states(caseCode);
    if (states == 0) {
      return null;
    }
    return Double.valueOf(states
        / (double) (caseCode.startsWith("pll_") ? PERMUTATION_STATES : ORIENTATION_STATES));
  }
}
