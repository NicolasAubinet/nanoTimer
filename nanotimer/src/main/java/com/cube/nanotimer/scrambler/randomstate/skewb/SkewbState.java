package com.cube.nanotimer.scrambler.randomstate.skewb;

public class SkewbState {
  public byte[] facesPermutation;
  public byte[] freeCornersPermutation;
  public byte[] freeCornersOrientation;
  public byte[] fixedCornersOrientation;

  public SkewbState(byte[] facesPermutation, byte[] freeCornersPermutation, byte[] freeCornersOrientation,
               byte[] fixedCornersOrientation) {
    this.facesPermutation = facesPermutation;
    this.freeCornersPermutation = freeCornersPermutation;
    this.freeCornersOrientation = freeCornersOrientation;
    this.fixedCornersOrientation = fixedCornersOrientation;
  }

  public SkewbState multiply(SkewbState move) {
    // faces permutation
    byte[] facesPermutation = new byte[6];
    for (int i = 0; i < facesPermutation.length; i++) {
      facesPermutation[i] =
        this.facesPermutation[move.facesPermutation[i]];
    }

    // free corners permutation
    byte[] freeCornersPermutation = new byte[4];
    for (int i = 0; i < freeCornersPermutation.length; i++) {
      freeCornersPermutation[i] =
        this.freeCornersPermutation[move.freeCornersPermutation[i]];
    }

    // free corners orientation
    byte[] freeCornersOrientation = new byte[4];
    for (int i = 0; i < freeCornersOrientation.length; i++) {
      freeCornersOrientation[i] =
        (byte) ((this.freeCornersOrientation[move.freeCornersPermutation[i]] +
          move.freeCornersOrientation[i]) % 3);
    }

    // fixed corners orientation
    byte[] fixedCornersOrientation = new byte[4];
    for (int i = 0; i < freeCornersOrientation.length; i++) {
      fixedCornersOrientation[i] =
        (byte) ((this.fixedCornersOrientation[i] +
          move.fixedCornersOrientation[i]) % 3);
    }

    return new SkewbState(
      facesPermutation,
      freeCornersPermutation,
      freeCornersOrientation,
      fixedCornersOrientation);
  }
}
