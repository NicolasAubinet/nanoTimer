package com.cube.nanotimer.gui.widget;

import com.cube.nanotimer.vo.SolveTime;

import java.io.Serializable;

public interface TimeChangedHandler extends Serializable {

  void onTimeChanged(SolveTime solveTime);
  void onTimeDeleted(SolveTime solveTime);

}
