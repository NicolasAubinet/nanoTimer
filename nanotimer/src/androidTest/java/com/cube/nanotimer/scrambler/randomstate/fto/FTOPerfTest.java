package com.cube.nanotimer.scrambler.randomstate.fto;

import android.util.Log;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * On-device FTO performance measurement (instrumented test, runs on the phone).
 * Not a pass/fail test - it logs timings under the "FTOPerf" tag.
 *
 *   adb logcat -s FTOPerf
 */
@RunWith(AndroidJUnit4.class)
public class FTOPerfTest {

  private static final String TAG = "FTOPerf";

  private static FtoCubie randomState(Random rnd) {
    FtoCubie fc = new FtoCubie();
    fc.ep = FtoMath.rndPerm(12, true, rnd);
    fc.uf = FtoMath.rndPerm(12, true, rnd);
    fc.rl = FtoMath.rndPerm(12, true, rnd);
    fc.cp = FtoMath.rndPerm(6, true, rnd);
    fc.co = FtoMath.setNOri(new int[6], rnd.nextInt(32), 6, -2);
    return fc;
  }

  @Test
  public void measure() {
    Log.i(TAG, "START measure()");

    // 1a) Move + symmetry tables (class init).
    long m = System.currentTimeMillis();
    FtoMoves.ensureInit();
    Log.i(TAG, "moves/symmetry init: " + (System.currentTimeMillis() - m) + " ms");

    // 1b) Phase pruning tables (the big one is the ~5.6 MB phase-2 table).
    long g = System.currentTimeMillis();
    FTOSolver.genTables();
    Log.i(TAG, "phase table generation: " + (System.currentTimeMillis() - g) + " ms");

    // 2) Single-thread per-scramble cost.
    FTOSolver solver = new FTOSolver();
    Random rnd = new Random(42);
    int warm = 3;
    for (int i = 0; i < warm; i++) {
      Log.i(TAG, "warmup solve " + i);
      solver.solveFacelet(randomState(rnd).toFaceCube(), true);
    }
    int n = 15;
    Log.i(TAG, "single-thread loop start (n=" + n + ")");
    long total = 0;
    long max = 0;
    long len = 0;
    int fails = 0;
    for (int i = 0; i < n; i++) {
      FtoCubie fc = randomState(rnd);
      long t = System.nanoTime();
      String[] scr = solver.solveFacelet(fc.toFaceCube(), true);
      long ms = (System.nanoTime() - t) / 1_000_000;
      Log.i(TAG, "  solve " + i + ": " + ms + " ms" + (scr == null ? " (NULL)" : ""));
      if (scr == null) {
        fails++;
        continue;
      }
      total += ms;
      max = Math.max(max, ms);
      len += scr.length;
    }
    int ok = n - fails;
    Log.i(TAG, "single-thread: avg " + (ok == 0 ? -1 : total / (double) ok) + " ms/scramble, max " + max
      + " ms, avg length " + (ok == 0 ? -1 : len / (double) ok) + " over " + ok + " (fails=" + fails + ")");

    // 3) Realistic cache fill: generate on (cores - 1) threads, as the app does.
    final int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    final int toGenerate = 20;
    final AtomicInteger counter = new AtomicInteger(0);
    final AtomicInteger done = new AtomicInteger(0);
    final AtomicLong totalLen = new AtomicLong(0);
    long wall = System.currentTimeMillis();
    Thread[] pool = new Thread[threads];
    for (int t = 0; t < pool.length; t++) {
      final long seed = 1000 + t;
      final int tid = t;
      pool[t] = new Thread(new Runnable() {
        @Override
        public void run() {
          Random r = new Random(seed);
          FTOSolver s = new FTOSolver();
          int idx;
          while ((idx = counter.getAndIncrement()) < toGenerate) {
            long t0 = System.nanoTime();
            String[] scr = s.solveFacelet(randomState(r).toFaceCube(), true);
            long ms = (System.nanoTime() - t0) / 1_000_000;
            Log.i(TAG, "  [fill] thread " + tid + " solve " + idx + ": " + ms + " ms"
              + (scr == null ? " (NULL)" : "") + " (done=" + done.incrementAndGet() + "/" + toGenerate + ")");
            if (scr != null) {
              totalLen.addAndGet(scr.length);
            }
          }
        }
      }, "fto-fill-" + t);
      pool[t].start();
    }
    // Watchdog: if the fill stalls, dump every thread's stack so we can tell
    // "stuck on a lock" (deadlock) from "grinding in idaSearch" (just slow).
    final Thread[] watched = pool;
    Thread watchdog = new Thread(new Runnable() {
      @Override
      public void run() {
        for (int tick = 1; tick <= 6; tick++) {
          try {
            Thread.sleep(20000);
          } catch (InterruptedException e) {
            return; // fill finished, watchdog dismissed
          }
          Log.w(TAG, "[watchdog] tick " + tick + ": done=" + done.get() + "/" + toGenerate
            + " after ~" + (tick * 20) + "s -- dumping fill-thread stacks");
          for (Thread th : watched) {
            StringBuilder sb = new StringBuilder(th.getName() + " state=" + th.getState());
            for (StackTraceElement el : th.getStackTrace()) {
              sb.append("\n    at ").append(el);
            }
            Log.w(TAG, sb.toString());
          }
        }
      }
    }, "fto-watchdog");
    watchdog.setDaemon(true);
    watchdog.start();
    for (Thread th : pool) {
      try {
        th.join();
      } catch (InterruptedException ignored) {
      }
    }
    watchdog.interrupt();
    long wallMs = System.currentTimeMillis() - wall;
    Log.i(TAG, "cache fill: " + toGenerate + " scrambles on " + threads + " threads in " + wallMs
      + " ms (" + (wallMs / (double) toGenerate) + " ms/scramble wall, avg length "
      + (totalLen.get() / (double) toGenerate) + ")");
    Log.i(TAG, "=> estimated time to fill 500: " + (wallMs * 500L / toGenerate) + " ms");
  }
}
