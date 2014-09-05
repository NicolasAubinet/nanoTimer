package com.cube.nanotimer.vo;

import java.io.Serializable;

public class CubeType implements Serializable {

  public enum Type {
    TWO_BY_TWO(1),
    THREE_BY_THREE(2),
    FOUR_BY_FOUR(3),
    FIVE_BY_FIVE(4),
    SIX_BY_SIX(5),
    SEVEN_BY_SEVEN(6),
    MEGAMINX(7),
    PYRAMINX(8),
    SKEWB(9),
    SQUARE1(10);

    private int id;
    Type(int id) {
      this.id = id;
    }
    public int getId() {
      return id;
    }
  }

  private int id;
  private String name;

  public CubeType(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    for (Type t : Type.values()) {
      if (t.getId() == id) {
        return t;
      }
    }
    return null;
  }
}
