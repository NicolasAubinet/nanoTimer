package com.cube.nanotimer.util;

import android.content.res.Configuration;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.Options.BigCubesNotation;
import com.cube.nanotimer.R;
import com.cube.nanotimer.vo.CubeType;

import java.util.regex.Pattern;

public enum ScrambleFormatterService {
  INSTANCE;

  /**
   * Format the scramble to add enough spaces to align the moves vertically,
   * to split it among multiple lines and to color it.
   * @param scramble the scramble containing one move in each array element
   * @param cubeType the cube type for which to format the scramble
   * @return the formatted scramble
   */
  public Spannable formatToColoredScramble(String[] scramble, CubeType cubeType, int orientation) {
    String s = formatScramble(scramble, cubeType, orientation);
    return colorFormattedScramble(s, cubeType);
  }

  /**
   * Format the scramble to add enough spaces to align the moves vertically,
   * to split it among multiple lines and to color it.
   * @param scramble the scramble with space-separated moves.
   * @param cubeType the cube type for which to format the scramble
   * @return the formatted scramble
   */
  public Spannable formatToColoredScramble(String scramble, CubeType cubeType) {
    if (getMovesPerLine(cubeType) > 0 && !scramble.contains("\n")) {
      String[] splitted = parseStringScrambleToArray(scramble, String.valueOf(getScrambleDelimiter(cubeType)));
      scramble = formatScramble(splitted, cubeType, null);
    }
    return colorFormattedScramble(scramble, cubeType);
  }

  private String[] parseStringScrambleToArray(String scramble, String delimiter) {
    String totalDelimiter = delimiter;
    if (!" ".equals(delimiter)) {
      totalDelimiter += " ";
    }
    String[] splitted = scramble.split(Pattern.quote(totalDelimiter));
    if (!" ".equals(delimiter)) {
      for (int i = 0; i < splitted.length; i++) {
        splitted[i] += delimiter;
      }
    }
    return splitted;
  }

  /**
   * Color a scramble that is already formatted, based on spaces.
   * @param scramble the formatted scramble
   * @param cubeType the cube type for which to color the scramble
   * @return the colored scramble
   */
  private Spannable colorFormattedScramble(String scramble, CubeType cubeType) {
    Spannable span = new SpannableString(scramble);
    final char delimiter = getScrambleDelimiter(cubeType);
    int defaultColor = App.INSTANCE.getContext().getResources().getColor(R.color.white);
    int alternateColor = App.INSTANCE.getContext().getResources().getColor(R.color.lightblue);
    int prevLinesCharCount = 0;
    String[] lines = scramble.split("\n");

    boolean twoElementsPerLine = true;
    for (String line : lines) {
      String totalDelimiter = (delimiter == ' ') ? String.valueOf(delimiter) : (delimiter + " ");
      if (line.split(Pattern.quote(totalDelimiter)).length > 2) {
        twoElementsPerLine = false;
        break;
      }
    }

    String prevLine = "";
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      if (twoElementsPerLine) { // color one line out of two
        span.setSpan(new ForegroundColorSpan((i % 2 == 0) ? defaultColor : alternateColor),
            prevLinesCharCount, prevLinesCharCount + line.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
      } else { // color based on moves
        int prevInd = prevLinesCharCount;
        int colorInd = 0;
        char prevChar = ')'; // initialized with a parenthesis for square-1 to color starting from 2nd column (and not from the 1st one)
        if (line.length() <= prevLine.length() / 2) {
          colorInd = 1; // used for the last square-1 line. if last line is half the size of the previous one, the first move will be colored
        }
        for (int j = 0; j < line.length(); j++) {
          char c = line.charAt(j);
          if (c == delimiter && prevChar != delimiter || j == line.length() - 1) {
            span.setSpan(new ForegroundColorSpan((colorInd % 2 == 0) ? defaultColor : alternateColor),
                prevInd, prevLinesCharCount + j + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            prevInd = prevLinesCharCount + j + 1;
            colorInd++;
          }
          prevChar = c;
        }
      }
      prevLine = line;
      prevLinesCharCount += line.length() + 1;
    }
    return span;
  }

  /**
   * Format the scramble to add enough spaces to align the moves vertically
   * and to split it among multiple lines.
   * @param scramble the scramble containing one move in each array element
   * @param cubeType the cube type for which to format the scramble
   * @param orientation optional parameter to indicate the screen orientation to adjust the scramble
   * @return the formatted scramble
   */
  private String formatScramble(String[] scramble, CubeType cubeType, Integer orientation) {
    int maxMoveLength = 1;
    for (String m : scramble) {
      if (m.length() > maxMoveLength) {
        maxMoveLength = m.length();
      }
    }
    int movesPerLine = getMovesPerLine(cubeType, orientation);
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < scramble.length; i++) {
      s.append(scramble[i]);
      if (movesPerLine > 0) {
        for (int j = scramble[i].length(); j < maxMoveLength; j++) {
          s.append(" ");
        }
        if (movesPerLine > 0 && (i + 1) % movesPerLine == 0 && i > 0) {
          s.append("\n");
        } else if (i < scramble.length - 1) {
          s.append(" ");
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
    return getMovesPerLine(cubeType, null);
  }

  private int getMovesPerLine(CubeType cubeType, Integer orientation) {
    int movesPerLine;
    if (orientation == null) {
      orientation = Configuration.ORIENTATION_PORTRAIT;
    }
    switch (cubeType.getType()) {
      case THREE_BY_THREE:
      case PYRAMINX:
      case SKEWB:
        movesPerLine = 5;
        break;
      case MEGAMINX:
        // don't add line breaks for this, as this a special kind of scramble that is already formatted
        movesPerLine = 0;
        break;
      case SQUARE1:
        movesPerLine = 4;
        break;
      case CLOCK:
        movesPerLine = (orientation == Configuration.ORIENTATION_PORTRAIT) ? 3 : 2;
        break;
      case FOUR_BY_FOUR:
        if (Options.INSTANCE.getBigCubesNotation() == BigCubesNotation.RUF && orientation == Configuration.ORIENTATION_PORTRAIT) {
          movesPerLine = 10;
        } else {
          movesPerLine = 8;
        }
        break;
      case SIX_BY_SIX:
        if (Options.INSTANCE.getBigCubesNotation() == BigCubesNotation.RUF) {
          movesPerLine = 10;
        } else {
          movesPerLine = 8;
        }
        break;
      default:
        movesPerLine = 10;
        break;
    }
    return movesPerLine;
  }

  private char getScrambleDelimiter(CubeType cubeType) {
    char delimiter;
    switch (cubeType.getType()) {
      case SQUARE1:
      case CLOCK:
        delimiter = ')';
        break;
      default:
        delimiter = ' ';
        break;
    }
    return delimiter;
  }

  private int getDefaultTextColor() {
    TextView tv = new TextView(App.INSTANCE.getContext());
    return tv.getCurrentTextColor();
  }

}
