package com.cube.nanotimer.step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The shape group each OLL is learnt in, in the usual grouping. Every group holds at least two
 * cases, which is what {@code OCLL} is doing here: the seven cases that leave the edges already
 * oriented are one group of the set, not seven groups of one. Shape is public knowledge, so it lives
 * here where both the app and the coach read one copy.
 */
public final class OllShapes {

  private static final String OLL_PREFIX = "oll_";

  private static final Map<String, String> GROUPS = new LinkedHashMap<String, String>();

  static {
    put("OCLL", "21", "22", "23", "24", "25", "26", "27");
    put("Dot", "1", "2", "3", "4", "17", "18", "19", "20");
    put("Lightning", "7", "8", "11", "12", "39", "40");
    put("L shape", "47", "48", "49", "50", "53", "54");
    put("Fish", "9", "10", "35", "37");
    put("Knight move", "13", "14", "15", "16");
    put("P shape", "31", "32", "43", "44");
    put("I shape", "51", "52", "55", "56");
    put("Awkward", "29", "30", "41", "42");
    put("T shape", "33", "45");
    put("C shape", "34", "46");
    put("W shape", "36", "38");
    put("Square", "5", "6");
    put("Cross", "28", "57");
  }

  private OllShapes() {
  }

  private static void put(String group, String... cases) {
    for (String name : cases) {
      GROUPS.put(name, group);
    }
  }

  /** The group of an OLL, {@code oll_27} or its bare number, or null for anything else. */
  public static String groupOf(String caseCode) {
    if (caseCode == null) {
      return null;
    }
    return GROUPS.get(caseCode.startsWith(OLL_PREFIX)
        ? caseCode.substring(OLL_PREFIX.length()) : caseCode);
  }

  /** Every OLL of a group, as case codes ({@code oll_27}), in the order above. */
  public static List<String> casesOf(String group) {
    List<String> cases = new ArrayList<String>();
    for (Map.Entry<String, String> entry : GROUPS.entrySet()) {
      if (entry.getValue().equals(group)) {
        cases.add(OLL_PREFIX + entry.getKey());
      }
    }
    return Collections.unmodifiableList(cases);
  }
}
