package com.cube.nanotimer.scrambler.randomstate;

import com.cube.nanotimer.scrambler.basic.Square1Scrambler;
import com.cube.nanotimer.scrambler.randomstate.square1.Square1RandomScrambler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

@RunWith(JUnit4.class)
public class Square1Test {

  @Test
  public void basicTest() {
    new Square1RandomScrambler().genTables();
    Square1RandomScrambler randomScrambler = new Square1RandomScrambler();
    String[] scramble1 = randomScrambler.getNewScramble(null);

    Square1Scrambler normalScrambler = new Square1Scrambler();
    String[] scramble2 = normalScrambler.getNewScramble();

    System.out.println(Arrays.toString(scramble2) + "\n" + Arrays.toString(scramble1));
  }

}
