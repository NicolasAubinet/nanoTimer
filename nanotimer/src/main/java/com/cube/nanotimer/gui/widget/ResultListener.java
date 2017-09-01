package com.cube.nanotimer.gui.widget;

import java.io.Serializable;

public interface ResultListener extends Serializable {
  void onResult(Object... params);
}
