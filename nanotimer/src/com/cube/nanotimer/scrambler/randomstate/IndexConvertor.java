package com.cube.nanotimer.scrambler.randomstate;

public class IndexConvertor {

  private static final byte MAX_RELATIVE_PERM_SIZE = 8;
  private static byte[][] relPermIndices;

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

  public static int packPermutation(byte[] perm) {
    int nDifferentValues = perm.length;
    int permInd = 0;
    for (int i = 0; i < perm.length; i++) {
      int curInd = perm[i];
      for (int j = 0; j < i; j++) {
        if (perm[j] < perm[i]) {
          curInd--;
        }
      }
      permInd = permInd * (nDifferentValues - i) + curInd;
    }
    return permInd;
  }

  public static int packPermMult(byte[] state, byte[] permIndices) {
    int nDifferentValues = state.length;
    int permInd = 0;
    for (int i = 0; i < state.length; i++) {
      int curInd = state[permIndices[i]];
      for (int j = 0; j < i; j++) {
        if (state[permIndices[j]] < state[permIndices[i]]) {
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

  public static int packOrientMult(byte[] state, byte[] permIndices, byte[] orientIndices, int nDifferentValues) {
    int orientInd = 0;
    for (int i = 0; i < state.length - 1; i++) {
      byte cur = (byte) ((state[permIndices[i]] + orientIndices[i]) % nDifferentValues);
      orientInd = orientInd * nDifferentValues + cur;
    }
    return orientInd;
  }

  public static byte[] unpackPermutation(int permInd, byte[] dest) {
    int length = dest.length;
    dest[length - 1] = 0;
    for (int i = dest.length - 2; i >= 0; i--) {
      dest[i] = (byte) (permInd % (length - i));
      permInd /= (length - i);
      for (int j = i + 1; j < length; j++) {
        if (dest[j] >= dest[i]) {
          dest[j]++;
        }
      }
    }
    return dest;
  }

  /*public static byte[] unpackPermutation(int permInd, int length) {
    byte[] perm = new byte[length];
    perm[length - 1] = 0;
    for (int i = perm.length - 2; i >= 0; i--) {
      perm[i] = (byte) (permInd % (length - i));
      permInd /= (length - i);
      for (int j = i + 1; j < length; j++) {
        if (perm[j] >= perm[i]) {
          perm[j]++;
        }
      }
    }
    return perm;
  }*/

  public static byte[] unpackOrientation(int orientInd, byte[] dest, byte nDifferentValues) {
    dest[dest.length - 1] = 0;
    for (int i = dest.length - 2; i >= 0; i--) {
      dest[i] = (byte) (orientInd % nDifferentValues);
      orientInd /= nDifferentValues;
      dest[dest.length - 1] += dest[i];
    }
    dest[dest.length - 1] = (byte) ((nDifferentValues - dest[dest.length - 1] % nDifferentValues) % nDifferentValues);
    return dest;
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

  public static int packCombPermMult(boolean[] state, byte[] permIndices, int k) {
    int index = 0;
    for (int i = state.length - 1; i >= 0 && k > 0; i--) {
      if (state[permIndices[i]]) {
        index += nChooseK(i, k--);
      }
    }
    return index;
  }

  public static boolean[] unpackCombination(int index, boolean[] dest, int k) {
    for (int i = dest.length - 1; i >= 0 && k >= 0; i--) {
      int nk = nChooseK(i, k);
      if (index >= nk) {
        dest[i] = true;
        index -= nk;
        k--;
      } else {
        dest[i] = false;
      }
    }
    return dest;
  }

  /*public static boolean[] unpackCombination(int index, int k, int length) {
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
  }*/

}
