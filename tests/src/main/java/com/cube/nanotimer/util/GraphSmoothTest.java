package com.cube.nanotimer.util;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import com.cube.nanotimer.util.chart.ChartData;
import com.cube.nanotimer.util.chart.ChartUtils;

import java.util.ArrayList;
import java.util.List;

public class GraphSmoothTest extends AndroidTestCase {

  @SmallTest
  public void testSmooth() {
    List<ChartData> chartDataList = new ArrayList<ChartData>();
    chartDataList.add(new ChartData(0, 10));
    chartDataList.add(new ChartData(0, 20));
    chartDataList.add(new ChartData(0, 30));
    chartDataList.add(new ChartData(10, 40));
    chartDataList.add(new ChartData(0, 50));
    chartDataList.add(new ChartData(0, 60));

    List<ChartData> smoothed = ChartUtils.getSmoothedChartTimes(chartDataList);
    assertEquals(smoothed.size(), 6);
    assertEquals(0.0f, smoothed.get(0).getData());
    assertEquals(2.0f, smoothed.get(1).getData());
    assertEquals(2.0f, smoothed.get(2).getData());
    assertEquals(2.0f, smoothed.get(3).getData());
    assertEquals(2.0f, smoothed.get(4).getData());
    assertEquals(2.0f, smoothed.get(5).getData());
  }

  @SmallTest
  public void testSmoothWithOneNonZeroTime() {
    List<ChartData> chartDataList = new ArrayList<ChartData>();
    for (int i = 0; i < 15; i++) {
      chartDataList.add(new ChartData(0, 10));
    }
    chartDataList.add(new ChartData(5, 40));
    for (int i = 0; i < 15; i++) {
      chartDataList.add(new ChartData(0, 10));
    }

    List<ChartData> smoothed = ChartUtils.getSmoothedChartTimes(chartDataList);
    assertEquals(smoothed.size(), 31);
    for (int i = 0; i < 13; i++) {
      assertEquals(0.0f, smoothed.get(i).getData());
    }
    for (int i = 13; i < 18; i++) {
      assertEquals(1.0f, smoothed.get(i).getData());
    }
    for (int i = 18; i < smoothed.size(); i++) {
      assertEquals(0.0f, smoothed.get(i).getData());
    }
  }

}
