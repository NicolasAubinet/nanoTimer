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

  private static final byte MAX_RELATIVE_PERM_SIZE = 8;
  static byte[][] relPermIndices;
  static {
    relPermIndices = new byte[MAX_RELATIVE_PERM_SIZE][MAX_RELATIVE_PERM_SIZE];
    int nPerFace = MAX_RELATIVE_PERM_SIZE / 2;
    for (byte startInd = 0; startInd < MAX_RELATIVE_PERM_SIZE; startInd++) {
      byte found = 0;
      byte i = startInd;
      while (found < MAX_RELATIVE_PERM_SIZE) {
        relPermIndices[startInd][found] = i;
        found++;
        if (startInd < nPerFace) {
          i = (found < nPerFace) ? (byte) ((i + 1) % nPerFace) : (byte) ((((startInd + nPerFace + (found - nPerFace))) % nPerFace) + nPerFace);
        } else {
          i = (found < nPerFace) ? (byte) (((i + 1) % nPerFace) + nPerFace) : (byte) (((startInd - nPerFace + (found - nPerFace))) % nPerFace);
        }
      }
    }
  }

  // Pack a permutation with positions relative to each other.
  // Used to reduce the memory impact. 1, 2, 3, 4, 5, 6, 7, 8 is then equal for example to 3, 4, 1, 2, 7, 8, 5, 6.
  // This method can only be used for 8 elements permutations (optimized for performance)
  public static int packRel8Permutation(byte[] perm) {
    byte nDifferentValues = (byte) perm.length;
    int permInd = 0;
    byte startInd = -1;
    byte i = 0;
    // TODO : see if could optimize a bit more (still takes ~17% more time than regular perm pack)
    while (i < nDifferentValues) {
      if (startInd >= 0 || perm[i] == 1) {
        if (startInd < 0) {
          startInd = i;
          i = 0;
        }
        byte cur = perm[relPermIndices[startInd][i]];
        byte curInd = (byte) (cur - 1); // -1 because positions start at 1
        for (byte j = 0; j < i; j++) {
          if (perm[relPermIndices[startInd][j]] < cur) {
            curInd--;
          }
        }
        permInd = permInd * (nDifferentValues - i) + curInd;
      }
      i++;
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
