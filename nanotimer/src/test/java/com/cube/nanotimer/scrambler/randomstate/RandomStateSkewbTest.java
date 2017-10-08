package com.cube.nanotimer.scrambler.randomstate;

import com.cube.nanotimer.scrambler.randomstate.skewb.RSSkewbScrambler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

@RunWith(JUnit4.class)
public class RandomStateSkewbTest {

  @Test
  public void basicTest() {
    new RSSkewbScrambler().genTables();
    RSSkewbScrambler randomScrambler = new RSSkewbScrambler();
    String[] scramble = randomScrambler.getNewScramble(null);

    System.out.println("scramble: " + Arrays.toString(scramble));
  }

}
