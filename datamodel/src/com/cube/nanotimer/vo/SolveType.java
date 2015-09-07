package com.cube.nanotimer.vo;

import java.io.Serializable;
import java.util.Arrays;

public class SolveType implements Serializable {

  private int id;
  private String name;
  private int cubeTypeId;
  private SolveTypeStep[] steps = new SolveTypeStep[0];
  private boolean blind = false;

  public SolveType() {
  }

  public SolveType(String name, boolean blind, int cubeTypeId) {
    this.name = name;
    this.blind = blind;
    this.cubeTypeId = cubeTypeId;
  }

  public SolveType(int id, String name, boolean blind, int cubeTypeId) {
    this(name, blind, cubeTypeId);
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getCubeTypeId() {
    return cubeTypeId;
  }

  public void setCubeTypeId(int cubeTypeId) {
    this.cubeTypeId = cubeTypeId;
  }

  public boolean isBlind() {
    return blind;
  }

  public SolveTypeStep[] getSteps() {
    return steps;
  }

  public void setSteps(SolveTypeStep[] steps) {
    this.steps = steps;
  }

  public boolean hasSteps() {
    return steps != null && steps.length > 0;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SolveType)) {
      return false;
    }
    SolveType st = (SolveType) o;
    return this.id == st.id && this.name.equals(st.name) && this.cubeTypeId == st.cubeTypeId && Arrays.equals(this.steps, st.steps) && this.blind == st.blind;
  }
}
