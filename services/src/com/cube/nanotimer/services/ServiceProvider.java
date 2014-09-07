package com.cube.nanotimer.services;

import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveTimeAverages;
import com.cube.nanotimer.vo.SolveType;

import java.util.List;

public interface ServiceProvider {
  List<CubeType> getCubeTypes(boolean getEmpty);
  List<SolveType> getSolveTypes(CubeType cubeType);
  SolveAverages saveTime(SolveTime solveTime);
  SolveAverages getSolveAverages(SolveType solveType);
  SolveAverages removeTime(SolveTime solveTime);
  List<SolveTime> getHistory(SolveType solveType);
  List<SolveTime> getHistory(SolveType solveType, long from);
  void deleteHistory();
  void deleteHistory(SolveType solveType);
  List<Long> getSessionTimes(SolveType solveType);
  void startNewSession(SolveType solveType, long startTs);
  void saveSolveTypesOrder(List<SolveType> solveTypes);
  SolveTimeAverages getSolveTimeAverages(SolveTime solveTime);

  int addSolveType(SolveType solveType);
  void addSolveTypeSteps(SolveType solveType);
  void updateSolveType(SolveType solveType);
  void deleteSolveType(SolveType solveType);
}
