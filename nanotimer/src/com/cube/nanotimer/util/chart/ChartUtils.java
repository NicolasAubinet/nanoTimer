package com.cube.nanotimer.util.chart;

import java.util.ArrayList;
import java.util.List;

public class ChartUtils {

  public static List<ChartData> getSmoothedChartTimes(List<ChartData> chartData) {
    if (chartData == null || chartData.size() == 0) {
      return chartData;
    }
    int averageTimesCount = 2; // number of times to average together around each time (bigger will smooth out more)
    final int totalTimesToShow = 50; // maximum number of times to display (approximation, might be a bit more)
    final int timesToKeep = Math.max(1, chartData.size() / totalTimesToShow); // will keep 1 time for every timesToKeep times
    if (averageTimesCount < timesToKeep - 1) { // if too many times, adjust averageTimesCount to avoid losing times while averaging
      averageTimesCount = timesToKeep - 1;
    }
    List<ChartData> times = new ArrayList<ChartData>();
    for (int i = 0; i < chartData.size(); i += timesToKeep) {
      double total = 0;
      for (int j = (i - averageTimesCount); j < (i + averageTimesCount + 1); j++) {
        int dataIndex;
        if (j < 0) {
          dataIndex = 0;
        } else if (j >= chartData.size()) {
          dataIndex = chartData.size() - 1;
        } else {
          dataIndex = j;
        }
        total += chartData.get(dataIndex).getData();
      }
      float time = ((float) total) / ((averageTimesCount * 2) + 1);
      times.add(new ChartData(time, chartData.get(i).getTimestamp()));
    }
    return times;
  }

  /*public static List<ChartData> getSmoothedChartTimes(List<ChartData> chartData) {
    double epsilon = chartData.size() / 20;
    RamerDouglasPeuckerFilter filter = new RamerDouglasPeuckerFilter(epsilon);
    ChartDataContainer[] data = chartData.toArray(new ChartData[0]);
    ChartDataContainer[] filtered = filter.filter(data);
    List<ChartData> filteredChartData = new ArrayList<ChartData>();
    for (ChartDataContainer filteredData : filtered) {
      filteredChartData.add((ChartData) filteredData);
    }
    return filteredChartData;
  }*/

}
