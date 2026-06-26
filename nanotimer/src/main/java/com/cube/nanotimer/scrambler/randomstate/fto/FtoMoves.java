package com.cube.nanotimer.scrambler.randomstate.fto;

/**
 * Move definitions and symmetry tables for the FTO, computed once at class load.
 *
 * Ported (JS -> Java) from cstimer's "ftocta.js" ({@code initMoveCube}), GPL-3.0,
 * by Shuang Chen (cs0x7f): https://github.com/cs0x7f/cstimer
 * This port is GPL-3.0 (same as NanoTimer).
 *
 * {@code moveCube} holds 24 cubie moves: indices 0..15 are the 8 face moves
 * (U F r l D B R L), each as a generator (even index) and its square (odd index);
 * 16..23 are wide moves. The 12-element symmetry group is captured by
 * {@code symCube}/{@code symMult}/{@code symMulI}/{@code symMulM}.
 */
public final class FtoMoves {

  public static final FtoCubie[] moveCube = new FtoCubie[24];
  public static final FtoCubie[] symCube = new FtoCubie[12];
  public static final int[][] symMult = new int[12][12];
  public static final int[][] symMulI = new int[12][12];
  public static final int[][] symMulM = new int[12][8];
  public static final FtoCubie[] pyraSymCube = new FtoCubie[12];

  private FtoMoves() {
  }

  static {
    init();
  }

  /** No-op trigger to force class initialization at a chosen time. */
  public static void ensureInit() {
  }

  private static void init() {
    FtoCubie rotU = new FtoCubie(
      new int[] {1, 2, 0, 4, 5, 3}, new int[] {0, 0, 0, 0, 0, 0}, new int[] {2, 0, 1, 5, 3, 4, 10, 11, 6, 7, 8, 9},
      new int[] {1, 2, 0, 7, 8, 6, 10, 11, 9, 4, 5, 3}, new int[] {2, 0, 1, 8, 6, 7, 11, 9, 10, 5, 3, 4});
    FtoCubie rotR = new FtoCubie(
      new int[] {5, 0, 4, 2, 3, 1}, new int[] {1, 1, 0, 1, 1, 0}, new int[] {6, 5, 7, 9, 2, 10, 11, 4, 3, 8, 1, 0},
      new int[] {5, 3, 4, 8, 6, 7, 2, 0, 1, 11, 9, 10}, new int[] {4, 5, 3, 7, 8, 6, 1, 2, 0, 10, 11, 9});

    FtoCubie rotUi = FtoCubie.mult(rotU, rotU, null);
    FtoCubie rotRi = FtoCubie.mult(rotR, rotR, null);
    FtoCubie rotL = FtoCubie.mult(rotUi, rotR, rotU, null);
    FtoCubie rotF = FtoCubie.mult(rotR, rotU, rotRi, null);

    moveCube[0] = new FtoCubie( // U
      new int[] {1, 2, 0, 3, 4, 5}, new int[] {0, 0, 0, 0, 0, 0}, new int[] {2, 0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11},
      new int[] {1, 2, 0, 3, 4, 5, 6, 7, 8, 9, 10, 11}, new int[] {0, 1, 2, 3, 6, 7, 11, 9, 8, 5, 10, 4});
    moveCube[2] = new FtoCubie( // F
      new int[] {4, 1, 2, 3, 5, 0}, new int[] {1, 0, 0, 0, 1, 0}, new int[] {0, 1, 2, 3, 4, 6, 7, 5, 8, 9, 10, 11},
      new int[] {0, 1, 2, 4, 5, 3, 6, 7, 8, 9, 10, 11}, new int[] {0, 9, 10, 3, 4, 5, 2, 7, 1, 8, 6, 11});
    moveCube[4] = new FtoCubie( // r
      new int[] {0, 5, 2, 1, 4, 3}, new int[] {0, 1, 0, 0, 0, 1}, new int[] {0, 1, 2, 3, 10, 5, 6, 7, 8, 9, 11, 4},
      new int[] {0, 1, 2, 3, 4, 5, 7, 8, 6, 9, 10, 11}, new int[] {5, 3, 2, 11, 4, 10, 6, 7, 8, 9, 0, 1});
    moveCube[6] = new FtoCubie( // l
      new int[] {0, 1, 3, 4, 2, 5}, new int[] {0, 0, 1, 1, 0, 0}, new int[] {0, 1, 2, 8, 4, 5, 6, 7, 9, 3, 10, 11},
      new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 9}, new int[] {8, 1, 7, 2, 0, 5, 6, 3, 4, 9, 10, 11});
    moveCube[8] = new FtoCubie( // D
      new int[] {0, 1, 2, 5, 3, 4}, new int[] {0, 0, 0, 0, 0, 0}, new int[] {0, 1, 2, 4, 5, 3, 6, 7, 8, 9, 10, 11},
      new int[] {0, 1, 2, 3, 9, 10, 5, 7, 4, 8, 6, 11}, new int[] {1, 2, 0, 3, 4, 5, 6, 7, 8, 9, 10, 11});
    moveCube[10] = new FtoCubie( // B
      new int[] {0, 3, 1, 2, 4, 5}, new int[] {0, 1, 1, 0, 0, 0}, new int[] {0, 1, 10, 3, 4, 5, 6, 7, 8, 2, 9, 11},
      new int[] {0, 6, 7, 3, 4, 5, 11, 9, 8, 2, 10, 1}, new int[] {0, 1, 2, 4, 5, 3, 6, 7, 8, 9, 10, 11});
    moveCube[12] = new FtoCubie( // R
      new int[] {5, 0, 2, 3, 4, 1}, new int[] {1, 1, 0, 0, 0, 0}, new int[] {6, 1, 2, 3, 4, 5, 11, 7, 8, 9, 10, 0},
      new int[] {5, 3, 2, 8, 4, 7, 6, 0, 1, 9, 10, 11}, new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 9});
    moveCube[14] = new FtoCubie( // L
      new int[] {2, 1, 4, 3, 0, 5}, new int[] {1, 0, 1, 0, 0, 0}, new int[] {0, 8, 2, 3, 4, 5, 6, 1, 7, 9, 10, 11},
      new int[] {11, 1, 10, 2, 0, 5, 6, 7, 8, 9, 3, 4}, new int[] {0, 1, 2, 3, 4, 5, 7, 8, 6, 9, 10, 11});

    moveCube[16] = FtoCubie.mult(rotU, moveCube[8], null);  // Uw = [U] * D
    moveCube[18] = FtoCubie.mult(rotF, moveCube[10], null); // Fw = [F] * B
    moveCube[20] = FtoCubie.mult(rotR, moveCube[6], null);  // Rw = [R] * l
    moveCube[22] = FtoCubie.mult(rotL, moveCube[4], null);  // Lw = [L] * r

    for (int i = 1; i < 24; i += 2) {
      moveCube[i] = new FtoCubie();
      FtoCubie.mult(moveCube[i - 1], moveCube[i - 1], moveCube[i]);
    }

    String[] moveHash = new String[24];
    for (int i = 0; i < 24; i++) {
      moveHash[i] = epKey(moveCube[i]);
    }

    String[] symHash = new String[12];
    FtoCubie fc = new FtoCubie();
    for (int s = 0; s < 12; s++) {
      symCube[s] = new FtoCubie(fc.cp, fc.co, fc.ep, fc.uf, fc.rl);
      symHash[s] = epKey(symCube[s]);
      fc = FtoCubie.mult(fc, rotU, null);
      if (s % 3 == 2) {
        fc = FtoCubie.mult(fc, rotR, rotU, null);
      }
      if (s % 6 == 5) {
        fc = FtoCubie.mult(fc, rotU, rotR, null);
      }
    }

    FtoCubie tmp = new FtoCubie();
    for (int i = 0; i < 12; i++) {
      for (int j = 0; j < 12; j++) {
        FtoCubie.mult(symCube[i], symCube[j], tmp);
        int k = indexOf(symHash, epKey(tmp));
        symMult[i][j] = k;
        symMulI[k][j] = i;
      }
    }
    for (int s = 0; s < 12; s++) {
      for (int j = 0; j < 8; j++) {
        FtoCubie.mult(symCube[symMulI[0][s]], moveCube[j * 2], symCube[s], tmp);
        int k = indexOf(moveHash, epKey(tmp));
        symMulM[s][j] = k >> 1;
      }
    }

    for (int i = 0; i < 12; i++) {
      pyraSymCube[i] = new FtoCubie(symCube[i].cp, symCube[i].co, null, symCube[i].uf, null);
    }
  }

  private static String epKey(FtoCubie c) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 12; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(c.ep[i]);
    }
    return sb.toString();
  }

  private static int indexOf(String[] arr, String key) {
    for (int i = 0; i < arr.length; i++) {
      if (arr[i].equals(key)) {
        return i;
      }
    }
    return -1;
  }
}
