package com.cube.nanotimer.util;

import com.cube.nanotimer.session.TimesStatistics;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@RunWith(JUnit4.class)
public class TimesStatisticsTest {

  @Test
  public void testEmptyTimes() {
    List<Long> times = new ArrayList<Long>();
    TimesStatistics timesStatistics = new TimesStatistics(times);
    Assert.assertEquals(-2, timesStatistics.getAverageOf(5));
    Assert.assertEquals(-2, timesStatistics.getMeanOf(5));
    Assert.assertEquals(-2, timesStatistics.getSuccessAverageOf(5, false));
    Assert.assertEquals(-2, timesStatistics.getSuccessMeanOf(5, false));
    Assert.assertEquals(-2, timesStatistics.getAccuracy(5, false));
  }

  @Test
  public void testTimesSizeSmaller() {
    List<Long> times = new ArrayList<Long>();
    times.add(1000l);
    times.add(2000l);
    times.add(3000l);
    TimesStatistics timesStatistics = new TimesStatistics(times);
    Assert.assertEquals(-2, timesStatistics.getAverageOf(5));
    Assert.assertEquals(-2, timesStatistics.getMeanOf(5));
    Assert.assertEquals(-2, timesStatistics.getSuccessAverageOf(5, false));
    Assert.assertEquals(-2, timesStatistics.getSuccessMeanOf(5, false));
    Assert.assertEquals(-2, timesStatistics.getAccuracy(5, false));
  }

  @Test
  public void testCalculateAverageWithNotEnoughTimes() {
    List<Long> times = new ArrayList<Long>();
    times.add(1000l);
    times.add(2000l);
    times.add(3000l);
    TimesStatistics timesStatistics = new TimesStatistics(times);
    Assert.assertEquals(-2, timesStatistics.getAverageOf(3));
    Assert.assertEquals(-2, timesStatistics.getSuccessAverageOf(3, true));
    Assert.assertEquals(-2, timesStatistics.getSuccessAverageOf(3, false));
  }

  @Test
  public void testCalculateAllWithSmaller() {
    List<Long> times = new ArrayList<Long>();
    times.add(1000l);
    times.add(2000l);
    times.add(3000l);
    times.add(4000l);
    times.add(5000l);
    TimesStatistics timesStatistics = new TimesStatistics(times);
    Assert.assertEquals(3000, timesStatistics.getSuccessAverageOf(10, true));
    Assert.assertEquals(3000, timesStatistics.getSuccessMeanOf(10, true));
    Assert.assertEquals(100, timesStatistics.getAccuracy(10, true));
  }

  @Test
  public void testAverage() {
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getAverageOf(5));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 5000)).getAverageOf(5));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 12000)).getAverageOf(5));
    Assert.assertEquals(4000, new TimesStatistics(getTimesList(200, 2000, 6000, 4000, 12000)).getAverageOf(5));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(3000, 1000, 4000, 2000, 5000)).getAverageOf(5));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(6000, 3000, 1000, 4000, 2000, 5000)).getAverageOf(5));
    Assert.assertEquals(4333, new TimesStatistics(getTimesList(-1, 6000, 3000, 1000, 4000, 2000, 5000)).getAverageOf(5));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(-1, -1, 6000, 3000, 1000, 4000, 2000, 5000)).getAverageOf(5));
  }

  @Test
  public void testMean() {
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getMeanOf(5));
    Assert.assertEquals(2840, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 5000)).getMeanOf(5));
    Assert.assertEquals(4240, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 12000)).getMeanOf(5));
    Assert.assertEquals(4840, new TimesStatistics(getTimesList(200, 2000, 6000, 4000, 12000)).getMeanOf(5));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(3000, 1000, 4000, 2000, 5000)).getMeanOf(5));
    Assert.assertEquals(3200, new TimesStatistics(getTimesList(6000, 3000, 1000, 4000, 2000, 5000)).getMeanOf(5));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(-1, 6000, 3000, 1000, 4000, 2000, 5000)).getMeanOf(5));
  }

  @Test
  public void testSuccessAverage() {
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getSuccessAverageOf(5, false));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 5000)).getSuccessAverageOf(5, false));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 12000)).getSuccessAverageOf(5, false));
    Assert.assertEquals(4000, new TimesStatistics(getTimesList(200, 2000, 6000, 4000, 12000)).getSuccessAverageOf(5, false));

    Assert.assertEquals(-2, new TimesStatistics(getTimesList(1000, -1, 3000, 4000, 5000)).getSuccessAverageOf(5, false));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(-1, 6000, 3000, 1000, 4000, 2000, 5000)).getSuccessAverageOf(5, false));
    Assert.assertEquals(4333, new TimesStatistics(getTimesList(8000, -1, 6000, 3000, 1000, 4000, 2000, 5000)).getSuccessAverageOf(5, false));

    List<Long> times = getTimesList(6000, 8000, -1, 3000, 1000, 4000, 5000);
    Assert.assertEquals(4333, new TimesStatistics(times).getSuccessAverageOf(5, false));
    Assert.assertEquals(4500, new TimesStatistics(times).getSuccessAverageOf(12, true));
    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessAverageOf(12, false));
//    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessAverageOf(5, 7, true));
//    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessAverageOf(5, 7, false));
//    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessAverageOf(12, 7, true));
//    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessAverageOf(12, 6, false));
//    Assert.assertEquals(4500, new TimesStatistics(times).getSuccessAverageOf(12, 6, true));
//    Assert.assertEquals(6000, new TimesStatistics(times).getSuccessAverageOf(3, 3, true));
  }

  @Test
  public void testSuccessMean() {
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getSuccessMeanOf(5, false));
    Assert.assertEquals(2840, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 5000)).getSuccessMeanOf(5, false));
    Assert.assertEquals(4240, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 12000)).getSuccessMeanOf(5, false));
    Assert.assertEquals(4840, new TimesStatistics(getTimesList(200, 2000, 6000, 4000, 12000)).getSuccessMeanOf(5, false));

    Assert.assertEquals(3250, new TimesStatistics(getTimesList(1000, -1, 3000, 4000, 5000)).getSuccessMeanOf(5, true));
    Assert.assertEquals(-2, new TimesStatistics(getTimesList(1000, -1, 3000, 4000, 5000)).getSuccessMeanOf(5, false));
    Assert.assertEquals(3200, new TimesStatistics(getTimesList(-1, 6000, 3000, 1000, 4000, 2000, 5000)).getSuccessMeanOf(5, false));
    Assert.assertEquals(4400, new TimesStatistics(getTimesList(8000, -1, 6000, 3000, 1000, 4000, 2000, 5000)).getSuccessMeanOf(5, false));

    List<Long> times = getTimesList(6000, 8000, -1, 3000, 1000, 4000, 5000);
    Assert.assertEquals(4400, new TimesStatistics(times).getSuccessMeanOf(5, false));
    Assert.assertEquals(4400, new TimesStatistics(times).getSuccessMeanOf(5, true));
    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessMeanOf(12, false));
    Assert.assertEquals(4500, new TimesStatistics(times).getSuccessMeanOf(12, true));
    Assert.assertEquals(5666, new TimesStatistics(times).getSuccessMeanOf(3, false));
    Assert.assertEquals(5666, new TimesStatistics(times).getSuccessMeanOf(3, true));
  }

  @Test
  public void testAccuracy() {
    Assert.assertEquals(100, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getAccuracy(5, false));
    Assert.assertEquals(100, new TimesStatistics(getTimesList(1000)).getAccuracy(1, false));
    Assert.assertEquals(50, new TimesStatistics(getTimesList(1000, -1)).getAccuracy(2, false));
    Assert.assertEquals(50, new TimesStatistics(getTimesList(-1, 1000)).getAccuracy(2, false));
    Assert.assertEquals(25, new TimesStatistics(getTimesList(-1, 1000, -1, -1)).getAccuracy(4, false));
    Assert.assertEquals(0, new TimesStatistics(getTimesList(-1)).getAccuracy(1, false));
    Assert.assertEquals(-2, new TimesStatistics(getTimesList()).getAccuracy(0, false));
    Assert.assertEquals(0, new TimesStatistics(getTimesList(-1, 1000)).getAccuracy(1, false));
    Assert.assertEquals(-2, new TimesStatistics(getTimesList(-1, 1000)).getAccuracy(5, false));
    Assert.assertEquals(50, new TimesStatistics(getTimesList(-1, 1000, -1)).getAccuracy(2, false));
    Assert.assertEquals(75, new TimesStatistics(getTimesList(-1, 1000, 1000, 1000)).getAccuracy(5, true));
    Assert.assertEquals(-2, new TimesStatistics(getTimesList(-1, 1000, 1000, 1000)).getAccuracy(5, false));
    Assert.assertEquals(75, new TimesStatistics(getTimesList(-1, 1000, 1000, 1000)).getAccuracy(4, false));
    Assert.assertEquals(50, new TimesStatistics(getTimesList(-1, 1000, -1, 1000)).getAccuracy(10, true));
  }

  @Test
  public void testBestTimeInd() {
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getBestTimeInd(4, false));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getBestTimeInd(4, true));
    Assert.assertEquals(0, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getBestTimeInd(5, false));
    Assert.assertEquals(1, new TimesStatistics(getTimesList(2000, 1000, 3000, 4000, 5000)).getBestTimeInd(5, false));
    Assert.assertEquals(0, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getBestTimeInd(5, true));
    Assert.assertEquals(1, new TimesStatistics(getTimesList(2000, 1000, 3000, 4000, 5000)).getBestTimeInd(5, true));
    Assert.assertEquals(4, new TimesStatistics(getTimesList(2000, 4000, -1, 5000, 1000)).getBestTimeInd(5, false));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, -1, 5000, 1000)).getBestTimeInd(5, true));
    Assert.assertEquals(4, new TimesStatistics(getTimesList(2000, 4000, -1, 5000, 1000, 3000)).getBestTimeInd(5, true));
    Assert.assertEquals(5, new TimesStatistics(getTimesList(2000, 4000, -1, 5000, 1000, 500)).getBestTimeInd(5, true));
    Assert.assertEquals(1, new TimesStatistics(getTimesList(2000, 1000, -1, 4000, 1000, 3000, -1)).getBestTimeInd(5, true));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1)).getBestTimeInd(5, true));
    Assert.assertEquals(5, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 500)).getBestTimeInd(5, true));
    Assert.assertEquals(4, new TimesStatistics(getTimesList(-1, -1, 8, 6, 4, 10, 3, 5)).getBestTimeInd(5, false));
    Assert.assertEquals(6, new TimesStatistics(getTimesList(-1, -1, 8, 6, 4, 10, 3, 5)).getBestTimeInd(5, true));
    Assert.assertEquals(5, new TimesStatistics(getTimesList(115, -1, 100, 111, 103, 82)).getBestTimeInd(5, true));

    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 500)).getBestTimeInd(10, false));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 500)).getBestTimeInd(10, true));
    Assert.assertEquals(5, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 500)).getBestTimeInd(6, false));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 500)).getBestTimeInd(6, true));
  }

  @Test
  public void testWorstTimeInd() {
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getWorstTimeInd(4, false));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getWorstTimeInd(4, true));
    Assert.assertEquals(4, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getWorstTimeInd(5, false));
    Assert.assertEquals(3, new TimesStatistics(getTimesList(2000, 1000, 3000, 5000, 4000)).getWorstTimeInd(5, false));
    Assert.assertEquals(4, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getWorstTimeInd(5, true));
    Assert.assertEquals(4, new TimesStatistics(getTimesList(2000, 1000, 3000, 4000, 5000)).getWorstTimeInd(5, true));
    Assert.assertEquals(2, new TimesStatistics(getTimesList(2000, 4000, -1, 5000, 1000)).getWorstTimeInd(5, false));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, -1, 5000, 1000)).getWorstTimeInd(5, true));
    Assert.assertEquals(3, new TimesStatistics(getTimesList(2000, 4000, -1, 5000, 1000, 3000)).getWorstTimeInd(5, true));
    Assert.assertEquals(4, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 6000)).getWorstTimeInd(5, false));
    Assert.assertEquals(5, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 6000)).getWorstTimeInd(5, true));

    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 6000)).getWorstTimeInd(10, false));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 6000)).getWorstTimeInd(10, true));
    Assert.assertEquals(4, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 6000)).getWorstTimeInd(6, false));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 6000)).getWorstTimeInd(6, true));
  }

  @Test
  public void testBestTime() {
    Assert.assertEquals(10, new TimesStatistics(getTimesList(10, 20, 30)).getBestTime(3));
    Assert.assertEquals(10, new TimesStatistics(getTimesList(20, 10, 30)).getBestTime(3));
    Assert.assertEquals(10, new TimesStatistics(getTimesList(20, 30, 10)).getBestTime(3));
    Assert.assertEquals(20, new TimesStatistics(getTimesList(20, 30, 10)).getBestTime(2));
    Assert.assertEquals(10, new TimesStatistics(getTimesList(20, 30, 10)).getBestTime(4));
    Assert.assertEquals(15, new TimesStatistics(getTimesList(45, -1, 15, 30)).getBestTime(4));
    Assert.assertEquals(30, new TimesStatistics(getTimesList(45, -1, -1, 30)).getBestTime(4));
    Assert.assertEquals(45, new TimesStatistics(getTimesList(45, -1, -1, 30)).getBestTime(3));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(-1, -1, -1)).getBestTime(3));
    Assert.assertEquals(-2, new TimesStatistics(getTimesList()).getBestTime(0));
    Assert.assertEquals(-2, new TimesStatistics(getTimesList()).getBestTime(1));
  }

  @Test
  public void testDeviation() {
    Assert.assertEquals(0, new TimesStatistics(getTimesList(10, 10, 10)).getDeviation(3));
    Assert.assertEquals(100, new TimesStatistics(getTimesList(100, 200, 300)).getDeviation(3));
    Assert.assertEquals(200, new TimesStatistics(getTimesList(100, 300, 500)).getDeviation(3));
    Assert.assertEquals(264, new TimesStatistics(getTimesList(100, 200, 600)).getDeviation(3));
    Assert.assertEquals(264, new TimesStatistics(getTimesList(100, 200, 600)).getDeviation(3));
    Assert.assertEquals(397, new TimesStatistics(getTimesList(245, 790, 530, 1024, 42)).getDeviation(5));
    Assert.assertEquals(-2, new TimesStatistics(getTimesList(245, 790, 530, 1024, 42)).getDeviation(6));
    Assert.assertEquals(335, new TimesStatistics(getTimesList(245, 790, 530, 1024, 42)).getDeviation(4));
    Assert.assertEquals(397, new TimesStatistics(getTimesList(245, 790, -1, 530, 1024, 42)).getDeviation(6));
    Assert.assertEquals(397, new TimesStatistics(getTimesList(245, 790, -1, 530, -1, 1024, 42)).getDeviation(7));
    Assert.assertEquals(-2, new TimesStatistics(getTimesList(245, 790, -1, 530, -1, 1024, 42)).getDeviation(8));
    Assert.assertEquals(397, new TimesStatistics(getTimesList(245, -1, 790, -1, 530, 1024, 42)).getDeviation(7));
    Assert.assertEquals(272, new TimesStatistics(getTimesList(245, -1, 790, -1, 530, 1024, 42)).getDeviation(5));
    Assert.assertEquals(385, new TimesStatistics(getTimesList(245, -1, 790, -1)).getDeviation(4));
    Assert.assertEquals(-2, new TimesStatistics(getTimesList(-1, -1, -1)).getDeviation(3));
    Assert.assertEquals(-2, new TimesStatistics(getTimesList(-1, -1, 790, -1)).getDeviation(4));
    Assert.assertEquals(-2, new TimesStatistics(getTimesList()).getDeviation(0));
    Assert.assertEquals(-2, new TimesStatistics(getTimesList(300)).getDeviation(1));
  }

  @Test
  public void testBigAverages() {
    // Test averages of 50 and 100: only the 90% middle times should be considered (avg100 drops best 5 and world 5 times, to avoid DNF avg if less than 5 DNF times in last 100 solves)
    List<Long> orderedTimes = new ArrayList<>();
    for (int i = 1; i <= 100; i++) {
      orderedTimes.add((long) i * 1000);
    }
    List<Long> times = new ArrayList<>(orderedTimes);
    TimesStatistics timesStatistics = new TimesStatistics(times);
    Assert.assertEquals(3000, timesStatistics.getAverageOf(5));
    Assert.assertEquals(25500, timesStatistics.getAverageOf(50));
    Assert.assertEquals(50500, timesStatistics.getAverageOf(100));

    Collections.shuffle(timesStatistics.getTimes(), new Random(1000l));
    Assert.assertEquals(50500, timesStatistics.getAverageOf(100));

    List<Long> reversedTimes = new ArrayList<>(orderedTimes);
    Collections.reverse(reversedTimes);

    timesStatistics = new TimesStatistics(reversedTimes);
    Assert.assertEquals(75500, timesStatistics.getAverageOf(50));
    timesStatistics.getTimes().set(0, -1l);
    Assert.assertEquals(75500, timesStatistics.getAverageOf(50));
    timesStatistics.getTimes().set(1, -1l);
    Assert.assertEquals(75500, timesStatistics.getAverageOf(50));
    timesStatistics.getTimes().set(2, -1l);
    Assert.assertEquals(-1, timesStatistics.getAverageOf(50));
    timesStatistics.getTimes().set(3, -1l);
    timesStatistics.getTimes().set(4, -1l);
    Assert.assertEquals(50500, timesStatistics.getAverageOf(100));
    timesStatistics.getTimes().set(5, -1l);
    Assert.assertEquals(-1, timesStatistics.getAverageOf(100));

    timesStatistics = new TimesStatistics(orderedTimes);
    timesStatistics.getTimes().set(10, -1l);
    Assert.assertEquals(26326, timesStatistics.getAverageOf(50));
    timesStatistics.getTimes().set(20, -1l);
    Assert.assertEquals(26956, timesStatistics.getAverageOf(50));
    timesStatistics.getTimes().set(30, -1l);
    Assert.assertEquals(-1, timesStatistics.getAverageOf(50));
  }

  private List<Long> getTimesList(int... times) {
    List<Long> list = new ArrayList<Long>();
    for (int t : times) {
      list.add((long) t);
    }
    return list;
  }

}
