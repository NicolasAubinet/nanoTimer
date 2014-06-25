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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceProviderImpl implements ServiceProvider {

  private SQLiteDatabase db;

  private SolveType currentSolveType;
  private List<Long> cachedSolveTimes;
  private Map<Integer, Long> cachedBestAverages;
  private final int CACHE_MAX_SIZE = 1010;
  private final int HISTORY_PAGE_SIZE = 20;

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
    syncCaches(solveTime.getSolveType());
    cachedSolveTimes.add(0, solveTime.getTime());
    if (cachedSolveTimes.size() > CACHE_MAX_SIZE) {
      cachedSolveTimes.remove(cachedSolveTimes.size() - 1);
    }
    Long avg5 = getLastAvg(5);
    Long avg12 = getLastAvg(12);
    Long avg100 = getLastAvg(100);
    Long avg1000 = getLastAvg(1000);

    ContentValues values = new ContentValues();
    values.put(DB.COL_TIMEHISTORY_TIME, solveTime.getTime());
    values.put(DB.COL_TIMEHISTORY_SOLVETYPE_ID, solveTime.getSolveType().getId());
    values.put(DB.COL_TIMEHISTORY_SCRAMBLE, solveTime.getScramble());
    values.put(DB.COL_TIMEHISTORY_TIMESTAMP, solveTime.getTimestamp());
    values.put(DB.COL_TIMEHISTORY_AVG5, avg5);
    values.put(DB.COL_TIMEHISTORY_AVG12, avg12);
    values.put(DB.COL_TIMEHISTORY_AVG100, avg100);
    values.put(DB.COL_TIMEHISTORY_AVG1000, avg1000);
    db.insert(DB.TABLE_TIMEHISTORY, null, values);

    SolveAverages solveAverages = new SolveAverages();
    solveAverages.setAvgOf5(avg5);
    solveAverages.setAvgOf12(avg12);
    solveAverages.setAvgOf100(avg100);
    solveAverages.setAvgOf1000(avg1000);
    syncBestAveragesWithCurrent(avg5, avg12, avg100, avg1000);
    solveAverages.setBestOf5(cachedBestAverages.get(5));
    solveAverages.setBestOf12(cachedBestAverages.get(12));
    solveAverages.setBestOf100(cachedBestAverages.get(100));
    solveAverages.setBestOf1000(cachedBestAverages.get(1000));

    return solveAverages;
  }

  private void syncBestAveragesWithCurrent(Long avg5, Long avg12, Long avg100, Long avg1000) {
    if (isAverageBetter(cachedBestAverages.get(5), avg5)) {
      cachedBestAverages.put(5, avg5);
    } else if (isAverageBetter(cachedBestAverages.get(12), avg12)) {
      cachedBestAverages.put(12, avg12);
    } else if (isAverageBetter(cachedBestAverages.get(100), avg100)) {
      cachedBestAverages.put(100, avg100);
    } else if (isAverageBetter(cachedBestAverages.get(1000), avg1000)) {
      cachedBestAverages.put(1000, avg1000);
    }
  }

  private boolean isAverageBetter(Long oldVal, Long newVal) {
    if (oldVal == null && newVal != null) {
      return true;
    } else if (oldVal != null && newVal != null && newVal < oldVal) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public SolveAverages getSolveAverages(SolveType solveType) {
    syncCaches(solveType);
    SolveAverages solveAverages = new SolveAverages();
    solveAverages.setAvgOf5(getLastAvg(5));
    solveAverages.setAvgOf12(getLastAvg(12));
    solveAverages.setAvgOf100(getLastAvg(100));
    solveAverages.setAvgOf1000(getLastAvg(1000));
    solveAverages.setBestOf5(cachedBestAverages.get(5));
    solveAverages.setBestOf12(cachedBestAverages.get(12));
    solveAverages.setBestOf100(cachedBestAverages.get(100));
    solveAverages.setBestOf1000(cachedBestAverages.get(1000));
    return solveAverages;
  }

  @Override
  public void removeTime(SolveTime solveTime) {
    // TODO : remove from cache
    // TODO : remove from DB
    // TODO : re-read if cache became < 1000
    // TODO : search for best averages again (maybe check if the history line that was deleted belongs to the best avg, otherwise don't re-read. see if would be useful)
  }

  @Override
  public List<SolveTime> getHistory(SolveType solveType) {
    // TODO : allow to read more (by passing the page id, or the timestamp to start reading from)
    List<SolveTime> history = new ArrayList<SolveTime>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_ID);
    q.append("     , ").append(DB.COL_TIMEHISTORY_TIME);
    q.append("     , ").append(DB.COL_TIMEHISTORY_TIMESTAMP);
    q.append("     , ").append(DB.COL_TIMEHISTORY_SCRAMBLE);
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
    q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
    q.append(" ORDER BY ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" DESC");
    q.append(" LIMIT ").append(HISTORY_PAGE_SIZE);
    Cursor cursor = db.rawQuery(q.toString(), new String[] { String.valueOf(solveType.getId()) });
    if (cursor != null) {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        SolveTime st = new SolveTime();
        st.setId(cursor.getInt(0));
        st.setTime(cursor.getInt(1));
        st.setTimestamp(cursor.getLong(2));
        st.setScramble(cursor.getString(3));
        st.setSolveType(solveType);
        history.add(st);
      }
      cursor.close();
    }
    return history;
  }

  private void syncCaches(SolveType solveType) {
    if (cachedSolveTimes == null || solveType.getId() != currentSolveType.getId()) {
      currentSolveType = solveType;
      loadSolveTimes(solveType.getId());
      loadBestAverages(solveType.getId());
    }
  }

  private List<Long> loadSolveTimes(int solveTypeId) {
    cachedSolveTimes = new ArrayList<Long>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_TIMEHISTORY_TIME);
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
    q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
    q.append(" ORDER BY ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" DESC");
    q.append(" LIMIT ").append(CACHE_MAX_SIZE);
    Cursor cursor = db.rawQuery(q.toString(), new String[] { String.valueOf(solveTypeId) });
    if (cursor != null) {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        cachedSolveTimes.add(cursor.getLong(0));
      }
      cursor.close();
    }
    return cachedSolveTimes;
  }

  private Map<Integer, Long> loadBestAverages(int solveTypeId) {
    cachedBestAverages = new HashMap<Integer, Long>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT MIN(").append(DB.COL_TIMEHISTORY_AVG5).append(")");
    q.append(", MIN(").append(DB.COL_TIMEHISTORY_AVG12).append(")");
    q.append(", MIN(").append(DB.COL_TIMEHISTORY_AVG100).append(")");
    q.append(", MIN(").append(DB.COL_TIMEHISTORY_AVG1000).append(")");
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
    q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
    Cursor cursor = db.rawQuery(q.toString(), new String[] { String.valueOf(solveTypeId) });
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        cachedBestAverages.put(5, getCursorLong(cursor, 0));
        cachedBestAverages.put(12, getCursorLong(cursor, 1));
        cachedBestAverages.put(100, getCursorLong(cursor, 2));
        cachedBestAverages.put(1000, getCursorLong(cursor, 3));
      }
      cursor.close();
    }
    return cachedBestAverages;
  }

  private Long getLastAvg(int n) {
    long total = 0;
    int i = 0;
    for (Long t : cachedSolveTimes) {
      total += t;
      i++;
      if (i == n) {
        return total / n;
      }
    }
    return null;
  }

  protected Long getCursorLong(Cursor cursor, int ind) {
    if (cursor == null) {
      return null;
    }
    if (cursor.isNull(ind)) {
      return null;
    } else {
      return cursor.getLong(ind);
    }
  }

}
