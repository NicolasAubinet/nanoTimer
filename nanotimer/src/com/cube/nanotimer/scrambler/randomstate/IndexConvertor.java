package com.cube.nanotimer.scrambler.randomstate;

public class IndexConvertor {

  public static int packPermutation(byte[] perm) {
    int nDifferentValues = perm.length;
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

  public static int packOrientation(byte[] orient, int nDifferentValues) {
    int orientInd = 0;
    for (int i = 0; i < orient.length - 1; i++) {
      orientInd = orientInd * nDifferentValues + orient[i];
    }
    return orientInd;
  }

  public static byte[] unpackPermutation(int permInd, int length) {
    byte[] perm = new byte[length];
    perm[length - 1] = 1;
    for (int i = perm.length - 2; i >= 0; i--) {
      perm[i] = (byte) ((permInd % (length - i)) + 1);
      permInd /= (length - i);
      for (int j = i + 1; j < length; j++) {
        if (perm[j] >= perm[i]) {
          perm[j]++;
        }
      }
    }
    return perm;
  }

  public static byte[] unpackOrientation(int orientInd, int nDifferentValues, int length) {
    byte[] orient = new byte[length];
    orient[orient.length - 1] = 0;
    for (int i = orient.length - 2; i >= 0; i--) {
      orient[i] = (byte) (orientInd % nDifferentValues);
      orientInd /= nDifferentValues;
      orient[orient.length - 1] += orient[i];
    }
    orient[length - 1] = (byte) ((nDifferentValues - orient[orient.length - 1] % nDifferentValues) % nDifferentValues);
    return orient;
  }

  // combinations
  private static int nChooseK(int n, int k) {
    int value = 1;

    for (int i = 0; i < k; i++) {
      value *= n - i;
    }

    for (int i = 0; i < k; i++) {
      value /= k - i;
    }

    return value;
  }

  public static int packCombination(boolean[] combination, int k) {
    int index = 0;
    for (int i = combination.length - 1; i >= 0 && k > 0; i--) {
      if (combination[i]) {
        index += nChooseK(i, k--);
      }
    }
    return index;
  }

  public static boolean[] unpackCombination(int index, int k, int length) {
    boolean[] combination = new boolean[length];
    for (int i = length - 1; i >= 0 && k >= 0; i--) {
      int nk = nChooseK(i, k);
      if (index >= nk) {
        combination[i] = true;
        index -= nk;
        k--;
      }
    }
    return combination;
  }

}
