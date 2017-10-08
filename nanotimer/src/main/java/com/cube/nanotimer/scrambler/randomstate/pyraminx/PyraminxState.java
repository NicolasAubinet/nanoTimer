package com.cube.nanotimer.scrambler.randomstate.pyraminx;

public class PyraminxState {
  public byte[] tipsOrientation;
  public byte[] verticesOrientation;
  public byte[] edgesPermutation;
  public byte[] edgesOrientation;

  public PyraminxState(byte[] tipsOrientation, byte[] verticesOrientation, byte[] edgesPermutation, byte[] edgesOrientation) {
    this.tipsOrientation = tipsOrientation;
    this.verticesOrientation = verticesOrientation;
    this.edgesPermutation = edgesPermutation;
    this.edgesOrientation = edgesOrientation;
  }

  public PyraminxState multiply(PyraminxState move) {
    byte[] tipsOrientation = new byte[4];
    for (int i = 0; i < 4; i++) {
      tipsOrientation[i] = (byte) ((this.tipsOrientation[i] + move.tipsOrientation[i]) % 3);
    }

    byte[] verticesOrientation = new byte[4];
    for (int i = 0; i < 4; i++) {
      verticesOrientation[i] = (byte) ((this.verticesOrientation[i] + move.verticesOrientation[i]) % 3);
    }

    byte[] edgesPermutation = new byte[6];
    byte[] edgesOrientation = new byte[6];
    for (int i = 0; i < 6; i++) {
      edgesPermutation[i] = this.edgesPermutation[move.edgesPermutation[i]];
      edgesOrientation[i] = (byte) ((this.edgesOrientation[move.edgesPermutation[i]] + move.edgesOrientation[i]) % 2);
    }

    return new PyraminxState(tipsOrientation, verticesOrientation, edgesPermutation, edgesOrientation);
  }
}
