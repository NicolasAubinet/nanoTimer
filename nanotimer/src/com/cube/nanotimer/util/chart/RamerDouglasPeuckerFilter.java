package com.cube.nanotimer.util.chart;

public class RamerDouglasPeuckerFilter {

  private double epsilon;

  /**
   *
   * @param epsilon
   *            epsilon in Ramer-Douglas-Peucker algorithm (maximum distance
   *            of a point in data between original curve and simplified
   *            curve)
   * @throws IllegalArgumentException
   *             when {@code epsilon <= 0}
   */
  public RamerDouglasPeuckerFilter(double epsilon) {
    if (epsilon <= 0) {
      throw new IllegalArgumentException("Epsilon must be > 0");
    }
    this.epsilon = epsilon;
  }

  public ChartDataContainer[] filter(ChartDataContainer[] data) {
    return ramerDouglasPeuckerFunction(data, 0, data.length - 1);
  }

  protected ChartDataContainer[] ramerDouglasPeuckerFunction(ChartDataContainer[] data, int startIndex, int endIndex) {
    double dmax = 0;
    int idx = 0;
    double a = endIndex - startIndex;
    double b = data[endIndex].getData() - data[startIndex].getData();
    double c = -(b * startIndex - a * data[startIndex].getData());
    double norm = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    for (int i = startIndex + 1; i < endIndex; i++) {
      double distance = Math.abs(b * i - a * data[i].getData() + c) / norm;
      if (distance > dmax) {
        idx = i;
        dmax = distance;
      }
    }
    if (dmax >= epsilon) {
      ChartDataContainer[] recursiveResult1 = ramerDouglasPeuckerFunction(data,
        startIndex, idx);
      ChartDataContainer[] recursiveResult2 = ramerDouglasPeuckerFunction(data,
        idx, endIndex);
      ChartDataContainer[] result = new ChartDataContainer[(recursiveResult1.length - 1)
        + recursiveResult2.length];
      System.arraycopy(recursiveResult1, 0, result, 0,
        recursiveResult1.length - 1);
      System.arraycopy(recursiveResult2, 0, result,
        recursiveResult1.length - 1, recursiveResult2.length);
      return result;
    } else {
      return new ChartDataContainer[] { data[startIndex], data[endIndex] };
    }
  }

  /**
   *
   * @return {@code epsilon}
   */
  public double getEpsilon() {
    return epsilon;
  }

  /**
   *
   * @param epsilon
   *            maximum distance of a point in data between original curve and
   *            simplified curve
   */
  public void setEpsilon(double epsilon) {
    if (epsilon <= 0) {
      throw new IllegalArgumentException("Epsilon must be > 0");
    }
    this.epsilon = epsilon;
  }

}
