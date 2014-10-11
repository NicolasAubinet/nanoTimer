package com.cube.nanotimer.vo;

import java.util.List;

public class SessionDetails {

  private int totalSolvesCount;
  private long sessionStartTime;
  private List<Long> sessionTimes;

  public SessionDetails() {
  }

  public int getTotalSolvesCount() {
    return totalSolvesCount;
  }

  public void setTotalSolvesCount(int totalSolvesCount) {
    this.totalSolvesCount = totalSolvesCount;
  }

  public long getSessionStartTime() {
    return sessionStartTime;
  }

  public void setSessionStartTime(long sessionStartTime) {
    this.sessionStartTime = sessionStartTime;
  }

  public List<Long> getSessionTimes() {
    return sessionTimes;
  }

  public void setSessionTimes(List<Long> sessionTimes) {
    this.sessionTimes = sessionTimes;
  }

}
