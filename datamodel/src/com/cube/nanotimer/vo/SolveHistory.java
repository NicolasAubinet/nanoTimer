package com.cube.nanotimer.vo;

import java.util.List;

public class SolveHistory {

  private int solvesCount;
  private List<SolveTime> solveTimes;

  public SolveHistory() {
  }

  public int getSolvesCount() {
    return solvesCount;
  }

  public void setSolvesCount(int solvesCount) {
    this.solvesCount = solvesCount;
  }

  public List<SolveTime> getSolveTimes() {
    return solveTimes;
  }

  public void setSolveTimes(List<SolveTime> solveTimes) {
    this.solveTimes = solveTimes;
  }

}
