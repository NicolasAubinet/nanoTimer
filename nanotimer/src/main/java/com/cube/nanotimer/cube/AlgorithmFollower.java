package com.cube.nanotimer.cube;

import com.cube.nanotimer.smartcube.model.CubeMove;
import com.cube.nanotimer.smartcube.step.AlgorithmForm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * How far into an algorithm the cube has got, for greying its moves as they are turned the way the
 * timer greys a scramble's.
 *
 * <p>A case is dealt with a random turn of U before it, which the algorithm does not carry, and the
 * solver may run it from any side. So {@link ScrambleFollower} runs once for each of the four U
 * turns before it and each of the four sides it can be turned from, and the one furthest along is
 * what is shown. A rotation in the algorithm turns nothing the cube sees, so it greys with the move
 * before it.
 */
public class AlgorithmFollower {

  private static final String[] AUF = {null, "U", "U2", "U'"};
  /** The side faces in the order a y rotation carries them. */
  private static final String Y_CYCLE = "RFLB";

  private final String[] tokens;
  /** For each token, how many tokens that turn something end at or before it. */
  private final int[] turningUpTo;
  private final List<ScrambleFollower> followers = new ArrayList<>();
  private final List<Boolean> prefixed = new ArrayList<>();

  /** @throws IllegalArgumentException if a token is not a turn */
  public AlgorithmFollower(String algorithm) {
    tokens = algorithm.trim().split("\\s+");
    String[] cleaned = new String[tokens.length];
    for (int i = 0; i < tokens.length; i++) {
      cleaned[i] = tokens[i].replace("(", "").replace(")", "");
    }
    List<List<String>> turns = AlgorithmForm.perToken(cleaned);
    turningUpTo = new int[tokens.length];
    int turning = 0;
    for (int i = 0; i < tokens.length; i++) {
      if (!turns.get(i).isEmpty()) {
        turning++;
      }
      turningUpTo[i] = turning;
    }
    for (String auf : AUF) {
      for (int sides = 0; sides < 4; sides++) {
        List<List<String>> path = new ArrayList<>();
        if (auf != null) {
          path.add(Collections.singletonList(auf));
        }
        for (List<String> token : turns) {
          path.add(turnedBy(token, sides));
        }
        followers.add(ScrambleFollower.ofTurns(path));
        prefixed.add(auf != null);
      }
    }
  }

  /** The algorithm's tokens, as written, for the screen to colour one by one. */
  public String[] getTokens() {
    return tokens.clone();
  }

  public void onMove(CubeMove move) {
    for (ScrambleFollower follower : followers) {
      follower.onMove(move);
    }
  }

  public void reset() {
    for (ScrambleFollower follower : followers) {
      follower.reset();
    }
  }

  /** How many of the algorithm's tokens are done, a rotation counting with the move before it. */
  public int getDoneCount() {
    int turned = 0;
    for (int i = 0; i < followers.size(); i++) {
      int done = followers.get(i).getDoneCount() - (prefixed.get(i) ? 1 : 0);
      turned = Math.max(turned, done);
    }
    if (turned == 0) {
      return 0;
    }
    int done = 0;
    while (done < tokens.length && turningUpTo[done] <= turned) {
      done++;
    }
    return done;
  }

  private static List<String> turnedBy(List<String> token, int sides) {
    List<String> turned = new ArrayList<>(token.size());
    for (String turn : token) {
      int at = Y_CYCLE.indexOf(turn.charAt(0));
      turned.add(at < 0 ? turn : Y_CYCLE.charAt((at + sides) % 4) + turn.substring(1));
    }
    return turned;
  }
}
