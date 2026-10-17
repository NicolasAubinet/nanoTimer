package com.cube.nanotimer.cube;

import static org.junit.Assert.assertEquals;

import com.cube.nanotimer.smartcube.model.CubeMove;
import com.cube.nanotimer.smartcube.model.Face;

import org.junit.Test;

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

  private static void turn(AlgorithmFollower follower, String moves) {
    for (String move : moves.split(" ")) {
      Face face = Face.valueOf(move.substring(0, 1));
      follower.onMove(new CubeMove(face, move.endsWith("'"), 0));
    }
  }
}
