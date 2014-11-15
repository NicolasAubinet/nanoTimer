package com.cube.nanotimer.session;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TimesStatistics {

  private static final int MIN_TIMES_FOR_AVERAGE = 5;

  protected LinkedList<Long> times = new LinkedList<Long>(); // most recent time is in position 0

  public TimesStatistics() {
  }

  public TimesStatistics(List<Long> times) {
    this.times = new LinkedList<Long>(times);
  }

  /**
   * Returns the best time index.
   * @param count number of times to parse
   * @param blind indicates whether this is a blind type or not. if true, it will return -1 if there are not enough successes
   * @return the best time index
   */
  public int getBestTimeInd(int count, boolean blind) {
    int bestInd = -1;
    int specificTimesSize = getTimes(blind).size();
    if (specificTimesSize >= count && count >= MIN_TIMES_FOR_AVERAGE) {
      List<Long> times = getTimes();
      Long best = (long) 0;
      int parsed = 0;
      for (int i = 0; i < times.size(); i++) {
        long t = times.get(i);
        if (t > 0 && (t < best || best == 0)) {
          best = t;
          bestInd = i;
        }
        if (t > 0 || !blind) {
          if (++parsed == count) {
            break;
          }
        }
      }
    }
    return bestInd;
  }

  public int getBestTimeInd(boolean blind) {
    return getBestTimeInd(getTimes(blind).size(), blind);
  }

  public int getBestTimeInd(int count) {
    return getBestTimeInd(count, false);
  }

  /**
   * Returns the worst time index.
   * If blind is true, it will not return the index of a DNF
   * @param count number of times to parse
   * @param blind if true, it will only take non-DNF times into account
   * @return the worst time index
   */
  public int getWorstTimeInd(int count, boolean blind) {
    int worstInd = -1;
    int specificTimesSize = getTimes(blind).size();
    if (specificTimesSize >= count && count >= MIN_TIMES_FOR_AVERAGE) {
      List<Long> times = getTimes();
      Long worst = (long) 0;
      int parsed = 0;
      for (int i = 0; i < times.size(); i++) {
        long t = times.get(i);
        if (t > worst || (t == -1 && !blind)) {
          worst = t;
          worstInd = i;
          if (t == -1) {
            break; // nothing can be worse than a DNF
          }
        }
        if (t > 0 || !blind) {
          if (++parsed == count) {
            break;
          }
        }
      }
    }
    return worstInd;
  }

  public int getWorstTimeInd(boolean blind) {
    return getWorstTimeInd(getTimes(blind).size(), blind);
  }

  public int getWorstTimeInd(int count) {
    return getWorstTimeInd(count, false);
  }

  public long getAverageOf(int n) {
    List<Long> times = getTimes();
    if (times.size() < MIN_TIMES_FOR_AVERAGE || n < MIN_TIMES_FOR_AVERAGE) {
      return -2;
    }
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

  public long getSuccessAverageOf(int n, boolean calculateAll) {
    List<Long> successes = getSuccesses();
    if (successes.size() < MIN_TIMES_FOR_AVERAGE || n < MIN_TIMES_FOR_AVERAGE) {
      return -2;
    }
    if (successes.size() >= n) {
      return new TimesStatistics(successes).getAverageOf(n);
    } else if (calculateAll) {
      return new TimesStatistics(successes).getAverageOf(successes.size());
    } else {
      return -2;
    }
//    int i = 0;
//    for (Long t : successes) {
//      i++;
//      successes.add(t);
//      if (i == n) {
//        return new TimesStatistics(successes).getAverageOf(n);
//      }
//    }
//    if (i >= MIN_TIMES_FOR_AVERAGE && calculateAll) {
//      return new TimesStatistics(successes).getAverageOf(i);
//    } else {
//      return -2;
//    }
  }

  public long getSuccessMeanOf(int n, boolean calculateAll) {
    long total = 0;
    int i = 0;
    for (Long t : getSuccesses()) {
      total += t;
      i++;
      if (i == n) {
        return total / n;
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

  public List<Long> getSuccesses() {
    List<Long> successes = new ArrayList<Long>();
    for (Long t : getTimes()) {
      if (t > 0) {
        successes.add(t);
      }
    }
    return successes;
  }

  public List<Long> getTimes(boolean blind) {
    if (blind) {
      return getSuccesses();
    } else {
      return getTimes();
    }
  }

  public List<Long> getTimes() {
    return times;
  }

}
