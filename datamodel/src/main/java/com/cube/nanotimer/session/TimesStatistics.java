package com.cube.nanotimer.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
   * @param bannedIndexes indexes that can not be chosen as best time index
   * @return the best time index
   */
  private int getBestTimeInd(int count, boolean blind, Collection<Integer> bannedIndexes) {
    int bestInd = -1;
    int specificTimesSize = getTimes(blind).size();
    if (specificTimesSize >= count && count >= MIN_TIMES_FOR_AVERAGE) {
      List<Long> times = getTimes();
      Long best = (long) 0;
      int parsed = 0;
      for (int i = 0; i < times.size(); i++) {
        long t = times.get(i);
        if ((t > 0 && (t < best || best == 0)) && !bannedIndexes.contains(i)) {
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

  public int getBestTimeInd(int count, boolean blind) {
    return getBestTimeInd(count, blind, Collections.<Integer>emptyList());
  }

  public int getBestTimeInd(boolean blind) {
    return getBestTimeInd(getTimes(blind).size(), blind);
  }

  public int getBestTimeInd(int count) {
    return getBestTimeInd(count, false);
  }

  private List<Integer> getBestTimeIndexes(int count, int indexesCount) {
    List<Integer> bestTimeIndexes = new ArrayList<>();
    for (int i = 0; i < indexesCount; i++) {
      int bestTimeInd = getBestTimeInd(count, false, bestTimeIndexes);
      if (bestTimeInd >= 0) {
        bestTimeIndexes.add(bestTimeInd);
      }
    }
    return bestTimeIndexes;
  }

  /**
   * Returns the worst time index.
   * If blind is true, it will not return the index of a DNF
   * @param count number of times to parse
   * @param blind if true, it will only take non-DNF times into account
   * @param bannedIndexes indexes that can not be chosen as worst time index
   * @return the worst time index
   */
  private int getWorstTimeInd(int count, boolean blind, Collection<Integer> bannedIndexes) {
    int worstInd = -1;
    int specificTimesSize = getTimes(blind).size();
    if (specificTimesSize >= count && count >= MIN_TIMES_FOR_AVERAGE) {
      List<Long> times = getTimes();
      Long worst = (long) 0;
      int parsed = 0;
      for (int i = 0; i < times.size(); i++) {
        long t = times.get(i);
        if ((t > worst || (t == -1 && !blind)) && !bannedIndexes.contains(i)) {
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

  public int getWorstTimeInd(int count, boolean blind) {
    return getWorstTimeInd(count, blind, Collections.<Integer>emptyList());
  }

  public int getWorstTimeInd(boolean blind) {
    return getWorstTimeInd(getTimes(blind).size(), blind);
  }

  private List<Integer> getWorstTimeIndexes(int count, int indexesCount) {
    List<Integer> worstTimeIndexes = new ArrayList<>();
    for (int i = 0; i < indexesCount; i++) {
      int worstTimeInd = getWorstTimeInd(count, false, worstTimeIndexes);
      if (worstTimeInd >= 0) {
        worstTimeIndexes.add(worstTimeInd);
      }
    }
    return worstTimeIndexes;
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
      int excludedCount = Math.max(1, n / 20); // only consider 90% middle times (mostly for avg50 and avg100)
      List<Integer> bestIndexes = getBestTimeIndexes(n, excludedCount);
      List<Integer> worstIndexes = getWorstTimeIndexes(n, excludedCount);
      int validTimesCount = 0;
      long sum = 0;
      if (bestIndexes.size() > 0 && worstIndexes.size() > 0) {
        for (int i = 0; i < n; i++) {
          if (!bestIndexes.contains(i) && !worstIndexes.contains(i)) {
            if (times.get(i) > 0) {
              sum += times.get(i);
              validTimesCount++;
            }
          }
        }
      }
      if (validTimesCount >= (n - (excludedCount * 2))) { // -2 because of best/worst times not used
        if (validTimesCount > 0) {
          return (sum / validTimesCount);
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

  public long getBestTime(int n) {
    long best = -2;
    for (int i = 0; i < times.size() && i < n; i++) {
      long time = times.get(i);
      if ((time > 0 && time < best) || best < 0) {
        best = time;
      }
    }
    return best;
  }

  public long getDeviation(int n) {
    if (times.size() < n) {
      return -2;
    }
    int validTimesCount = 0;
    long mean = 0;
    // calculate mean manually because we don't want a DNF�mean if there is a DNF time (DNF's are just ignored)
    for (int i = 0; i < n; i++) {
      long time = times.get(i);
      if (time > 0) {
        mean += time;
        validTimesCount++;
      }
    }
    if (validTimesCount <= 1) {
      return -2;
    }
    mean /= validTimesCount;

    // calculate deviation
    long sum = 0;
    for (int i = 0; i < n; i++) {
      long time = times.get(i);
      if (time > 0) {
        long diff = times.get(i) - mean;
        sum += diff * diff;
      }
    }
    return (long) Math.sqrt(sum / (validTimesCount - 1));
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
