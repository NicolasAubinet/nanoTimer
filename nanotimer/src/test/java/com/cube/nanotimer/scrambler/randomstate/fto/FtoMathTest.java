package com.cube.nanotimer.scrambler.randomstate.fto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Random;

public class FtoMathTest {

  private static int permParity(int[] perm) {
    int inv = 0;
    for (int i = 0; i < perm.length; i++) {
      for (int j = i + 1; j < perm.length; j++) {
        if (perm[i] > perm[j]) {
          inv++;
        }
      }
    }
    return inv & 1;
  }

  @Test
  public void getNParityMatchesActualParity() {
    Random rnd = new Random(42);
    for (int t = 0; t < 5000; t++) {
      int n = 2 + rnd.nextInt(11); // 2..12
      int[] perm = FtoMath.rndPerm(n, false, rnd);
      int expected = permParity(perm);
      int actual = FtoMath.getNParity(FtoMath.getNPerm(perm, n), n);
      assertEquals(expected, actual);
    }
  }

  @Test
  public void rndPermProducesValidPermutationsWithRequestedParity() {
    Random rnd = new Random(7);
    for (int t = 0; t < 2000; t++) {
      int[] even = FtoMath.rndPerm(12, true, rnd);
      assertTrue(isPermutation(even, 12));
      assertEquals(0, permParity(even));

      int[] even6 = FtoMath.rndPerm(6, true, rnd);
      assertTrue(isPermutation(even6, 6));
      assertEquals(0, permParity(even6));
    }
  }

  @Test
  public void setNOriIsInverseOfGetNOri() {
    int[] arr = new int[6];
    for (int idx = 0; idx < 32; idx++) {
      FtoMath.setNOri(arr, idx, 6, -2);
      // zero-sum (even) orientation
      int sum = 0;
      for (int v : arr) {
        sum += v;
      }
      assertEquals(0, sum & 1);
      assertEquals(idx, FtoMath.getNOri(arr, 6, -2));
    }
  }

  @Test
  public void centerCoordRoundTrips() {
    FtoMath.Coord coord = new FtoMath.Coord(new int[] {3, 3, 3, 3});
    int count = 369600; // 12! / (3!^4)
    int[] arr = new int[12];
    for (int i = 0; i < count; i++) {
      coord.set(arr, i);
      assertEquals(i, coord.get(arr));
    }
  }

  private static boolean isPermutation(int[] arr, int n) {
    boolean[] seen = new boolean[n];
    for (int v : arr) {
      if (v < 0 || v >= n || seen[v]) {
        return false;
      }
      seen[v] = true;
    }
    return arr.length == n;
  }
}
