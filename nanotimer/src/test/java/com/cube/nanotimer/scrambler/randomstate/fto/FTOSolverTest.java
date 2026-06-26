package com.cube.nanotimer.scrambler.randomstate.fto;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FTOSolverTest {

  private static final List<String> TOKENS = Arrays.asList(
    "U", "U'", "F", "F'", "BR", "BR'", "BL", "BL'", "D", "D'", "B", "B'", "R", "R'", "L", "L'");

  private static int tokenToMove(String token) {
    int idx = TOKENS.indexOf(token);
    if (idx < 0) {
      throw new IllegalArgumentException("bad token: " + token);
    }
    return idx;
  }

  private static FtoCubie apply(FtoCubie start, String[] tokens) {
    FtoCubie fc = start;
    for (String token : tokens) {
      fc = FtoCubie.mult(fc, FtoMoves.moveCube[tokenToMove(token)], null);
    }
    return fc;
  }

  private static boolean isSolved(FtoCubie fc) {
    int[] f = fc.toFaceCube();
    for (int face = 0; face < 8; face++) {
      for (int j = 1; j < 9; j++) {
        if (f[face * 9 + j] != f[face * 9]) {
          return false;
        }
      }
    }
    return true;
  }

  /** A uniformly random, legal FTO state, normalised through the facelet round-trip. */
  private static FtoCubie randomState(Random rnd) {
    FtoCubie fc = new FtoCubie();
    fc.ep = FtoMath.rndPerm(12, true, rnd);
    fc.uf = FtoMath.rndPerm(12, true, rnd);
    fc.rl = FtoMath.rndPerm(12, true, rnd);
    fc.cp = FtoMath.rndPerm(6, true, rnd);
    fc.co = FtoMath.setNOri(new int[6], rnd.nextInt(32), 6, -2);
    FtoCubie canon = new FtoCubie();
    canon.fromFacelet(fc.toFaceCube());
    return canon;
  }

  private static FtoCubie copy(FtoCubie fc) {
    return new FtoCubie(fc.cp, fc.co, fc.ep, fc.uf, fc.rl);
  }

  @Test
  public void solvesAndScramblesRandomStates() {
    long genStart = System.currentTimeMillis();
    FTOSolver.genTables();
    long genMs = System.currentTimeMillis() - genStart;

    FTOSolver solver = new FTOSolver();
    Random rnd = new Random(2024);

    int n = 200;
    long totalSolveMs = 0;
    long totalLen = 0;
    int maxLen = 0;

    for (int t = 0; t < n; t++) {
      FtoCubie fc = randomState(rnd);

      // 1) The forward solution must actually solve the random state.
      String[] sol = solver.solve(fc, false);
      assertNotNull("solver returned null", sol);
      assertTrue("solution did not solve the cube", isSolved(apply(copy(fc), sol)));

      // 2) The scramble (inverse) applied to a solved cube must produce a
      //    legal, scrambled, and solvable state.
      long s = System.currentTimeMillis();
      String[] scramble = solver.solve(fc, true);
      totalSolveMs += System.currentTimeMillis() - s;
      assertNotNull(scramble);
      assertTrue("scramble too short/long: " + scramble.length, scramble.length > 0 && scramble.length <= 60);
      for (String token : scramble) {
        assertTrue("illegal token " + token, TOKENS.contains(token));
      }

      FtoCubie scrambled = apply(new FtoCubie(), scramble);
      assertTrue("scramble produced a solved cube", !isSolved(scrambled));

      FtoCubie canon = new FtoCubie();
      assertTrue("scrambled state is illegal", canon.fromFacelet(scrambled.toFaceCube()));
      String[] reSol = solver.solve(canon, false);
      assertNotNull(reSol);
      assertTrue("could not solve the scrambled state", isSolved(apply(copy(canon), reSol)));

      totalLen += scramble.length;
      maxLen = Math.max(maxLen, scramble.length);
    }

    System.out.println("[FTO] table gen: " + genMs + " ms");
    System.out.println("[FTO] avg solve: " + (totalSolveMs / (double) n) + " ms over " + n + " states");
    System.out.println("[FTO] avg scramble length: " + (totalLen / (double) n) + ", max: " + maxLen);
  }
}
