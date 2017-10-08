package com.cube.nanotimer.scrambler.randomstate;

import com.cube.nanotimer.scrambler.randomstate.pyraminx.RSPyraminxScrambler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

@RunWith(JUnit4.class)
public class RandomStatePyraminxTest {

  @Test
  public void basicTest() {
    new RSPyraminxScrambler().genTables();
    RSPyraminxScrambler randomScrambler = new RSPyraminxScrambler();
    ScrambleConfig scrambleConfig = new ScrambleConfig(11, null);
    String[] scramble = randomScrambler.getNewScramble(scrambleConfig);

    System.out.println("scramble: " + Arrays.toString(scramble));
  }

}
