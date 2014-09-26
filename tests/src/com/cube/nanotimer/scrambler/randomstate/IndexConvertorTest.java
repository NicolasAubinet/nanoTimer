package com.cube.nanotimer.scrambler.randomstate;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class IndexConvertorTest extends AndroidTestCase {

  @SmallTest
  public void testFixedConversion() {
    // Corner orientation
    byte[] state = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };
    Assert.assertEquals(0, packCornerOrientation(state));
    assertArrayEquals(state, unpackCornerOrientation(packCornerOrientation(state)));
    state = new byte[] { 0, 0, 2, 0, 0, 1, 0, 0 };
    assertArrayEquals(state, unpackCornerOrientation(packCornerOrientation(state)));
    state = new byte[] { 1, 0, 2, 1, 0, 2, 1, 2 };
    assertArrayEquals(state, unpackCornerOrientation(packCornerOrientation(state)));
    state = new byte[] { 0, 2, 1, 0, 0, 1, 1, 1 };
    assertArrayEquals(state, unpackCornerOrientation(packCornerOrientation(state)));

    // Corner permutation
    state = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
    Assert.assertEquals(0, packCornerPermutation(state));
    assertArrayEquals(state, unpackCornerPermutation(packCornerPermutation(state)));
    state = new byte[] { 1, 2, 3, 4, 5, 6, 8, 7 };
    assertArrayEquals(state, unpackCornerPermutation(packCornerPermutation(state)));
    state = new byte[] { 1, 2, 3, 4, 5, 7, 6, 8 };
    assertArrayEquals(state, unpackCornerPermutation(packCornerPermutation(state)));
    state = new byte[] { 1, 2, 3, 4, 6, 7, 5, 8 };
    assertArrayEquals(state, unpackCornerPermutation(packCornerPermutation(state)));
    state = new byte[] { 8, 7, 6, 5, 4, 2, 3, 1 };
    assertArrayEquals(state, unpackCornerPermutation(packCornerPermutation(state)));
    state = new byte[] { 8, 7, 6, 5, 4, 3, 1, 2 };
    assertArrayEquals(state, unpackCornerPermutation(packCornerPermutation(state)));
    state = new byte[] { 8, 7, 6, 5, 4, 3, 2, 1 };
    assertArrayEquals(state, unpackCornerPermutation(packCornerPermutation(state)));
    state = new byte[] { 6, 4, 1, 2, 7, 5, 8, 3 };
    assertArrayEquals(state, unpackCornerPermutation(packCornerPermutation(state)));
    state = new byte[] { 5, 4, 1, 2, 8, 6, 7, 3 };
    assertArrayEquals(state, unpackCornerPermutation(packCornerPermutation(state)));
    state = new byte[] { 4, 1, 3, 7, 2, 8, 6, 5 };
    assertArrayEquals(state, unpackCornerPermutation(packCornerPermutation(state)));

    // Edge orientation
    state = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    Assert.assertEquals(0, packEdgeOrientation(state));
    assertArrayEquals(state, unpackEdgeOrientation(packEdgeOrientation(state)));
    state = new byte[] { 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1 };
    assertArrayEquals(state, unpackEdgeOrientation(packEdgeOrientation(state)));
    state = new byte[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0 };
    assertArrayEquals(state, unpackEdgeOrientation(packEdgeOrientation(state)));
    state = new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
    assertArrayEquals(state, unpackEdgeOrientation(packEdgeOrientation(state)));

    // Edge permutation
    state = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
    Assert.assertEquals(0, packEdgePermutation(state));
    assertArrayEquals(state, unpackUDEdgePermutation(packEdgePermutation(state)));
    state = new byte[] { 1, 2, 3, 4 };
    Assert.assertEquals(0, packEdgePermutation(state));
    assertArrayEquals(state, unpackEEdgePermutation(packEdgePermutation(state)));
    state = new byte[] { 8, 7, 6, 5, 4, 3, 2, 1 };
    assertArrayEquals(state, unpackUDEdgePermutation(packEdgePermutation(state)));
    state = new byte[] { 4, 3, 2, 1 };
    assertArrayEquals(state, unpackEEdgePermutation(packEdgePermutation(state)));
    state = new byte[] { 1, 7, 6, 5, 4, 3, 2, 8 };
    assertArrayEquals(state, unpackUDEdgePermutation(packEdgePermutation(state)));
    state = new byte[] { 2, 3, 1, 4 };
    assertArrayEquals(state, unpackEEdgePermutation(packEdgePermutation(state)));
    state = new byte[] { 5, 4, 8, 1, 6, 3, 7, 2 };
    assertArrayEquals(state, unpackUDEdgePermutation(packEdgePermutation(state)));
    state = new byte[] { 4, 3, 1, 2 };
    assertArrayEquals(state, unpackEEdgePermutation(packEdgePermutation(state)));
    state = new byte[] { 4, 1, 5, 2, 7, 8, 3, 6 };
    assertArrayEquals(state, unpackUDEdgePermutation(packEdgePermutation(state)));
    state = new byte[] { 2, 4, 3, 1 };
    assertArrayEquals(state, unpackEEdgePermutation(packEdgePermutation(state)));

    // Edge combinations
    boolean[] comb = new boolean[] { true, true, true, true, false, false, false, false, false, false, false, false };
    Assert.assertEquals(0, packEEdgeCombination(comb));
    assertArrayEquals(comb, unpackEEdgeCombination(packEEdgeCombination(comb)));
    comb = new boolean[] { true, true, true, false, true, false, false, false, false, false, false, false };
    assertArrayEquals(comb, unpackEEdgeCombination(packEEdgeCombination(comb)));
    comb = new boolean[] { false, false, false, false, false, false, false, false, true, true, true, true };
    assertArrayEquals(comb, unpackEEdgeCombination(packEEdgeCombination(comb)));
  }

  @SmallTest
  public void testRandomConversion() {
    Random r = new Random();
    byte[] state;
    List<Byte> available;
    for (int i = 0; i < 200; i++) {
      // Corner orientation
      state = new byte[8];
      int sum = 0;
      for (int j = 0; j < 7; j++) {
        state[j] = (byte) r.nextInt(3);
        sum += state[j];
      }
      state[7] = (byte) ((3 - sum % 3) % 3);
      assertArrayEquals(state, unpackCornerOrientation(packCornerOrientation(state)));

      // Corner permutation
      state = new byte[8];
      available = new ArrayList<Byte>();
      for (byte j = 1; j <= 8; j++) { available.add(j); }
      Collections.shuffle(available, r);
      for (int j = 0; j < 8; j++) {
        state[j] = available.remove(0);
      }
      assertArrayEquals(state, unpackCornerPermutation(packCornerPermutation(state)));

      // Edge orientation
      state = new byte[12];
      sum = 0;
      for (int j = 0; j < 11; j++) {
        state[j] = (byte) r.nextInt(2);
        sum += state[j];
      }
      state[11] = (byte) ((2 - sum % 2) % 2);
      assertArrayEquals(state, unpackEdgeOrientation(packEdgeOrientation(state)));

      // Edge permutation
      state = new byte[4];
      available = new ArrayList<Byte>();
      for (byte j = 1; j <= 4; j++) { available.add(j); }
      Collections.shuffle(available, r);
      for (int j = 0; j < 4; j++) {
        state[j] = available.remove(0);
      }
      assertArrayEquals(state, unpackEEdgePermutation(packEdgePermutation(state)));

      state = new byte[8];
      available = new ArrayList<Byte>();
      for (byte j = 1; j <= 8; j++) { available.add(j); }
      Collections.shuffle(available, r);
      for (int j = 0; j < 8; j++) {
        state[j] = available.remove(0);
      }
      assertArrayEquals(state, unpackUDEdgePermutation(packEdgePermutation(state)));
    }
  }

  @SmallTest
  public void testPerformance() {
    int[] rands = new int[40320];
    for (int i = 0; i < rands.length; i++) {
      rands[i] = i % 40320;
    }
    long ts = System.currentTimeMillis();
    byte[][] states = new byte[40320][];
    for (int i = 0; i < rands.length; i++) {
      states[i] = IndexConvertor.unpackPermutation(rands[i], 8);
    }
    Log.i("[NanoTimerPerf]", "unpackPermutation: " + (System.currentTimeMillis() - ts));
    ts = System.currentTimeMillis();
    for (int i = 0; i < rands.length; i++) {
      IndexConvertor.packPermutation(states[i]);
    }
    Log.i("[NanoTimerPerf]", "packPermutation: " + (System.currentTimeMillis() - ts));
  }

  public static int packCornerPermutation(byte[] perm) {
    return IndexConvertor.packPermutation(perm);
  }

  public static int packCornerOrientation(byte[] perm) {
    return IndexConvertor.packOrientation(perm, (byte) 3);
  }

  public static int packEdgePermutation(byte[] perm) {
    return IndexConvertor.packPermutation(perm);
  }

  public static int packEdgeOrientation(byte[] perm) {
    return IndexConvertor.packOrientation(perm, (byte) 2);
  }

  public static int packEEdgeCombination(boolean[] combination) {
    return IndexConvertor.packCombination(combination, 4);
  }

  public static byte[] unpackCornerPermutation(int permInd) {
    return IndexConvertor.unpackPermutation(permInd, (byte) 8);
  }

  public static byte[] unpackCornerOrientation(int permInd) {
    return IndexConvertor.unpackOrientation(permInd, (byte) 3, (byte) 8);
  }

  public static byte[] unpackEEdgePermutation(int permInd) {
    return IndexConvertor.unpackPermutation(permInd, (byte) 4);
  }

  public static byte[] unpackUDEdgePermutation(int permInd) {
    return IndexConvertor.unpackPermutation(permInd, (byte) 8);
  }

  public static byte[] unpackEdgeOrientation(int permInd) {
    return IndexConvertor.unpackOrientation(permInd, (byte) 2, (byte) 12);
  }

  public static boolean[] unpackEEdgeCombination(int combInd) {
    return IndexConvertor.unpackCombination(combInd, 4, 12);
  }

  private void assertArrayEquals(byte[] ar1, byte[] ar2) {
    Assert.assertEquals(ar1.length, ar2.length);
    for (int i = 0; i < ar1.length; i++) {
      Assert.assertEquals(ar1[i], ar2[i]);
    }
  }

  private void assertArrayEquals(boolean[] ar1, boolean[] ar2) {
    Assert.assertEquals(ar1.length, ar2.length);
    for (int i = 0; i < ar1.length; i++) {
      Assert.assertEquals(ar1[i], ar2[i]);
    }
  }

}
