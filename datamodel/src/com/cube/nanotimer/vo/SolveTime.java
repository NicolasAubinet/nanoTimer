package com.cube.nanotimer.vo;

public class SolveTime {

  private float timestamp;
  private float time;
  private String scramble;
  private SolveType solveType;

  public SolveTime() {
  }

  public float getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(float timestamp) {
    this.timestamp = timestamp;
  }

  public float getTime() {
    return time;
  }

  public void setTime(float time) {
    this.time = time;
  }

  public String getScramble() {
    return scramble;
  }

  public void setScramble(String scramble) {
    this.scramble = scramble;
  }

  public SolveType getSolveType() {
    return solveType;
  }

  public void setSolveType(SolveType solveType) {
    this.solveType = solveType;
  }
}
