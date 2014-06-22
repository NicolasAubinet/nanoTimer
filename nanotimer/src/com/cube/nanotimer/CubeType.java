package com.cube.nanotimer;

import java.io.Serializable;

public enum CubeType implements Serializable {
  TWO_BY_TWO(R.string.two_by_two),
  THREE_BY_THREE(R.string.three_by_three),
  FOUR_BY_FOUR(R.string.four_by_four),
  FIVE_BY_FIVE(R.string.five_by_five),
  SIX_BY_SIX(R.string.six_by_six),
  SEVEN_BY_SEVEN(R.string.seven_by_seven),
  MEGAMINX(R.string.megaminx);

  private final int nameId;

  CubeType(int nameId) {
    this.nameId = nameId;
  }

  public String getName() {
    return App.getContext().getString(nameId);
  }
}
