package com.cube.nanotimer.services;

import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SessionDetails;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveHistory;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveTimeAverages;
import com.cube.nanotimer.vo.SolveType;

import java.util.List;

public interface ServiceProvider {
  List<CubeType> getCubeTypes(boolean getEmpty);
  List<SolveType> getSolveTypes(CubeType cubeType);
  SolveAverages saveTime(SolveTime solveTime);
  SolveAverages getSolveAverages(SolveType solveType);
  SolveAverages deleteTime(SolveTime solveTime);
  SolveHistory getHistory(SolveType solveType);
  SolveHistory getHistory(SolveType solveType, long from);
  void deleteHistory();
  void deleteHistory(SolveType solveType);
  List<Long> getSessionTimes(SolveType solveType);
  void startNewSession(SolveType solveType, long startTs);
  long getSessionStart(SolveType solveType);
  void saveSolveTypesOrder(List<SolveType> solveTypes);
  SolveTimeAverages getSolveTimeAverages(SolveTime solveTime);
  SessionDetails getSessionDetails(SolveType solveType);

  int addSolveType(SolveType solveType);
  void addSolveTypeSteps(SolveType solveType);
  void updateSolveType(SolveType solveType);
  void deleteSolveType(SolveType solveType);
}
