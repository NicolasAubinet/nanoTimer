package com.cube.nanotimer.scrambler.basic;

import com.cube.nanotimer.scrambler.Scrambler;

import java.util.Random;

public class ClockUUdduxdxScrambler implements Scrambler {

  // Style: UUdd u=-3,d=2 / dUdU u=0,d=6 / ddUU u=6,d=-1 / UdUd u=3,d=3 ... / UUUU u=-4 / dddd d=1 / UUUd
  // Scrambler: https://www.worldcubeassociation.org/regulations/history/files/scrambles/scramble_clock.htm

  private static final PinMoveType[] PINS_MOVE_TYPES = new PinMoveType[] {
    new PinMoveType("UUdd", true, true),
    new PinMoveType("dUdU", true, true),
    new PinMoveType("ddUU", true, true),
    new PinMoveType("UdUd", true, true),
    new PinMoveType("dUUU", true, false),
    new PinMoveType("UdUU", true, false),
    new PinMoveType("UUUd", true, false),
    new PinMoveType("UUdU", true, false),
    new PinMoveType("UUUU", true, false),
    new PinMoveType("dddd", false, true),
  };

  @Override
  public String[] getNewScramble() {
    String[] scramble = new String[PINS_MOVE_TYPES.length + 1];
    Random random = new Random();

    for (int i = 0; i < PINS_MOVE_TYPES.length; i++) {
      PinMoveType pinMoveType = PINS_MOVE_TYPES[i];
      StringBuilder pinMoves = new StringBuilder();
      pinMoves.append("(");
      pinMoves.append(pinMoveType.pinStates).append(" ");
      if (pinMoveType.moveUpPin) {
        pinMoves.append("u=").append(getRandomMove(random));
        if (pinMoveType.moveDownPin) {
          pinMoves.append(",");
        }
      }
      if (pinMoveType.moveDownPin) {
        pinMoves.append("d=").append(getRandomMove(random));
      }
      pinMoves.append(")");
      scramble[i] = formatToFixedLength(pinMoves.toString());
    }

    String lastPinState = "(";
    for (int i = 0; i < 4; i++) {
      String nextState;
      if (random.nextBoolean()) {
        nextState = "U";
      } else {
        nextState = "d";
      }
      lastPinState += nextState;
    }
    lastPinState += ")";
    scramble[scramble.length - 1] = formatLastPinState(lastPinState);

    return scramble;
  }

  private String formatToFixedLength(String s) {
    StringBuilder formatted = new StringBuilder(s);
    for (int i = s.length(); i < 14; i++) {
      formatted.append(" ");
    }
    return formatted.toString();
  }

  private String formatLastPinState(String lastPinState) {
    // add leading spaces to have the last pin state aligned properly (to make it non left-aligned)
    return "    " + lastPinState;
  }

  private int getRandomMove(Random random) {
    return random.nextInt(12) - 5;
  }

  static class PinMoveType {
    private String pinStates;
    private boolean moveUpPin;
    private boolean moveDownPin;

    public PinMoveType(String pinStates, boolean moveUpPin, boolean moveDownPin) {
      this.pinStates = pinStates;
      this.moveUpPin = moveUpPin;
      this.moveDownPin = moveDownPin;
    }
  }

}
