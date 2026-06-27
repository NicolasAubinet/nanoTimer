package com.cube.nanotimer.util;

import com.cube.nanotimer.vo.CubeType;

/**
 * Maps NanoTimer scrambles to the form cubing.js expects for its 2D net diagrams
 * (used by {@code ScrambleViewDialog}). The boundary stays "string in, string out":
 * a {@link CubeType} picks the renderer key, and the move array is reformatted into
 * a single notation string.
 *
 * <p>Renderer keys are either a {@code scramble-display} event id (e.g. {@code 333},
 * {@code minx}) or the literal {@code fto} (drawn by {@code twisty-player}). See
 * {@code assets/scramble/scramble.html}.</p>
 *
 * <p>Most puzzles are a near pass-through. The exceptions, derived from the
 * scramblers' output:
 * <ul>
 *   <li><b>Square-1</b> emits tuples like {@code ( 3, 2)} with no separators;
 *       cubing.js wants the WCA slash form {@code (3, 2) / (-2, 4) / ...}.</li>
 *   <li><b>Megaminx</b> elements already carry their own spacing (and the raw
 *       scramble has embedded newlines); we just collapse whitespace to one line.</li>
 *   <li><b>Clock</b> has three user-selectable notations; only the WCA-like
 *       {@code URx_DRx_DLx} form is a safe pass-through. The pin-based forms are
 *       NOT yet converted — flagged for on-device verification. If rendering fails,
 *       the dialog falls back to the text scramble.</li>
 * </ul>
 */
public final class ScrambleViewNotation {

  private ScrambleViewNotation() {
  }

  /**
   * @return the cubing.js renderer key for this puzzle, or {@code null} if we don't
   *         know how to draw it (caller should hide the diagram entry point).
   */
  public static String getRenderKey(CubeType cubeType) {
    if (cubeType == null) {
      return null;
    }
    switch (cubeType) {
      case TWO_BY_TWO:    return "222";
      case THREE_BY_THREE:return "333";
      case FOUR_BY_FOUR:  return "444";
      case FIVE_BY_FIVE:  return "555";
      case SIX_BY_SIX:    return "666";
      case SEVEN_BY_SEVEN:return "777";
      case MEGAMINX:      return "minx";
      case PYRAMINX:      return "pyram";
      case SKEWB:         return "skewb";
      case SQUARE1:       return "sq1";
      case CLOCK:         return "clock";
      case FTO:           return "fto";
      default:            return null;
    }
  }

  /**
   * Reformat a NanoTimer scramble (one move per array element) into the single-line
   * notation string cubing.js parses for the given puzzle.
   */
  public static String toCubingNotation(String[] scramble, CubeType cubeType) {
    if (scramble == null || scramble.length == 0) {
      return "";
    }
    if (cubeType == CubeType.SQUARE1) {
      // ( 3, 2)( -2, 4) ... -> (3,2) / (-2,4) / ...  (cubing.js parses the slash form)
      StringBuilder sb = new StringBuilder();
      for (String move : scramble) {
        if (move == null || move.trim().isEmpty()) {
          continue;
        }
        if (sb.length() > 0) {
          sb.append(" / ");
        }
        sb.append(move.replaceAll("\\s+", ""));
      }
      return sb.toString();
    }
    // Everything else: join the moves and collapse any whitespace/newlines (Megaminx
    // elements already include separators) into a single clean line.
    StringBuilder sb = new StringBuilder();
    for (String move : scramble) {
      if (move == null) {
        continue;
      }
      sb.append(move).append(' ');
    }
    return sb.toString().replaceAll("\\s+", " ").trim();
  }
}
