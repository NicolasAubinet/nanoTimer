package com.cube.nanotimer.scrambler.randomstate;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
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
    Assert.assertEquals(0, IndexConvertor.packCornerOrientation(state));
    assertArrayEquals(state, IndexConvertor.unpackCornerOrientation(IndexConvertor.packCornerOrientation(state)));
    state = new byte[] { 0, 0, 0, 0, 0, 1, 0, 0 };
    assertArrayEquals(state, IndexConvertor.unpackCornerOrientation(IndexConvertor.packCornerOrientation(state)));
    state = new byte[] { 1, 1, 1, 1, 1, 1, 1, 1 };
    assertArrayEquals(state, IndexConvertor.unpackCornerOrientation(IndexConvertor.packCornerOrientation(state)));
    state = new byte[] { 2, 2, 2, 2, 2, 2, 2, 2 };
    assertArrayEquals(state, IndexConvertor.unpackCornerOrientation(IndexConvertor.packCornerOrientation(state)));

    // Corner permutation
    state = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
    Assert.assertEquals(0, IndexConvertor.packCornerPermutation(state));
    assertArrayEquals(state, IndexConvertor.unpackCornerPermutation(IndexConvertor.packCornerPermutation(state)));
    state = new byte[] { 1, 2, 3, 4, 5, 6, 8, 7 };
    assertArrayEquals(state, IndexConvertor.unpackCornerPermutation(IndexConvertor.packCornerPermutation(state)));
    state = new byte[] { 1, 2, 3, 4, 5, 7, 6, 8 };
    assertArrayEquals(state, IndexConvertor.unpackCornerPermutation(IndexConvertor.packCornerPermutation(state)));
    state = new byte[] { 1, 2, 3, 4, 6, 7, 5, 8 };
    assertArrayEquals(state, IndexConvertor.unpackCornerPermutation(IndexConvertor.packCornerPermutation(state)));
    state = new byte[] { 8, 7, 6, 5, 4, 2, 3, 1 };
    assertArrayEquals(state, IndexConvertor.unpackCornerPermutation(IndexConvertor.packCornerPermutation(state)));
    state = new byte[] { 8, 7, 6, 5, 4, 3, 1, 2 };
    assertArrayEquals(state, IndexConvertor.unpackCornerPermutation(IndexConvertor.packCornerPermutation(state)));
    state = new byte[] { 8, 7, 6, 5, 4, 3, 2, 1 };
    assertArrayEquals(state, IndexConvertor.unpackCornerPermutation(IndexConvertor.packCornerPermutation(state)));
    state = new byte[] { 6, 4, 1, 2, 7, 5, 8, 3 };
    assertArrayEquals(state, IndexConvertor.unpackCornerPermutation(IndexConvertor.packCornerPermutation(state)));
    state = new byte[] { 5, 4, 1, 2, 8, 6, 7, 3 };
    assertArrayEquals(state, IndexConvertor.unpackCornerPermutation(IndexConvertor.packCornerPermutation(state)));
    state = new byte[] { 4, 1, 3, 7, 2, 8, 6, 5 };
    assertArrayEquals(state, IndexConvertor.unpackCornerPermutation(IndexConvertor.packCornerPermutation(state)));

    // Edge orientation
    state = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    Assert.assertEquals(0, IndexConvertor.packEdgeOrientation(state));
    assertArrayEquals(state, IndexConvertor.unpackEdgeOrientation(IndexConvertor.packEdgeOrientation(state)));
    state = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
    assertArrayEquals(state, IndexConvertor.unpackEdgeOrientation(IndexConvertor.packEdgeOrientation(state)));
    state = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    assertArrayEquals(state, IndexConvertor.unpackEdgeOrientation(IndexConvertor.packEdgeOrientation(state)));
    state = new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
    assertArrayEquals(state, IndexConvertor.unpackEdgeOrientation(IndexConvertor.packEdgeOrientation(state)));

    // Edge permutation
    state = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
    Assert.assertEquals(0, IndexConvertor.packEdgePermutation(state));
    assertArrayEquals(state, IndexConvertor.unpackUDEdgePermutation(IndexConvertor.packEdgePermutation(state)));
    state = new byte[] { 1, 2, 3, 4 };
    Assert.assertEquals(0, IndexConvertor.packEdgePermutation(state));
    assertArrayEquals(state, IndexConvertor.unpackEEdgePermutation(IndexConvertor.packEdgePermutation(state)));
    state = new byte[] { 8, 7, 6, 5, 4, 3, 2, 1 };
    assertArrayEquals(state, IndexConvertor.unpackUDEdgePermutation(IndexConvertor.packEdgePermutation(state)));
    state = new byte[] { 4, 3, 2, 1 };
    assertArrayEquals(state, IndexConvertor.unpackEEdgePermutation(IndexConvertor.packEdgePermutation(state)));
    state = new byte[] { 1, 7, 6, 5, 4, 3, 2, 8 };
    assertArrayEquals(state, IndexConvertor.unpackUDEdgePermutation(IndexConvertor.packEdgePermutation(state)));
    state = new byte[] { 2, 3, 1, 4 };
    assertArrayEquals(state, IndexConvertor.unpackEEdgePermutation(IndexConvertor.packEdgePermutation(state)));
    state = new byte[] { 5, 4, 8, 1, 6, 3, 7, 2 };
    assertArrayEquals(state, IndexConvertor.unpackUDEdgePermutation(IndexConvertor.packEdgePermutation(state)));
    state = new byte[] { 4, 3, 1, 2 };
    assertArrayEquals(state, IndexConvertor.unpackEEdgePermutation(IndexConvertor.packEdgePermutation(state)));
    state = new byte[] { 4, 1, 5, 2, 7, 8, 3, 6 };
    assertArrayEquals(state, IndexConvertor.unpackUDEdgePermutation(IndexConvertor.packEdgePermutation(state)));
    state = new byte[] { 2, 4, 3, 1 };
    assertArrayEquals(state, IndexConvertor.unpackEEdgePermutation(IndexConvertor.packEdgePermutation(state)));
  }

  @SmallTest
  public void testRandomConversion() {
    Random r = new Random();
    byte[] state;
    List<Byte> available;
    for (int i = 0; i < 200; i++) {
      // Corner orientation
      state = new byte[8];
      for (int j = 0; j < 8; j++) { state[j] = (byte) r.nextInt(3); }
      assertArrayEquals(state, IndexConvertor.unpackCornerOrientation(IndexConvertor.packCornerOrientation(state)));

      // Corner permutation
      state = new byte[8];
      available = new ArrayList<Byte>();
      for (byte j = 1; j <= 8; j++) { available.add(j); }
      Collections.shuffle(available, r);
      for (int j = 0; j < 8; j++) {
        state[j] = available.remove(0);
      }
      assertArrayEquals(state, IndexConvertor.unpackCornerPermutation(IndexConvertor.packCornerPermutation(state)));

      // Edge orientation
      state = new byte[12];
      for (int j = 0; j < 12; j++) { state[j] = (byte) r.nextInt(2); }
      assertArrayEquals(state, IndexConvertor.unpackEdgeOrientation(IndexConvertor.packEdgeOrientation(state)));

      // Edge permutation
      state = new byte[4];
      available = new ArrayList<Byte>();
      for (byte j = 1; j <= 4; j++) { available.add(j); }
      Collections.shuffle(available, r);
      for (int j = 0; j < 4; j++) {
        state[j] = available.remove(0);
      }
      assertArrayEquals(state, IndexConvertor.unpackEEdgePermutation(IndexConvertor.packEdgePermutation(state)));

      state = new byte[8];
      available = new ArrayList<Byte>();
      for (byte j = 1; j <= 8; j++) { available.add(j); }
      Collections.shuffle(available, r);
      for (int j = 0; j < 8; j++) {
        state[j] = available.remove(0);
      }
      assertArrayEquals(state, IndexConvertor.unpackUDEdgePermutation(IndexConvertor.packEdgePermutation(state)));
    }
  }

  private void assertArrayEquals(byte[] ar1, byte[] ar2) {
    Assert.assertEquals(ar1.length, ar2.length);
    for (int i = 0; i < ar1.length; i++) {
      Assert.assertEquals(ar1[i], ar2[i]);
    }
  }

}
