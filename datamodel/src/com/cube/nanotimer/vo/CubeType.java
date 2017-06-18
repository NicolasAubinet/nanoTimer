package com.cube.nanotimer.vo;

import java.io.Serializable;

public enum CubeType implements Serializable {

  TWO_BY_TWO(1, "2x2x2"),
  THREE_BY_THREE(2, "3x3x3", ScrambleTypes.THREE_BY_THREE),
  FOUR_BY_FOUR(3, "4x4x4"),
  FIVE_BY_FIVE(4, "5x5x5"),
  SIX_BY_SIX(5, "6x6x6"),
  SEVEN_BY_SEVEN(6, "7x7x7"),
  MEGAMINX(7, "Megaminx"),
  PYRAMINX(8, "Pyraminx"),
  SKEWB(9, "Skewb"),
  SQUARE1(10, "Square-1"),
  CLOCK(11, "Clock");

  private int id;
  private String name;
  private ScrambleType[] scrambleTypes = new ScrambleType[0];

  CubeType(int id, String name) {
    this.id = id;
    this.name = name;
  }

  CubeType(int id, String name, ScrambleType[] scrambleTypes) {
    this(id, name);
    this.scrambleTypes = scrambleTypes;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public ScrambleType[] getScrambleTypes() {
    return scrambleTypes;
  }

  public static CubeType getCubeType(int id) {
    for (CubeType t : values()) {
      if (t.getId() == id) {
        return t;
      }
    }
    return null;
  }

  public static CubeType getCubeTypeFromName(String cubeTypeName) {
    for (CubeType t : values()) {
      if (t.getName().equals(cubeTypeName)) {
        return t;
      }
    }
    return null;
  }

  public ScrambleType getScrambleTypeFromString(String parScrambleTypeName) {
    for (ScrambleType scrambleType : scrambleTypes) {
      if (scrambleType.getName().equals(parScrambleTypeName)) {
        return scrambleType;
      }
    }
    return null;
  }
}
