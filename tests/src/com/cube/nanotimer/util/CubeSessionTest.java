package com.cube.nanotimer.util;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

public class CubeSessionTest extends AndroidTestCase {

  @SmallTest
  public void testBestWorst() {
    List<Long> sessionTimes = new ArrayList<Long>();
    sessionTimes.add(5000l);
    sessionTimes.add(3000l);
    sessionTimes.add(10000l);
    sessionTimes.add(4000l);
    CubeSession cs = new CubeSession(sessionTimes);
    Assert.assertEquals(1, cs.getBestTimeInd(4));
    Assert.assertEquals(2, cs.getWorstTimeInd(4));
    Assert.assertEquals(-1, cs.getBestTimeInd(5));
    Assert.assertEquals(-1, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    sessionTimes.add(7000l);
    cs = new CubeSession(sessionTimes);
    Assert.assertEquals(1, cs.getBestTimeInd(4));
    Assert.assertEquals(2, cs.getWorstTimeInd(4));
    Assert.assertEquals(1, cs.getBestTimeInd(5));
    Assert.assertEquals(2, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    cs.addTime(8000);
    cs.addTime(9000); // 9 8 5 3 10 4 7
    Assert.assertEquals(3, cs.getBestTimeInd(5));
    Assert.assertEquals(4, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    cs.addTime(2000);
    cs.addTime(7000); // 7 2 9 8 5 3 10 4 7
    Assert.assertEquals(1, cs.getBestTimeInd(5));
    Assert.assertEquals(2, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    cs.addTime(5000);
    cs.addTime(11000); // 11 5 7 2 9 8 5 3 10 4 7
    Assert.assertEquals(3, cs.getBestTimeInd(5));
    Assert.assertEquals(0, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    cs.addTime(8000); // 8 11 5 7 2 9 8 5 3 10 4 7
    Assert.assertEquals(4, cs.getBestTimeInd(5));
    Assert.assertEquals(1, cs.getWorstTimeInd(5));
    Assert.assertEquals(4, cs.getBestTimeInd(12));
    Assert.assertEquals(1, cs.getWorstTimeInd(12));

    cs.addTime(3000); // 3 8 11 5 7 2 9 8 5 3 10 4 7
    Assert.assertEquals(0, cs.getBestTimeInd(5));
    Assert.assertEquals(2, cs.getWorstTimeInd(5));
    Assert.assertEquals(5, cs.getBestTimeInd(12));
    Assert.assertEquals(2, cs.getWorstTimeInd(12));

    cs.addTime(-1); // -1 3 8 11 5 7 2 9 8 5 3 10 4
    Assert.assertEquals(1, cs.getBestTimeInd(5));
    Assert.assertEquals(0, cs.getWorstTimeInd(5));
    Assert.assertEquals(6, cs.getBestTimeInd(12));
    Assert.assertEquals(0, cs.getWorstTimeInd(12));

    cs.addTime(15000); // 15 -1 3 8 11 5 7 2 9 8 5 3 10
    Assert.assertEquals(2, cs.getBestTimeInd(5));
    Assert.assertEquals(1, cs.getWorstTimeInd(5));
    Assert.assertEquals(7, cs.getBestTimeInd(12));
    Assert.assertEquals(1, cs.getWorstTimeInd(12));

    cs.addTime(-1); // -1 15 -1 3 8 11 5 7 2 9 8 5 3
    Assert.assertEquals(3, cs.getBestTimeInd(5));
    Assert.assertEquals(0, cs.getWorstTimeInd(5));
    Assert.assertEquals(8, cs.getBestTimeInd(12));
    Assert.assertEquals(0, cs.getWorstTimeInd(12));

    cs.setLastAsPlusTwo(); // -1 15 -1 3 8 11 5 7 2 9 8 5 3 (does not change anything because already DNF)
    Assert.assertEquals(3, cs.getBestTimeInd(5));
    Assert.assertEquals(0, cs.getWorstTimeInd(5));
    Assert.assertEquals(8, cs.getBestTimeInd(12));
    Assert.assertEquals(0, cs.getWorstTimeInd(12));

    cs.addTime(3000); // 3 -1 15 -1 3 8 11 5 7 2 9 8 5
    Assert.assertEquals(0, cs.getBestTimeInd(5));
    Assert.assertEquals(1, cs.getWorstTimeInd(5));
    Assert.assertEquals(9, cs.getBestTimeInd(12));
    Assert.assertEquals(1, cs.getWorstTimeInd(12));

    cs.addTime(5000); // 5 3 -1 15 -1 3 8 11 5 7 2 9 8
    Assert.assertEquals(1, cs.getBestTimeInd(5));
    Assert.assertEquals(2, cs.getWorstTimeInd(5));
    Assert.assertEquals(10, cs.getBestTimeInd(12));
    Assert.assertEquals(2, cs.getWorstTimeInd(12));

    cs.addTime(1000); // 1 5 3 -1 15 -1 3 8 11 5 7 2 9
    Assert.assertEquals(0, cs.getBestTimeInd(5));
    Assert.assertEquals(3, cs.getWorstTimeInd(5));
    Assert.assertEquals(0, cs.getBestTimeInd(12));
    Assert.assertEquals(3, cs.getWorstTimeInd(12));

    cs.setLastAsPlusTwo(); // 3 5 3 -1 15 -1 3 8 11 5 7 2 9
    Assert.assertEquals(0, cs.getBestTimeInd(5));
    Assert.assertEquals(3, cs.getWorstTimeInd(5));
    Assert.assertEquals(11, cs.getBestTimeInd(12));
    Assert.assertEquals(3, cs.getWorstTimeInd(12));

    cs.setLastAsDNF(); // -1 5 3 -1 15 -1 3 8 11 5 7 2 9
    Assert.assertEquals(2, cs.getBestTimeInd(5));
    Assert.assertEquals(0, cs.getWorstTimeInd(5));
    Assert.assertEquals(11, cs.getBestTimeInd(12));
    Assert.assertEquals(0, cs.getWorstTimeInd(12));

    cs.clearSession();
    Assert.assertEquals(-1, cs.getBestTimeInd(5));
    Assert.assertEquals(-1, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    cs.addTime(2000);
    cs.addTime(3000);
    cs.addTime(4000);
    cs.addTime(5000); // 5 4 3 2
    Assert.assertEquals(-1, cs.getBestTimeInd(5));
    Assert.assertEquals(-1, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    cs.addTime(3000); // 3 5 4 3 2
    Assert.assertEquals(4, cs.getBestTimeInd(5));
    Assert.assertEquals(1, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    cs.addTime(1000); // 1 3 5 4 3 2
    Assert.assertEquals(0, cs.getBestTimeInd(5));
    Assert.assertEquals(2, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    cs.deleteLast(); // 3 5 4 3 2
    Assert.assertEquals(4, cs.getBestTimeInd(5));
    Assert.assertEquals(1, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    cs.addTime(1000); // 1 3 5 4 3 2
    Assert.assertEquals(0, cs.getBestTimeInd(5));
    Assert.assertEquals(2, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    cs.setLastAsPlusTwo(); // 3 3 5 4 3 2
    Assert.assertEquals(0, cs.getBestTimeInd(5));
    Assert.assertEquals(2, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    cs.setLastAsDNF(); // -1 3 5 4 3 2
    Assert.assertEquals(1, cs.getBestTimeInd(5));
    Assert.assertEquals(0, cs.getWorstTimeInd(5));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));

    cs.addTime(6000); // 6 -1 3 5 4 3 2
    Assert.assertEquals(2, cs.getBestTimeInd(5));
    Assert.assertEquals(1, cs.getWorstTimeInd(5));
    Assert.assertEquals(6, cs.getBestTimeInd(7));
    Assert.assertEquals(1, cs.getWorstTimeInd(7));
    Assert.assertEquals(-1, cs.getBestTimeInd(12));
    Assert.assertEquals(-1, cs.getWorstTimeInd(12));
  }

  @SmallTest
  public void testRAs() {
    List<Long> sessionTimes = new ArrayList<Long>();
    sessionTimes.add(6l);
    sessionTimes.add(4l);
    sessionTimes.add(10l);
    sessionTimes.add(3l);
    sessionTimes.add(5l);
    CubeSession cs = new CubeSession(sessionTimes);
    Assert.assertEquals(5, cs.getAverageOfFive());
    Assert.assertEquals(-2, cs.getAverageOfTwelve()); // N/A
    cs.addTime(8);
    Assert.assertEquals(6, cs.getAverageOfFive());
    Assert.assertEquals(-2, cs.getAverageOfTwelve());
    cs.addTime(-1); // DNF
    Assert.assertEquals(8, cs.getAverageOfFive());
    Assert.assertEquals(-2, cs.getAverageOfTwelve());
    cs.addTime(-1);
    Assert.assertEquals(-1, cs.getAverageOfFive());
    Assert.assertEquals(-2, cs.getAverageOfTwelve());
    cs.addTime(12);
    Assert.assertEquals(-1, cs.getAverageOfFive());
    Assert.assertEquals(-2, cs.getAverageOfTwelve());
    cs.addTime(2);
    Assert.assertEquals(-1, cs.getAverageOfFive());
    Assert.assertEquals(-2, cs.getAverageOfTwelve());
    cs.addTime(4);
    Assert.assertEquals(-1, cs.getAverageOfFive());
    Assert.assertEquals(-2, cs.getAverageOfTwelve());
    cs.addTime(5);
    Assert.assertEquals(7, cs.getAverageOfFive());
    Assert.assertEquals(-1, cs.getAverageOfTwelve()); // DNF
    cs.addTime(9);
    Assert.assertEquals(6, cs.getAverageOfFive());
    Assert.assertEquals(-1, cs.getAverageOfTwelve());
    cs.addTime(10);
    cs.addTime(6);
    cs.addTime(5);
    cs.addTime(10);
    cs.addTime(8);
    cs.addTime(11);
    Assert.assertEquals(8, cs.getAverageOfFive());
    Assert.assertEquals(8, cs.getAverageOfTwelve());
    cs.deleteLast();
    Assert.assertEquals(8, cs.getAverageOfFive());
    Assert.assertEquals(-1, cs.getAverageOfTwelve());
    cs.addTime(11);
    Assert.assertEquals(8, cs.getAverageOfFive());
    Assert.assertEquals(8, cs.getAverageOfTwelve());
    cs.setLastAsDNF();
    Assert.assertEquals(8, cs.getAverageOfFive());
    Assert.assertEquals(-1, cs.getAverageOfTwelve());
    cs.addTime(9);
    Assert.assertEquals(9, cs.getAverageOfFive());
    Assert.assertEquals(7, cs.getAverageOfTwelve()); // 7.??? (not an int)
  }

  @SmallTest
  public void testAverageBug() {
    List<Long> sessionTimes = new ArrayList<Long>();
    CubeSession cs = new CubeSession(sessionTimes);
    cs.addTime(-1);
    cs.addTime(5);
    cs.addTime(15);
    cs.addTime(-1);
    cs.addTime(-1);
    cs.addTime(-1);
    cs.addTime(-1);
    cs.addTime(-1);
    cs.addTime(-1);
    cs.addTime(-1);
    cs.addTime(-1);
    cs.addTime(-1);
    Assert.assertEquals(-1, cs.getAverageOfFive());
    Assert.assertEquals(-1, cs.getAverageOfTwelve());
  }

}

