package com.cube.nanotimer.util.chart;

public class ChartData implements ChartDataContainer {

  private float data;
  private long timestamp;

  public ChartData(float data, long timestamp) {
    this.data = data;
    this.timestamp = timestamp;
  }

  public float getData() {
    return data;
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "ChartData{" +
      "data=" + data +
      ", timestamp=" + timestamp +
      '}';
  }

}
