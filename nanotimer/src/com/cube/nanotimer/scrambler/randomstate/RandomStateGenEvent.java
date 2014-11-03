package com.cube.nanotimer.scrambler.randomstate;

public class RandomStateGenEvent {

  public enum State { PREPARING, GENERATING, IDLE, STOPPING };

  private State state;
  private int curScramble;
  private int totToGen;

  public RandomStateGenEvent(State state, int curScramble, int totToGen) {
    this.state = state;
    this.curScramble = curScramble;
    this.totToGen = totToGen;
  }

  public State getState() {
    return state;
  }

  public int getCurScramble() {
    return curScramble;
  }

  public int getTotalToGenerate() {
    return totToGen;
  }

}
