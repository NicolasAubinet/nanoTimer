package com.cube.nanotimer.vo;

import java.util.List;

public class SolveAverages {

  private SolveTime solveTime;

  private Long avgOf5;
  private Long avgOf12;
  private Long avgOf50;
  private Long avgOf100;
  private Long avgOfLifetime;

  private Long bestOf5;
  private Long bestOf12;
  private Long bestOf50;
  private Long bestOf100;
  private Long bestOfLifetime;

  private List<Long> stepsAvgOf5;
  private List<Long> stepsAvgOf12;
  private List<Long> stepsAvgOf50;
  private List<Long> stepsAvgOf100;
  private List<Long> stepsAvgOfLifetime;

  private Long meanOf3;
  private Long bestOf3;
  private Integer accuracyOf12;
  private Integer accuracyOf50;
  private Integer accuracyOf100;
  private Integer lifetimeAccuracy;

  public SolveAverages() {
  }

  /**
   * Constructor for normal solve types
   */
  public SolveAverages(Long avgOf5, Long avgOf12, Long avgOf50, Long avgOf100, Long avgOfLifetime, Long bestOf5, Long bestOf12, Long bestOf50, Long bestOf100, Long bestOfLifetime) {
    this.avgOf5 = avgOf5;
    this.avgOf12 = avgOf12;
    this.avgOf50 = avgOf50;
    this.avgOf100 = avgOf100;
    this.avgOfLifetime = avgOfLifetime;
    this.bestOf5 = bestOf5;
    this.bestOf12 = bestOf12;
    this.bestOf50 = bestOf50;
    this.bestOf100 = bestOf100;
    this.bestOfLifetime = bestOfLifetime;
  }

  /**
   * Constructor for blind solve types
   */
  public SolveAverages(Long meanOf3, Long avgOf12, Long avgOf50, Long avgOf100, Long avgOfLifetime, Long bestOf3, Long bestOfLifetime,
                       Integer accuracyOf12, Integer accuracyOf50, Integer accuracyOf100,Integer lifetimeAccuracy) {
    this.meanOf3 = meanOf3;
    this.avgOf12 = avgOf12;
    this.avgOf50 = avgOf50;
    this.avgOf100 = avgOf100;
    this.avgOfLifetime = avgOfLifetime;
    this.bestOf3 = bestOf3;
    this.bestOfLifetime = bestOfLifetime;
    this.accuracyOf12 = accuracyOf12;
    this.accuracyOf50 = accuracyOf50;
    this.accuracyOf100 = accuracyOf100;
    this.lifetimeAccuracy = lifetimeAccuracy;
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

  public Long getAvgOf50() {
    return avgOf50;
  }

  public void setAvgOf50(Long avgOf50) {
    this.avgOf50 = avgOf50;
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

  public Long getBestOf50() {
    return bestOf50;
  }

  public void setBestOf50(Long bestOf50) {
    this.bestOf50 = bestOf50;
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
    } else if (n == 50) {
      this.stepsAvgOf50 = stepsAvgOf;
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

  public List<Long> getStepsAvgOf50() {
    return stepsAvgOf50;
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

  public void setStepsAvgOf5(List<Long> stepsAvgOf5) {
    this.stepsAvgOf5 = stepsAvgOf5;
  }

  public void setStepsAvgOf12(List<Long> stepsAvgOf12) {
    this.stepsAvgOf12 = stepsAvgOf12;
  }

  public void setStepsAvgOf50(List<Long> stepsAvgOf50) {
    this.stepsAvgOf50 = stepsAvgOf50;
  }

  public void setStepsAvgOf100(List<Long> stepsAvgOf100) {
    this.stepsAvgOf100 = stepsAvgOf100;
  }

  public Long getMeanOf3() {
    return meanOf3;
  }

  public void setMeanOf3(Long meanOf3) {
    this.meanOf3 = meanOf3;
  }

  public Long getBestOf3() {
    return bestOf3;
  }

  public void setBestOf3(Long bestOf3) {
    this.bestOf3 = bestOf3;
  }

  public Integer getAccuracyOf12() {
    return accuracyOf12;
  }

  public void setAccuracyOf12(Integer accuracyOf12) {
    this.accuracyOf12 = accuracyOf12;
  }

  public Integer getAccuracyOf50() {
    return accuracyOf50;
  }

  public void setAccuracyOf50(Integer accuracyOf50) {
    this.accuracyOf50 = accuracyOf50;
  }

  public Integer getAccuracyOf100() {
    return accuracyOf100;
  }

  public void setAccuracyOf100(Integer accuracyOf100) {
    this.accuracyOf100 = accuracyOf100;
  }

  public Integer getLifetimeAccuracy() {
    return lifetimeAccuracy;
  }

  public void setLifetimeAccuracy(Integer lifetimeAccuracy) {
    this.lifetimeAccuracy = lifetimeAccuracy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SolveAverages)) return false;

    SolveAverages that = (SolveAverages) o;

    if (avgOf100 != null ? !avgOf100.equals(that.avgOf100) : that.avgOf100 != null) return false;
    if (avgOf50 != null ? !avgOf50.equals(that.avgOf50) : that.avgOf50 != null) return false;
    if (avgOf12 != null ? !avgOf12.equals(that.avgOf12) : that.avgOf12 != null) return false;
    if (avgOf5 != null ? !avgOf5.equals(that.avgOf5) : that.avgOf5 != null) return false;
    if (avgOfLifetime != null ? !avgOfLifetime.equals(that.avgOfLifetime) : that.avgOfLifetime != null) return false;
    if (bestOf100 != null ? !bestOf100.equals(that.bestOf100) : that.bestOf100 != null) return false;
    if (bestOf50 != null ? !bestOf50.equals(that.bestOf50) : that.bestOf50 != null) return false;
    if (bestOf12 != null ? !bestOf12.equals(that.bestOf12) : that.bestOf12 != null) return false;
    if (bestOf5 != null ? !bestOf5.equals(that.bestOf5) : that.bestOf5 != null) return false;
    if (bestOfLifetime != null ? !bestOfLifetime.equals(that.bestOfLifetime) : that.bestOfLifetime != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = solveTime != null ? solveTime.hashCode() : 0;
    result = 31 * result + (avgOf5 != null ? avgOf5.hashCode() : 0);
    result = 31 * result + (avgOf12 != null ? avgOf12.hashCode() : 0);
    result = 31 * result + (avgOf50 != null ? avgOf50.hashCode() : 0);
    result = 31 * result + (avgOf100 != null ? avgOf100.hashCode() : 0);
    result = 31 * result + (avgOfLifetime != null ? avgOfLifetime.hashCode() : 0);
    result = 31 * result + (bestOf5 != null ? bestOf5.hashCode() : 0);
    result = 31 * result + (bestOf12 != null ? bestOf12.hashCode() : 0);
    result = 31 * result + (bestOf50 != null ? bestOf50.hashCode() : 0);
    result = 31 * result + (bestOf100 != null ? bestOf100.hashCode() : 0);
    result = 31 * result + (bestOfLifetime != null ? bestOfLifetime.hashCode() : 0);
    result = 31 * result + (stepsAvgOf5 != null ? stepsAvgOf5.hashCode() : 0);
    result = 31 * result + (stepsAvgOf12 != null ? stepsAvgOf12.hashCode() : 0);
    result = 31 * result + (stepsAvgOf50 != null ? stepsAvgOf50.hashCode() : 0);
    result = 31 * result + (stepsAvgOf100 != null ? stepsAvgOf100.hashCode() : 0);
    result = 31 * result + (stepsAvgOfLifetime != null ? stepsAvgOfLifetime.hashCode() : 0);
    result = 31 * result + (meanOf3 != null ? meanOf3.hashCode() : 0);
    result = 31 * result + (bestOf3 != null ? bestOf3.hashCode() : 0);
    result = 31 * result + (accuracyOf12 != null ? accuracyOf12.hashCode() : 0);
    result = 31 * result + (accuracyOf50 != null ? accuracyOf50.hashCode() : 0);
    result = 31 * result + (accuracyOf100 != null ? accuracyOf100.hashCode() : 0);
    result = 31 * result + (lifetimeAccuracy != null ? lifetimeAccuracy.hashCode() : 0);
    return result;
  }

}
