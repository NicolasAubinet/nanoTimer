package com.cube.nanotimer.vo;

import java.util.List;

public class SessionDetails {

  private long ra;
  private long mean;
  private int sessionSolvesCount;
  private int totalSolvesCount;
  private List<Long> sessionTimes;

  public SessionDetails() {
  }

  public long getRA() {
    return ra;
  }

  public void setRA(long ra) {
    this.ra = ra;
  }

  public long getMean() {
    return mean;
  }

  public void setMean(long mean) {
    this.mean = mean;
  }

  public int getSessionSolvesCount() {
    return sessionSolvesCount;
  }

  public void setSessionSolvesCount(int sessionSolvesCount) {
    this.sessionSolvesCount = sessionSolvesCount;
  }

  public int getTotalSolvesCount() {
    return totalSolvesCount;
  }

  public void setTotalSolvesCount(int totalSolvesCount) {
    this.totalSolvesCount = totalSolvesCount;
  }

  public List<Long> getSessionTimes() {
    return sessionTimes;
  }

  public void setSessionTimes(List<Long> sessionTimes) {
    this.sessionTimes = sessionTimes;
  }

}
