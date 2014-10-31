package com.cube.nanotimer.scrambler.randomstate;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class RSTwoScramblerTest extends AndroidTestCase {

  @SmallTest
  public void testGenerateScrambles() {
    RSTwoScrambler scrambler = new RSTwoScrambler();
    int nScrambles = 100;
    long min = Integer.MAX_VALUE;
    long max = 0;
    int minLength = Integer.MAX_VALUE;
    int maxLength = 0;
    int totalLength = 0;
    Map<Integer, Integer> lengthRepartition = new HashMap<Integer, Integer>();
    Log.i("[NanoTimer]", "Generating first scramble to generate tables (not counting this time)");
    long startTs = System.currentTimeMillis();
    scrambler.genTables();
    Log.i("[NanoTimer]", "Tables generated in " + (System.currentTimeMillis() - startTs) + "ms");
    Log.i("[NanoTimer]", "Generating " + nScrambles + " scrambles...");
    for (int i = 0; i < nScrambles; i++) {
      long ts = System.currentTimeMillis();
      String[] scramble = scrambler.getNewScramble();
      long t = System.currentTimeMillis() - ts;
      if (t < min) {
        min = t;
      }
      if (t > max) {
        max = t;
      }
      if (scramble.length < minLength) {
        minLength = scramble.length;
      }
      if (scramble.length > maxLength) {
        maxLength = scramble.length;
      }
      if (lengthRepartition.get(scramble.length) == null) {
        lengthRepartition.put(scramble.length, 1);
      } else {
        lengthRepartition.put(scramble.length, lengthRepartition.get(scramble.length) + 1);
      }
      totalLength += scramble.length;
    }
    long total = System.currentTimeMillis() - startTs;
    Log.i("[NanoTimer]", "Total time: " + total + " avg: " + (total / nScrambles) + " min: " + min + " max: " + max);
    Log.i("[NanoTimer]", "Scramble min: " + minLength + " max: " + maxLength + " avg length: " + (((float) totalLength) / nScrambles));
    Log.i("[NanoTimer]", "Length repartition:");
    for (Integer s : lengthRepartition.keySet()) {
      Log.i("[NanoTimer]", "  length " + s + ": " + lengthRepartition.get(s));
    }
  }

}
