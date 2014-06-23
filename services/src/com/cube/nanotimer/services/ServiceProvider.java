package com.cube.nanotimer.services;

import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;

import java.util.List;

public interface ServiceProvider {
  List<CubeType> getCubeTypes();
  List<SolveType> getSolveTypes(CubeType cubeType);
  SolveAverages saveTime(SolveTime solveTime);
  void removeTime(SolveTime solveTime);
}
