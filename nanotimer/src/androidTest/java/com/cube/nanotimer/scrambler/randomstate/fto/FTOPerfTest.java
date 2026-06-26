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
    // 1) One-time table generation (the cost paid once per process).
    long g = System.currentTimeMillis();
    FTOSolver.genTables();
    Log.i(TAG, "table generation: " + (System.currentTimeMillis() - g) + " ms");

    // 2) Single-thread per-scramble cost.
    FTOSolver solver = new FTOSolver();
    Random rnd = new Random(42);
    int warm = 20;
    for (int i = 0; i < warm; i++) {
      solver.solveFacelet(randomState(rnd).toFaceCube(), true);
    }
    int n = 100;
    long total = 0;
    long max = 0;
    long len = 0;
    for (int i = 0; i < n; i++) {
      FtoCubie fc = randomState(rnd);
      long t = System.nanoTime();
      String[] scr = solver.solveFacelet(fc.toFaceCube(), true);
      long ms = (System.nanoTime() - t) / 1_000_000;
      total += ms;
      max = Math.max(max, ms);
      len += scr.length;
    }
    Log.i(TAG, "single-thread: avg " + (total / (double) n) + " ms/scramble, max " + max
      + " ms, avg length " + (len / (double) n) + " over " + n);

    // 3) Realistic cache fill: generate on (cores - 1) threads, as the app does.
    final int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    final int toGenerate = 200;
    final AtomicInteger counter = new AtomicInteger(0);
    final AtomicLong totalLen = new AtomicLong(0);
    long wall = System.currentTimeMillis();
    Thread[] pool = new Thread[threads];
    for (int t = 0; t < pool.length; t++) {
      final long seed = 1000 + t;
      pool[t] = new Thread(new Runnable() {
        @Override
        public void run() {
          Random r = new Random(seed);
          FTOSolver s = new FTOSolver();
          while (counter.getAndIncrement() < toGenerate) {
            String[] scr = s.solveFacelet(randomState(r).toFaceCube(), true);
            totalLen.addAndGet(scr.length);
          }
        }
      });
      pool[t].start();
    }
    for (Thread th : pool) {
      try {
        th.join();
      } catch (InterruptedException ignored) {
      }
    }
    long wallMs = System.currentTimeMillis() - wall;
    Log.i(TAG, "cache fill: " + toGenerate + " scrambles on " + threads + " threads in " + wallMs
      + " ms (" + (wallMs / (double) toGenerate) + " ms/scramble wall, avg length "
      + (totalLen.get() / (double) toGenerate) + ")");
    Log.i(TAG, "=> estimated time to fill 500: " + (wallMs * 500L / toGenerate) + " ms");
  }
}
