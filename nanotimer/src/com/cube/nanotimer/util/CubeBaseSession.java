package com.cube.nanotimer.util;

import java.util.LinkedList;
import java.util.List;

public class CubeBaseSession {

  protected LinkedList<Long> sessionTimes = new LinkedList<Long>(); // most recent time is in position 0

  public CubeBaseSession() {
  }

  public CubeBaseSession(List<Long> sessionTimes) {
    this.sessionTimes = new LinkedList<Long>(sessionTimes);
  }

  public int getBestTimeInd(int count) {
    int bestInd = -1;
    List<Long> times = getSessionTimes();
    if (times.size() >= count) {
      Long best = (long) 0;
      for (int i = 0; i < count; i++) {
        long t = times.get(i);
        if (t > 0 && (t < best || best == 0)) {
          best = t;
          bestInd = i;
        }
      }
    }
    return bestInd;
  }

  public int getWorstTimeInd(int count) {
    int worstInd = -1;
    List<Long> times = getSessionTimes();
    if (times.size() >= count) {
      Long worst = (long) 0;
      for (int i = 0; i < count; i++) {
        long t = times.get(i);
        if (t > worst || t == -1) {
          worst = t;
          worstInd = i;
          if (t == -1) {
            break; // nothing can be worse than a DNF
          }
        }
      }
    }
    return worstInd;
  }

  public long getRAOf(int n) {
    List<Long> times = getSessionTimes();
    if (times.size() >= n) {
      int bestInd = getBestTimeInd(n);
      int worstInd = getWorstTimeInd(n);
      int validTimesCount = 0;
      long avg = 0;
      if (bestInd >= 0 && worstInd >= 0) {
        for (int i = 0; i < n; i++) {
          if (i != bestInd && i != worstInd) {
            if (times.get(i) > 0) {
              avg += times.get(i);
              validTimesCount++;
            }
          }
        }
      }
      if (validTimesCount >= (n - 2)) { // -2 because of best/worst times not used
        return (avg / (n - 2));
      } else {
        return -1;
      }
    }
    return -2;
  }

  public long getMean() {
    long mean = 0;
    if (sessionTimes.size() > 0) {
      for (Long t : sessionTimes) {
        mean += t;
      }
      mean /= sessionTimes.size();
    } else {
      mean = -2;
    }
    return mean;
  }

  public List<Long> getSessionTimes() {
    return sessionTimes;
  }

}
