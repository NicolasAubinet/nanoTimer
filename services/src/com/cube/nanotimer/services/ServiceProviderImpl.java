package com.cube.nanotimer.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.cube.nanotimer.services.db.DB;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServiceProviderImpl implements ServiceProvider {

  private SQLiteDatabase db;
  private Map<Integer, ArrayList<Float>> cachedSolveTimes = new LinkedHashMap<Integer, ArrayList<Float>>();
  private final int CACHE_MAX_SIZE = 1010;

  public ServiceProviderImpl(SQLiteDatabase db) {
    this.db = db;
  }

  @Override
  public List<CubeType> getCubeTypes() {
    List<CubeType> cubeTypes = new ArrayList<CubeType>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_ID).append(", ").append(DB.COL_CUBETYPE_NAME);
    q.append(" FROM ").append(DB.TABLE_CUBETYPE);
    Cursor cursor = db.rawQuery(q.toString(), null);
    if (cursor != null) {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        CubeType ct = new CubeType(cursor.getInt(0), cursor.getString(1));
        cubeTypes.add(ct);
      }
      cursor.close();
    }
    return cubeTypes;
  }

  @Override
  public List<SolveType> getSolveTypes(CubeType cubeType) {
    List<SolveType> solveTypes = new ArrayList<SolveType>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_ID).append(", ").append(DB.COL_SOLVETYPE_NAME);
    q.append(" FROM ").append(DB.TABLE_SOLVETYPE);
    q.append(" WHERE ").append(DB.COL_SOLVETYPE_CUBETYPE_ID).append(" = ?");
    Cursor cursor = db.rawQuery(q.toString(), new String[] { String.valueOf(cubeType.getId()) });
    if (cursor != null) {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        SolveType st = new SolveType(cursor.getInt(0), cursor.getString(1));
        solveTypes.add(st);
      }
      cursor.close();
    }
    return solveTypes;
  }

  @Override
  public SolveAverages saveTime(SolveTime solveTime) {
    ArrayList<Float> previousTimes = cachedSolveTimes.get(solveTime.getSolveType().getId());
    if (previousTimes == null) {
      previousTimes = loadSolveTimes(solveTime.getSolveType().getId());
    }
    previousTimes.add(0, solveTime.getTime());
    if (previousTimes.size() > CACHE_MAX_SIZE) {
      previousTimes.remove(previousTimes.size() - 1);
    }

    SolveAverages solveAverages = new SolveAverages();
    solveAverages.setAvgOf5(getLastAvg(previousTimes, 5));
    solveAverages.setAvgOf12(getLastAvg(previousTimes, 12));
    solveAverages.setAvgOf100(getLastAvg(previousTimes, 100));
    solveAverages.setAvgOf1000(getLastAvg(previousTimes, 1000));

    ContentValues values = new ContentValues();
    values.put(DB.COL_TIMEHISTORY_TIME, solveTime.getTime());
    values.put(DB.COL_TIMEHISTORY_SOLVETYPE_ID, solveTime.getSolveType().getId());
    values.put(DB.COL_TIMEHISTORY_SCRAMBLE, solveTime.getScramble());
    values.put(DB.COL_TIMEHISTORY_TIMESTAMP, solveTime.getTimestamp());
    values.put(DB.COL_TIMEHISTORY_AVG5, solveAverages.getAvgOf5());
    values.put(DB.COL_TIMEHISTORY_AVG12, solveAverages.getAvgOf12());
    values.put(DB.COL_TIMEHISTORY_AVG100, solveAverages.getAvgOf100());
    values.put(DB.COL_TIMEHISTORY_AVG1000, solveAverages.getAvgOf1000());
    db.insert(DB.TABLE_TIMEHISTORY, null, values);

    return solveAverages;
  }

  @Override
  public void removeTime(SolveTime solveTime) {
    // TODO : remove from cache
    // TODO : remove from DB
    // TODO : re-read if cache became < 1000
  }

  private ArrayList<Float> loadSolveTimes(int solveTypeId) {
    ArrayList<Float> solveTimes = cachedSolveTimes.get(solveTypeId);
    if (solveTimes == null) {
      solveTimes = new ArrayList<Float>();
      cachedSolveTimes.put(solveTypeId, solveTimes);
    }
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_TIMEHISTORY_TIME);
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
    q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
    q.append(" ORDER BY ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" DESC");
    q.append(" LIMIT ").append(CACHE_MAX_SIZE);
    Cursor cursor = db.rawQuery(q.toString(), new String[] { String.valueOf(solveTypeId) });
    if (cursor != null) {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        solveTimes.add(cursor.getFloat(0));
      }
      cursor.close();
    }
    return solveTimes;
  }

  private Float getLastAvg(List<Float> times, int n) {
    float total = 0;
    int i = 0;
    for (Float t : times) {
      total += t;
      i++;
      if (i == n) {
        return (total / n) / 1000;
      }
    }
    return null;
  }

}
