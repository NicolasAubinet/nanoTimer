package com.cube.nanotimer.util;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.CubeType.Type;

public enum ScrambleFormatterService {
  INSTANCE;

  /**
   * Format the scramble to add enough spaces to align the moves vertically,
   * to split it among multiple lines and to color it.
   * @param scramble the scramble containing one move in each array element
   * @param cubeType the cube type for which to format the scramble
   * @return the formatted scramble
   */
  public Spannable formatToColoredScramble(String[] scramble, CubeType cubeType) {
    String s = formatScramble(scramble, cubeType);
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
    if (getMovesPerLine(cubeType) > 0) {
      scramble = formatScramble(scramble.split(" "), cubeType);
    }
    return colorFormattedScramble(scramble, cubeType);
  }

  /**
   * Color a scramble that is already formatted, based on spaces.
   * @param scramble the formatted scramble
   * @param cubeType the cube type for which to color the scramble
   * @return the colored scramble
   */
  private Spannable colorFormattedScramble(String scramble, CubeType cubeType) {
    Spannable span = new SpannableString(scramble);
    final char separator = (cubeType.getType() == Type.SQUARE1 ? '(' : ' ');
    int defaultColor = getDefaultTextColor();
    int alternateColor = App.INSTANCE.getContext().getResources().getColor(R.color.scramblealternate);
    int prevLinesCharCount = 0;
    String prevLine = "";
    for (String line : scramble.split("\n")) {
      int prevSpaceInd = prevLinesCharCount;
      int colorInd = 0;
      char prevChar = '('; // initialized with a parenthesis for square-1 to color starting from 2nd column (and not from the 1st one)
      if (line.length() <= prevLine.length() / 2) {
        prevChar = ' '; // used for the last square-1 line. if last line is half the size of the previous one, the first move will be colored
      }
      for (int i = 0; i < line.length(); i++) {
        char c = line.charAt(i);
        if (c == separator && prevChar != separator || i == line.length() - 1) {
          span.setSpan(new ForegroundColorSpan((colorInd % 2 == 0) ? defaultColor : alternateColor),
              prevSpaceInd, prevLinesCharCount + i, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
          prevSpaceInd = prevLinesCharCount + i;
          colorInd++;
        }
        prevChar = c;
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
   * @return the formatted scramble
   */
  private String formatScramble(String[] scramble, CubeType cubeType) {
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
      case PYRAMINX:
      case SKEWB:
        movesPerLine = 5;
        break;
      case MEGAMINX:
      case SQUARE1:
        // don't add line breaks for this, as this a special kind of scramble that is already formatted
        movesPerLine = 0;
        break;
      default:
        movesPerLine = 10;
        break;
    }
    return movesPerLine;
  }

  private int getDefaultTextColor() {
    TextView tv = new TextView(App.INSTANCE.getContext());
    return tv.getCurrentTextColor();
  }

}
