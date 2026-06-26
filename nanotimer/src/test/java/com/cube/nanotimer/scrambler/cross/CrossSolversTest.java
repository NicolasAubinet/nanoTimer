package com.cube.nanotimer.scrambler.cross;

import com.cube.nanotimer.Options.CrossNeutrality;
import com.cube.nanotimer.scrambler.cross.CrossSolvers.FaceSolutions;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RunWith(JUnit4.class)
public class CrossSolversTest {

  private static final String[] FACES = { "U", "D", "R", "L", "F", "B" };
  private static final String[] SUFFIXES = { "", "2", "'" };

  @Test
  public void testEveryFaceSolvedOptimally() {
    CrossSolvers solvers = new CrossSolvers();
    Random random = new Random(123);

    for (int n = 0; n < 150; n++) {
      String scramble = randomScramble(random, 25);
      for (CrossFace face : CrossFace.values()) {
        FaceSolutions result = solvers.solveFace(face, scramble);
        Assert.assertFalse("No solution for face " + face + " on " + scramble,
            result.solutions.isEmpty());
        Assert.assertTrue("Cross must be <= 8 HTM, got " + result.length,
            result.length <= 8);

        byte[] pieces = face.getCrossPieces();
        for (String[] solution : result.solutions) {
          Assert.assertEquals(result.length, solution.length);
          Assert.assertTrue("Solution does not solve " + face + " cross for " + scramble
                  + " : " + Arrays.toString(solution),
              solvesCross(scramble, solution, pieces));
        }
      }
    }
  }

  @Test
  public void testSolvedScrambleIsZeroOnEveryFace() {
    CrossSolvers solvers = new CrossSolvers();
    for (CrossFace face : CrossFace.values()) {
      Assert.assertEquals(0, solvers.solveFace(face, "").length);
    }
  }

  @Test
  public void testSpecificModeReturnsChosenFaceOnly() {
    CrossSolvers solvers = new CrossSolvers();
    List<FaceSolutions> results = solvers.solve("R U R' U'", CrossNeutrality.SPECIFIC, CrossFace.D);
    Assert.assertEquals(1, results.size());
    Assert.assertEquals(CrossFace.D, results.get(0).face);
  }

  @Test
  public void testDualModeReturnsOppositePairSorted() {
    CrossSolvers solvers = new CrossSolvers();
    Random random = new Random(99);
    for (int n = 0; n < 30; n++) {
      String scramble = randomScramble(random, 25);
      List<FaceSolutions> results = solvers.solve(scramble, CrossNeutrality.DUAL, CrossFace.D);
      Assert.assertEquals(2, results.size());
      Assert.assertTrue(results.get(0).length <= results.get(1).length);

      boolean hasD = false, hasU = false;
      for (FaceSolutions r : results) {
        hasD |= r.face == CrossFace.D;
        hasU |= r.face == CrossFace.U;
      }
      Assert.assertTrue("Dual mode for D must contain D and its opposite U", hasD && hasU);
    }
  }

  @Test
  public void testFullModeReturnsAllSixSorted() {
    CrossSolvers solvers = new CrossSolvers();
    Random random = new Random(55);
    for (int n = 0; n < 30; n++) {
      String scramble = randomScramble(random, 25);
      List<FaceSolutions> results = solvers.solve(scramble, CrossNeutrality.FULL, CrossFace.D);
      Assert.assertEquals(6, results.size());
      for (int i = 1; i < results.size(); i++) {
        Assert.assertTrue("Full mode results must be sorted by length",
            results.get(i - 1).length <= results.get(i).length);
      }
    }
  }

  private boolean solvesCross(String scramble, String[] solution, byte[] pieces) {
    String combined = scramble + " " + String.join(" ", solution);
    byte[][] state = ScrambleParser.toEdgeState(combined);
    byte[] perm = state[0];
    byte[] orient = state[1];
    for (byte piece : pieces) {
      if (perm[piece] != piece || orient[piece] != 0) {
        return false;
      }
    }
    return true;
  }

  private String randomScramble(Random random, int length) {
    StringBuilder sb = new StringBuilder();
    int lastFace = -1;
    for (int i = 0; i < length; i++) {
      int face;
      do {
        face = random.nextInt(FACES.length);
      } while (face == lastFace);
      lastFace = face;
      if (i > 0) {
        sb.append(' ');
      }
      sb.append(FACES[face]).append(SUFFIXES[random.nextInt(SUFFIXES.length)]);
    }
    return sb.toString();
  }
}
