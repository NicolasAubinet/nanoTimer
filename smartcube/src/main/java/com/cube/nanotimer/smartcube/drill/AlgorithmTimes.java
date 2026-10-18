package com.cube.nanotimer.smartcube.drill;

import com.cube.nanotimer.smartcube.step.CaseAlgorithms;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The last few execution times of each algorithm tried for one case, so they can be compared while
 * choosing one. Two spellings of the same turning share their times.
 */
public final class AlgorithmTimes {

  /** How many times an algorithm keeps, and how many make a full average. */
  public static final int KEPT = 5;

  /** What a rep turned with an algorithm on screen says about that algorithm's time. */
  public enum Verdict {
    COUNTED,
    /** The turns were not the algorithm alone: a slip, its fix, or a wrong angle put right. */
    WRONG_MOVES,
    /** A restarted attempt, at a case already read and begun on. */
    RESET,
    /** Nothing was solved, or the algorithm was looked up part way. */
    NONE
  }

  private final String caseCode;
  /** Times by algorithm, oldest first, keyed by the turning so spellings fold together. */
  private final Map<String, List<Long>> times = new LinkedHashMap<String, List<Long>>();

  private AlgorithmTimes(String caseCode) {
    this.caseCode = caseCode;
  }

  /**
   * @param strayed whether the turns left the algorithm at some point, which a slip undone at once
   *     or a wrong angle put right no longer show in the turns as a whole
   */
  public static Verdict judge(DrillRep rep, String algorithm, boolean strayed) {
    if (rep.isAbandoned() || rep.wasRevealed()) {
      return Verdict.NONE;
    }
    if (rep.getResetCount() > 0) {
      return Verdict.RESET;
    }
    return !strayed && CaseAlgorithms.sameTurning(rep.getCaseCode(), rep.getTurned(), algorithm)
        ? Verdict.COUNTED : Verdict.WRONG_MOVES;
  }

  /** Read back from {@link #toString}; anything unreadable is dropped rather than failing. */
  public static AlgorithmTimes parse(String caseCode, String stored) {
    AlgorithmTimes parsed = new AlgorithmTimes(caseCode);
    if (stored == null) {
      return parsed;
    }
    for (String line : stored.split("\n")) {
      int tab = line.indexOf('\t');
      if (tab <= 0) {
        continue;
      }
      List<Long> kept = new ArrayList<Long>();
      for (String ms : line.substring(tab + 1).split(",")) {
        try {
          kept.add(Long.parseLong(ms.trim()));
        } catch (NumberFormatException e) {
          // a damaged figure, skipped
        }
      }
      if (!kept.isEmpty()) {
        parsed.times.put(line.substring(0, tab), kept);
      }
    }
    return parsed;
  }

  /** Adds the time of a rep {@link #judge} counted, the oldest dropped past {@link #KEPT}. */
  public void add(String algorithm, long executionMs) {
    String key = keyOf(algorithm);
    List<Long> kept = times.get(key);
    if (kept == null) {
      kept = new ArrayList<Long>();
      times.put(key, kept);
    }
    kept.add(executionMs);
    while (kept.size() > KEPT) {
      kept.remove(0);
    }
  }

  /** The algorithm's kept times, oldest first. */
  public List<Long> get(String algorithm) {
    List<Long> kept = algorithm == null ? null : times.get(keyOf(algorithm));
    return kept == null ? Collections.<Long>emptyList() : Collections.unmodifiableList(kept);
  }

  /** The mean of the kept times, or -1 with none. */
  public long mean(String algorithm) {
    List<Long> kept = get(algorithm);
    if (kept.isEmpty()) {
      return -1;
    }
    long total = 0;
    for (long ms : kept) {
      total += ms;
    }
    return Math.round((double) total / kept.size());
  }

  /** The fastest of those with a full average, or null unless two of them have one to compare. */
  public String fastest(Collection<String> algorithms) {
    String fastest = null;
    int full = 0;
    for (String algorithm : algorithms) {
      if (get(algorithm).size() < KEPT) {
        continue;
      }
      full++;
      if (fastest == null || mean(algorithm) < mean(fastest)) {
        fastest = algorithm;
      }
    }
    return full >= 2 ? fastest : null;
  }

  /** One line per algorithm: its turning, a tab, its times. */
  @Override
  public String toString() {
    StringBuilder stored = new StringBuilder();
    for (Map.Entry<String, List<Long>> entry : times.entrySet()) {
      if (stored.length() > 0) {
        stored.append('\n');
      }
      stored.append(entry.getKey()).append('\t');
      for (int i = 0; i < entry.getValue().size(); i++) {
        stored.append(i == 0 ? "" : ",").append(entry.getValue().get(i));
      }
    }
    return stored.toString();
  }

  private String keyOf(String algorithm) {
    String key = CaseAlgorithms.keyAsDrawn(caseCode, algorithm);
    return key != null ? key : algorithm.trim(); // unreadable notation keeps its own spelling
  }
}
