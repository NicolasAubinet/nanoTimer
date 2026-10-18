package com.cube.nanotimer.cube;

import com.cube.nanotimer.smartcube.drill.LayerRotation;
import com.cube.nanotimer.smartcube.model.CubeMove;
import com.cube.nanotimer.smartcube.step.AlgorithmForm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * How far into an algorithm the cube has got, for greying its moves as they are turned the way the
 * timer greys a scramble's.
 *
 * <p>A case is dealt with a random turn of its layer before it, which the algorithm does not carry,
 * and the solver may run it from any side. So {@link ScrambleFollower} runs once for each of the
 * four layer turns before it and each of the four sides it can be turned from, and the one furthest
 * along is what is shown. A rotation in the algorithm turns nothing the cube sees, so it greys with
 * the move before it.
 *
 * <p>The cube reports turns on its own faces, so the algorithm, written with the layer on U, is
 * moved onto the face the drill deals the layer on: with yellow on top, the solver's R U arrives as
 * L D.
 */
public class AlgorithmFollower {

  private static final String[] AUF = {null, "", "2", "'"};

  private final String[] tokens;
  /** For each token, how many tokens that turn something end at or before it. */
  private final int[] turningUpTo;
  private final List<ScrambleFollower> followers = new ArrayList<>();
  private final List<Boolean> prefixed = new ArrayList<>();

  /** For an algorithm turned with the layer on U. */
  public AlgorithmFollower(String algorithm) {
    this(algorithm, "U");
  }

  /**
   * @param layerFace the face the drill deals the last layer on, as its letter; null for U
   * @throws IllegalArgumentException if a token is not a turn
   */
  public AlgorithmFollower(String algorithm, String layerFace) {
    String face = layerFace == null ? "U" : layerFace;
    tokens = algorithm.trim().split("\s+");
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
          path.add(Collections.singletonList(LayerRotation.piece("U", 0, face) + auf));
        }
        for (List<String> token : turns) {
          path.add(turnedBy(token, sides, face));
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

  /** Each outer turn after {@code sides} quarter turns of the cube about U, then onto the layer. */
  private static List<String> turnedBy(List<String> token, int sides, String face) {
    List<String> turned = new ArrayList<>(token.size());
    for (String turn : token) {
      turned.add(LayerRotation.piece(turn.substring(0, 1), sides, face) + turn.substring(1));
    }
    return turned;
  }
}
