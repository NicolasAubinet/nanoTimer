package com.cube.nanotimer.util;

import com.cube.nanotimer.vo.CubeType;

import java.text.SimpleDateFormat;
import java.util.Date;

public enum FormatterService {
  INSTANCE;

  public String formatSolveTime(Long solveTime) {
    return formatSolveTime(solveTime, null);
  }

  public String formatSolveTime(Long solveTime, String defaultValue) {
    if (solveTime == null) {
      return defaultValue == null ? "" : defaultValue;
    }
    StringBuilder sb = new StringBuilder();
    int minutes = (int) (solveTime / 60000);
    int seconds = (int) (solveTime / 1000) % 60;
    int hundreds = Math.round((float) solveTime / 10) % 100;
    if (minutes > 0) {
      sb.append(minutes).append(":");
      sb.append(String.format("%02d", seconds));
    } else {
      sb.append(seconds);
    }
    sb.append(".").append(String.format("%02d", hundreds));
    return sb.toString();
  }

  public String formatDateTime(Long ms) {
    if (ms == null) {
      return "";
    }
    SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy - HH:mm:ss");
    return sdf.format(new Date(ms));
  }

  /**
   * Format the scramble to add enough spaces to align the moves vertically
   * and to split it among multiple lines.
   *
   * @param scramble the scramble in array form
   * @return the formatted scramble
   */
  public String formatScramble(String[] scramble, CubeType cubeType) {
    int maxMoveLength = 1;
    for (String m : scramble) {
      if (m.length() > maxMoveLength) {
        maxMoveLength = m.length();
      }
    }
    int movesPerLine = getMovesPerLine(cubeType);
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < scramble.length; i++) {
      s.append(scramble[i]);
      if (movesPerLine > 0) {
        for (int j = scramble[i].length(); j < maxMoveLength + 1; j++) {
          s.append(" ");
        }
        if (movesPerLine > 0 && (i + 1) % movesPerLine == 0 && i > 0 && i < scramble.length - 1) {
          s.append("\n");
        }
      }
    }
    return s.toString();
  }

  public String formatScrambleAsSingleLine(String[] scramble, CubeType cubeType) {
    StringBuilder sb = new StringBuilder();
    boolean addSpace = getMovesPerLine(cubeType) > 0;
    if (scramble != null) {
      for (String s : scramble) {
        sb.append(s);
        if (addSpace) {
          sb.append(" ");
        }
      }
    }
    return sb.toString();
  }

  private int getMovesPerLine(CubeType cubeType) {
    int movesPerLine;
    switch (cubeType.getType()) {
      case THREE_BY_THREE:
        movesPerLine = 5;
        break;
      case MEGAMINX:
        // don't add line breaks for this, as this a special kind of scramble that is already formatted
        movesPerLine = 0;
        break;
      default:
        movesPerLine = 10;
        break;
    }
    return movesPerLine;
  }

}
