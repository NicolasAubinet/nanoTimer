package com.cube.nanotimer.scrambler.randomstate;

import com.cube.nanotimer.vo.CubeType;

public class RandomStateGenEvent {

  public enum State { PREPARING, GENERATING, IDLE, STOPPING };

  private State state;
  private CubeType cubeType;
  private int curScramble;
  private int totToGen;

  public RandomStateGenEvent(State state, CubeType cubeType, int curScramble, int totToGen) {
    this.state = state;
    this.cubeType = cubeType;
    this.curScramble = curScramble;
    this.totToGen = totToGen;
  }

  public State getState() {
    return state;
  }

  public CubeType getCubeType() {
    return cubeType;
  }

  public String getCubeTypeName() {
    return cubeType == null ? "" : cubeType.getName();
  }

  public int getCurScramble() {
    return curScramble;
  }

  public int getTotalToGenerate() {
    return totToGen;
  }

}
