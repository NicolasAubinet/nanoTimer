package com.cube.nanotimer.vo;

import java.util.Arrays;

public class TwoCubeState implements CubeState {
  public byte[] permutations = new byte[7];
  public byte[] orientations = new byte[7];

  public TwoCubeState() {
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Corner permutations: ").append(Arrays.toString(permutations)).append("\n");
    sb.append("Corner orientations: ").append(Arrays.toString(orientations)).append("\n");
    return sb.toString();
  }
}
