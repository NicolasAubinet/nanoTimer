package com.cube.nanotimer.scrambler.randomstate.fto;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Random;

public class FtoCubieTest {

  /** Build a uniformly random, legal FTO state (same recipe as scramble generation). */
  private static FtoCubie randomState(Random rnd) {
    FtoCubie fc = new FtoCubie();
    fc.ep = FtoMath.rndPerm(12, true, rnd);
    fc.uf = FtoMath.rndPerm(12, true, rnd);
    fc.rl = FtoMath.rndPerm(12, true, rnd);
    fc.cp = FtoMath.rndPerm(6, true, rnd);
    fc.co = FtoMath.setNOri(new int[6], rnd.nextInt(32), 6, -2);
    return fc;
  }

  @Test
  public void solvedRoundTrip() {
    FtoCubie solved = new FtoCubie();
    FtoCubie fc = new FtoCubie();
    assertTrue(fc.fromFacelet(solved.toFaceCube()));
    assertArrayEquals(new int[] {0, 1, 2, 3, 4, 5}, fc.cp);
    assertArrayEquals(new int[] {0, 0, 0, 0, 0, 0}, fc.co);
    assertArrayEquals(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, fc.ep);
    assertArrayEquals(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, fc.uf);
    assertArrayEquals(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, fc.rl);
  }

  @Test
  public void randomStateFaceletRoundTripIsConsistent() {
    Random rnd = new Random(123);
    for (int t = 0; t < 2000; t++) {
      FtoCubie fc = randomState(rnd);
      int[] facelets = fc.toFaceCube();

      FtoCubie decoded = new FtoCubie();
      assertTrue("legal state should decode", decoded.fromFacelet(facelets));

      // fromFacelet canonicalises identical centers, but painting the decoded
      // state must reproduce exactly the same facelets.
      assertArrayEquals(facelets, decoded.toFaceCube());
    }
  }

  @Test
  public void illegalFaceletIsRejected() {
    FtoCubie fc = new FtoCubie();
    int[] facelets = fc.toFaceCube();
    facelets[0] = (facelets[0] + 1) % 8; // break the colour counts
    assertTrue(!new FtoCubie().fromFacelet(facelets));
  }
}
