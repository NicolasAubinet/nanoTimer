package com.cube.nanotimer.util.exportimport.csvimport;

import android.content.Context;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.exportimport.CSVFormatException;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;

import java.util.ArrayList;
import java.util.Collection;
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
        // happens if a same solve type has conflicts like if it's once set as blind and once as non-blind,
        // or one has steps and an other doesn't
        throw new CSVFormatException(context.getString(R.string.solve_type_conflict,
          solveType.getName(),
          cubeType.getName(),
          context.getString(R.string.email)));
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

  public Map<CubeType, List<SolveType>> getSolveTypes() {
    return solveTypes;
  }

  public Map<SolveType, List<SolveTime>> getSolveTimes() {
    return solveTimes;
  }

  public int getSolveTimesCount() {
    int count = 0;
    for (Collection<SolveTime> st : solveTimes.values()) {
      count += st.size();
    }
    return count;
  }

  public boolean isEmpty() {
    return solveTypes.size() == 0 && solveTimes.size() == 0;
  }
}
