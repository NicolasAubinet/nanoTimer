package com.cube.nanotimer.services;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;
import junit.framework.Assert;

import java.util.List;

public class TestServiceProvider extends AndroidTestCase {

  private ServiceProviderImpl provider;
  private ServiceImpl service;

  private static final String SCRAMBLE = "F B L R U D";
  private CubeType cubeType1;
  private SolveType solveType1;
  private SolveType solveType2;
  private SolveType solveTypeSteps;
  private int timeCpt = 0;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    service = ServiceImpl.getInstance(getContext(), "testDB");
    provider = new ServiceProviderImpl(service.getWritableDatabase());

    List<CubeType> cubeTypes = provider.getCubeTypes(true);
    for (CubeType ct : cubeTypes) {
      if ("3x3x3".equals(ct.getName())) {
        cubeType1 = ct;
        List<SolveType> solveTypes = provider.getSolveTypes(ct);
        solveType1 = solveTypes.get(0);
        solveType2 = solveTypes.get(1);
        for (SolveType st : solveTypes) {
          if ("CFOP".equals(st.getName())) {
            solveTypeSteps = st; // TODO : create this type by calling a provider method
            break;
          }
        }
      }
    }
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    provider.deleteHistory();
  }

  @SmallTest
  public void testBasicAverage() {
    provider.deleteHistory();
    SolveAverages averages = provider.getSolveAverages(solveType1);
    assertAvgEquals(null, null, null, null, null, null, null, null, averages);
    saveTime(600);
    saveTime(3000);
    saveTime(2000);
    averages = saveTime(2000);
    assertAvgEquals(null, null, null, 1900, null, null, null, 600, averages);
    averages = saveTime(2000);
    assertAvgEquals(1920, null, null, 1920, 1920, null, null, 600, averages);
    averages = saveTime(21000);
    assertAvgEquals(6000, null, null, 5100, 1920, null, null, 600, averages);
    saveTimes(2000, 11);
    averages = saveTime(14);
    assertAvgEquals(1602, 1834, null, 2923 /*52614/18*/, 1602, 1834, null, 14, averages);
    averages = saveTime(3000);
    assertAvgEquals(1802, 1917, null, 2927 /*55614/19*/, 1602, 1834, null, 14, averages);
    averages = saveTime(-1); // DNF
    assertAvgEquals(1802, 1917, null, 2927 /*55614/19*/, 1602, 1834, null, 14, averages);
    averages = saveTime(-1); // DNF
    assertAvgEquals(1802, 1917, null, 2927 /*55614/19*/, 1602, 1834, null, 14, averages);
    averages = saveTime(2000, solveType2);
    assertAvgEquals(null, null, null, 2000, null, null, null, 2000, averages);
    averages = saveTime(2000);
    assertAvgEquals(1802, 1917, null, 2880 /*57614/20*/, 1602, 1834, null, 14, averages);
    averages = saveTime(10);
    assertAvgEquals(1404, 1752, null, 2744 /*57624/21*/, 1404, 1752, null, 10, averages);
    averages = saveTime(10, solveType2);
    assertAvgEquals(null, null, null, 1005, null, null, null, 10, averages);

    averages = provider.getSolveAverages(solveType1);
    assertAvgEquals(1404, 1752, null, 2744 /*57624/21*/, 1404, 1752, null, 10, averages);
    averages = provider.getSolveAverages(solveType2);
    assertAvgEquals(null, null, null, 1005, null, null, null, 10, averages);
  }

  @SmallTest
  public void testTimeDeletion() {
    provider.deleteHistory();
    SolveAverages averages = saveTimes(1000, 4);
    assertAvgEquals(null, null, null, 1000, null, null, null, 1000, averages); // 1000(x4)
    averages = saveTime(1000);
    assertAvgEquals(1000, null, null, 1000, 1000, null, null, 1000, averages); // 1000(x5)
    averages = provider.removeTime(getFirstTime());
    assertAvgEquals(null, null, null, 1000, null, null, null, 1000, averages); // 1000(x4)
    saveTime(1000);
    averages = saveTime(500);
    assertAvgEquals(900, null, null, 916, 900, null, null, 500, averages); // 1000(x5), 500(x1)
    averages = provider.removeTime(getLastTime());
    assertAvgEquals(1000, null, null, 1000, 1000, null, null, 1000, averages); // 1000(x5)
    averages = provider.removeTime(getLastTime());
    assertAvgEquals(null, null, null, 1000, null, null, null, 1000, averages); // 1000(x4)
    averages = saveTime(1000);
    assertAvgEquals(1000, null, null, 1000, 1000, null, null, 1000, averages); // 1000(x5)
    averages = saveTimes(300, 6);
    assertAvgEquals(300, null, null, 618, 300, null, null, 300, averages); // 1000(x5), 300(x6)
    averages = saveTime(300);
    assertAvgEquals(300, 591, null, 591, 300, 591, null, 300, averages); // 1000(x5), 300(x7)
    averages = saveTime(200);
    assertAvgEquals(280, 525, null, 561, 280, 525, null, 200, averages); // 1000(x5), 300(x7), 200(x1)

    List<SolveTime> times = provider.getHistory(solveType1);
    averages = provider.removeTime(times.get(1)); // remove 300
    assertAvgEquals(280, 583, null, 583, 280, 583, null, 200, averages); // 1000(x5), 300(x6), 200(x1)

    times = provider.getHistory(solveType1);
    averages = provider.removeTime(times.get(times.size()-4)); // remove 1000
    assertAvgEquals(280, null, null, 545, 280, null, null, 200, averages); // 1000(x4), 300(x6), 200(x1)
    averages = saveTime(150);
    assertAvgEquals(250, 512, null, 512, 250, 512, null, 150, averages); // 1000(x4), 300(x6), 200(x1), 150(x1)

    times = provider.getHistory(solveType1);
    averages = provider.removeTime(times.get(1)); // remove 200
    assertAvgEquals(270, null, null, 540, 270, null, null, 150, averages); // 1000(x4), 300(x6), 150(x1)

    times = provider.getHistory(solveType1);
    for (int i = 4; i < 10; i++) {
      provider.removeTime(times.get(i));
    }
    averages = provider.getSolveAverages(solveType1);
    assertAvgEquals(410, null, null, 410, 410, null, null, 150, averages); // 1000(x1) 300(x3), 150(x1)
    provider.removeTime(getLastTime());
    averages = provider.getSolveAverages(solveType1);
    assertAvgEquals(null, null, null, 475, null, null, null, 300, averages); // 1000(x1) 300(x3)

    times = provider.getHistory(solveType1);
    for (int i = 0; i < 3; i++) {
      provider.removeTime(times.get(i));
    }
    averages = provider.getSolveAverages(solveType1);
    assertAvgEquals(null, null, null, 1000, null, null, null, 1000, averages); // 1000(x1)
    averages = provider.removeTime(getFirstTime());
    assertAvgEquals(null, null, null, null, null, null, null, null, averages);
  }

  @SmallTest
  public void testTimeModification() {
    provider.deleteHistory();
    SolveAverages averages = saveTimes(1000, 5);
    assertAvgEquals(1000, null, null, 1000, 1000, null, null, 1000, averages); // 1000(x5)

    SolveTime st = getFirstTime();
    st.setTime(3000);
    averages = provider.saveTime(st);
    assertAvgEquals(1400, null, null, 1400, 1400, null, null, 1000, averages); // 3000, 1000(x4)
    averages = saveTimes(500, 2);
    assertAvgEquals(800, null, null, 1142, 800, null, null, 500, averages); // 3000, 1000(x4), 500(x2)

    List<SolveTime> times = provider.getHistory(solveType1);
    times.get(3).setTime(-1);
    averages = provider.saveTime(times.get(3));
    assertAvgEquals(800, null, null, 1166, 800, null, null, 500, averages); // 3000, 1000(x2), -1, 1000, 500(x2)
    times.get(4).setTime(3000);
    averages = provider.saveTime(times.get(4));
    assertAvgEquals(1200, null, null, 1500, 1200, null, null, 500, averages); // 3000, 1000, 3000, -1, 1000, 500(x2)
  }

  @SmallTest
  public void testDNF() {
    provider.deleteHistory();
    SolveAverages averages = saveTimes(1000, 5);
    assertAvgEquals(1000, null, null, 1000, 1000, null, null, 1000, averages); // 1000(x5)

    SolveTime time = getLastTime();
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(null, null, null, 1000, null, null, null, 1000, averages); // 1000(x4), -1

    averages = saveTime(6000);
    assertAvgEquals(2000, null, null, 2000, 2000, null, null, 1000, averages); // 1000(x4), -1, 6000

    averages = saveTime(2000);
    assertAvgEquals(2200, null, null, 2000, 2000, null, null, 1000, averages); // 1000(x4), -1, 6000, 2000

    List<SolveTime> times = provider.getHistory(solveType1);
    times.get(3).setTime(-1);
    averages = provider.saveTime(times.get(3));
    assertAvgEquals(2200, null, null, 2200, 2200, null, null, 1000, averages); // 1000(x3), -1(x2), 6000, 2000

    time = getFirstTime();
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(null, null, null, 2500, null, null, null, 1000, averages); // -1, 1000(x2), -1(x2), 6000, 2000

    averages = saveTime(500);
    assertAvgEquals(2100, null, null, 2100, 2100, null, null, 500, averages); // -1, 1000(x2), -1(x2), 6000, 2000, 500

    time = getLastTime();
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(null, null, null, 2500, null, null, null, 1000, averages); // -1, 1000(x2), -1(x2), 6000, 2000, -1

    time = provider.getHistory(solveType1).get(6);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(null, null, null, 3000, null, null, null, 1000, averages); // -1(x2), 1000, -1(x2), 6000, 2000, -1

    time = provider.getHistory(solveType1).get(5);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(null, null, null, 4000, null, null, null, 2000, averages); // -1(x5), 6000, 2000, -1

    time = provider.getHistory(solveType1).get(1);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(null, null, null, 6000, null, null, null, 6000, averages); // -1(x5), 6000, -1(x2)

    time = provider.getHistory(solveType1).get(2);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(null, null, null, null, null, null, null, null, averages); // -1(x8)

    averages = saveTime(10);
    assertAvgEquals(null, null, null, 10, null, null, null, 10, averages); // -1(x8), 10
    averages = saveTime(30);
    assertAvgEquals(null, null, null, 20, null, null, null, 10, averages); // -1(x8), 10, 30
    averages = saveTime(50);
    assertAvgEquals(null, null, null, 30, null, null, null, 10, averages); // -1(x8), 10, 30, 50

    time = getLastTime();
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(null, null, null, 20, null, null, null, 10, averages);  // -1(x8), 10, 30, -1

    time = provider.getHistory(solveType1).get(2);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(null, null, null, 30, null, null, null, 30, averages);  // -1(x9), 30, -1

    saveTime(50);
    averages = saveTime(100);
    assertAvgEquals(null, null, null, 60, null, null, null, 30, averages);  // -1(x9), 30, -1, 50, 100

    time = getLastTime();
    time.setTime(70);
    averages = provider.saveTime(time);
    assertAvgEquals(null, null, null, 50, null, null, null, 30, averages);  // -1(x9), 30, -1, 50, 70

    time = provider.getHistory(solveType1).get(3);
    time.setTime(60);
    averages = provider.saveTime(time);
    assertAvgEquals(null, null, null, 60, null, null, null, 50, averages);  // -1(x9), 60, -1, 50, 70

    // TODO add more tests (specially to test the "Handle DNFs" part of recalculateAverages) (if not already done) (could use code coverage to check this)
  }

  @SmallTest
  public void testLifetimeAverage() {
    provider.deleteHistory();
    saveTime(1000);
    SolveAverages averages = saveTimes(5000, 999);
    assertAvgEquals(5000, 5000, 5000, 4996, 4200, 4666, 4960, 1000, averages); // 1000, 5000(x999)

    averages = saveTime(5000);
    assertAvgEquals(5000, 5000, 5000, 5000, 4200, 4666, 4960, 1000, averages); // 1000, 5000(x1000)

    averages = saveTime(5000);
    assertAvgEquals(5000, 5000, 5000, 5000, 4200, 4666, 4960, 1000, averages); // 1000, 5000(x1001)

    provider.removeTime(getLastTime());
    averages = provider.removeTime(getLastTime());
    assertAvgEquals(5000, 5000, 5000, 4996, 4200, 4666, 4960, 1000, averages); // 1000, 5000(x999)
  }

  @SmallTest
  public void testStepsAverages() {
    provider.deleteHistory();
    SolveAverages averages = saveStepTimes(10, 20, 30, 40);
    assertNull(averages.getStepsAvgOf5());
    assertNull(averages.getStepsAvgOf12());
    assertNull(averages.getStepsAvgOf100());
    assertStepsEquals(averages.getStepsAvgOfLifetime(), 10, 20, 30, 40);

    averages = saveStepTimes(12, 22, 32, 42);
    assertStepsEquals(averages.getStepsAvgOfLifetime(), 11, 21, 31, 41);

    averages = saveStepTimes(8, 18, 28, 38);
    assertStepsEquals(averages.getStepsAvgOfLifetime(), 10, 20, 30, 40);

    averages = saveStepTimes(-1, -1, -1, -1); // to simulate a DNF (not taken into account)
    assertStepsEquals(averages.getStepsAvgOfLifetime(), 10, 20, 30, 40);

    averages = saveStepTimes(30, 40, 50, 60);
    assertStepsEquals(averages.getStepsAvgOfLifetime(), 15, 25, 35, 45);
    assertNull(averages.getStepsAvgOf5());

    averages = saveStepTimes(10, 20, 30, 40);
    assertStepsEquals(averages.getStepsAvgOfLifetime(), 14, 24, 34, 44);
    assertStepsEquals(averages.getStepsAvgOf5(), 14, 24, 34, 44);

    averages = saveStepTimes(50, 30, 10, 20);
    assertStepsEquals(averages.getStepsAvgOfLifetime(), 20, 25, 30, 40);
    assertStepsEquals(averages.getStepsAvgOf5(), 22, 26, 30, 40);
  }

  private SolveAverages saveTimes(long time, int count) {
    SolveAverages averages = null;
    for (int i = 0; i < count; i++) {
      averages = saveTime(time);
    }
    return averages;
  }

  private SolveAverages saveTime(long time) {
    return saveTime(time, solveType1);
  }

  private SolveAverages saveTime(long time, SolveType solveType) {
    SolveTime st = new SolveTime();
    st.setScramble(SCRAMBLE);
    st.setTimestamp(100 + (timeCpt++));
    st.setSolveType(solveType);
    st.setTime(time);
    return provider.saveTime(st);
  }

  private SolveAverages saveStepTimes(long... times) {
    SolveTime st = new SolveTime();
    st.setScramble(SCRAMBLE);
    st.setTimestamp(100 + (timeCpt++));
    st.setSolveType(solveTypeSteps);
    long sum = 0;
    Long[] t = new Long[times.length];
    for (int i = 0; i < times.length; i++) {
      t[i] = times[i];
      sum += t[i];
    }
    st.setTime(sum);
    st.setStepsTimes(t);
    return provider.saveTime(st);
  }

  private SolveTime getFirstTime() {
    List<SolveTime> times = provider.getHistory(solveType1);
    Assert.assertTrue(times != null && times.size() > 0);
    return times.get(times.size() - 1);
  }

  private SolveTime getLastTime() {
    List<SolveTime> times = provider.getHistory(solveType1);
    Assert.assertTrue(times != null && times.size() > 0);
    return times.get(0);
  }

  private void assertAvgEquals(Integer avg5, Integer avg12, Integer avg100, Integer avgLifetime,
                               Integer best5, Integer best12, Integer best100, Integer bestLifetime, SolveAverages averages) {
    Long lavg5 = (avg5 == null) ? null : Long.valueOf(avg5);
    Long lavg12 = (avg12 == null) ? null : Long.valueOf(avg12);
    Long lavg100 = (avg100 == null) ? null : Long.valueOf(avg100);
    Long lavgLifetime = (avgLifetime == null) ? null : Long.valueOf(avgLifetime);
    Long lbest5 = (best5 == null) ? null : Long.valueOf(best5);
    Long lbest12 = (best12 == null) ? null : Long.valueOf(best12);
    Long lbest100 = (best100 == null) ? null : Long.valueOf(best100);
    Long lbestLifetime = (bestLifetime == null) ? null : Long.valueOf(bestLifetime);
    Assert.assertEquals(new SolveAverages(lavg5, lavg12, lavg100, lavgLifetime, lbest5, lbest12, lbest100, lbestLifetime), averages);
  }

  private void assertStepsEquals(List<Long> averages, long... values) {
    assertNotNull(averages);
    assertEquals(values.length, averages.size());
    for (int i = 0; i < averages.size(); i++) {
      assertEquals(values[i], averages.get(i).longValue());
    }
  }

}
