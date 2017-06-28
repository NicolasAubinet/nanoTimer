package com.cube.nanotimer.scrambler.basic;

import com.cube.nanotimer.scrambler.Scrambler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClockURxDRxDLxScrambler implements Scrambler {
  // Style: UR1+ DR6+ DL5- UL6+ U6+ R2+ D3- L3- ALL1+ y2 U3+ R3- D2- L1+ ALL6+ DR DL

  private static final String[] FIRST_MOVES_SET = new String[] { "UR", "DR", "DL", "UL", "U", "R", "D", "L", "ALL" };
  private static final String[] SECOND_MOVES_SET = new String[] { "U", "R", "D", "L", "ALL" };
  private static final String[] POSSIBLE_FINAL_MOVES = new String[] { "UR", "DR", "DL", "UL" };

  @Override
  public String[] getNewScramble() {
    Random random = new Random();
    List<String> finalMoves = new ArrayList<>();

    for (int i = 0; i < POSSIBLE_FINAL_MOVES.length; i++) {
      if (random.nextBoolean()) {
        finalMoves.add(POSSIBLE_FINAL_MOVES[i]);
      }
    }

    String[] scramble = new String[FIRST_MOVES_SET.length + 1 + SECOND_MOVES_SET.length + finalMoves.size()];
    int scrambleMoveIndex = 0;

    for (int i = 0; i < FIRST_MOVES_SET.length; i++) {
      String move = FIRST_MOVES_SET[i] + getRandomRotation(random);
      scramble[scrambleMoveIndex++] = move;
    }

    scramble[scrambleMoveIndex++] = "y2";

    for (int i = 0; i < SECOND_MOVES_SET.length; i++) {
      String move = SECOND_MOVES_SET[i] + getRandomRotation(random);
      scramble[scrambleMoveIndex++] = move;
    }

    for (String move : finalMoves) {
      scramble[scrambleMoveIndex++] = move;
    }

    return scramble;
  }

  private String getRandomRotation(Random random) {
    String randomRotation;
    int locRandom = random.nextInt(12) - 5;
    if (locRandom >= 0) {
      randomRotation = locRandom + "+";
    } else {
      randomRotation = Math.abs(locRandom) + "-";
    }
    return randomRotation;
  }

}
