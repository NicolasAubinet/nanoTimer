package com.cube.nanotimer.scrambler.randomstate;

import com.cube.nanotimer.util.ScrambleFormatterService;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.ScrambleType;
import com.cube.nanotimer.vo.ScrambleTypes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RunWith(JUnit4.class)
public class RSThreeScramblerTest {

  @Test
  public void testGenerateScrambles() {
    RSThreeScrambler scrambler = new RSThreeScrambler();
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
      String[] scramble = scrambler.getNewScramble(new ScrambleConfig(23));
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

  @Test
  public void testCustomScrambles() {
    RSThreeScrambler scrambler = new RSThreeScrambler();
    scrambler.genTables();
    System.out.println("getNewScramble...");
    String[] scramble = scrambler.getNewScramble(new ScrambleConfig(23, ScrambleTypes.DEFAULT));
    String formattedScramble = ScrambleFormatterService.INSTANCE.formatScrambleAsSingleLine(scramble, CubeType.THREE_BY_THREE);
    System.out.println("Custom scramble: " + formattedScramble);
  }

  @Test
  public void testHasParity() {
    System.out.println("No parity:");
    displaySign(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 });
    displaySign(new byte[] { 4, 1, 0, 3, 5, 6, 2, 7 });
    displaySign(new byte[] { 0, 6, 5, 3, 4, 2, 1, 7 }); // F2
    displaySign(new byte[] { 2, 4, 7, 1, 6, 0, 3, 5 }); // F2 R2 L2
    displaySign(new byte[] { 2, 4, 6, 1, 0, 3, 5, 7 }); //  L2 R D L D2 B' U2 R F' D' U' L' R' D2 F L D2 B F' L' F2 L' R D' L2
    displaySign(new byte[] { 8, 3, 9, 11, 10, 7, 5, 0, 2, 1, 4, 6 }); //  L2 R D L D2 B' U2 R F' D' U' L' R' D2 F L D2 B F' L' F2 L' R D' L2

    System.out.println("Parity:");
    displaySign(new byte[] { 0, 1, 6, 2, 4, 5, 7, 3 });
    displaySign(new byte[] { 0, 1, 6, 2, 4, 5, 7, 3 });
    displaySign(new byte[] { 3, 0, 4, 1, 6, 2, 5, 7 }); //  F L2 R' D2 R2 D' U' R D' B2 U2 F' U2 L2 D L R' U' F2 L R D2 B R' U2
    displaySign(new byte[] { 1, 9, 8, 0, 11, 7, 6, 10, 4, 5, 3, 2 }); //  F L2 R' D2 R2 D' U' R D' B2 U2 F' U2 L2 D L R' U' F2 L R D2 B R' U2
  }

  private void displaySign(byte[] pos) {
    System.out.println(Arrays.toString(pos) + " parity? " + ScrambleType.hasParity(pos));
  }

}
