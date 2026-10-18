package com.cube.nanotimer.cube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.cube.nanotimer.smartcube.cube.CubieCube;
import com.cube.nanotimer.smartcube.model.CubeMove;
import com.cube.nanotimer.smartcube.model.Face;

import org.junit.Test;

import java.util.List;
import java.util.function.Predicate;

public class AlgorithmFollowerTest {

  @Test
  public void greysTheMovesTurnedSoFar() {
    AlgorithmFollower follower = new AlgorithmFollower("R U R' U'");
    assertEquals(0, follower.getDoneCount());
    turn(follower, "R U");
    assertEquals(2, follower.getDoneCount());
    turn(follower, "R' U'");
    assertEquals(4, follower.getDoneCount());
  }

  @Test
  public void aTurnOfUBeforeTheAlgorithmIsNotAMove() {
    AlgorithmFollower follower = new AlgorithmFollower("R U R' U'");
    turn(follower, "U'");
    assertEquals(0, follower.getDoneCount());
    turn(follower, "R U");
    assertEquals(2, follower.getDoneCount());
  }

  @Test
  public void followsTheAlgorithmFromAnotherSide() {
    AlgorithmFollower follower = new AlgorithmFollower("R U R' U'");
    turn(follower, "F U F'");
    assertEquals(3, follower.getDoneCount());
  }

  @Test
  public void aRotationGreysWithTheMoveBeforeIt() {
    AlgorithmFollower follower = new AlgorithmFollower("(R U) y R'");
    turn(follower, "R U");
    assertEquals(3, follower.getDoneCount());
  }

  @Test
  public void aWrongTurnHoldsTheProgress() {
    AlgorithmFollower follower = new AlgorithmFollower("R U R' U'");
    turn(follower, "R D");
    assertEquals(1, follower.getDoneCount());
  }

  /** Yellow on top: the cube reports the solver's R U R' U' on its own faces, as L D L' D'. */
  @Test
  public void followsTheAlgorithmOnTheLayerFace() {
    AlgorithmFollower follower = new AlgorithmFollower("R U R' U'", "D", null);
    turn(follower, "L D L' D'");
    assertEquals(4, follower.getDoneCount());
  }

  @Test
  public void aTurnOfTheLayerFaceBeforeTheAlgorithmIsNotAMove() {
    AlgorithmFollower follower = new AlgorithmFollower("R U R' U'", "D", null);
    turn(follower, "D' F D");
    assertEquals(2, follower.getDoneCount());
  }

  private static final String SUNE = "R U R' U R U2 R'";

  /** A Sune dealt so that it is started from the front, R first. */
  @Test
  public void greysFromTheAngleThatFinishesTheCase() {
    AlgorithmFollower follower = new AlgorithmFollower(SUNE, "U", finishes(SUNE));
    turn(follower, "R U R'");
    assertEquals(3, follower.getDoneCount());
    assertFalse(follower.isWrongAngle());
  }

  /** The same Sune started from the left side: an R turned as an F. */
  @Test
  public void aSideTurnFromAnotherAngleIsAWrongAngle() {
    AlgorithmFollower follower = new AlgorithmFollower(SUNE, "U", finishes(SUNE));
    turn(follower, "F");
    assertTrue(follower.isWrongAngle());
    assertEquals(0, follower.getDoneCount());
  }

  @Test
  public void aTurnOfTheLayerIsNeverAWrongAngle() {
    AlgorithmFollower follower = new AlgorithmFollower(SUNE, "U", finishes(SUNE));
    turn(follower, "U");
    assertFalse(follower.isWrongAngle());
    turn(follower, "U'");
    assertFalse(follower.isWrongAngle());
  }

  @Test
  public void undoingTheWrongTurnClearsIt() {
    AlgorithmFollower follower = new AlgorithmFollower(SUNE, "U", finishes(SUNE));
    turn(follower, "F F'");
    assertFalse(follower.isWrongAngle());
    turn(follower, "R U");
    assertEquals(2, follower.getDoneCount());
  }

  /** Squaring the layer up first and then starting from the front is a right angle too. */
  @Test
  public void aLayerTurnThenTheRightSideIsNotWrong() {
    AlgorithmFollower follower = new AlgorithmFollower(SUNE, "U", finishes("U' " + SUNE));
    turn(follower, "U' R U");
    assertFalse(follower.isWrongAngle());
    assertEquals(2, follower.getDoneCount());
  }

  /** Whether a path finishes the case the given moves solve, allowing a last turn of U. */
  private static Predicate<List<String>> finishes(String solution) {
    CubieCube dealt = new CubieCube();
    String[] moves = solution.split(" ");
    for (int i = moves.length - 1; i >= 0; i--) {
      String m = moves[i];
      boolean prime = !m.endsWith("'");
      Face face = Face.valueOf(m.substring(0, 1));
      for (int q = 0; q < (m.endsWith("2") ? 2 : 1); q++) {
        dealt.applyMove(face, prime);
      }
    }
    String start = dealt.toFaceCube();
    return path -> {
      CubieCube state = new CubieCube();
      state.fromFacelet(start);
      for (String turn : path) {
        for (int q = 0; q < (turn.endsWith("2") ? 2 : 1); q++) {
          state.applyMove(Face.valueOf(turn.substring(0, 1)), turn.endsWith("'"));
        }
      }
      for (int u = 0; u < 4; u++) {
        if (state.isSolved()) {
          return true;
        }
        state.applyMove(Face.U, false);
      }
      return false;
    };
  }

  private static void turn(AlgorithmFollower follower, String moves) {
    for (String move : moves.split(" ")) {
      Face face = Face.valueOf(move.substring(0, 1));
      follower.onMove(new CubeMove(face, move.endsWith("'"), 0));
    }
  }
}
