package com.cube.nanotimer.gui.widget;

import java.io.Serializable;
import java.util.List;

public interface StepsCreator extends Serializable {
  void addSteps(List<String> steps, int pos);
}
