package com.cube.nanotimer.vo;

import java.util.List;

public class SolveAverages {

  private SolveTime solveTime;

  private Long avgOf5;
  private Long avgOf12;
  private Long avgOf100;
  private Long avgOfLifetime;

  private Long bestOf5;
  private Long bestOf12;
  private Long bestOf100;
  private Long bestOfLifetime;

  private List<Long> stepsAvgOf5;
  private List<Long> stepsAvgOf12;
  private List<Long> stepsAvgOf100;
  private List<Long> stepsAvgOfLifetime;

  public SolveAverages() {
  }

  public SolveAverages(Long avgOf5, Long avgOf12, Long avgOf100, Long avgOfLifetime, Long bestOf5, Long bestOf12, Long bestOf100, Long bestOfLifetime) {
    this.avgOf5 = avgOf5;
    this.avgOf12 = avgOf12;
    this.avgOf100 = avgOf100;
    this.avgOfLifetime = avgOfLifetime;
    this.bestOf5 = bestOf5;
    this.bestOf12 = bestOf12;
    this.bestOf100 = bestOf100;
    this.bestOfLifetime = bestOfLifetime;
  }

  public SolveTime getSolveTime() {
    return solveTime;
  }

  public void setSolveTime(SolveTime solveTime) {
    this.solveTime = solveTime;
  }

  public Long getAvgOf5() {
    return avgOf5;
  }

  public void setAvgOf5(Long avgOf5) {
    this.avgOf5 = avgOf5;
  }

  public Long getAvgOf12() {
    return avgOf12;
  }

  public void setAvgOf12(Long avgOf12) {
    this.avgOf12 = avgOf12;
  }

  public Long getAvgOf100() {
    return avgOf100;
  }

  public void setAvgOf100(Long avgOf100) {
    this.avgOf100 = avgOf100;
  }

  public Long getAvgOfLifetime() {
    return avgOfLifetime;
  }

  public void setAvgOfLifetime(Long avgOfLifetime) {
    this.avgOfLifetime = avgOfLifetime;
  }

  public Long getBestOf5() {
    return bestOf5;
  }

  public void setBestOf5(Long bestOf5) {
    this.bestOf5 = bestOf5;
  }

  public Long getBestOf12() {
    return bestOf12;
  }

  public void setBestOf12(Long bestOf12) {
    this.bestOf12 = bestOf12;
  }

  public Long getBestOf100() {
    return bestOf100;
  }

  public void setBestOf100(Long bestOf100) {
    this.bestOf100 = bestOf100;
  }

  public Long getBestOfLifetime() {
    return bestOfLifetime;
  }

  public void setBestOfLifetime(Long bestOfLifetime) {
    this.bestOfLifetime = bestOfLifetime;
  }

  public void setStepsAvgOf(int n, List<Long> stepsAvgOf) {
    if (n == 5) {
      this.stepsAvgOf5 = stepsAvgOf;
    } else if (n == 12) {
      this.stepsAvgOf12 = stepsAvgOf;
    } else if (n == 100) {
      this.stepsAvgOf100 = stepsAvgOf;
    }
  }

  public List<Long> getStepsAvgOf5() {
    return stepsAvgOf5;
  }

  public List<Long> getStepsAvgOf12() {
    return stepsAvgOf12;
  }

  public List<Long> getStepsAvgOf100() {
    return stepsAvgOf100;
  }

  public List<Long> getStepsAvgOfLifetime() {
    return stepsAvgOfLifetime;
  }

  public void setStepsAvgOfLifetime(List<Long> stepsAvgOfLifetime) {
    this.stepsAvgOfLifetime = stepsAvgOfLifetime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SolveAverages)) return false;

    SolveAverages that = (SolveAverages) o;

    if (avgOf100 != null ? !avgOf100.equals(that.avgOf100) : that.avgOf100 != null) return false;
    if (avgOf12 != null ? !avgOf12.equals(that.avgOf12) : that.avgOf12 != null) return false;
    if (avgOf5 != null ? !avgOf5.equals(that.avgOf5) : that.avgOf5 != null) return false;
    if (avgOfLifetime != null ? !avgOfLifetime.equals(that.avgOfLifetime) : that.avgOfLifetime != null) return false;
    if (bestOf100 != null ? !bestOf100.equals(that.bestOf100) : that.bestOf100 != null) return false;
    if (bestOf12 != null ? !bestOf12.equals(that.bestOf12) : that.bestOf12 != null) return false;
    if (bestOf5 != null ? !bestOf5.equals(that.bestOf5) : that.bestOf5 != null) return false;
    if (bestOfLifetime != null ? !bestOfLifetime.equals(that.bestOfLifetime) : that.bestOfLifetime != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = avgOf5 != null ? avgOf5.hashCode() : 0;
    result = 31 * result + (avgOf12 != null ? avgOf12.hashCode() : 0);
    result = 31 * result + (avgOf100 != null ? avgOf100.hashCode() : 0);
    result = 31 * result + (avgOfLifetime != null ? avgOfLifetime.hashCode() : 0);
    result = 31 * result + (bestOf5 != null ? bestOf5.hashCode() : 0);
    result = 31 * result + (bestOf12 != null ? bestOf12.hashCode() : 0);
    result = 31 * result + (bestOf100 != null ? bestOf100.hashCode() : 0);
    result = 31 * result + (bestOfLifetime != null ? bestOfLifetime.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "SolveAverages{" +
        "avgOf5=" + avgOf5 +
        ", avgOf12=" + avgOf12 +
        ", avgOf100=" + avgOf100 +
        ", avgOfLifetime=" + avgOfLifetime +
        ", bestOf5=" + bestOf5 +
        ", bestOf12=" + bestOf12 +
        ", bestOf100=" + bestOf100 +
        ", bestOfLifetime=" + bestOfLifetime +
        '}';
  }

}
