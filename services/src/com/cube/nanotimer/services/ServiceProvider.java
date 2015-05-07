package com.cube.nanotimer.services;

import com.cube.nanotimer.vo.*;

import java.util.List;

public interface ServiceProvider {
  List<CubeType> getCubeTypes(boolean getEmpty);
  List<SolveType> getSolveTypes(CubeType cubeType);
  SolveAverages saveTime(SolveTime solveTime);
  SolveAverages getSolveAverages(SolveType solveType);
  SolveAverages deleteTime(SolveTime solveTime);
  SolveHistory getPagedHistory(SolveType solveType, TimesSort timesSort);
  SolveHistory getPagedHistory(SolveType solveType, Long from, TimesSort timesSort);
  SolveHistory getHistory(SolveType solveType, Long from);
  void deleteHistory();
  void deleteHistory(SolveType solveType);
  List<Long> getSessionTimes(SolveType solveType);
  void startNewSession(SolveType solveType, long startTs);
  long getSessionStart(SolveType solveType);
  void saveSolveTypesOrder(List<SolveType> solveTypes);
  SolveTimeAverages getSolveTimeAverages(SolveTime solveTime);
  SessionDetails getSessionDetails(SolveType solveType);
  int getSolvesCount(SolveType solveType);
  List<ExportResult> getExportResults(List<Integer> solveTypeIds, int limit);
  SolveTime getSolveTime(int solveTimeId);

  int addSolveType(SolveType solveType);
  void addSolveTypeSteps(SolveType solveType);
  void updateSolveType(SolveType solveType);
  void deleteSolveType(SolveType solveType);
}
