package com.cube.nanotimer.vo;

import java.io.Serializable;

public class SolveTime implements Serializable {

  private int id;
  private long timestamp;
  private long time;
  private boolean plusTwo;
  private String scramble;
  private SolveType solveType;

  public SolveTime() {
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  public boolean isPlusTwo() {
    return plusTwo;
  }

  public void setPlusTwo(boolean plusTwo) {
    this.plusTwo = plusTwo;
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

  public void plusTwo() {
    setTime(time + 2000);
    setPlusTwo(true);
  }

}
