package com.cube.nanotimer.vo;

import java.io.Serializable;

public class SolveTypeStep implements Serializable {

  private int id;
  private String name;

  public static final char[] FORBIDDEN_NAME_CHARACTERS = new char[] { '"', ',', ';', '|', '=' };

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

  public static Character checkForForbiddenCharacters(String stepName) {
    Character forbiddenChar = null;
    for (char c : SolveTypeStep.FORBIDDEN_NAME_CHARACTERS) {
      if (stepName.contains(String.valueOf(c))) {
        forbiddenChar = c;
        break;
      }
    }
    return forbiddenChar;
  }
}
