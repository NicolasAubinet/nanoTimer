package com.cube.nanotimer.util;

import java.util.LinkedList;
import java.util.List;

public class CubeSession {

  public static final int SESSION_MAX_SIZE = 13; // 12, + 1 for deletion

  private LinkedList<Long> sessionTimes = new LinkedList<Long>(); // most recent time is in position 0

  public CubeSession() {
  }

  public CubeSession(List<Long> sessionTimes) {
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

  public long getAverageOfFive() {
    return getAverageOf(5);
  }

  public long getAverageOfTwelve() {
    return getAverageOf(12);
  }

  private long getAverageOf(int n) {
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

  public void addTime(long time) {
    sessionTimes.addFirst(time);
    if (sessionTimes.size() > SESSION_MAX_SIZE) {
      sessionTimes.removeLast();
    }
  }

  public void setLastAsDNF() {
    if (!sessionTimes.isEmpty()) {
      sessionTimes.set(0, (long) -1);
    }
  }

  public void setLastAsPlusTwo() {
    if (!sessionTimes.isEmpty()) {
      long curLastTime = sessionTimes.get(0);
      if (curLastTime > 0) {
        sessionTimes.set(0, curLastTime + 2000);
      }
    }
  }

  public void deleteLast() {
    if (!sessionTimes.isEmpty()) {
      sessionTimes.removeFirst();
    }
  }

  public void clearSession() {
    if (sessionTimes != null) {
      sessionTimes.clear();
    }
  }

  public List<Long> getSessionTimes() {
    if (sessionTimes.size() > 12) {
      return sessionTimes.subList(0, 12);
    } else {
      return sessionTimes;
    }
  }

  public void setSessionTimes(LinkedList<Long> sessionTimes) {
    this.sessionTimes = sessionTimes;
  }

}
