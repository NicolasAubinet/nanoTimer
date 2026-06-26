package com.cube.nanotimer.scrambler.randomstate.fto;

import java.util.Random;

/**
 * Numeric / combinatorial helpers used by the FTO random-state solver.
 *
 * Ported (JS -> Java) from the reduced subset of cstimer's "mathlib.js" that the
 * face-turning-octahedron solver relies on. cstimer is GPL-3.0 licensed, by
 * Shuang Chen (cs0x7f) and contributors:
 *   https://github.com/cs0x7f/cstimer  (src/js/lib/mathlib.js)
 * This port is GPL-3.0 as well (same as NanoTimer); see the project LICENSE.
 *
 * Only the functions actually exercised by the FTO solver are ported. Some
 * functions are reimplemented with the straightforward algorithm rather than
 * cstimer's bit-twiddling fast path: for FTO they are only used to compute
 * permutation parity and self-consistent coordinates, so the exact numbering
 * does not need to match cstimer, only to be internally consistent.
 */
public final class FtoMath {

  private FtoMath() {
  }

  /** Factorials 0!..12! (12 pieces is the maximum for FTO). */
  static final long[] FACT = new long[13];
  static {
    FACT[0] = 1;
    for (int i = 1; i <= 12; i++) {
      FACT[i] = FACT[i - 1] * i;
    }
  }

  /** Read a 4-bit value packed into an int[] (8 nibbles per int). */
  public static int getPruning(int[] table, int index) {
    return (table[index >> 3] >> ((index & 7) << 2)) & 15;
  }

  /**
   * Lehmer-code index of a permutation. If {@code even < 0}, only even
   * permutations are encoded and the index is halved.
   */
  public static int getNPerm(int[] arr, int n, int even) {
    int idx = 0;
    for (int i = 0; i < n - 1; i++) {
      idx *= (n - i);
      for (int j = i + 1; j < n; j++) {
        if (arr[j] < arr[i]) {
          idx++;
        }
      }
    }
    return even < 0 ? (idx >> 1) : idx;
  }

  public static int getNPerm(int[] arr, int n) {
    return getNPerm(arr, n, 0);
  }

  /** Parity of the permutation whose full Lehmer index is {@code idx}. */
  public static int getNParity(int idx, int n) {
    int p = 0;
    for (int i = n - 2; i >= 0; i--) {
      p ^= idx % (n - i);
      idx = idx / (n - i);
    }
    return p & 1;
  }

  public static int getNOri(int[] arr, int n, int evenbase) {
    int base = Math.abs(evenbase);
    int idx = evenbase < 0 ? 0 : arr[0] % base;
    for (int i = n - 1; i > 0; i--) {
      idx = idx * base + arr[i] % base;
    }
    return idx;
  }

  /**
   * Decode an orientation index into {@code arr} (length >= n). With
   * {@code evenbase < 0}, the first element is derived so the sum is 0 mod base.
   */
  public static int[] setNOri(int[] arr, int idx, int n, int evenbase) {
    int base = Math.abs(evenbase);
    int parity = base * n;
    for (int i = 1; i < n; i++) {
      arr[i] = idx % base;
      parity -= arr[i];
      idx = idx / base;
    }
    arr[0] = (evenbase < 0 ? parity : idx) % base;
    return arr;
  }

  /** Rank of a multiset permutation (matched pair with {@link #setMPerm}). */
  public static int getMPerm(int[] arr, int n, int[] cnts, int[] cums) {
    int seen = ~0;
    long idx = 0;
    long x = 1;
    for (int i = 0; i < n; i++) {
      int pi = arr[i];
      idx = idx * (n - i) + (long) Integer.bitCount(seen & ((1 << cums[pi]) - 1)) * x;
      int oldCnt = cnts[pi];
      x = x * oldCnt;
      cnts[pi] = oldCnt - 1;
      seen &= ~(1 << (cums[pi] + cnts[pi]));
    }
    return (int) Math.round((double) idx / (double) x);
  }

  /** Unrank a multiset permutation into {@code arr} (matched pair with {@link #getMPerm}). */
  public static int[] setMPerm(int[] arr, int idx, int n, int[] cnts, long x) {
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < cnts.length; j++) {
        if (cnts[j] == 0) {
          continue;
        }
        long x2 = x * cnts[j] / (n - i);
        if (idx < x2) {
          cnts[j]--;
          arr[i] = j;
          x = x2;
          break;
        }
        idx -= x2;
      }
    }
    return arr;
  }

  /**
   * Combination coordinate (cstimer's {@code Coord} with {@code type == 'c'}),
   * the only Coord variant the FTO solver uses (for the 12 same-orbit centers,
   * grouped 3 per colour).
   */
  public static final class Coord {
    private final int n;
    private final int[] cnts;
    private final int[] cums;
    private final long x;

    public Coord(int[] cnts) {
      this.cnts = cnts.clone();
      int cntn = cnts.length;
      this.cums = new int[cntn + 1];
      for (int i = 1; i <= cntn; i++) {
        cums[i] = cums[i - 1] + cnts[i - 1];
      }
      this.n = cums[cntn];
      double xd = 1;
      int m = n;
      for (int i = 0; i < cntn; i++) {
        for (int j = 1; j <= cnts[i]; j++, m--) {
          xd *= (double) m / j;
        }
      }
      this.x = Math.round(xd);
    }

    public int get(int[] arr) {
      return getMPerm(arr, n, cnts.clone(), cums);
    }

    public int[] set(int[] arr, int idx) {
      return setMPerm(arr, idx, n, cnts.clone(), x);
    }
  }

  /**
   * Paint stickers for grouped pieces (corners/edges). {@code facelets[i]} is the
   * set of sticker ids of piece slot i; {@code perm}/{@code ori} say which piece
   * (and twist) currently occupies slot i. {@code f[sticker] = colour}.
   */
  public static void fillFaceletGrouped(int[][] facelets, int[] f, int[] perm, int[] ori, int divcol) {
    for (int i = 0; i < facelets.length; i++) {
      int[] cubie = facelets[i];
      int p = (perm == null) ? i : perm[i];
      int o = (ori == null || i >= ori.length) ? 0 : ori[i];
      for (int j = 0; j < cubie.length; j++) {
        f[cubie[(j + o) % cubie.length]] = facelets[p][j] / divcol;
      }
    }
  }

  /** Paint stickers for single-sticker pieces (centers). */
  public static void fillFaceletFlat(int[] facelets, int[] f, int[] perm, int divcol) {
    for (int i = 0; i < facelets.length; i++) {
      int cubie = facelets[i];
      int p = (perm == null) ? i : perm[i];
      f[cubie] = facelets[p] / divcol;
    }
  }

  /**
   * Inverse of {@link #fillFaceletGrouped}: read {@code perm}/{@code ori} back
   * from the painted stickers {@code f}. Returns -1 if no match (illegal state).
   */
  public static int detectFaceletGrouped(int[][] facelets, int[] f, int[] perm, int[] ori, int divcol) {
    for (int i = 0; i < facelets.length; i++) {
      int nOri = facelets[i].length;
      boolean matched = false;
      for (int j = 0; j < facelets.length && !matched; j++) {
        if (facelets[j].length != nOri) {
          continue;
        }
        for (int o = 0; o < nOri; o++) {
          boolean isMatch = true;
          for (int t = 0; t < nOri; t++) {
            if (facelets[j][t] / divcol != f[facelets[i][(t + o) % nOri]]) {
              isMatch = false;
              break;
            }
          }
          if (isMatch) {
            perm[i] = j;
            ori[i] = o;
            matched = true;
            break;
          }
        }
      }
      if (!matched) {
        return -1;
      }
    }
    return 0;
  }

  /**
   * Random permutation of 0..n-1 via Fisher-Yates; if {@code isEven} the result
   * is forced to an even permutation.
   */
  public static int[] rndPerm(int n, boolean isEven, Random rnd) {
    int[] arr = new int[n];
    for (int i = 0; i < n; i++) {
      arr[i] = i;
    }
    boolean parityOdd = false;
    for (int i = 0; i < n - 1; i++) {
      int k = rnd.nextInt(n - i);
      if (k != 0) {
        int tmp = arr[i];
        arr[i] = arr[i + k];
        arr[i + k] = tmp;
        parityOdd = !parityOdd;
      }
    }
    if (isEven && parityOdd) {
      int tmp = arr[0];
      arr[0] = arr[1];
      arr[1] = tmp;
    }
    return arr;
  }
}
