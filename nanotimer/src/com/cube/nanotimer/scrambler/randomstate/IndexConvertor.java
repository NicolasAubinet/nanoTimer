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

  // Pack a permutation with positions relative to each other.
  // Used to reduce the memory impact. 0, 1, 2, 3, 4, 5, 6, 7 is then equal for example to 2, 3, 0, 1, 6, 7, 4, 5.
  // This method can only be used for 8 elements permutations (optimized for performance)
  public static int packRel8Permutation(byte[] perm) {
    byte nDifferentValues = (byte) perm.length;
    int permInd = 0;
    byte startInd = -1;
    byte i = 0;
    // TODO : see if could optimize a bit more (still takes ~17% more time than regular perm pack)
    while (i < nDifferentValues) {
      if (startInd >= 0 || perm[i] == 0) {
        if (startInd < 0) {
          startInd = i;
          i = 0;
        }
        byte cur = perm[relPermIndices[startInd][i]];
        byte curInd = cur;
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

  // Permutes and packs a permutation at the same time. Faster than permuting and then packing (avoids new array creation)
  // TODO : see if worth the trouble (see how complicated it would be). Also, it can not handle half turn moves, so it complicates the calls (sometimes would have to call perm and pack separately, sometimes not)
  // TODO : do the same for other needed (like for orientation, those who take a lot of time)
  // Note: can only handle one cycling permutation (does not handle multiple permutations like for half turns)
  /*static static int packRel8PermResult(byte[] state, byte[] permIndices) {
    byte permStart = -1;
    byte tmp = 0;
    byte i = 0;
    while (i < state.length) {
      if (i != permIndices[i]) {
        if (permStart < 0) { // start of permutation
          permStart = i;
          tmp = state[i];
        } else if (permIndices[i] == permStart) { // end of permutation
          state[i] = tmp;
          break;
        }
        state[i] = state[permIndices[i]];
        i = permIndices[i];
      } else {
        i++;
      }
    }
    return state;
  }*/


  public static int packOrientation(byte[] orient, int nDifferentValues) {
    int orientInd = 0;
    for (int i = 0; i < orient.length - 1; i++) {
      orientInd = orientInd * nDifferentValues + orient[i];
    }
    return orientInd;
  }

  public static byte[] unpackPermutation(int permInd, int length) {
    // TODO : takes much longer than packPermutation (pbly due to array creation). See if can improve
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
