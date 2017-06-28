package com.cube.nanotimer.vo;

public class FrequencyData {
  private int solvesCount;
  private long day;

  public FrequencyData(int solvesCount, long day) {
    this.solvesCount = solvesCount;
    this.day = day;
  }

  public int getSolvesCount() {
    return solvesCount;
  }

  public void setSolvesCount(int solvesCount) {
    this.solvesCount = solvesCount;
  }

  public long getDay() {
    return day;
  }

  public void setDay(long day) {
    this.day = day;
  }
}
