package com.cube.nanotimer.scrambler.randomstate.fto;

/**
 * Cubie-level representation of a Face-Turning Octahedron state.
 *
 * Ported (JS -> Java) from cstimer's "ftocta.js" (GPL-3.0), by Shuang Chen
 * (cs0x7f) and contributors: https://github.com/cs0x7f/cstimer
 * (src/js/solver/ftocta.js). This port is GPL-3.0 (same as NanoTimer).
 *
 * State:
 *   cp[6]/co[6] : corner permutation / orientation
 *   ep[12]      : edge permutation (FTO edges have no tracked orientation)
 *   uf[12]      : the 12 centers on the U/F/r/l orbit
 *   rl[12]      : the 12 centers on the D/B/R/L orbit
 *
 * The same-colour centers are physically identical on a real FTO; the facelet
 * round-trip ({@link #toFaceCube()} + {@link #fromFacelet(int[])}) collapses the
 * super-cube distinctions and fixes parity, normalising a state into a legal one.
 */
public class FtoCubie {

  // Face base offsets into the 72-sticker facelet array (9 stickers per face).
  private static final int U = 0, F = 9, r = 18, l = 27, D = 36, B = 45, R = 54, L = 63;

  static final int[][] CORN = {
    {U + 0, R + 0, F + 0, L + 0},
    {U + 4, B + 8, r + 4, R + 8},
    {U + 8, L + 4, l + 8, B + 4},
    {l + 0, D + 0, r + 0, B + 0},
    {F + 4, D + 8, l + 4, L + 8},
    {r + 8, D + 4, F + 8, R + 4},
  };

  static final int[][] EDGE = {
    {U + 1, R + 3}, {U + 3, L + 1}, {U + 6, B + 6},
    {l + 1, D + 3}, {r + 3, D + 1}, {F + 6, D + 6},
    {F + 3, R + 1}, {F + 1, L + 3}, {l + 6, L + 6},
    {l + 3, B + 1}, {r + 1, B + 3}, {r + 6, R + 6},
  };

  static final int[] CTUF = {U + 2, U + 5, U + 7, F + 2, F + 5, F + 7, r + 2, r + 5, r + 7, l + 2, l + 5, l + 7};
  static final int[] CTRL = {D + 2, D + 5, D + 7, B + 2, B + 5, B + 7, L + 2, L + 5, L + 7, R + 2, R + 5, R + 7};

  private static final int[] DEF_CP = {0, 1, 2, 3, 4, 5};
  private static final int[] DEF_CO = {0, 0, 0, 0, 0, 0};
  private static final int[] DEF_E12 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

  public int[] cp;
  public int[] co;
  public int[] ep;
  public int[] uf;
  public int[] rl;

  public FtoCubie() {
    this(null, null, null, null, null);
  }

  public FtoCubie(int[] cp, int[] co, int[] ep, int[] uf, int[] rl) {
    this.cp = (cp != null) ? cp.clone() : DEF_CP.clone();
    this.co = (co != null) ? co.clone() : DEF_CO.clone();
    this.ep = (ep != null) ? ep.clone() : DEF_E12.clone();
    this.uf = (uf != null) ? uf.clone() : DEF_E12.clone();
    this.rl = (rl != null) ? rl.clone() : DEF_E12.clone();
  }

  private static void combine(FtoCubie prod, FtoCubie a, FtoCubie b) {
    for (int i = 0; i < 6; i++) {
      prod.co[i] = a.co[b.cp[i]] ^ b.co[i];
      prod.cp[i] = a.cp[b.cp[i]];
    }
    for (int i = 0; i < 12; i++) {
      prod.ep[i] = a.ep[b.ep[i]];
      prod.uf[i] = a.uf[b.uf[i]];
      prod.rl[i] = a.rl[b.rl[i]];
    }
  }

  /**
   * Product of cubies. Mirrors cstimer's {@code FtoCubie.FtoMult(...)}: the last
   * argument is the output buffer (may be {@code null} to allocate a fresh one),
   * the preceding arguments are the factors applied right-to-left, so
   * {@code mult(p, q, r, out)} computes {@code p.X[i] = ... q.X[r.X[i]] ...}.
   */
  public static FtoCubie mult(FtoCubie... args) {
    int m = args.length - 1; // number of factors; args[m] is the output buffer
    FtoCubie prod = args[m];
    if (prod == null) {
      prod = new FtoCubie();
    }
    if (m == 1) {
      return args[0]; // reduceRight on a single factor returns it unchanged
    }
    FtoCubie b = args[m - 1];
    for (int t = m - 2; t >= 0; t--) {
      combine(prod, args[t], b);
      b = prod;
    }
    return prod;
  }

  public boolean isEqual(FtoCubie fc) {
    for (int i = 0; i < 12; i++) {
      if (ep[i] != fc.ep[i] || uf[i] != fc.uf[i] || rl[i] != fc.rl[i]
          || (i < 6 && (cp[i] != fc.cp[i] || co[i] != fc.co[i]))) {
        return false;
      }
    }
    return true;
  }

  /** Paint the 72 stickers (colours 0..7) for this state. */
  public int[] toFaceCube() {
    int[] f = new int[72];
    int[] co2 = new int[6];
    for (int i = 0; i < 6; i++) {
      co2[i] = co[i] * 2;
    }
    FtoMath.fillFaceletGrouped(CORN, f, cp, co2, 9);
    FtoMath.fillFaceletGrouped(EDGE, f, ep, null, 9);
    FtoMath.fillFaceletFlat(CTUF, f, uf, 9);
    FtoMath.fillFaceletFlat(CTRL, f, rl, 9);
    return f;
  }

  /**
   * Rebuild this state from a 72-sticker facelet array, normalising identical
   * centers and fixing parity. Returns {@code false} if the facelets are illegal.
   */
  public boolean fromFacelet(int[] facelet) {
    int[] f = new int[72];
    long count = 0;
    for (int i = 0; i < 72; i++) {
      f[i] = facelet[i];
      count += 1L << (4 * f[i]);
    }
    if (count != 0x99999999L) {
      return false;
    }
    int[] co2 = new int[6];
    if (FtoMath.detectFaceletGrouped(CORN, f, cp, co2, 9) == -1
        || FtoMath.detectFaceletGrouped(EDGE, f, ep, new int[12], 9) == -1) {
      return false;
    }
    int parity = 0;
    for (int i = 0; i < 6; i++) {
      co[i] = co2[i] >> 1;
      parity ^= co[i];
    }
    if (parity != 0
        || FtoMath.getNParity(FtoMath.getNPerm(cp, 6), 6) != 0
        || FtoMath.getNParity(FtoMath.getNPerm(ep, 12), 12) != 0) {
      return false;
    }
    int[] remainCnts = {3, 3, 3, 3};
    for (int i = 0; i < 12; i++) {
      int col = f[CTUF[i]];
      if (col >= 4 || remainCnts[col] <= 0) {
        return false;
      }
      uf[i] = col * 3 + 3 - remainCnts[col];
      remainCnts[col]--;
    }
    int[] ctrlMap = {0, 1, 3, 2};
    remainCnts = new int[] {3, 3, 3, 3};
    for (int i = 0; i < 12; i++) {
      int faceCol = f[CTRL[i]] - 4;
      if (faceCol < 0 || faceCol >= 4) {
        return false;
      }
      int col = ctrlMap[faceCol];
      if (remainCnts[col] <= 0) {
        return false;
      }
      rl[i] = col * 3 + 3 - remainCnts[col];
      remainCnts[col]--;
    }
    if (FtoMath.getNParity(FtoMath.getNPerm(uf, 12), 12) != 0) {
      for (int i = 0; i < 12; i++) { // swap 0 and 1 to fix parity
        uf[i] ^= (uf[i] < 2) ? 1 : 0;
      }
    }
    if (FtoMath.getNParity(FtoMath.getNPerm(rl, 12), 12) != 0) {
      for (int i = 0; i < 12; i++) { // swap 0 and 1 to fix parity
        rl[i] ^= (rl[i] < 2) ? 1 : 0;
      }
    }
    return true;
  }
}
