package com.cube.nanotimer.util;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import com.cube.nanotimer.session.TimesStatistics;
import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

public class TimesStatisticsTest extends AndroidTestCase {

  @SmallTest
  public void testEmptyTimes() {
    List<Long> times = new ArrayList<Long>();
    TimesStatistics timesStatistics = new TimesStatistics(times);
    Assert.assertEquals(-2, timesStatistics.getAverageOf(5));
    Assert.assertEquals(-2, timesStatistics.getMeanOf(5));
    Assert.assertEquals(-2, timesStatistics.getSuccessAverageOf(5, 5, false));
    Assert.assertEquals(-2, timesStatistics.getSuccessMeanOf(5, false));
    Assert.assertEquals(-2, timesStatistics.getAccuracy(5, false));
  }

  @SmallTest
  public void testTimesSizeSmaller() {
    List<Long> times = new ArrayList<Long>();
    times.add(1000l);
    times.add(2000l);
    times.add(3000l);
    TimesStatistics timesStatistics = new TimesStatistics(times);
    Assert.assertEquals(-2, timesStatistics.getAverageOf(5));
    Assert.assertEquals(-2, timesStatistics.getMeanOf(5));
    Assert.assertEquals(-2, timesStatistics.getSuccessAverageOf(5, 5, false));
    Assert.assertEquals(-2, timesStatistics.getSuccessMeanOf(5, false));
    Assert.assertEquals(-2, timesStatistics.getAccuracy(5, false));
  }

  @SmallTest
  public void testCalculateAllWithSmaller() {
    List<Long> times = new ArrayList<Long>();
    times.add(1000l);
    times.add(2000l);
    times.add(3000l);
    TimesStatistics timesStatistics = new TimesStatistics(times);
    Assert.assertEquals(2000, timesStatistics.getSuccessAverageOf(5, 3, true));
    Assert.assertEquals(2000, timesStatistics.getSuccessMeanOf(5, true));
    Assert.assertEquals(100, timesStatistics.getAccuracy(5, true));
  }

  @SmallTest
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

  @SmallTest
  public void testMean() {
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getMeanOf(5));
    Assert.assertEquals(2840, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 5000)).getMeanOf(5));
    Assert.assertEquals(4240, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 12000)).getMeanOf(5));
    Assert.assertEquals(4840, new TimesStatistics(getTimesList(200, 2000, 6000, 4000, 12000)).getMeanOf(5));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(3000, 1000, 4000, 2000, 5000)).getMeanOf(5));
    Assert.assertEquals(3200, new TimesStatistics(getTimesList(6000, 3000, 1000, 4000, 2000, 5000)).getMeanOf(5));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(-1, 6000, 3000, 1000, 4000, 2000, 5000)).getMeanOf(5));
  }

  @SmallTest
  public void testSuccessAverage() {
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getSuccessAverageOf(5, 5, false));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 5000)).getSuccessAverageOf(5, 5, false));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 12000)).getSuccessAverageOf(5, 5, false));
    Assert.assertEquals(4000, new TimesStatistics(getTimesList(200, 2000, 6000, 4000, 12000)).getSuccessAverageOf(5, 5, false));

    Assert.assertEquals(-2, new TimesStatistics(getTimesList(1000, -1, 3000, 4000, 5000)).getSuccessAverageOf(5, 5, false));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(-1, 6000, 3000, 1000, 4000, 2000, 5000)).getSuccessAverageOf(5, 5, false));
    Assert.assertEquals(4333, new TimesStatistics(getTimesList(8000, -1, 6000, 3000, 1000, 4000, 2000, 5000)).getSuccessAverageOf(5, 5, false));

    List<Long> times = getTimesList(6000, 8000, -1, 3000, 1000, 4000, 5000);
    Assert.assertEquals(4333, new TimesStatistics(times).getSuccessAverageOf(5, 5, false));
    Assert.assertEquals(4500, new TimesStatistics(times).getSuccessAverageOf(12, 5, true));
    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessAverageOf(12, 5, false));
    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessAverageOf(5, 7, true));
    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessAverageOf(5, 7, false));
    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessAverageOf(12, 7, true));
    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessAverageOf(12, 6, false));
    Assert.assertEquals(4500, new TimesStatistics(times).getSuccessAverageOf(12, 6, true));
    Assert.assertEquals(6000, new TimesStatistics(times).getSuccessAverageOf(3, 3, true));
  }

  @SmallTest
  public void testSuccessMean() {
    // TODO
  }

  @SmallTest
  public void testAccuracy() {
    // TODO
  }

  private List<Long> getTimesList(int... times) {
    List<Long> list = new ArrayList<Long>();
    for (int t : times) {
      list.add((long) t);
    }
    return list;
  }

}
