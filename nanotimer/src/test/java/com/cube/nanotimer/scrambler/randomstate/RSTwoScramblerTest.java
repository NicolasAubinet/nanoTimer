package com.cube.nanotimer.scrambler.randomstate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Map;

@RunWith(JUnit4.class)
public class RSTwoScramblerTest {

  @Test
  public void testGenerateScrambles() {
    RSTwoScrambler scrambler = new RSTwoScrambler();
    int nScrambles = 100;
    long min = Integer.MAX_VALUE;
    long max = 0;
    int minLength = Integer.MAX_VALUE;
    int maxLength = 0;
    int totalLength = 0;
    Map<Integer, Integer> lengthRepartition = new HashMap<Integer, Integer>();
    System.out.println("Generating first scramble to generate tables (not counting this time)");
    long startTs = System.currentTimeMillis();
    scrambler.genTables();
    System.out.println("Tables generated in " + (System.currentTimeMillis() - startTs) + "ms");
    System.out.println("Generating " + nScrambles + " scrambles...");
    for (int i = 0; i < nScrambles; i++) {
      long ts = System.currentTimeMillis();
      String[] scramble = scrambler.getNewScramble(new ScrambleConfig(11));
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
    System.out.println("Total time: " + total + " avg: " + (total / nScrambles) + " min: " + min + " max: " + max);
    System.out.println("Scramble min: " + minLength + " max: " + maxLength + " avg length: " + (((float) totalLength) / nScrambles));
    System.out.println("Length repartition:");
    for (Integer s : lengthRepartition.keySet()) {
      System.out.println("  length " + s + ": " + lengthRepartition.get(s));
    }
  }

}
