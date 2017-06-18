package com.cube.nanotimer.vo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum ThreeScrambleType {
  RANDOM {
  },
  F2L {
    @Override
    protected byte[] getFixedEdgePermutationIndices() {
      return new byte[] { 8, 9, 10, 11 };
    }

    @Override
    protected byte[] getFixedEdgeOrientationIndices() {
      return new byte[] { 8, 9, 10, 11 };
    }
  },
  LAST_LAYER {
    @Override
    protected byte[] getFixedCornerPermutationIndices() {
      return new byte[] { 4, 5, 6, 7 };
    }

    @Override
    protected byte[] getFixedCornerOrientationIndices() {
      return new byte[] { 4, 5, 6, 7 };
    }

    @Override
    protected byte[] getFixedEdgePermutationIndices() {
      return new byte[] { 0, 1, 2, 3, 8, 9, 10, 11 };
    }

    @Override
    protected byte[] getFixedEdgeOrientationIndices() {
      return new byte[] { 0, 1, 2, 3, 8, 9, 10, 11 };
    }
  },
  PLL {
    @Override
    protected byte[] getFixedCornerPermutationIndices() {
      return new byte[] { 4, 5, 6, 7 };
    }

    @Override
    protected byte[] getFixedCornerOrientationIndices() {
      return new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 };
    }

    @Override
    protected byte[] getFixedEdgePermutationIndices() {
      return new byte[] { 0, 1, 2, 3, 8, 9, 10, 11 };
    }

    @Override
    protected byte[] getFixedEdgeOrientationIndices() {
      return new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    }
  },
  CORNERS {
    @Override
    protected byte[] getFixedEdgePermutationIndices() {
      return new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    }

    @Override
    protected byte[] getFixedEdgeOrientationIndices() {
      return new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    }
  },
  EDGES {
    @Override
    protected byte[] getFixedCornerOrientationIndices() {
      return new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 };
    }

    @Override
    protected byte[] getFixedCornerPermutationIndices() {
      return new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 };
    }
  },
  ROUX_LAST_10_PIECES {
    @Override
    protected byte[] getFixedCornerPermutationIndices() {
      return new byte[] { 4, 5, 6, 7 };
    }

    @Override
    protected byte[] getFixedCornerOrientationIndices() {
      return new byte[] { 4, 5, 6, 7 };
    }

    @Override
    protected byte[] getFixedEdgePermutationIndices() {
      return new byte[] { 0, 1, 2, 3, 9, 11 };
    }

    @Override
    protected byte[] getFixedEdgeOrientationIndices() {
      return new byte[] { 0, 1, 2, 3, 9, 11 };
    }

    @Override
    public String[] finalizeScramble(String[] scramble) {
      // add final "m" move so that the centers are not always aligned with the blocks
      int locResult = new Random().nextInt(4);

      if (locResult > 0) {
        String additionalMove = "";
        switch (locResult) {
          case 1:
            additionalMove = "m";
            break;
          case 2:
            additionalMove = "m'";
            break;
          case 3:
            additionalMove = "m2";
            break;
        }

        scramble = addMoveToScramble(scramble, additionalMove);
      }

      return scramble;
    }
  },
  ROUX_LAST_6_EDGES {
    @Override
    protected byte[] getFixedCornerPermutationIndices() {
      return new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 };
    }

    @Override
    protected byte[] getFixedCornerOrientationIndices() {
      return new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 };
    }

    @Override
    protected byte[] getFixedEdgePermutationIndices() {
      return new byte[] { 0, 1, 2, 3, 9, 11 };
    }

    @Override
    protected byte[] getFixedEdgeOrientationIndices() {
      return new byte[] { 0, 1, 2, 3, 9, 11 };
    }
  };

  protected byte[] getFixedCornerPermutationIndices() {
    return new byte[0];
  }

  protected byte[] getFixedCornerOrientationIndices() {
    return new byte[0];
  }

  protected byte[] getFixedEdgePermutationIndices() {
    return new byte[0];
  }

  protected byte[] getFixedEdgeOrientationIndices() {
    return new byte[0];
  }

  private byte[] getRandomPermutation(byte[] fixedIndices, int size) {
    List<Byte> randomPermutation = new ArrayList<>();
    for (byte i = 0; i < size; i++) {
      if (!containsIndex(fixedIndices, i)) {
        randomPermutation.add(i);
      }
    }
    Collections.shuffle(randomPermutation, new Random());

    Arrays.sort(fixedIndices);
    for (byte fixedIndex : fixedIndices) {
      randomPermutation.add(fixedIndex, fixedIndex);
    }

    byte[] randomPermutationArray = new byte[size];
    for (int i = 0; i < size; i++) {
      randomPermutationArray[i] = randomPermutation.get(i);
    }
    return randomPermutationArray;
  }

  protected String[] addMoveToScramble(String[] scramble, String move) {
    // add to scramble (and try to merge it with the last scramble move, like "R m" becomes "r")
    String lastMove = scramble[scramble.length - 1];
    if (lastMove.equals("R")) {
      if (move.equals("m")) {
        scramble[scramble.length - 1] = "r";
      } else if (move.equals("m2")) {
        scramble[scramble.length - 1] = "r";
        scramble = appendToArray(scramble, "m");
      } else {
        scramble = appendToArray(scramble, move);
      }
    } else if (lastMove.equals("R'")) {
      if (move.equals("m'")) {
        scramble[scramble.length - 1] = "r'";
      } else if (move.equals("m2")) {
        scramble[scramble.length - 1] = "r'";
        scramble = appendToArray(scramble, "m'");
      } else {
        scramble = appendToArray(scramble, move);
      }
    } else if (lastMove.equals("R2")) {
      if (move.equals("m")) {
        scramble[scramble.length - 1] = "R";
        scramble = appendToArray(scramble, "r");
      } else if (move.equals("m'")) {
        scramble[scramble.length - 1] = "R'";
        scramble = appendToArray(scramble, "r'");
      } else if (move.equals("m2")) {
        scramble[scramble.length - 1] = "r2";
      } else {
        scramble = appendToArray(scramble, move);
      }
    } else if (lastMove.equals("L")) {
      if (move.equals("m'")) {
        scramble[scramble.length - 1] = "l";
      } else if (move.equals("m2")) {
        scramble[scramble.length - 1] = "l";
        scramble = appendToArray(scramble, "m'");
      } else {
        scramble = appendToArray(scramble, move);
      }
    } else if (lastMove.equals("L'")) {
      if (move.equals("m")) {
        scramble[scramble.length - 1] = "l'";
      } else if (move.equals("m2")) {
        scramble[scramble.length - 1] = "l'";
        scramble = appendToArray(scramble, "m");
      } else {
        scramble = appendToArray(scramble, move);
      }
    } else if (lastMove.equals("L2")) {
      if (move.equals("m")) {
        scramble[scramble.length - 1] = "L'";
        scramble = appendToArray(scramble, "l'");
      } else if (move.equals("m'")) {
        scramble[scramble.length - 1] = "L";
        scramble = appendToArray(scramble, "l");
      } else if (move.equals("m2")) {
        scramble[scramble.length - 1] = "L2";
      } else {
        scramble = appendToArray(scramble, move);
      }
    } else {
      scramble = appendToArray(scramble, move);
    }
    return scramble;
  }

  private String[] appendToArray(String[] scramble, String toAppend) {
    String[] finalizedScramble = new String[scramble.length + 1];
    System.arraycopy(scramble, 0, finalizedScramble, 0, scramble.length);
    finalizedScramble[finalizedScramble.length - 1] = toAppend;
    return finalizedScramble;
  }

  private byte[] getRandomOrientation(byte[] fixedIndices, int size, int nDifferentValues) {
    byte[] randomOrientation = new byte[size];
    Random random = new Random();

    int parity;
    do {
      parity = 0;
      for (byte i = 0; i < randomOrientation.length; i++) {
        byte orientation;
        if (containsIndex(fixedIndices, i)) {
          orientation = 0;
        } else {
          orientation = (byte) random.nextInt(nDifferentValues);
        }
        randomOrientation[i] = orientation;
        parity += orientation;
      }
    } while (parity % nDifferentValues != 0);

    return randomOrientation;
  }

  private boolean containsIndex(byte[] indexes, byte index) {
    for (byte currentIndex : indexes) {
      if (currentIndex == index) {
        return true;
      }
    }
    return false;
  }

  public ThreeCubeState getRandomState() {
    ThreeCubeState cubeState;

    do {
      cubeState = new ThreeCubeState();
      cubeState.cornerPermutations = getRandomPermutation(getFixedCornerPermutationIndices(), 8);
      cubeState.edgePermutations = getRandomPermutation(getFixedEdgePermutationIndices(), 12);
      cubeState.cornerOrientations = getRandomOrientation(getFixedCornerOrientationIndices(), 8, 3);
      cubeState.edgeOrientations = getRandomOrientation(getFixedEdgeOrientationIndices(), 12, 2);
    } while (hasParity(cubeState.cornerPermutations) != hasParity(cubeState.edgePermutations));

    return cubeState;
  }

  public String[] finalizeScramble(String[] scramble) {
    return scramble;
  }

  public static boolean hasParity(byte[] perm) {
    int inversion = 0;
    for (int i = 0; i < perm.length; i++) {
      for (int j = i + 1; j < perm.length; j++) {
        if (perm[i] > perm[j]) {
          inversion++;
        }
      }
    }
    return (inversion % 2 != 0);
  }

  public static ThreeScrambleType fromString(String scrambleTypeStr) {
    ThreeScrambleType scrambleType = null;
    if (scrambleTypeStr != null && !scrambleTypeStr.equals("")) {
      scrambleType = ThreeScrambleType.valueOf(scrambleTypeStr);
    }
    return scrambleType;
  }
}
