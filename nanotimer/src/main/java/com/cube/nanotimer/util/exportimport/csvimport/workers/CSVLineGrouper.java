package com.cube.nanotimer.util.exportimport.csvimport.workers;

import java.util.ArrayList;
import java.util.List;

public class CSVLineGrouper {

  /**
   * Groups physical lines that belong to the same CSV record. A quoted field (typically a
   * scramble, e.g. Megaminx) may contain real newlines and therefore span several physical
   * lines: such a line leaves an odd number of '"' open, so the following line(s) get appended
   * to it until the quotes balance out again.
   *
   * This relies on every well-formed record having an even number of quotes. Free-text fields
   * (comments) must therefore never contain a raw '"' (see ExportResultConverter.encodeComment),
   * otherwise an unbalanced comment would wrongly swallow the next record.
   */
  public static List<String> group(List<String> lines) {
    List<String> grouped = new ArrayList<String>();
    boolean inUnendedQuotes = false;
    boolean prevLineInUnendedQuotes = false;
    for (String line : lines) {
      for (char c : line.toCharArray()) {
        if (c == '"') {
          inUnendedQuotes = !inUnendedQuotes;
        }
      }
      if (prevLineInUnendedQuotes) {
        int index = grouped.size() - 1;
        String l = grouped.get(index);
        grouped.set(index, l + "\n" + line);
      } else {
        grouped.add(line);
      }
      prevLineInUnendedQuotes = inUnendedQuotes;
    }
    return grouped;
  }
}
