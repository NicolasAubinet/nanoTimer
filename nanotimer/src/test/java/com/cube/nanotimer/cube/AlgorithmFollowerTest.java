package com.cube.nanotimer.cube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.cube.nanotimer.smartcube.cube.CubieCube;
import com.cube.nanotimer.smartcube.model.CubeMove;
import com.cube.nanotimer.smartcube.model.Face;
import com.cube.nanotimer.smartcube.step.AlgorithmForm;

import org.junit.Test;

import java.util.ArrayList;
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

  @Test
  public void onTheAlgorithmThereIsNothingToUndo() {
    AlgorithmFollower follower = new AlgorithmFollower(SUNE, "U", finishes(SUNE));
    turn(follower, "R U");
    assertEquals("", follower.getUndoMoves());
  }

  /** A slip off the right angle is undone as the timer undoes a scramble's: its inverse. */
  @Test
  public void aSlipIsUndoneByItsInverse() {
    AlgorithmFollower follower = new AlgorithmFollower(SUNE, "U", finishes(SUNE));
    turn(follower, "R U D");
    assertEquals("D'", follower.getUndoMoves());
    turn(follower, "D'");
    assertEquals("", follower.getUndoMoves());
    assertEquals(2, follower.getDoneCount());
  }

  /** Yellow on top, the solver's R turned as the cube's L: the undo is named as they hold it. */
  @Test
  public void theUndoIsNamedAsTheCubeIsHeld() {
    AlgorithmFollower follower = new AlgorithmFollower("R U R' U'", "D", null);
    turn(follower, "L D L' R");
    assertEquals("L'", follower.getUndoMoves());
  }

  /** An R turned from the left side reaches the cube as F, and is undone as the R' it was. */
  @Test
  public void aWrongAngleIsUndoneAsHeld() {
    AlgorithmFollower follower = new AlgorithmFollower(SUNE, "U", finishes(SUNE));
    turn(follower, "F U");
    assertTrue(follower.isWrongAngle());
    assertEquals("U' R'", follower.getUndoMoves());
  }

  /** A slip put right leaves nothing to undo, but the rep still strayed from the algorithm. */
  @Test
  public void aSlipPutRightIsStillRemembered() {
    AlgorithmFollower follower = new AlgorithmFollower(SUNE, "U", finishes(SUNE));
    turn(follower, "R U");
    assertFalse(follower.hasStrayed());
    turn(follower, "D D'");
    assertEquals("", follower.getUndoMoves());
    assertTrue(follower.hasStrayed());
    follower.reset();
    assertFalse(follower.hasStrayed());
  }

  @Test
  public void aWrongAnglePutRightIsStillRemembered() {
    AlgorithmFollower follower = new AlgorithmFollower(SUNE, "U", finishes(SUNE));
    turn(follower, "F U U' F'");
    assertFalse(follower.isWrongAngle());
    assertTrue(follower.hasStrayed());
  }

  /** Squaring the layer up first and then the algorithm is no stray. */
  @Test
  public void theAlgorithmAfterALayerTurnNeverStrays() {
    AlgorithmFollower follower = new AlgorithmFollower(SUNE, "U", finishes("U' " + SUNE));
    turn(follower, "U' R U R' U R U U R'"); // the cube reports U2 as two quarters
    assertFalse(follower.hasStrayed());
  }

  /** After a y the solver's R is another face of the cube, and their slip on it is undone as R. */
  @Test
  public void aSlipAfterARotationIsNamedFromTheNewGrip() {
    assertSlipNamedAsHeld("R U R' y R U R'", 3, "R'", "R");
  }

  /** A wide u turns the solver's hands round the cube like a y. */
  @Test
  public void aSlipAfterAWideTurnIsNamedFromTheNewGrip() {
    assertSlipNamedAsHeld("R U R' u R U R'", 4, "R'", "R");
  }

  /** A slice tilts the frame: after M' the solver's U is what was their front. */
  @Test
  public void aSlipAfterASliceIsNamedFromTheNewFrame() {
    assertSlipNamedAsHeld("R U R' M' U R U' R'", 5, "U'", "U");
  }

  /**
   * Turns the algorithm's first {@code done} cube turns, then {@code slip} as the solver means it
   * at that point, and expects {@code undo} named the same way.
   */
  private static void assertSlipNamedAsHeld(String algorithm, int done, String slip, String undo) {
    List<String> cube = new ArrayList<>();
    String[] tokens = algorithm.split(" ");
    for (List<String> turns : AlgorithmForm.perToken(tokens)) {
      cube.addAll(turns);
    }
    AlgorithmFollower follower =
        new AlgorithmFollower(algorithm, "U", finishes(String.join(" ", cube)));
    turn(follower, String.join(" ", cube.subList(0, done)));
    assertEquals("", follower.getUndoMoves());
    // The slip's face as the frame now names it: the letter's face in the algorithm's first frame
    String frame = AlgorithmForm.frames(tokens).get(tokensFor(tokens, done));
    turn(follower, frame.charAt("URFDLB".indexOf(slip.charAt(0))) + slip.substring(1));
    assertEquals(undo, follower.getUndoMoves());
  }

  /** How many tokens, rotations included, make up the first {@code turned} cube turns. */
  private static int tokensFor(String[] tokens, int turned) {
    int count = 0;
    while (count < tokens.length && turned > 0) {
      turned -= AlgorithmForm.perToken(tokens[count]).get(0).size();
      count++;
    }
    while (count < tokens.length && AlgorithmForm.perToken(tokens[count]).get(0).isEmpty()) {
      count++; // the rotation that follows is made before the next turn
    }
    return count;
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
