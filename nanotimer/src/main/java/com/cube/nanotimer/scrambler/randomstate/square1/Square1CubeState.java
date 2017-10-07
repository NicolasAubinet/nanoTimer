package com.cube.nanotimer.scrambler.randomstate.square1;

public class Square1CubeState {
  public byte[] cornersPermutation;
  public byte[] edgesPermutation;

  public Square1CubeState(byte[] cornersPermutation, byte[] edgesPermutation) {
    this.cornersPermutation = cornersPermutation;
    this.edgesPermutation = edgesPermutation;
  }

  public Square1CubeState multiply(Square1CubeState move) {
    byte[] cornersPermutation = new byte[8];
    byte[] edgesPermutation = new byte[8];

    for (int i = 0; i < 8; i++) {
      cornersPermutation[i] = this.cornersPermutation[move.cornersPermutation[i]];
      edgesPermutation[i] = this.edgesPermutation[move.edgesPermutation[i]];
    }

    return new Square1CubeState(cornersPermutation, edgesPermutation);
  }
}
