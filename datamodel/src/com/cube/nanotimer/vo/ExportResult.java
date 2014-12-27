package com.cube.nanotimer.vo;

public class ExportResult {

  private int cubeTypeId;
  private String cubeTypeName;
  private int solveTypeId;
  private String solveTypeName;
  private long time;
  private long timestamp;
  private boolean plusTwo;
  private String scramble;

  public ExportResult(int cubeTypeId, String cubeTypeName, int solveTypeId, String solveTypeName, long time, long timestamp, boolean plusTwo, String scramble) {
    this.cubeTypeId = cubeTypeId;
    this.cubeTypeName = cubeTypeName;
    this.solveTypeId = solveTypeId;
    this.solveTypeName = solveTypeName;
    this.time = time;
    this.timestamp = timestamp;
    this.plusTwo = plusTwo;
    this.scramble = scramble;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getScramble() {
    return scramble;
  }

  public void setScramble(String scramble) {
    this.scramble = scramble;
  }

  public int getCubeTypeId() {
    return cubeTypeId;
  }

  public void setCubeTypeId(int cubeTypeId) {
    this.cubeTypeId = cubeTypeId;
  }

  public String getCubeTypeName() {
    return cubeTypeName;
  }

  public void setCubeTypeName(String cubeTypeName) {
    this.cubeTypeName = cubeTypeName;
  }

  public int getSolveTypeId() {
    return solveTypeId;
  }

  public void setSolveTypeId(int solveTypeId) {
    this.solveTypeId = solveTypeId;
  }

  public String getSolveTypeName() {
    return solveTypeName;
  }

  public void setSolveTypeName(String solveTypeName) {
    this.solveTypeName = solveTypeName;
  }

  public boolean isPlusTwo() {
    return plusTwo;
  }

  public void setPlusTwo(boolean plusTwo) {
    this.plusTwo = plusTwo;
  }

}
