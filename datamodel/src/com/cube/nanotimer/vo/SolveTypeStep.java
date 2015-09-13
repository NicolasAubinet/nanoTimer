package com.cube.nanotimer.vo;

import java.io.Serializable;

public class SolveTypeStep implements Serializable {

  private int id;
  private String name;

  public SolveTypeStep() {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SolveTypeStep)) return false;

    SolveTypeStep that = (SolveTypeStep) o;

    if (id != that.id) return false;
    return !(name != null ? !name.equals(that.name) : that.name != null);

  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    return result;
  }
}
