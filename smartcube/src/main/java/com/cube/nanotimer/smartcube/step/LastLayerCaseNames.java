package com.cube.nanotimer.smartcube.step;

import com.cube.nanotimer.step.OllShapes;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * What a last-layer case is called: the short name a speedcuber writes it as, for an OLL the shape
 * it is known by, and the group it is learnt in. The last two differ for seven cases only, which is
 * why they are two things: a solver says "Sune", and learns it alongside the six others that leave
 * the edges oriented.
 *
 * <p>Left in English on purpose, as the rest of the app's cubing vocabulary is. "Sune" and "Ga" are
 * the words a solver has already learnt from every tutorial and chart there is, and a translated
 * shape name would be a second name for something that already has one.
 */
public final class LastLayerCaseNames {

  /**
   * The seven that have a name of their own as well as a group. They are the ones a solver says out
   * loud, so the picture is captioned with the name and only the filtering goes by the group.
   */
  private static final Map<String, String> ORIENTATION_NAMES = new LinkedHashMap<String, String>();

  static {
    name("H", "21");
    name("Pi", "22");
    name("U", "23");
    name("T", "24");
    name("L", "25");
    name("Antisune", "26");
    name("Sune", "27");
  }

  private LastLayerCaseNames() {
  }

  private static void name(String name, String caseNumber) {
    ORIENTATION_NAMES.put(caseNumber, name);
  }

  /**
   * The case as it is written down: {@code "Ga"} for a permutation, {@code "21"} for an
   * orientation, which is a number because that is the only name an OLL has.
   */
  public static String shortName(String caseCode) {
    String name = bareName(caseCode);
    if (name == null || name.isEmpty()) {
      return name;
    }
    return name.substring(0, 1).toUpperCase(Locale.US) + name.substring(1);
  }

  /**
   * What an OLL is called under its picture: its own name where it has one, otherwise the group it
   * is learnt in. Null for a permutation, which is named by its letter.
   */
  public static String shape(String caseCode) {
    String group = group(caseCode);
    if (group == null) {
      return null;
    }
    String name = ORIENTATION_NAMES.get(bareName(caseCode));
    return name == null ? group : name;
  }

  /**
   * The group an OLL is learnt and filtered in, or null for a permutation. Unlike {@link #shape},
   * this never names a single case: it is what a solver learns a handful of cases as.
   */
  public static String group(String caseCode) {
    if (caseCode == null || !caseCode.startsWith("oll_")) {
      return null;
    }
    return OllShapes.groupOf(caseCode);
  }

  private static String bareName(String caseCode) {
    int split = caseCode == null ? -1 : caseCode.indexOf('_');
    return split < 0 ? caseCode : caseCode.substring(split + 1);
  }
}
