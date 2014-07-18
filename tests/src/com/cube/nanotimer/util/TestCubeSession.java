package com.cube.nanotimer.util;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

public class TestCubeSession extends AndroidTestCase {

  @SmallTest
  public void testBestWorst() {
    List<Long> sessionTimes = new ArrayList<Long>();
    sessionTimes.add(5l);
    sessionTimes.add(3l);
    sessionTimes.add(10l);
    sessionTimes.add(4l);
    CubeSession cs = new CubeSession(sessionTimes);
    Assert.assertEquals(1, cs.getBestTimeInd(4));
    Assert.assertEquals(2, cs.getWorstTimeInd(4));
    Assert.assertEquals(-1, cs.getBestTimeInd(5));
    Assert.assertEquals(-1, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    sessionTimes.add(7l);
    cs = new CubeSession(sessionTimes);
    Assert.assertEquals(1, cs.getBestTimeInd(4));
    Assert.assertEquals(2, cs.getWorstTimeInd(4));
    Assert.assertEquals(1, cs.getBestTimeInd(5));
    Assert.assertEquals(2, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    cs.addTime(8);
    cs.addTime(9);
    Assert.assertEquals(3, cs.getBestTimeInd(5));
    Assert.assertEquals(2, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    cs.addTime(2);
    cs.addTime(7);
    Assert.assertEquals(7, cs.getBestTimeInd(5));
    Assert.assertEquals(6, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    cs.addTime(5);
    cs.addTime(11);
    Assert.assertEquals(7, cs.getBestTimeInd(5));
    Assert.assertEquals(10, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    cs.addTime(8);
    Assert.assertEquals(7, cs.getBestTimeInd(5));
    Assert.assertEquals(10, cs.getWorstTimeInd(5));
    Assert.assertEquals(7, cs.getBestTimeInd(12));
    Assert.assertEquals(10, cs.getWorstTimeInd(12));

    cs.addTime(3);
    Assert.assertEquals(11, cs.getBestTimeInd(5));
    Assert.assertEquals(9, cs.getWorstTimeInd(5));
    Assert.assertEquals(6, cs.getBestTimeInd(12));
    Assert.assertEquals(9, cs.getWorstTimeInd(12));

    cs.addTime(-1);
    Assert.assertEquals(10, cs.getBestTimeInd(5));
    Assert.assertEquals(11, cs.getWorstTimeInd(5));
    Assert.assertEquals(5, cs.getBestTimeInd(12));
    Assert.assertEquals(11, cs.getWorstTimeInd(12));

    cs.addTime(15);
    Assert.assertEquals(9, cs.getBestTimeInd(5));
    Assert.assertEquals(10, cs.getWorstTimeInd(5));
    Assert.assertEquals(4, cs.getBestTimeInd(12));
    Assert.assertEquals(10, cs.getWorstTimeInd(12));

    // TODO : add some more stuff (special cases etc)
  }

}

