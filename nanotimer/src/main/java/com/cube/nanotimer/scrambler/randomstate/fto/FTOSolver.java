package com.cube.nanotimer.scrambler.randomstate.fto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Random-state solver for the Face-Turning Octahedron.
 *
 * Ported (JS -> Java) from cstimer's "ftocta.js" (GPL-3.0), by Shuang Chen
 * (cs0x7f) and contributors: https://github.com/cs0x7f/cstimer
 * (src/js/solver/ftocta.js). This port is GPL-3.0 (same as NanoTimer).
 *
 * It is a symmetry-reduced three-phase IDA* search. The heavy move/pruning tables
 * are built once ({@link #genTables()}) and shared read-only; each solver instance
 * holds its own {@link FtoSearch.Searcher}s so generation can run on many threads.
 */
public class FTOSolver {

  // Face base offsets into the 72-sticker facelet array.
  private static final int U = 0, F = 9, r = 18, l = 27, D = 36, B = 45, R = 54, L = 63;

  // Extra corner stickers (the ones adjacent to the U-corners), used by the
  // phase-2 "necessity" heuristic.
  private static final int[][] CORN_EX = {
    {U + 2, R + 2, F + 2, L + 2},
    {U + 5, B + 7, r + 5, R + 7},
    {U + 7, L + 5, l + 7, B + 5},
    {l + 2, D + 2, r + 2, B + 2},
    {F + 5, D + 7, l + 5, L + 7},
    {r + 7, D + 5, F + 7, R + 5},
  };

  private static final int[] PHASE1_MOVES = {0, 2, 22, 6, 16, 10, 12, 14}; // keep the (D, DR) edge
  private static final int[] PHASE2_MOVES = {0, 12, 14, 8, 10};
  private static final int[] PHASE3_MOVES = {8, 10, 12, 14};
  // More phase-1 candidates give phase 2 more (and better) starting points, so it
  // finds a short solution faster. Counter-intuitively, lowering this slows solving
  // down (phase 2 must search deeper) and lengthens scrambles, so keep it high.
  private static final int N_PHASE1_SOLS = 1000;
  private static final int P2EPRL_MAXL = 11;

  private static final String[] PRETTY =
    {"U", "U'", "F", "F'", "BR", "BR'", "BL", "BL'", "D", "D'", "B", "B'", "R", "R'", "L", "L'"};

  private static final int[] P2NEC_PRUN = { // idx = (a << 2 | b) * 7 + c
    0, 99, 3, 4, 5, 6, 8,
    99, 2, 3, 4, 5, 6, 8,
    1, 3, 4, 5, 6, 7, 8,
    1, 3, 4, 5, 6, 7, 9,
    99, 2, 3, 4, 5, 6, 8,
    2, 2, 4, 4, 5, 6, 8,
    3, 3, 4, 5, 6, 7, 8,
    3, 3, 4, 5, 6, 7, 9,
    3, 3, 4, 5, 6, 7, 8,
    4, 4, 4, 5, 6, 7, 8,
    4, 4, 5, 6, 7, 8, 9,
    4, 4, 5, 6, 7, 8, 9,
    4, 4, 5, 6, 7, 8, 9,
    4, 4, 5, 6, 7, 8, 9,
    5, 5, 6, 7, 8, 9, 10,
    5, 5, 6, 7, 8, 9, 10,
  };

  private static volatile boolean inited = false;

  // Phase 1
  private static FtoSearch.MoveHash p1epMoves, p1rlMoves;
  private static int[] p1eprlPrun, ckmv1;
  private static int nP1ep, nP1rl;

  // Phase 2
  private static FtoSearch.MoveHash p2epMoves, p2rlMoves, p2ccMoves;
  private static int[] p2eprlPrun, ckmv2;
  private static int nP2ep, nP2rl;
  private static int[] p2symMap;
  private static int[] ufRaw2Std;
  private static int[] ufStd2Bit;
  private static int[][] p2ufMoveStd;
  private static int[] cc2Bit;
  private static int[][] p2ccRecol;
  // Populated during genTables() (single-threaded); only read during solving. A
  // concurrent map keeps it safe even if a never-before-seen (cp,co) showed up while
  // generating on several threads.
  private static final Map<String, Integer> p2cc2ufBit = new java.util.concurrent.ConcurrentHashMap<>();
  private static final FtoMath.Coord p2ufCoord = new FtoMath.Coord(new int[] {3, 3, 3, 3});

  // Phase 3
  private static FtoSearch.MoveHash p3epMoves, p3ufMoves;
  private static int[] p3epPrun, p3ufPrun, ckmv3;

  // Per-instance search state
  private final FtoSearch.Searcher solv1;
  private final FtoSearch.Searcher solv2;
  private final FtoSearch.Searcher solv3;
  private volatile boolean stopped;

  public FTOSolver() {
    genTables();
    solv1 = buildSolv1();
    solv2 = buildSolv2();
    solv3 = buildSolv3();
  }

  public void stop() {
    stopped = true;
    solv1.aborted = true;
    solv2.aborted = true;
    solv3.aborted = true;
  }

  // ------------------------------------------------------------------ moves

  private static int[] permMove(int[] perm, int[] movePerm) {
    int[] ret = new int[12];
    for (int i = 0; i < 12; i++) {
      ret[i] = perm[movePerm[i]];
    }
    return ret;
  }

  private static FtoCubie fullMove(FtoCubie fc, int move) {
    return FtoCubie.mult(fc, FtoMoves.moveCube[move], null);
  }

  private static int[] genCkmv(int[] moves) {
    int[] ckmv = new int[moves.length];
    FtoCubie tmp1 = new FtoCubie();
    FtoCubie tmp2 = new FtoCubie();
    for (int m1 = 0; m1 < moves.length; m1++) {
      ckmv[m1] = 1 << m1;
      for (int m2 = 0; m2 < m1; m2++) {
        FtoCubie.mult(FtoMoves.moveCube[moves[m1]], FtoMoves.moveCube[moves[m2]], tmp1);
        FtoCubie.mult(FtoMoves.moveCube[moves[m2]], FtoMoves.moveCube[moves[m1]], tmp2);
        if (tmp1.isEqual(tmp2)) {
          ckmv[m1] |= 1 << m2;
        }
      }
    }
    return ckmv;
  }

  // ------------------------------------------------------------------ hashes

  private static int phase1EdgeHash(int[] ep) {
    int ret = 0;
    int e3fst = -1;
    for (int i = 0; i < 12; i++) {
      if (((0x38 >> ep[i]) & 1) == 0) {
        continue;
      }
      if (e3fst == -1) {
        e3fst = ep[i];
      }
      ret += (((ep[i] - e3fst + 3) % 3) + 1) << (i * 2);
    }
    return ret;
  }

  private static int phase1CtrlHash(int[] rl) {
    int ret = 0;
    for (int i = 0; i < 12; i++) {
      if (rl[i] < 3) {
        ret |= 1 << i;
      }
    }
    return ret;
  }

  private static final int[] EDGE2GROUP = {0, 1, 2, 3, 3, 3, 0, 1, 1, 2, 2, 0};
  private static final int[][] EDGE_GROUPS = {{0, 6, 11}, {1, 7, 8}, {2, 9, 10}, {3, 4, 5}};

  private static long phase2EdgeHash(int[] ep) {
    long ret = 0;
    int[] egoff = {-1, -1, -1, -1};
    for (int i = 0; i < 12; i++) {
      int g = EDGE2GROUP[ep[i]];
      int gidx = indexOf(EDGE_GROUPS[g], ep[i]);
      if (egoff[g] == -1) {
        egoff[g] = gidx;
      }
      ret += (long) (g * 4 + (gidx - egoff[g] + 3) % 3) * (1L << (4 * i));
    }
    return ret;
  }

  private static int phase2CtHash(int[] ct) {
    int ret = 0;
    for (int i = 0; i < 12; i++) {
      ret |= (ct[i] / 3) << (i * 2);
    }
    return ret;
  }

  private static String phase3EdgeHash(int[] ep) {
    return charKey(ep);
  }

  private static String phase3CcufHash(FtoCubie fc) {
    return charKey(fc.cp, fc.co);
  }

  private static String phase2CpcoHash(FtoCubie fc) {
    String ret = charKey(fc.cp, fc.co);
    if (!p2cc2ufBit.containsKey(ret)) {
      int[] co = new int[6];
      for (int i = 0; i < 6; i++) {
        co[i] = fc.co[i] * 2;
      }
      int[] facelet = fc.toFaceCube();
      FtoMath.fillFaceletGrouped(CORN_EX, facelet, fc.cp, co, 9);
      FtoCubie fc2 = new FtoCubie();
      fc2.fromFacelet(facelet);
      p2cc2ufBit.put(ret, phase2CtHash(fc2.uf));
    }
    return ret;
  }

  // re-color so uf is minimised in lexicographical order; returns the sym used
  private static int phase2ufStd(int[] uf, int[] symMap) {
    int col1 = uf[0];
    int col2 = -1;
    for (int i = 1; i < 12; i++) {
      if (uf[i] != col1) {
        col2 = uf[i];
        break;
      }
    }
    int sym = symMap[col1 * 4 + col2];
    for (int i = 0; i < 12; i++) {
      uf[i] = FtoMoves.symCube[sym].uf[uf[i] * 3] / 3;
    }
    return sym;
  }

  private static int getPhase2ufIdx(int[] uf) {
    int[] ufstd = new int[12];
    for (int i = 0; i < 12; i++) {
      ufstd[i] = uf[i] / 3;
    }
    int sym = phase2ufStd(ufstd, p2symMap);
    return ufRaw2Std[p2ufCoord.get(ufstd)] << 4 | sym;
  }

  // ------------------------------------------------------------------ init

  public static void genTables() {
    if (inited) {
      return; // fast path: avoid locking on every per-scramble solver creation
    }
    synchronized (FTOSolver.class) {
      if (inited) {
        return;
      }
      FtoMoves.ensureInit();
      phase1Init();
      phase2Init();
      phase3Init();
      inited = true;
    }
  }

  private static void phase1Init() {
    FtoCubie fc = new FtoCubie();
    p1epMoves = FtoSearch.createMoveHash(fc.ep.clone(), PHASE1_MOVES,
      new FtoSearch.HashFunc() {
        @Override
        public Object hash(Object state) {
          return phase1EdgeHash((int[]) state);
        }
      },
      new FtoSearch.MoveFunc() {
        @Override
        public Object move(Object state, int move) {
          return permMove((int[]) state, FtoMoves.moveCube[move].ep);
        }
      });
    p1rlMoves = FtoSearch.createMoveHash(fc.rl.clone(), PHASE1_MOVES,
      new FtoSearch.HashFunc() {
        @Override
        public Object hash(Object state) {
          return phase1CtrlHash((int[]) state);
        }
      },
      new FtoSearch.MoveFunc() {
        @Override
        public Object move(Object state, int move) {
          return permMove((int[]) state, FtoMoves.moveCube[move].rl);
        }
      });
    nP1ep = p1epMoves.nStates();
    nP1rl = p1rlMoves.nStates();
    ckmv1 = genCkmv(PHASE1_MOVES);
    p1eprlPrun = FtoSearch.createPrun(0, nP1ep * nP1rl, 14, new FtoSearch.DoMove() {
      @Override
      public int move(int idx, int move) {
        int rl = idx / nP1ep;
        int ep = idx % nP1ep;
        return p1rlMoves.moveTable[move][rl] * nP1ep + p1epMoves.moveTable[move][ep];
      }
    }, PHASE1_MOVES.length, 2);
  }

  private static void phase2Init() {
    FtoCubie fc = new FtoCubie();
    p2epMoves = FtoSearch.createMoveHash(fc.ep.clone(), PHASE2_MOVES,
      new FtoSearch.HashFunc() {
        @Override
        public Object hash(Object state) {
          return phase2EdgeHash((int[]) state);
        }
      },
      new FtoSearch.MoveFunc() {
        @Override
        public Object move(Object state, int move) {
          return permMove((int[]) state, FtoMoves.moveCube[move].ep);
        }
      });
    p2rlMoves = FtoSearch.createMoveHash(fc.rl.clone(), PHASE2_MOVES,
      new FtoSearch.HashFunc() {
        @Override
        public Object hash(Object state) {
          return phase2CtHash((int[]) state);
        }
      },
      new FtoSearch.MoveFunc() {
        @Override
        public Object move(Object state, int move) {
          return permMove((int[]) state, FtoMoves.moveCube[move].rl);
        }
      });
    p2ccMoves = FtoSearch.createMoveHash(new FtoCubie(), PHASE2_MOVES,
      new FtoSearch.HashFunc() {
        @Override
        public Object hash(Object state) {
          return phase2CpcoHash((FtoCubie) state);
        }
      },
      new FtoSearch.MoveFunc() {
        @Override
        public Object move(Object state, int move) {
          return fullMove((FtoCubie) state, move);
        }
      });

    p2symMap = new int[16];
    p2ccRecol = new int[12][];
    for (int s = 0; s < 12; s++) {
      int[] uf = FtoMoves.symCube[s].uf;
      int col1 = indexOf(uf, 0) / 3;
      int col2 = indexOf(uf, 3) / 3;
      p2symMap[col1 * 4 + col2] = s;
    }

    // enumerate the lexicographically-minimal ("standard") center colourings
    List<Integer> ufStd2Raw = new ArrayList<>();
    ufRaw2Std = new int[42000];
    int[] arr = new int[12];
    int[] arr2 = new int[12];
    outer:
    for (int i = 0; i < 42000; i++) {
      p2ufCoord.set(arr, i);
      for (int j = 1; j < 12; j++) {
        if (arr[j] > 1) {
          continue outer;
        } else if (arr[j] == 1) {
          break;
        }
      }
      ufRaw2Std[i] = ufStd2Raw.size();
      ufStd2Raw.add(i);
    }

    int nStd = ufStd2Raw.size();
    ufStd2Bit = new int[nStd];
    p2ufMoveStd = new int[PHASE2_MOVES.length][nStd];
    for (int i = 0; i < nStd; i++) {
      p2ufCoord.set(arr, ufStd2Raw.get(i));
      int hash = 0;
      for (int j = 0; j < 12; j++) {
        hash |= arr[j] << (j * 2);
      }
      ufStd2Bit[i] = hash;
      for (int m = 0; m < PHASE2_MOVES.length; m++) {
        int[] moveUf = FtoMoves.moveCube[PHASE2_MOVES[m]].uf;
        for (int k = 0; k < 12; k++) {
          arr2[k] = arr[moveUf[k]];
        }
        int sym = phase2ufStd(arr2, p2symMap);
        p2ufMoveStd[m][i] = ufRaw2Std[p2ufCoord.get(arr2)] << 4 | sym;
      }
    }

    int nP2cc = p2ccMoves.nStates();
    cc2Bit = new int[nP2cc];
    for (int s = 0; s < 12; s++) {
      p2ccRecol[s] = new int[nP2cc];
    }
    for (Map.Entry<Object, Integer> e : p2ccMoves.hash2idx.entrySet()) {
      String key = (String) e.getKey();
      int idx = e.getValue();
      cc2Bit[idx] = p2cc2ufBit.get(key);
      for (int s = 0; s < 12; s++) {
        FtoCubie sc = FtoMoves.symCube[s];
        char[] cpco = new char[12];
        for (int i = 0; i < 6; i++) {
          int scpi = key.charAt(i);
          cpco[i] = (char) sc.cp[scpi];
          cpco[i + 6] = (char) (sc.co[scpi] ^ key.charAt(i + 6));
        }
        p2ccRecol[s][idx] = p2ccMoves.hash2idx.get(new String(cpco));
      }
    }

    nP2ep = p2epMoves.nStates();
    nP2rl = p2rlMoves.nStates();
    // Guard the fixed phase-2 long-packing layout (ep 14b, rl 12b, cc 14b, uf 24b):
    // fail loudly here rather than silently corrupt coordinates if a table grew.
    if (nP2ep > (1 << 14) || nP2rl > (1 << 12) || nP2cc > (1 << 14)
        || (((long) (nStd - 1) << 4) | 0xf) >= (1L << 24)) {
      throw new IllegalStateException("FTO phase-2 coordinates exceed the long-packing budget");
    }
    p2eprlPrun = FtoSearch.createPrun(0, nP2ep * nP2rl, P2EPRL_MAXL - 2, new FtoSearch.DoMove() {
      @Override
      public int move(int idx, int move) {
        int rl = idx / nP2ep;
        int ep = idx % nP2ep;
        return p2rlMoves.moveTable[move][rl] * nP2ep + p2epMoves.moveTable[move][ep];
      }
    }, PHASE2_MOVES.length, 2);
    ckmv2 = genCkmv(PHASE2_MOVES);
  }

  private static void phase3Init() {
    FtoCubie fc = new FtoCubie();
    p3epMoves = FtoSearch.createMoveHash(fc.ep.clone(), PHASE3_MOVES,
      new FtoSearch.HashFunc() {
        @Override
        public Object hash(Object state) {
          return phase3EdgeHash((int[]) state);
        }
      },
      new FtoSearch.MoveFunc() {
        @Override
        public Object move(Object state, int move) {
          return permMove((int[]) state, FtoMoves.moveCube[move].ep);
        }
      });
    p3ufMoves = FtoSearch.createMoveHash(new FtoCubie(), PHASE3_MOVES,
      new FtoSearch.HashFunc() {
        @Override
        public Object hash(Object state) {
          return phase3CcufHash((FtoCubie) state);
        }
      },
      new FtoSearch.MoveFunc() {
        @Override
        public Object move(Object state, int move) {
          return fullMove((FtoCubie) state, move);
        }
      });
    p3epPrun = FtoSearch.createPrun(0, p3epMoves.nStates(), 14, p3epMoves.moveTable, PHASE3_MOVES.length, 2);
    p3ufPrun = FtoSearch.createPrun(0, p3ufMoves.nStates(), 14, p3ufMoves.moveTable, PHASE3_MOVES.length, 2);
    ckmv3 = genCkmv(PHASE3_MOVES);
  }

  // ------------------------------------------------------------------ searchers

  private FtoSearch.Searcher buildSolv1() {
    return new FtoSearch.Searcher(null,
      new FtoSearch.SearchPrun() {
        @Override
        public int prun(long idx) {
          return FtoMath.getPruning(p1eprlPrun, hi32(idx) * nP1ep + lo32(idx));
        }
      },
      new FtoSearch.SearchMove() {
        @Override
        public long move(long idx, int axis) {
          return pack32(p1epMoves.moveTable[axis][lo32(idx)], p1rlMoves.moveTable[axis][hi32(idx)]);
        }
      }, 8, 2, ckmv1);
  }

  private FtoSearch.Searcher buildSolv2() {
    return new FtoSearch.Searcher(null,
      new FtoSearch.SearchPrun() {
        @Override
        public int prun(long idx) {
          int uf = p2uf(idx);
          int xors = ufStd2Bit[uf >> 4] ^ cc2Bit[p2ccRecol[uf & 0xf][p2cc(idx)]];
          xors = (xors | (xors >> 1)) & 0x555555;
          int necIdx = ((Integer.bitCount(xors & 0x3f) << 2) | Integer.bitCount(xors & 0xc0c0c0)) * 7
            + Integer.bitCount(xors & 0x3f3f00);
          return Math.max(
            Math.min(P2EPRL_MAXL, FtoMath.getPruning(p2eprlPrun, p2rl(idx) * nP2ep + p2ep(idx))),
            P2NEC_PRUN[necIdx]);
        }
      },
      new FtoSearch.SearchMove() {
        @Override
        public long move(long idx, int axis) {
          int uf = p2uf(idx);
          int ufidx1 = p2ufMoveStd[axis][uf >> 4];
          int ufcol = FtoMoves.symMult[ufidx1 & 0xf][uf & 0xf];
          return pack2(
            p2epMoves.moveTable[axis][p2ep(idx)],
            p2rlMoves.moveTable[axis][p2rl(idx)],
            p2ccMoves.moveTable[axis][p2cc(idx)],
            (ufidx1 & ~0xf) | ufcol);
        }
      }, PHASE2_MOVES.length, 2, ckmv2);
  }

  private FtoSearch.Searcher buildSolv3() {
    return new FtoSearch.Searcher(null,
      new FtoSearch.SearchPrun() {
        @Override
        public int prun(long idx) {
          return Math.max(FtoMath.getPruning(p3epPrun, lo32(idx)), FtoMath.getPruning(p3ufPrun, hi32(idx)));
        }
      },
      new FtoSearch.SearchMove() {
        @Override
        public long move(long idx, int axis) {
          return pack32(p3epMoves.moveTable[axis][lo32(idx)], p3ufMoves.moveTable[axis][hi32(idx)]);
        }
      }, 4, 2, ckmv3);
  }

  // ------------------------------------------------------------------ phases

  private int[] move2std(int[] moves, int[] symOut) {
    int sym = 0;
    int[] ret = new int[moves.length];
    int[] w2axis = {4, 5, 3, 2};
    int[] w2rot = {1, 10, 5, 11};
    for (int i = 0; i < moves.length; i++) {
      int rot = 0;
      int axis = moves[i] >> 1;
      int pow = moves[i] & 1;
      if (axis >= 8) {
        rot = w2rot[axis - 8];
        axis = w2axis[axis - 8];
      }
      if (pow == 0) {
        rot = FtoMoves.symMult[rot][rot];
      }
      ret[i] = FtoMoves.symMulM[sym][axis] * 2 + pow;
      sym = FtoMoves.symMult[rot][sym];
    }
    symOut[0] = sym;
    return ret;
  }

  private Object[] phase1GenIdxs(FtoCubie fc) {
    List<Long> idxs = new ArrayList<>();
    List<int[]> syms = new ArrayList<>();
    FtoCubie fc2 = new FtoCubie();
    FtoCubie fc3 = new FtoCubie();
    for (int sidx = 0; sidx < 12; sidx += 3) {
      FtoCubie.mult(FtoMoves.symCube[sidx % 12], fc, fc2);
      int rot;
      for (rot = 0; rot < 12; rot++) {
        FtoCubie.mult(fc2, FtoMoves.symCube[rot], fc3);
        if (fc3.ep[4] == 4) {
          break;
        }
      }
      idxs.add(pack32(
        p1epMoves.hash2idx.get(phase1EdgeHash(fc3.ep)),
        p1rlMoves.hash2idx.get(phase1CtrlHash(fc3.rl))));
      syms.add(new int[] {sidx, rot});
    }
    long[] idxArr = new long[idxs.size()];
    for (int i = 0; i < idxArr.length; i++) {
      idxArr[i] = idxs.get(i);
    }
    return new Object[] {idxArr, syms};
  }

  private Object[] phase1ProcSol(int[][] solPairs, int[] solsym, FtoCubie fc) {
    int n = solPairs.length;
    int[] sol = new int[n];
    for (int i = 0; i < n; i++) {
      sol[i] = PHASE1_MOVES[solPairs[i][0]] + solPairs[i][1];
    }
    int[] symOut = new int[1];
    int[] std = move2std(sol, symOut);
    for (int i = 0; i < std.length; i++) {
      int move = std[i];
      sol[i] = FtoMoves.symMulM[FtoMoves.symMulI[0][solsym[1]]][move >> 1] * 2 + (move & 1);
      fc = FtoCubie.mult(fc, FtoMoves.moveCube[sol[i]], null);
    }
    solsym[1] = FtoMoves.symMulI[solsym[1]][symOut[0]];
    fc = FtoCubie.mult(
      FtoMoves.pyraSymCube[solsym[0] / 12], FtoMoves.symCube[solsym[0] % 12],
      fc, FtoMoves.symCube[solsym[1]], null);
    return new Object[] {fc, sol, solsym[0], solsym[1]};
  }

  private List<Object[]> solvePhase1(final FtoCubie fc) {
    Object[] gen = phase1GenIdxs(fc);
    final long[] idxs = (long[]) gen[0];
    @SuppressWarnings("unchecked")
    final List<int[]> syms = (List<int[]>) gen[1];
    final List<Object[]> p1sols = new ArrayList<>();
    solv1.solveMulti(idxs, 0, 12, new FtoSearch.SearchCallback() {
      @Override
      public boolean found(int[][] sol, int sidx) {
        p1sols.add(phase1ProcSol(sol, syms.get(sidx).clone(), fc));
        return p1sols.size() >= N_PHASE1_SOLS;
      }
    });
    return p1sols;
  }

  private Object[] solvePhase2(List<Object[]> solvInfos) {
    long[] idxs = new long[solvInfos.size()];
    for (int i = 0; i < solvInfos.size(); i++) {
      FtoCubie fc = (FtoCubie) solvInfos.get(i)[0];
      idxs[i] = pack2(
        p2epMoves.hash2idx.get(phase2EdgeHash(fc.ep)),
        p2rlMoves.hash2idx.get(phase2CtHash(fc.rl)),
        p2ccMoves.hash2idx.get(phase2CpcoHash(fc)),
        getPhase2ufIdx(fc.uf));
    }
    Object[] res = solv2.solveMulti(idxs, 0, 25, null);
    if (res == null) {
      return null;
    }
    int[][] solPairs = (int[][]) res[0];
    int src = (Integer) res[1];
    Object[] solvInfo = solvInfos.get(src);
    FtoCubie fc = (FtoCubie) solvInfo[0];
    int sym1 = (Integer) solvInfo[3];
    int[] sol = new int[solPairs.length];
    for (int i = 0; i < solPairs.length; i++) {
      int move = PHASE2_MOVES[solPairs[i][0]] + solPairs[i][1];
      sol[i] = FtoMoves.symMulM[FtoMoves.symMulI[0][sym1]][move >> 1] * 2 + (move & 1);
      fc = FtoCubie.mult(fc, FtoMoves.moveCube[move], null);
    }
    return new Object[] {fc, sol, solvInfo[2], solvInfo[3], src};
  }

  private Object[] solvePhase3(Object[] solvInfo) {
    FtoCubie fc = (FtoCubie) solvInfo[0];
    int sym1 = (Integer) solvInfo[3];
    int epIdx = p3epMoves.hash2idx.get(phase3EdgeHash(fc.ep));
    int ufIdx = p3ufMoves.hash2idx.get(phase3CcufHash(fc));
    int[][] solPairs = solv3.solve(pack32(epIdx, ufIdx), 0, 25);
    if (solPairs == null) {
      return null;
    }
    int[] sol = new int[solPairs.length];
    for (int i = 0; i < solPairs.length; i++) {
      int move = PHASE3_MOVES[solPairs[i][0]] + solPairs[i][1];
      sol[i] = FtoMoves.symMulM[FtoMoves.symMulI[0][sym1]][move >> 1] * 2 + (move & 1);
      fc = FtoCubie.mult(fc, FtoMoves.moveCube[move], null);
    }
    return new Object[] {fc, sol, solvInfo[2], solvInfo[3]};
  }

  /**
   * Solve the given cubie state. With {@code invSol} the result is inverted and
   * reversed, i.e. a scramble that produces {@code fc} from the solved state.
   * Returns one move token per array element, or null if interrupted.
   */
  public String[] solve(FtoCubie fc, boolean invSol) {
    List<Object[]> solvInfos = solvePhase1(fc);
    if (stopped) {
      return null;
    }
    Object[] solvInfo2 = solvePhase2(solvInfos);
    if (solvInfo2 == null || stopped) {
      return null;
    }
    Object[] solvInfo1 = solvInfos.get((Integer) solvInfo2[4]);
    int[] sol1 = (int[]) solvInfo1[1];
    int sym1Idx = (Integer) solvInfo1[2];

    int[] sol2 = (int[]) solvInfo2[1];
    solvInfo2[0] = FtoCubie.mult(
      FtoMoves.pyraSymCube[FtoMoves.symMulI[0][sym1Idx / 12]], (FtoCubie) solvInfo2[0], null);

    Object[] solvInfo3 = solvePhase3(solvInfo2);
    if (solvInfo3 == null || stopped) {
      return null;
    }
    int[] sol3 = (int[]) solvInfo3[1];

    int[] sol = new int[sol1.length + sol2.length + sol3.length];
    int p = 0;
    for (int v : sol1) {
      sol[p++] = v;
    }
    for (int v : sol2) {
      sol[p++] = v;
    }
    for (int v : sol3) {
      sol[p++] = v;
    }
    if (invSol) {
      for (int i = 0; i < sol.length; i++) {
        sol[i] ^= 1;
      }
      for (int i = 0, j = sol.length - 1; i < j; i++, j--) {
        int t = sol[i];
        sol[i] = sol[j];
        sol[j] = t;
      }
    }
    String[] tokens = new String[sol.length];
    for (int i = 0; i < sol.length; i++) {
      tokens[i] = PRETTY[sol[i]];
    }
    return tokens;
  }

  /** Normalise the facelets into a legal state and solve them. */
  public String[] solveFacelet(int[] facelet, boolean invSol) {
    FtoCubie fc = new FtoCubie();
    if (!fc.fromFacelet(facelet)) {
      return null;
    }
    return solve(fc, invSol);
  }

  // ------------------------------------------------------------ coord packing

  // Phases 1 and 3 hold two small coordinates -> a plain 32/32 split of the long.
  private static long pack32(int lo, int hi) {
    return (lo & 0xffffffffL) | ((long) hi << 32);
  }

  private static int lo32(long x) {
    return (int) x;
  }

  private static int hi32(long x) {
    return (int) (x >>> 32);
  }

  // Phase 2 holds four coordinates packed into one long (see the budget guard in
  // phase2Init): ep [0,14), rl [14,26), cc [26,40), uf [40,64).
  private static final int P2_RL_SH = 14, P2_CC_SH = 26, P2_UF_SH = 40;

  private static long pack2(int ep, int rl, int cc, int uf) {
    return (long) ep | ((long) rl << P2_RL_SH) | ((long) cc << P2_CC_SH) | ((long) uf << P2_UF_SH);
  }

  private static int p2ep(long x) {
    return (int) (x & 0x3FFF);
  }

  private static int p2rl(long x) {
    return (int) ((x >>> P2_RL_SH) & 0xFFF);
  }

  private static int p2cc(long x) {
    return (int) ((x >>> P2_CC_SH) & 0x3FFF);
  }

  private static int p2uf(long x) {
    return (int) (x >>> P2_UF_SH);
  }

  // ------------------------------------------------------------------ helpers

  private static int indexOf(int[] arr, int value) {
    for (int i = 0; i < arr.length; i++) {
      if (arr[i] == value) {
        return i;
      }
    }
    return -1;
  }

  private static String charKey(int[] a) {
    char[] c = new char[a.length];
    for (int i = 0; i < a.length; i++) {
      c[i] = (char) a[i];
    }
    return new String(c);
  }

  private static String charKey(int[] a, int[] b) {
    char[] c = new char[a.length + b.length];
    for (int i = 0; i < a.length; i++) {
      c[i] = (char) a[i];
    }
    for (int i = 0; i < b.length; i++) {
      c[a.length + i] = (char) b[i];
    }
    return new String(c);
  }
}
