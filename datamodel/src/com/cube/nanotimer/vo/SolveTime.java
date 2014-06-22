package com.cube.nanotimer.vo;

public class SolveTime {

  private long timestamp;
  private long time;
  private String scramble;
  private int solveType;

  public SolveTime() {
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public String getScramble() {
    return scramble;
  }

  public void setScramble(String scramble) {
    this.scramble = scramble;
  }

  public int getSolveType() {
    return solveType;
  }

  public void setSolveType(int solveType) {
    this.solveType = solveType;
  }
}
