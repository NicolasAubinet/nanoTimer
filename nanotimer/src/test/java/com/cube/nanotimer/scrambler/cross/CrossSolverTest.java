package com.cube.nanotimer.scrambler.cross;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@RunWith(JUnit4.class)
public class CrossSolverTest {

  private static final String[] FACES = { "U", "D", "R", "L", "F", "B" };
  private static final String[] SUFFIXES = { "", "2", "'" };
  private static final Set<String> LEGAL_TOKENS = new HashSet<>();
  static {
    for (String f : FACES) {
      for (String s : SUFFIXES) {
        LEGAL_TOKENS.add(f + s);
      }
    }
  }

  // D-face cross: edges with home indices 8,9,10,11 must be home with orientation 0.
  private static final int[] D_SLOTS = { 8, 9, 10, 11 };

  @Test
  public void testSolvedScrambleHasEmptySolution() {
    CrossSolver solver = CrossSolver.dCross();
    List<String[]> solutions = solver.solve("");
    Assert.assertEquals(1, solutions.size());
    Assert.assertEquals(0, solutions.get(0).length);
    Assert.assertEquals(0, solver.optimalLength(""));
  }

  @Test
  public void testRandomScramblesAreSolvedOptimallyAndLegally() {
    CrossSolver solver = CrossSolver.dCross();
    Random random = new Random(42);

    for (int n = 0; n < 500; n++) {
      String scramble = randomScramble(random, 25);
      List<String[]> solutions = solver.solve(scramble);

      Assert.assertFalse("Expected at least one solution for " + scramble, solutions.isEmpty());

      int optimal = solutions.get(0).length;
      Assert.assertTrue("Cross is always solvable in <= 8 HTM, got " + optimal + " for " + scramble,
          optimal <= 8);
      Assert.assertEquals("optimalLength must match the returned solution length",
          optimal, solver.optimalLength(scramble));

      for (String[] solution : solutions) {
        Assert.assertEquals("All returned solutions must have the same (minimal) length",
            optimal, solution.length);
        for (String token : solution) {
          Assert.assertTrue("Illegal move token: " + token, LEGAL_TOKENS.contains(token));
        }
        Assert.assertTrue("Solution does not solve the D cross for " + scramble
                + " : " + Arrays.toString(solution),
            solvesDCross(scramble, solution));
      }
    }
  }

  @Test
  public void testAllOptimalSolutionsAreDistinct() {
    CrossSolver solver = CrossSolver.dCross();
    Random random = new Random(7);
    for (int n = 0; n < 50; n++) {
      String scramble = randomScramble(random, 25);
      List<String[]> solutions = solver.solve(scramble);
      Set<String> seen = new HashSet<>();
      for (String[] solution : solutions) {
        Assert.assertTrue("Duplicate optimal solution returned for " + scramble,
            seen.add(String.join(" ", solution)));
      }
    }
  }

  // Applies scramble then solution to a solved cube and checks the D cross edges are home, oriented.
  private boolean solvesDCross(String scramble, String[] solution) {
    String combined = scramble + " " + String.join(" ", solution);
    byte[][] state = ScrambleParser.toEdgeState(combined);
    byte[] perm = state[0];
    byte[] orient = state[1];
    for (int slot : D_SLOTS) {
      if (perm[slot] != slot || orient[slot] != 0) {
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
