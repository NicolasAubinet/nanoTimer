package com.cube.nanotimer.scrambler.randomstate;

import java.util.ArrayList;
import java.util.List;

public class IndexConvertor {

  private static List<Byte> available;

  static {
    available = new ArrayList<Byte>();
    for (byte i = 1; i <= 12; i++) {
      available.add(i);
    }
  }

  public static int packCornerPermutation(byte[] perm) {
    return packPermutation(perm);
  }

  public static int packCornerOrientation(byte[] perm) {
    return packOrientation(perm, (byte) 3);
  }

  public static int packEdgePermutation(byte[] perm) {
    return packPermutation(perm);
  }

  public static int packEdgeOrientation(byte[] perm) {
    return packOrientation(perm, (byte) 2);
  }


  public static byte[] unpackCornerPermutation(int permInd) {
    return unpackPermutation(permInd, (byte) 7);
  }

  public static byte[] unpackCornerOrientation(int permInd) {
    return unpackOrientation(permInd, (byte) 3, (byte) 7);
  }

  public static byte[] unpackEdgePermutation(int permInd) {
    return unpackPermutation(permInd, (byte) 11);
  }

  public static byte[] unpackEdgeOrientation(int permInd) {
    return unpackOrientation(permInd, (byte) 2, (byte) 11);
  }


  private static int packPermutation(byte[] perm) {
    int nDifferentValues = perm.length + 1; // +1 because the perm array is missing the last element
    int permInd = 0;
    for (int i = 0; i < perm.length; i++) {
      int curInd = perm[i] - 1; // -1 because positions start at 1
      for (int j = 0; j < i; j++) {
        if (perm[j] < perm[i]) {
          curInd--;
        }
      }
      permInd = permInd * (nDifferentValues - i) + curInd;
    }
    return permInd;
  }

  private static int packOrientation(byte[] orient, byte nDifferentValues) {
    int orientInd = 0;
    for (byte p : orient) {
      orientInd = orientInd * nDifferentValues + p;
    }
    return orientInd;
  }

  private static byte[] unpackPermutation(int permInd, byte length) {
    int nDifferentValues = length + 1; // +1 because the perm array is missing the last element
    byte[] perm = new byte[length];
    // TODO : could maybe merge the loops for improved performance
    for (int i = perm.length - 1; i >= 0; i--) {
      perm[i] = (byte) (permInd % (nDifferentValues - i));
      permInd /= (nDifferentValues - i);
    }
    List<Byte> avail = new ArrayList<Byte>(available.subList(0, nDifferentValues));
    for (int i = 0; i < perm.length; i++) {
      perm[i] = avail.remove(perm[i]);
    }
    return perm;
  }

  private static byte[] unpackOrientation(int orientInd, byte nDifferentValues, byte length) {
    byte[] orient = new byte[length];
    for (int i = orient.length - 1; i >= 0; i--) {
      orient[i] = (byte) (orientInd % nDifferentValues);
      orientInd /= nDifferentValues;
    }
    return orient;
  }

}
