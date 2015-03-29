package com.cube.nanotimer.vo;

import java.io.Serializable;
import java.util.Arrays;

public class SolveTime implements Serializable {

  private int id;
  private long timestamp;
  private long time;
  private boolean plusTwo;
  private boolean pb;
  private String scramble;
  private SolveType solveType;
  private Long[] stepsTimes;

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

  public boolean isPb() {
    return pb;
  }

  public void setPb(boolean pb) {
    this.pb = pb;
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

  public Long[] getStepsTimes() {
    return stepsTimes;
  }

  public void setStepsTimes(Long[] stepsTimes) {
    this.stepsTimes = stepsTimes;
  }

  public boolean isDNF() {
    return time < 0;
  }

  public void plusTwo() {
    setTime(time + 2000);
    setPlusTwo(true);
  }

  public boolean hasSteps() {
    return stepsTimes != null && stepsTimes.length > 0;
  }

  @Override
  public String toString() {
    return "SolveTime{" +
        ", time=" + time +
        ", stepsTimes=" + Arrays.toString(stepsTimes) +
        '}';
  }

}
