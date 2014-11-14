package com.cube.nanotimer.session;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TimesStatistics {

  protected LinkedList<Long> times = new LinkedList<Long>(); // most recent time is in position 0

  public TimesStatistics() {
  }

  public TimesStatistics(List<Long> times) {
    this.times = new LinkedList<Long>(times);
  }

  public int getBestTimeInd(int count) {
    int bestInd = -1;
    List<Long> times = getTimes();
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
    List<Long> times = getTimes();
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

  public long getAverageOf(int n) {
    List<Long> times = getTimes();
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
        if (validTimesCount > 0) {
          return (avg / validTimesCount);
        }
      } else {
        return -1;
      }
    }
    return -2;
  }

  public long getMeanOf(int n) {
    List<Long> times = getTimes();
    long mean = 0;
    if (times.size() >= n) {
      for (int i = 0; i < n; i++) {
        long t = times.get(i);
        if (t == -1) {
          return -1;
        } else {
          mean += t;
        }
      }
    }
    if (mean > 0) {
      return mean / n;
    } else {
      return -2;
    }
  }

  public long getSuccessAverageOf(int n, int minSuccesses, boolean calculateAll) {
    int i = 0;
    if (n < minSuccesses) {
      return -2;
    }
    List<Long> successes = new ArrayList<Long>();
    for (Long t : getTimes()) {
      if (t > 0) { // if not a DNF
        i++;
        successes.add(t);
        if (i == n) {
          return new TimesStatistics(successes).getAverageOf(n);
        }
      }
    }
    if (i >= minSuccesses && calculateAll) {
      return new TimesStatistics(successes).getAverageOf(i);
    } else {
      return -2;
    }
  }

  public long getSuccessMeanOf(int n, boolean calculateAll) {
    long total = 0;
    int i = 0;
    for (Long t : getTimes()) {
      if (t > 0) { // if not a DNF
        total += t;
        i++;
        if (i == n) {
          return total / n;
        }
      }
    }
    if (i > 0 && calculateAll) {
      return total / i;
    } else {
      return -2;
    }
  }

  public int getAccuracy(int n) {
    return getAccuracy(n, false);
  }

  public int getAccuracy(int n, boolean calculateAll) {
    List<Long> times = getTimes();
    if (times.isEmpty()) {
      return -2;
    }
    long successes = 0;
    int i;
    for (i = 0; i < times.size() && i < n; i++) {
      if (times.get(i) > 0) { // if not a DNF
        successes++;
      }
    }
    if (i == n || (i > 0 && calculateAll)) {
      if (successes == 0) {
        return 0;
      } else {
        return (int) (successes * 100 / i);
      }
    } else {
      return -2;
    }
  }

  public List<Long> getTimes() {
    return times;
  }

}
