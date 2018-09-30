package com.cube.nanotimer.util.chart;

import java.util.List;

public class ChartLineData {

  private List<ChartData> data;
  private String label;
  private int color;
  private int xOffset = 0;
  private float lineWidth = 1f;
  private float circleSize = 3f;

  public ChartLineData(List<ChartData> data, String label, int color) {
    this.data = data;
    this.label = label;
    this.color = color;
  }

  public List<ChartData> getData() {
    return data;
  }

  public String getLabel() {
    return label;
  }

  public int getColor() {
    return color;
  }

  public int getxOffset() {
    return xOffset;
  }

  public void setxOffset(int xOffset) {
    this.xOffset = xOffset;
  }

  public float getLineWidth() {
    return lineWidth;
  }

  public void setLineWidth(float lineWidth) {
    this.lineWidth = lineWidth;
  }

  public float getCircleSize() {
    return circleSize;
  }

  public void setCircleSize(float circleSize) {
    this.circleSize = circleSize;
  }

}
