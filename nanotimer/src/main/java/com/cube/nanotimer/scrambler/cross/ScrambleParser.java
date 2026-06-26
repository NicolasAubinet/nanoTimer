package com.cube.nanotimer.scrambler.cross;

import com.cube.nanotimer.scrambler.randomstate.Move;
import com.cube.nanotimer.scrambler.randomstate.StateTables;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses a 3x3 scramble string ("R U R' F2 ...") and applies it to a solved cube's edges.
 *
 * <p>The random-state machinery only ever goes state -> scramble (for generation); the cross
 * solver needs the reverse. We reuse the {@link Move} edge transforms and
 * {@link StateTables#getPermResult}/{@link StateTables#getOrientResult} so the orientation basis
 * matches the rest of the solvers exactly.
 *
 * <p>3x3 default scrambles only ever contain face turns ({@code U/D/R/L/F/B} optionally followed
 * by {@code 2} or {@code '}); rotations and wide moves are not supported and raise an exception.
 */
public class ScrambleParser {

  private static final Map<String, Move> BY_NAME = new HashMap<>();
  static {
    for (Move m : Move.values()) {
      BY_NAME.put(m.name, m);
    }
  }

  private ScrambleParser() {
  }

  /**
   * Applies the scramble to a solved cube and returns its edge state as {@code {perm[12], orient[12]}}.
   * {@code perm[slot]} is the home index of the edge currently in that slot; {@code orient[slot]} is
   * its orientation (0 or 1) using the existing {@link Move} convention.
   *
   * @throws IllegalArgumentException if an unknown / unsupported token is encountered.
   */
  public static byte[][] toEdgeState(String scramble) {
    byte[] perm = new byte[12];
    byte[] orient = new byte[12];
    for (byte i = 0; i < 12; i++) {
      perm[i] = i;
    }
    if (scramble != null) {
      String trimmed = scramble.trim();
      if (!trimmed.isEmpty()) {
        for (String token : trimmed.split("\\s+")) {
          if (token.isEmpty()) {
            continue;
          }
          Move m = BY_NAME.get(token);
          if (m == null) {
            throw new IllegalArgumentException("Unsupported scramble token: '" + token + "'");
          }
          perm = StateTables.getPermResult(perm, m.edgPerm);
          orient = StateTables.getOrientResult(orient, m.edgPerm, m.edgOrient, 2);
        }
      }
    }
    return new byte[][] { perm, orient };
  }
}
