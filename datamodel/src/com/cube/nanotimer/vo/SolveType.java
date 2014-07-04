package com.cube.nanotimer.vo;

import java.io.Serializable;

public class SolveType implements Serializable {

  private int id;
  private String name;
  private int cubeTypeId;

  public SolveType() {
  }

  public SolveType(String name, int cubeTypeId) {
    this.name = name;
    this.cubeTypeId = cubeTypeId;
  }

  public SolveType(int id, String name, int cubeTypeId) {
    this.id = id;
    this.name = name;
    this.cubeTypeId = cubeTypeId;
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
}
