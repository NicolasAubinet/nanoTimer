package com.cube.nanotimer.util.exportimport;

import android.content.Context;
import com.cube.nanotimer.R;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportTimesData {

  private Context context;
  private Map<CubeType, List<SolveType>> solveTypes = new HashMap<CubeType, List<SolveType>>();
  private Map<SolveType, List<SolveTime>> solveTimes = new HashMap<SolveType, List<SolveTime>>();

  public ImportTimesData(Context context) {
    this.context = context;
  }

  public SolveType addSolveTypeIfNotExists(CubeType cubeType, SolveType solveType) throws CSVFormatException {
    List<SolveType> solveTypesList = solveTypes.get(cubeType);
    if (solveTypesList == null) {
      solveTypesList = new ArrayList<SolveType>();
      solveTypes.put(cubeType, solveTypesList);
    }
    SolveType existingSolveType = null;
    for (SolveType curSolveType : solveTypesList) {
      if (curSolveType.getName().equals(solveType.getName())) {
        existingSolveType = curSolveType;
        break;
      }
    }
    SolveType solveTypeInList;
    if (existingSolveType == null) {
      solveTypesList.add(solveType);
      solveTypeInList = solveType;
    } else {
      if (!solveType.equals(existingSolveType)) {
        throw new CSVFormatException(context.getString(R.string.solve_type_conflict,
          solveType.getName(),
          cubeType.getName(),
          R.string.email));
      }
      solveTypeInList = existingSolveType;
    }
    return solveTypeInList;
  }

  public void addSolveTime(SolveType solveType, SolveTime solveTime) throws CSVFormatException {
    List<SolveTime> solveTimesList = solveTimes.get(solveType);
    if (solveTimesList == null) {
      solveTimesList = new ArrayList<SolveTime>();
      solveTimes.put(solveType, solveTimesList);
    }
    solveTimesList.add(solveTime);
  }

}
