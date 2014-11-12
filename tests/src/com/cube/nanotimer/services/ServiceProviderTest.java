package com.cube.nanotimer.services;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveTimeAverages;
import com.cube.nanotimer.vo.SolveType;
import junit.framework.Assert;

import java.util.List;

public class ServiceProviderTest extends AndroidTestCase {

  private ServiceProviderImpl provider;
  private ServiceImpl service;

  private static final String SCRAMBLE = "F B L R U D";
  private CubeType cubeType1;
  private SolveType solveType1;
  private SolveType solveType2;
  private SolveType solveTypeSteps;
  private SolveType solveTypeBlind;
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
          if ("CFOP steps".equals(st.getName())) {
            solveTypeSteps = st;
          } else if ("BLD".equals(st.getName())) {
            solveTypeBlind = st;
          }
        }
      }
    }
    if (solveTypeBlind == null) {
      provider.addSolveType(new SolveType("BLD", true, cubeType1.getId()));
      for (SolveType st : provider.getSolveTypes(cubeType1)) {
        if ("BLD".equals(st.getName())) {
          solveTypeBlind = st;
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
    assertAvgEquals(null, null, null, null, null, null, null, null, null, null, averages);
    saveTime(600);
    saveTime(3000);
    saveTime(2000);
    averages = saveTime(2000);
    assertAvgEquals(null, null, null, null, 1900, null, null, null, null, 600, averages);
    averages = saveTime(2000);
    assertAvgEquals(2000, null, null, null, 1920, 2000, null, null, null, 600, averages);
    averages = saveTime(21000);
    assertAvgEquals(2333, null, null, null, 5100, 2000, null, null, null, 600, averages);
    saveTimes(2000, 11);
    averages = saveTime(14);
    assertAvgEquals(2000, 2000, null, null, 2923 /*52614/18*/, 2000, 2000, null, null, 14, averages);
    averages = saveTime(3000);
    assertAvgEquals(2000, 2000, null, null, 2927 /*55614/19*/, 2000, 2000, null, null, 14, averages);
    averages = saveTime(-1); // DNF
    assertAvgEquals(2333, 2100, null, null, 2927 /*55614/19*/, 2000, 2000, null, null, 14, averages);
    averages = saveTime(1000, solveType2);
    assertAvgEquals(null, null, null, null, 1000, null, null, null, null, 1000, averages);
    averages = saveTime(2000);
    assertAvgEquals(2333, 2100, null, null, 2880 /*57614/20*/, 2000, 2000, null, null, 14, averages);
    averages = saveTime(10);
    assertAvgEquals(1671, 1901, null, null, 2744 /*57624/21*/, 1671, 1901, null, null, 10, averages);

    averages = provider.getSolveAverages(solveType1);
    assertAvgEquals(1671, 1901, null, null, 2744 /*57624/21*/, 1671, 1901, null, null, 10, averages);

    averages = saveTime(10, solveType2);
    assertAvgEquals(null, null, null, null, 505, null, null, null, null, 10, averages);
    averages = saveTime(-1); // DNF
    assertAvgEquals(-1, -1, null, null, 2744 /*57624/21*/, 1671, 1901, null, null, 10, averages);

    averages = provider.getSolveAverages(solveType2);
    assertAvgEquals(null, null, null, null, 505, null, null, null, null, 10, averages);
  }

  @SmallTest
  public void testTimeDeletion() {
    provider.deleteHistory();
    SolveAverages averages = saveTimes(1000, 4);
    assertAvgEquals(null, null, null, null, 1000, null, null, null, null, 1000, averages); // 1000(x4)
    averages = saveTime(1000);
    assertAvgEquals(1000, null, null, null, 1000, 1000, null, null, null, 1000, averages); // 1000(x5)
    averages = provider.deleteTime(getFirstTime());
    assertAvgEquals(null, null, null, null, 1000, null, null, null, null, 1000, averages); // 1000(x4)
    saveTime(1000);
    averages = saveTime(500);
    assertAvgEquals(1000, null, null, null, 916, 1000, null, null, null, 500, averages); // 1000(x5), 500(x1)
    averages = provider.deleteTime(getLastTime());
    assertAvgEquals(1000, null, null, null, 1000, 1000, null, null, null, 1000, averages); // 1000(x5)
    averages = provider.deleteTime(getLastTime());
    assertAvgEquals(null, null, null, null, 1000, null, null, null, null, 1000, averages); // 1000(x4)
    averages = saveTime(1000);
    assertAvgEquals(1000, null, null, null, 1000, 1000, null, null, null, 1000, averages); // 1000(x5)
    averages = saveTimes(300, 6);
    assertAvgEquals(300, null, null, null, 618, 300, null, null, null, 300, averages); // 1000(x5), 300(x6)
    averages = saveTime(300);
    assertAvgEquals(300, 580, null, null, 591, 300, 580, null, null, 300, averages); // 1000(x5), 300(x7)
    averages = saveTime(200);
    assertAvgEquals(300, 510, null, null, 561, 300, 510, null, null, 200, averages); // 1000(x5), 300(x7), 200(x1)

    List<SolveTime> times = provider.getHistory(solveType1).getSolveTimes();
    averages = provider.deleteTime(times.get(1)); // remove 300
    assertAvgEquals(300, 580, null, null, 583, 300, 580, null, null, 200, averages); // 1000(x5), 300(x6), 200(x1)

    times = provider.getHistory(solveType1).getSolveTimes();
    averages = provider.deleteTime(times.get(times.size() - 4)); // remove 1000
    assertAvgEquals(300, null, null, null, 545, 300, null, null, null, 200, averages); // 1000(x4), 300(x6), 200(x1)
    averages = saveTime(150);
    assertAvgEquals(266, 500, null, null, 512, 266, 500, null, null, 150, averages); // 1000(x4), 300(x6), 200(x1), 150(x1)

    times = provider.getHistory(solveType1).getSolveTimes();
    averages = provider.deleteTime(times.get(1)); // remove 200
    assertAvgEquals(300, null, null, null, 540, 300, null, null, null, 150, averages); // 1000(x4), 300(x6), 150(x1)

    times = provider.getHistory(solveType1).getSolveTimes();
    for (int i = 4; i < 10; i++) {
      provider.deleteTime(times.get(i));
    }
    averages = provider.getSolveAverages(solveType1);
    assertAvgEquals(300, null, null, null, 410, 300, null, null, null, 150, averages); // 1000(x1) 300(x3), 150(x1)
    provider.deleteTime(getLastTime());
    averages = provider.getSolveAverages(solveType1);
    assertAvgEquals(null, null, null, null, 475, null, null, null, null, 300, averages); // 1000(x1) 300(x3)

    times = provider.getHistory(solveType1).getSolveTimes();
    for (int i = 0; i < 3; i++) {
      provider.deleteTime(times.get(i));
    }
    averages = provider.getSolveAverages(solveType1);
    assertAvgEquals(null, null, null, null, 1000, null, null, null, null, 1000, averages); // 1000(x1)
    averages = provider.deleteTime(getFirstTime());
    assertAvgEquals(null, null, null, null, null, null, null, null, null, null, averages);

    saveTime(150);
    saveTime(250);
    saveTime(350);
    saveTime(450);
    averages = saveTime(550);
    assertAvgEquals(350, null, null, null, 350, 350, null, null, null, 150, averages); // 150, 250, 350, 450, 550
    averages = provider.deleteTime(getLastTime());
    assertAvgEquals(null, null, null, null, 300, null, null, null, null, 150, averages); // 150, 250, 350, 450
    averages = saveTime(700);
    assertAvgEquals(350, null, null, null, 380, 350, null, null, null, 150, averages); // 150, 250, 350, 450, 700
    averages = saveTime(800);
    assertAvgEquals(500, null, null, null, 450, 350, null, null, null, 150, averages); // 150, 250, 350, 450, 700, 800
    averages = saveTime(600);
    assertAvgEquals(583, null, null, null, 471, 350, null, null, null, 150, averages); // 150, 250, 350, 450, 700, 800, 600
    times = provider.getHistory(solveType1).getSolveTimes();
    averages = provider.deleteTime(times.get(2)); // remove 700
    assertAvgEquals(466, null, null, null, 433, 350, null, null, null, 150, averages); // 150, 250, 350, 450, 800, 600
    times = provider.getHistory(solveType1).getSolveTimes();
    averages = provider.deleteTime(times.get(3)); // remove 350
    assertAvgEquals(433, null, null, null, 450, 433, null, null, null, 150, averages); // 150, 250, 450, 800, 600
  }

  @SmallTest
  public void testTimeModification() {
    provider.deleteHistory();
    SolveAverages averages = saveTimes(1000, 5);
    assertAvgEquals(1000, null, null, null, 1000, 1000, null, null, null, 1000, averages); // 1000(x5)

    SolveTime st = getFirstTime();
    st.setTime(3000);
    averages = provider.saveTime(st);
    assertAvgEquals(1000, null, null, null, 1400, 1000, null, null, null, 1000, averages); // 3000, 1000(x4)
    averages = saveTimes(500, 2);
    assertAvgEquals(833, null, null, null, 1142, 833, null, null, null, 500, averages); // 3000, 1000(x4), 500(x2)

    List<SolveTime> times = provider.getHistory(solveType1).getSolveTimes();
    times.get(3).setTime(-1);
    averages = provider.saveTime(times.get(3));
    assertAvgEquals(833, null, null, null, 1166, 833, null, null, null, 500, averages); // 3000, 1000(x2), -1, 1000, 500(x2)
    times.get(4).setTime(3000);
    averages = provider.saveTime(times.get(4));
    assertAvgEquals(1500, null, null, null, 1500, 1500, null, null, null, 500, averages); // 3000, 1000, 3000, -1, 1000, 500(x2)
    times.get(2).setTime(3000);
    averages = provider.saveTime(times.get(2));
    assertAvgEquals(2166, null, null, null, 1833, 2166, null, null, null, 500, averages); // 3000, 1000, 3000, -1, 3000, 500(x2)

    saveTime(4000);
    times = provider.getHistory(solveType1).getSolveTimes();
    times.get(7).setTime(1000);
    provider.saveTime(times.get(7));
    times.get(5).setTime(1000);
    averages = provider.saveTime(times.get(5));
    assertAvgEquals(2500, null, null, null, 1571, 1500, null, null, null, 500, averages); // 1000(x3), -1, 3000, 500(x2), 4000
    times.get(1).setTime(-1);
    averages = provider.saveTime(times.get(1));
    assertAvgEquals(-1, null, null, null, 1750, 1666, null, null, null, 500, averages); // 1000(x3), -1, 3000, 500, -1, 4000
    times.get(2).setTime(2500);
    averages = provider.saveTime(times.get(2));
    assertAvgEquals(-1, null, null, null, 2083, 1666, null, null, null, 1000, averages); // 1000(x3), -1, 3000, 2500, -1, 4000
    times.get(7).setTime(4000);
    averages = provider.saveTime(times.get(7));
    assertAvgEquals(-1, null, null, null, 2583, 2166, null, null, null, 1000, averages); // 4000, 1000(x2), -1, 3000, 2500, -1, 4000
  }

  @SmallTest
  public void testDNF() {
    provider.deleteHistory();
    SolveAverages averages = saveTimes(1000, 5);
    assertAvgEquals(1000, null, null, null, 1000, 1000, null, null, null, 1000, averages); // 1000(x5)

    SolveTime time = getLastTime();
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(1000, null, null, null, 1000, 1000, null, null, null, 1000, averages); // 1000(x4), -1

    averages = saveTime(6000);
    assertAvgEquals(2666, null, null, null, 2000, 1000, null, null, null, 1000, averages); // 1000(x4), -1, 6000

    averages = saveTime(2000);
    assertAvgEquals(3000, null, null, null, 2000, 1000, null, null, null, 1000, averages); // 1000(x4), -1, 6000, 2000

    List<SolveTime> times = provider.getHistory(solveType1).getSolveTimes();
    times.get(3).setTime(-1);
    averages = provider.saveTime(times.get(3));
    assertAvgEquals(-1, null, null, null, 2200, null, null, null, null, 1000, averages); // 1000(x3), -1(x2), 6000, 2000

    time = getFirstTime();
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(-1, null, null, null, 2500, null, null, null, null, 1000, averages); // -1, 1000(x2), -1(x2), 6000, 2000

    averages = saveTime(500);
    assertAvgEquals(-1, null, null, null, 2100, null, null, null, null, 500, averages); // -1, 1000(x2), -1(x2), 6000, 2000, 500

    time = getLastTime();
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(-1, null, null, null, 2500, null, null, null, null, 1000, averages); // -1, 1000(x2), -1(x2), 6000, 2000, -1

    time = provider.getHistory(solveType1).getSolveTimes().get(6);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(-1, null, null, null, 3000, null, null, null, null, 1000, averages); // -1(x2), 1000, -1(x2), 6000, 2000, -1

    time = provider.getHistory(solveType1).getSolveTimes().get(5);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(-1, null, null, null, 4000, null, null, null, null, 2000, averages); // -1(x5), 6000, 2000, -1

    time = provider.getHistory(solveType1).getSolveTimes().get(1);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(-1, null, null, null, 6000, null, null, null, null, 6000, averages); // -1(x5), 6000, -1(x2)

    time = provider.getHistory(solveType1).getSolveTimes().get(2);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(-1, null, null, null, null, null, null, null, null, null, averages); // -1(x8)

    averages = saveTime(10);
    assertAvgEquals(-1, null, null, null, 10, null, null, null, null, 10, averages); // -1(x8), 10
    averages = saveTime(30);
    assertAvgEquals(-1, null, null, null, 20, null, null, null, null, 10, averages); // -1(x8), 10, 30
    averages = saveTime(50);
    assertAvgEquals(-1, null, null, null, 30, null, null, null, null, 10, averages); // -1(x8), 10, 30, 50
    averages = saveTime(70);
    assertAvgEquals(50, -1, null, null, 40, 50, null, null, null, 10, averages); // -1(x8), 10, 30, 50, 70
    provider.deleteTime(getLastTime());

    time = getLastTime();
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(-1, null, null, null, 20, null, null, null, null, 10, averages);  // -1(x8), 10, 30, -1

    time = provider.getHistory(solveType1).getSolveTimes().get(2);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(-1, null, null, null, 30, null, null, null, null, 30, averages);  // -1(x9), 30, -1

    saveTime(50);
    averages = saveTime(100);
    assertAvgEquals(-1, -1, null, null, 60, null, null, null, null, 30, averages);  // -1(x9), 30, -1, 50, 100

    time = getLastTime();
    time.setTime(70);
    averages = provider.saveTime(time);
    assertAvgEquals(-1, -1, null, null, 50, null, null, null, null, 30, averages);  // -1(x9), 30, -1, 50, 70

    time = provider.getHistory(solveType1).getSolveTimes().get(3);
    time.setTime(60);
    averages = provider.saveTime(time);
    assertAvgEquals(-1, -1, null, null, 60, null, null, null, null, 50, averages);  // -1(x9), 60, -1, 50, 70
    averages = saveTime(80);
    assertAvgEquals(70, -1, null, null, 65, 70, null, null, null, 50, averages);  // -1(x9), 60, -1, 50, 70, 80

    provider.deleteHistory();
    averages = saveTimes(1000, 102);
    assertAvgEquals(1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, averages);
    times = getWholeHistory();
    time = times.get(10);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, averages);
    time = times.get(101);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, averages);
    time = times.get(98);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(1000, 1000, 1000, -1, 1000, 1000, 1000, 1000, null, 1000, averages);

    // Test cache clearing
    provider.deleteHistory(solveType1);
    averages = saveTimes(1000, 102);
    assertAvgEquals(1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, averages);
    times = getWholeHistory();
    time = times.get(10);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, averages);
    time = times.get(101);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, averages);
  }

  @SmallTest
  public void testLifetimeAverage() {
    provider.deleteHistory();
    saveTime(1000);
    saveTime(1000);
    SolveAverages averages = saveTimes(5000, 999);
    assertAvgEquals(5000, 5000, 5000, 5000, 4996, 3666, 4600, 4916, 4959, 1000, averages); // 1000(x2), 5000(x999)

    averages = saveTime(5000);
    assertAvgEquals(5000, 5000, 5000, 5000, 5000, 3666, 4600, 4916, 4959, 1000, averages); // 1000(x2), 5000(x1000)

    averages = saveTime(5000);
    assertAvgEquals(5000, 5000, 5000, 5000, 5000, 3666, 4600, 4916, 4959, 1000, averages); // 1000(x2), 5000(x1001)

    provider.deleteTime(getLastTime());
    averages = provider.deleteTime(getLastTime());
    assertAvgEquals(5000, 5000, 5000, 5000, 4996, 3666, 4600, 4916, 4959, 1000, averages); // 1000(x2), 5000(x999)
  }

  @SmallTest
  public void testAvg50AndRecalculation() {
    provider.deleteHistory();
    SolveAverages averages = saveTimes(5000, 25);
    assertAvgEquals(5000, 5000, null, null, 5000, 5000, 5000, null, null, 5000, averages); // 5000(x25)
    averages = saveTimes(10000, 24);
    assertAvgEquals(10000, 10000, null, null, 7448, 5000, 5000, null, null, 5000, averages); // 5000(x25), 10000(x24)
    averages = saveTime(10000);
    assertAvgEquals(10000, 10000, 7500, null, 7500, 5000, 5000, 7500, null, 5000, averages); // 5000(x25), 10000(x25)
    averages = saveTime(10000);
    assertAvgEquals(10000, 10000, 7604, null, 7549, 5000, 5000, 7500, null, 5000, averages); // 5000(x25), 10000(x26)

    SolveTime time = getLastTime();
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(10000, 10000, 7604, null, 7500, 5000, 5000, 7500, null, 5000, averages); // 5000(x25), 10000(x25), DNF

    time = provider.getHistory(solveType1).getSolveTimes().get(5);
    time.setTime(12000);
    averages = provider.saveTime(time);
    assertAvgEquals(10000, 10200, 7645, null, 7540, 5000, 5000, 7500, null, 5000, averages); // 5000(x25), 10000(x20), 12000, 10000(x4), DNF

    List<SolveTime> times = getWholeHistory();
    time = times.get(times.size() - 5);
    time.setTime(10000);
    provider.saveTime(time);
    time = times.get(times.size() - 10);
    time.setTime(10000);
    provider.saveTime(time);
    time = times.get(times.size() - 15);
    time.setTime(10000);
    provider.saveTime(time);
    time = times.get(times.size() - 20);
    time.setTime(10000);
    provider.saveTime(time);
    time = times.get(times.size() - 25);
    time.setTime(10000);
    averages = provider.saveTime(time);
    assertAvgEquals(10000, 10200, 8166, null, 8040, 5000, 5500, 8020, null, 5000, averages); // (5000(x4), 10000)(x5), 10000(x20), 12000, 10000(x4), DNF
  }

  @SmallTest
  public void testSolveTimeAveragesAndRecalculation() {
    provider.deleteHistory();
    saveTimes(1000, 5);
    List<SolveTime> times = provider.getHistory(solveType1).getSolveTimes();
    SolveTime time = times.get(0);
    time.setTime(3000);
    SolveAverages averages = provider.saveTime(time);
    assertAvgEquals(1000, null, null, null, 1400, 1000, null, null, null, 1000, averages); // 1000(x4), 3000
    assertSolveTimeAveragesEquals(1000, null, null, null, provider.getSolveTimeAverages(time));

    times = provider.getHistory(solveType1).getSolveTimes();
    time = times.get(3);
    time.setTime(800);
    averages = provider.saveTime(time);
    assertAvgEquals(1000, null, null, null, 1360, 1000, null, null, null, 800, averages); // 1000, 800, 1000(x2), 3000
    assertSolveTimeAveragesEquals(null, null, null, null, provider.getSolveTimeAverages(time));

    times = provider.getHistory(solveType1).getSolveTimes();
    time = times.get(0);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(1000, null, null, null, 950, 1000, null, null, null, 800, averages); // 1000, 800, 1000(x2), -1
    assertSolveTimeAveragesEquals(1000, null, null, null, provider.getSolveTimeAverages(time));

    saveTimes(1000, 11);

    times = provider.getHistory(solveType1).getSolveTimes();
    time = times.get(0);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(1000, -1, null, null, 985, 1000, 1000, null, null, 800, averages); // 1000, 800, 1000(x2), -1, 1000(x10), -1
    assertSolveTimeAveragesEquals(1000, -1, null, null, provider.getSolveTimeAverages(times.get(0)));
    assertSolveTimeAveragesEquals(1000, 1000, null, null, provider.getSolveTimeAverages(times.get(1)));
    assertSolveTimeAveragesEquals(1000, 1000, null, null, provider.getSolveTimeAverages(times.get(2)));
    assertSolveTimeAveragesEquals(1000, 1000, null, null, provider.getSolveTimeAverages(times.get(3)));
    assertSolveTimeAveragesEquals(1000, 1000, null, null, provider.getSolveTimeAverages(times.get(4)));
    assertSolveTimeAveragesEquals(1000, null, null, null, provider.getSolveTimeAverages(times.get(5)));

    provider.deleteTime(times.get(0)); // 1000, 800, 1000(x2), -1, 1000(x10)

    times = provider.getHistory(solveType1).getSolveTimes();
    time = times.get(2);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(1000, -1, null, null, 984, 1000, 1000, null, null, 800, averages); // 1000, 800, 1000(x2), -1, 1000(x7), -1, 1000(x2)
    assertSolveTimeAveragesEquals(1000, -1, null, null, provider.getSolveTimeAverages(times.get(0)));
    assertSolveTimeAveragesEquals(1000, -1, null, null, provider.getSolveTimeAverages(times.get(1)));
    assertSolveTimeAveragesEquals(1000, -1, null, null, provider.getSolveTimeAverages(times.get(2)));
    assertSolveTimeAveragesEquals(1000, 1000, null, null, provider.getSolveTimeAverages(times.get(3)));
    assertSolveTimeAveragesEquals(1000, null, null, null, provider.getSolveTimeAverages(times.get(4)));

    provider.deleteHistory();

    averages = saveTimes(1000, 205);
    assertAvgEquals(1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, averages);
    times = getWholeHistory();
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(0)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(50)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(95)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(99)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(104)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(105)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, null, provider.getSolveTimeAverages(times.get(106)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, null, provider.getSolveTimeAverages(times.get(107)));

    time = times.get(110);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, averages);
    times = getWholeHistory();
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(0)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(50)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(95)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(99)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(103)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(104)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(105)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, null, provider.getSolveTimeAverages(times.get(106)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, null, provider.getSolveTimeAverages(times.get(107)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, null, provider.getSolveTimeAverages(times.get(108)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(90)));

    time = times.get(109);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, averages);
    times = getWholeHistory();
    assertSolveTimeAveragesEquals(1000, 1000, 1000, -1, provider.getSolveTimeAverages(times.get(59)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, -1, provider.getSolveTimeAverages(times.get(60)));
    assertSolveTimeAveragesEquals(1000, 1000, -1, -1, provider.getSolveTimeAverages(times.get(61)));
    assertSolveTimeAveragesEquals(1000, 1000, -1, -1, provider.getSolveTimeAverages(times.get(90)));
    assertSolveTimeAveragesEquals(1000, 1000, -1, -1, provider.getSolveTimeAverages(times.get(98)));
    assertSolveTimeAveragesEquals(1000, -1, -1, -1, provider.getSolveTimeAverages(times.get(99)));
    assertSolveTimeAveragesEquals(1000, -1, -1, -1, provider.getSolveTimeAverages(times.get(103)));
    assertSolveTimeAveragesEquals(1000, -1, -1, -1, provider.getSolveTimeAverages(times.get(104)));
    assertSolveTimeAveragesEquals(1000, -1, -1, -1, provider.getSolveTimeAverages(times.get(105)));
    assertSolveTimeAveragesEquals(-1, -1, -1, null, provider.getSolveTimeAverages(times.get(106)));
    assertSolveTimeAveragesEquals(-1, -1, -1, null, provider.getSolveTimeAverages(times.get(107)));
    assertSolveTimeAveragesEquals(-1, -1, -1, null, provider.getSolveTimeAverages(times.get(108)));
    assertSolveTimeAveragesEquals(-1, -1, -1, null, provider.getSolveTimeAverages(times.get(109)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, null, provider.getSolveTimeAverages(times.get(110)));

    time = times.get(204);
    time.setTime(-1);
    averages = provider.saveTime(time);
    assertAvgEquals(1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, averages);
    times = getWholeHistory();
    assertSolveTimeAveragesEquals(1000, 1000, -1, -1, provider.getSolveTimeAverages(times.get(90)));
    assertSolveTimeAveragesEquals(1000, -1, -1, -1, provider.getSolveTimeAverages(times.get(102)));
    assertSolveTimeAveragesEquals(1000, -1, -1, -1, provider.getSolveTimeAverages(times.get(103)));
    assertSolveTimeAveragesEquals(1000, -1, -1, -1, provider.getSolveTimeAverages(times.get(104)));
    assertSolveTimeAveragesEquals(1000, -1, -1, -1, provider.getSolveTimeAverages(times.get(105)));
    assertSolveTimeAveragesEquals(-1, -1, -1, null, provider.getSolveTimeAverages(times.get(106)));
    assertSolveTimeAveragesEquals(null, null, null, null, provider.getSolveTimeAverages(times.get(204))); // oldest one, now DNF
    assertSolveTimeAveragesEquals(null, null, null, null, provider.getSolveTimeAverages(times.get(203)));
    assertSolveTimeAveragesEquals(null, null, null, null, provider.getSolveTimeAverages(times.get(201)));
    assertSolveTimeAveragesEquals(1000, null, null, null, provider.getSolveTimeAverages(times.get(200)));
    assertSolveTimeAveragesEquals(1000, null, null, null, provider.getSolveTimeAverages(times.get(199)));
    assertSolveTimeAveragesEquals(1000, 1000, null, null, provider.getSolveTimeAverages(times.get(193)));
    assertSolveTimeAveragesEquals(1000, 1000, null, null, provider.getSolveTimeAverages(times.get(192)));
    assertSolveTimeAveragesEquals(1000, 1000, null, null, provider.getSolveTimeAverages(times.get(191)));
    assertSolveTimeAveragesEquals(1000, 1000, null, null, provider.getSolveTimeAverages(times.get(156)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, null, provider.getSolveTimeAverages(times.get(155)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, null, provider.getSolveTimeAverages(times.get(154)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, null, provider.getSolveTimeAverages(times.get(153)));

    time = times.get(101);
    time.setTime(3000); // +2
    averages = provider.saveTime(time);
    assertAvgEquals(1000, 1000, 1000, 1000, 1009, 1000, 1000, 1000, 1000, 1000, averages);
    times = getWholeHistory();
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(0)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(1)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1000, provider.getSolveTimeAverages(times.get(2)));
    assertSolveTimeAveragesEquals(1000, 1000, 1000, 1020, provider.getSolveTimeAverages(times.get(10)));
    assertSolveTimeAveragesEquals(1000, -1, -1, -1, provider.getSolveTimeAverages(times.get(100)));
    assertSolveTimeAveragesEquals(1000, -1, -1, -1, provider.getSolveTimeAverages(times.get(101)));
    assertSolveTimeAveragesEquals(1000, -1, -1, -1, provider.getSolveTimeAverages(times.get(102)));
  }

  @SmallTest
  public void testStepsAverages() {
    provider.deleteHistory();
    SolveAverages averages = saveStepTimes(10, 20, 30, 40);
    assertNull(averages.getStepsAvgOf5());
    assertNull(averages.getStepsAvgOf12());
    assertNull(averages.getStepsAvgOf50());
    assertNull(averages.getStepsAvgOf100());
    assertStepsEquals(averages.getStepsAvgOfLifetime(), 10, 20, 30, 40);

    averages = saveStepTimes(12, 22, 28, 42);
    assertStepsEquals(averages.getStepsAvgOfLifetime(), 11, 21, 29, 41);

    averages = saveStepTimes(8, 18, 32, 38);
    assertStepsEquals(averages.getStepsAvgOfLifetime(), 10, 20, 30, 40);

    averages = saveStepTimes(-1, -1, -1, -1); // to simulate a DNF (not taken into account)
    assertStepsEquals(averages.getStepsAvgOfLifetime(), 10, 20, 30, 40);
    assertNull(averages.getStepsAvgOf5());

    averages = saveStepTimes(30, 40, 50, 60);
    assertStepsEquals(averages.getStepsAvgOfLifetime(), 15, 25, 35, 45);
    assertNull(averages.getStepsAvgOf5());

    averages = saveStepTimes(10, 20, 30, 40);
    assertStepsEquals(averages.getStepsAvgOfLifetime(), 14, 24, 34, 44);
    assertStepsEquals(averages.getStepsAvgOf5(), 10, 20, 30, 40);

    averages = saveStepTimes(50, 30, 10, 20);
    assertStepsEquals(averages.getStepsAvgOfLifetime(), 20, 25, 30, 40);
    assertStepsEquals(averages.getStepsAvgOf5(), 17, 24, 30, 40);

    averages = saveStepTimes(60, 40, 20, 30);
    assertStepsEquals(averages.getStepsAvgOfLifetime(), 25, 27, 28, 38);
    assertStepsEquals(averages.getStepsAvgOf5(), 30, 30, 27, 36);

    for (int i = 0; i < 96; i++) {
      averages = saveStepTimes(40, 45, 35, 50);
    }
    assertStepsEquals(averages.getStepsAvgOfLifetime(), 39, 43, 34, 49);
    assertStepsEquals(averages.getStepsAvgOf5(), 40, 45, 35, 50);
    assertStepsEquals(averages.getStepsAvgOf12(), 40, 45, 35, 50);
    assertStepsEquals(averages.getStepsAvgOf50(), 40, 45, 35, 50);
    assertStepsEquals(averages.getStepsAvgOf100(), 40, 44, 34, 49);
  }

  @SmallTest
  public void testBlindMeanOf3() {
    provider.deleteHistory();

    saveTime(1000, solveTypeBlind);
    SolveAverages averages = saveTime(2000, solveTypeBlind);
    assertEquals(null, averages.getMeanOf3());
    averages = saveTime(2500, solveTypeBlind);
    assertEquals(Long.valueOf(1833), averages.getMeanOf3());
    averages = saveTime(2700, solveTypeBlind);
    assertEquals(Long.valueOf(2400), averages.getMeanOf3());
    averages = saveTime(1200, solveTypeBlind);
    assertEquals(Long.valueOf(2133), averages.getMeanOf3());
    averages = saveTime(-1, solveTypeBlind);
    assertEquals(Long.valueOf(-1), averages.getMeanOf3());
    averages = saveTime(1400, solveTypeBlind);
    assertEquals(Long.valueOf(-1), averages.getMeanOf3());
    averages = saveTime(1500, solveTypeBlind);
    assertEquals(Long.valueOf(-1), averages.getMeanOf3());
    averages = saveTime(1300, solveTypeBlind);
    assertEquals(Long.valueOf(1400), averages.getMeanOf3());
    averages = saveTime(1200, solveTypeBlind);
    assertEquals(Long.valueOf(1333), averages.getMeanOf3());
    averages = saveTime(2200, solveTypeBlind);
    assertEquals(Long.valueOf(1566), averages.getMeanOf3());
    averages = saveTime(2600, solveTypeBlind);
    assertEquals(Long.valueOf(2000), averages.getMeanOf3());
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
    List<SolveTime> times = provider.getHistory(solveType1).getSolveTimes();
    Assert.assertTrue(times != null && times.size() > 0);
    return times.get(times.size() - 1);
  }

  private SolveTime getLastTime() {
    List<SolveTime> times = provider.getHistory(solveType1).getSolveTimes();
    Assert.assertTrue(times != null && times.size() > 0);
    return times.get(0);
  }

  private List<SolveTime> getWholeHistory() {
    return provider.getHistoryTimes(solveType1, System.currentTimeMillis(), null);
  }

  private void assertAvgEquals(Integer avg5, Integer avg12, Integer avg50, Integer avg100, Integer avgLifetime,
                               Integer best5, Integer best12, Integer best50, Integer best100, Integer bestLifetime, SolveAverages averages) {
    Long lavg5 = (avg5 == null) ? null : Long.valueOf(avg5);
    Long lavg12 = (avg12 == null) ? null : Long.valueOf(avg12);
    Long lavg50 = (avg50 == null) ? null : Long.valueOf(avg50);
    Long lavg100 = (avg100 == null) ? null : Long.valueOf(avg100);
    Long lavgLifetime = (avgLifetime == null) ? null : Long.valueOf(avgLifetime);
    Long lbest5 = (best5 == null) ? null : Long.valueOf(best5);
    Long lbest12 = (best12 == null) ? null : Long.valueOf(best12);
    Long lbest50 = (best50 == null) ? null : Long.valueOf(best50);
    Long lbest100 = (best100 == null) ? null : Long.valueOf(best100);
    Long lbestLifetime = (bestLifetime == null) ? null : Long.valueOf(bestLifetime);
    Assert.assertEquals(new SolveAverages(lavg5, lavg12, lavg50, lavg100, lavgLifetime, lbest5, lbest12, lbest50, lbest100, lbestLifetime), averages);
  }

  private void assertSolveTimeAveragesEquals(Integer avg5, Integer avg12, Integer avg50, Integer avg100, SolveTimeAverages sta) {
    Assert.assertNotNull(sta);
    Assert.assertEquals((avg5 == null) ? null : Long.valueOf(avg5), sta.getAvgOf5());
    Assert.assertEquals((avg12 == null) ? null : Long.valueOf(avg12), sta.getAvgOf12());
    Assert.assertEquals((avg50 == null) ? null : Long.valueOf(avg50), sta.getAvgOf50());
    Assert.assertEquals((avg100 == null) ? null : Long.valueOf(avg100), sta.getAvgOf100());
  }

  private void assertStepsEquals(List<Long> averages, long... values) {
    assertNotNull(averages);
    assertEquals(values.length, averages.size());
    for (int i = 0; i < averages.size(); i++) {
      assertEquals(values[i], averages.get(i).longValue());
    }
  }

}
