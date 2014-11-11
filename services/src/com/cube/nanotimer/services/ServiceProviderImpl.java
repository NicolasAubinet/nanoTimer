package com.cube.nanotimer.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.cube.nanotimer.services.db.DB;
import com.cube.nanotimer.session.CubeBaseSession;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SessionDetails;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveHistory;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveTimeAverages;
import com.cube.nanotimer.vo.SolveType;
import com.cube.nanotimer.vo.SolveTypeStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ServiceProviderImpl implements ServiceProvider {

  private SQLiteDatabase db;

  private SolveType currentSolveType;
  private List<CachedTime> cachedSolveTimes;
  private Map<Integer, Long> cachedBestAverages = new HashMap<Integer, Long>();
  private Long cachedLifetimeBest;

  private final Object cacheSyncHelper = new Object();

  private final int CACHE_MAX_SIZE = 1010;
  private final int CACHE_MIN_SIZE = 1000;
  private final int HISTORY_PAGE_SIZE = 20;
  private final int SESSION_TIMES_COUNT = 12;
  private final int MAX_AVERAGE_COUNT = 100;

  public ServiceProviderImpl(SQLiteDatabase db) {
    this.db = db;
  }

  @Override
  public List<CubeType> getCubeTypes(boolean getEmpty) {
    List<CubeType> cubeTypes = new ArrayList<CubeType>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_ID);
    q.append(" FROM ").append(DB.TABLE_CUBETYPE);
    if (!getEmpty) {
      q.append(" WHERE 0 < (");
      q.append("     SELECT COUNT(*) FROM ").append(DB.TABLE_SOLVETYPE);
      q.append("     WHERE ").append(DB.TABLE_SOLVETYPE).append(".").append(DB.COL_SOLVETYPE_CUBETYPE_ID);
      q.append("         = ").append(DB.TABLE_CUBETYPE).append(".").append(DB.COL_ID).append(")");
    }
    Cursor cursor = db.rawQuery(q.toString(), null);
    if (cursor != null) {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        cubeTypes.add(CubeType.getCubeType(cursor.getInt(0)));
      }
      cursor.close();
    }
    return cubeTypes;
  }

  @Override
  public List<SolveType> getSolveTypes(CubeType cubeType) {
    List<SolveType> solveTypes = new ArrayList<SolveType>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_ID);
    q.append(", ").append(DB.COL_SOLVETYPE_NAME);
    q.append(", ").append(DB.COL_SOLVETYPE_BLIND);
    q.append(", ").append(DB.COL_SOLVETYPE_CUBETYPE_ID);
    q.append(" FROM ").append(DB.TABLE_SOLVETYPE);
    q.append(" WHERE ").append(DB.COL_SOLVETYPE_CUBETYPE_ID).append(" = ?");
    q.append(" ORDER BY ").append(DB.COL_SOLVETYPE_POSITION);
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(cubeType.getId()));
    if (cursor != null) {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        SolveType st = new SolveType(cursor.getInt(0), cursor.getString(1), (cursor.getInt(2) == 1), cursor.getInt(3));
        st.setSteps(getSolveTypeSteps(st).toArray(new SolveTypeStep[0]));
        solveTypes.add(st);
      }
      cursor.close();
    }
    return solveTypes;
  }

  private List<SolveTypeStep> getSolveTypeSteps(SolveType solveType) {
    List<SolveTypeStep> solveTypeSteps = new ArrayList<SolveTypeStep>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_ID).append(", ").append(DB.COL_SOLVETYPESTEP_NAME);
    q.append(" FROM ").append(DB.TABLE_SOLVETYPESTEP);
    q.append(" WHERE ").append(DB.COL_SOLVETYPESTEP_SOLVETYPE_ID).append(" = ?");
    q.append(" ORDER BY ").append(DB.COL_SOLVETYPESTEP_POSITION);
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveType.getId()));
    if (cursor != null) {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        SolveTypeStep st = new SolveTypeStep();
        st.setId(cursor.getInt(0));
        st.setName(cursor.getString(1));
        solveTypeSteps.add(st);
      }
      cursor.close();
    }
    return solveTypeSteps;
  }

  @Override
  public SolveAverages saveTime(SolveTime solveTime) {
    syncCaches(solveTime.getSolveType());
    if (solveTime.getId() > 0) {
      return updateTime(solveTime);
    } else {
      return createTime(solveTime);
    }
  }

  private SolveAverages updateTime(SolveTime solveTime) {
    ContentValues values = new ContentValues();
    values.put(DB.COL_TIMEHISTORY_TIME, solveTime.getTime());
    values.put(DB.COL_TIMEHISTORY_PLUSTWO, solveTime.isPlusTwo() ? 1 : 0);
    db.update(DB.TABLE_TIMEHISTORY, values, DB.COL_ID + " = ?", getStringArray(solveTime.getId()));
    synchronized (cacheSyncHelper) {
      for (CachedTime ct : cachedSolveTimes) {
        if (ct.getSolveId() == solveTime.getId()) {
          ct.setTime(solveTime.getTime());
          break;
        }
      }
    }

    recalculateAverages(solveTime.getTimestamp(), solveTime.getSolveType());

    loadBestAverages(solveTime.getSolveType().getId());
    loadLifetimeBest(solveTime.getSolveType().getId());
    SolveAverages solveAverages = getSolveAverages(solveTime.getSolveType());
    solveAverages.setSolveTime(solveTime);

    return solveAverages;
  }

  private SolveAverages createTime(SolveTime solveTime) {
    CachedTime cachedTime = new CachedTime(solveTime);
    synchronized (cacheSyncHelper) {
      cachedSolveTimes.add(0, cachedTime);
      if (cachedSolveTimes.size() > CACHE_MAX_SIZE) {
        cachedSolveTimes.remove(cachedSolveTimes.size() - 1);
      }
    }
    Long avg5 = getLastAvg5();
    Long avg12 = getLastAvg(12);
    Long avg50 = getLastAvg(50);
    Long avg100 = getLastAvg(100);

    syncBestAveragesWithCurrent(avg5, avg12, avg50, avg100);
    if (isTimeBetter(cachedLifetimeBest, solveTime.getTime())) {
      cachedLifetimeBest = solveTime.getTime();
    }

    ContentValues values = new ContentValues();
    values.put(DB.COL_TIMEHISTORY_TIME, solveTime.getTime());
    values.put(DB.COL_TIMEHISTORY_SOLVETYPE_ID, solveTime.getSolveType().getId());
    values.put(DB.COL_TIMEHISTORY_SCRAMBLE, solveTime.getScramble());
    values.put(DB.COL_TIMEHISTORY_TIMESTAMP, solveTime.getTimestamp());
    values.put(DB.COL_TIMEHISTORY_PLUSTWO, solveTime.isPlusTwo() ? 1 : 0);
    values.put(DB.COL_TIMEHISTORY_AVG5, avg5);
    values.put(DB.COL_TIMEHISTORY_AVG12, avg12);
    values.put(DB.COL_TIMEHISTORY_AVG50, avg50);
    values.put(DB.COL_TIMEHISTORY_AVG100, avg100);
    long historyId = db.insert(DB.TABLE_TIMEHISTORY, null, values);
    if (solveTime.hasSteps()) {
      Iterator<SolveTypeStep> stsIt = getSolveTypeSteps(solveTime.getSolveType()).iterator();
      for (Long stepTime : solveTime.getStepsTimes()) {
        values = new ContentValues();
        values.put(DB.COL_TIMEHISTORYSTEP_TIME, stepTime);
        values.put(DB.COL_TIMEHISTORYSTEP_SOLVETYPESTEP_ID, stsIt.next().getId());
        values.put(DB.COL_TIMEHISTORYSTEP_TIMEHISTORY_ID, historyId);
        db.insert(DB.TABLE_TIMEHISTORYSTEP, null, values);
      }
    }
    cachedTime.setSolveId((int) historyId);
    solveTime.setId((int) historyId);

    SolveAverages solveAverages = getSolveAverages(solveTime.getSolveType());
    solveAverages.setSolveTime(solveTime);
    return solveAverages;
  }

  @Override
  public SolveAverages getSolveAverages(SolveType solveType) {
    syncCaches(solveType);
    SolveAverages solveAverages = new SolveAverages();
    if (!solveType.hasSteps()) {
      solveAverages.setAvgOf5(getLastAvg5());
      solveAverages.setAvgOf12(getLastAvg(12));
      solveAverages.setAvgOf50(getLastAvg(50));
      solveAverages.setAvgOf100(getLastAvg(100));
      solveAverages.setAvgOfLifetime(getLastMean(1000, true));
      solveAverages.setBestOf5(cachedBestAverages.get(5));
      solveAverages.setBestOf12(cachedBestAverages.get(12));
      solveAverages.setBestOf50(cachedBestAverages.get(50));
      solveAverages.setBestOf100(cachedBestAverages.get(100));
      solveAverages.setBestOfLifetime(cachedLifetimeBest);
    } else {
      setStepsAverages(solveAverages, solveType);
    }
    return solveAverages;
  }

  void setStepsAverages(SolveAverages solveAverages, SolveType solveType) {
    if (!solveType.hasSteps()) {
      return;
    }
    List<List<Long>> stepsTimes = new ArrayList<List<Long>>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.TABLE_SOLVETYPESTEP).append(".").append(DB.COL_SOLVETYPESTEP_POSITION).append(",");
    q.append("       ").append(DB.TABLE_TIMEHISTORYSTEP).append(".").append(DB.COL_TIMEHISTORYSTEP_TIME);
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
    q.append(" JOIN ").append(DB.TABLE_TIMEHISTORYSTEP);
    q.append("   ON ").append(DB.TABLE_TIMEHISTORYSTEP).append(".").append(DB.COL_TIMEHISTORYSTEP_TIMEHISTORY_ID);
    q.append("    = ").append(DB.TABLE_TIMEHISTORY).append(".").append(DB.COL_ID);
    q.append(" JOIN ").append(DB.TABLE_SOLVETYPESTEP);
    q.append("   ON ").append(DB.TABLE_SOLVETYPESTEP).append(".").append(DB.COL_ID);
    q.append("    = ").append(DB.TABLE_TIMEHISTORYSTEP).append(".").append(DB.COL_TIMEHISTORYSTEP_SOLVETYPESTEP_ID);
    q.append(" WHERE ").append(DB.TABLE_TIMEHISTORY).append(".").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
    q.append("   AND ").append(DB.TABLE_TIMEHISTORY).append(".").append(DB.COL_TIMEHISTORY_TIME).append(" > 0");
    q.append(" ORDER BY ").append(DB.COL_SOLVETYPESTEP_POSITION).append(", ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" DESC");
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveType.getId()));
    if (cursor != null) {
      int curStep = -1;
      List<Long> times = new ArrayList<Long>();
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        int step = cursor.getInt(0);
        if (step != curStep) {
          if (times.size() > 0) {
            stepsTimes.add(times);
          }
          times = new ArrayList<Long>();
          curStep = step;
        }
        times.add(cursor.getLong(1));
      }
      if (times.size() > 0) {
        stepsTimes.add(times);
      }
      cursor.close();
    }

    if (stepsTimes.size() > 0) {
      Map<Integer, List<Long>> averages = new HashMap<Integer, List<Long>>();
      long[] stepsSums = new long[stepsTimes.size()];
      int timesCount = stepsTimes.get(0).size();
      int[] avgsToGet = new int[] { 5, 12, 50, 100 };
      for (int i = 0; i < stepsTimes.size(); i++) {
        List<Long> st = stepsTimes.get(i);
        CubeBaseSession session = new CubeBaseSession(st);
        for (int a : avgsToGet) {
          if (averages.get(a) == null) {
            averages.put(a, new ArrayList<Long>());
          }
          averages.get(a).add(session.getRAOf(a));
        }
        for (int j = 0; j < st.size(); j++) {
          stepsSums[i] += st.get(j);
        }
      }
      for (int a : avgsToGet) {
        List<Long> avgs = averages.get(a);
        if (avgs.get(0) == -2) {
          avgs = null;
        }
        solveAverages.setStepsAvgOf(a, avgs);
      }

      List<Long> lifeAvgs = new ArrayList<Long>();
      for (long l : stepsSums) {
        lifeAvgs.add(l / timesCount);
      }
      solveAverages.setStepsAvgOfLifetime(lifeAvgs);
    }
  }

  private void syncBestAveragesWithCurrent(Long avg5, Long avg12, Long avg50, Long avg100) {
    if (isTimeBetter(cachedBestAverages.get(5), avg5) || avg5 == null) {
      cachedBestAverages.put(5, avg5);
    }
    if (isTimeBetter(cachedBestAverages.get(12), avg12) || avg12 == null) {
      cachedBestAverages.put(12, avg12);
    }
    if (isTimeBetter(cachedBestAverages.get(50), avg50) || avg50 == null) {
      cachedBestAverages.put(50, avg50);
    }
    if (isTimeBetter(cachedBestAverages.get(100), avg100) || avg100 == null) {
      cachedBestAverages.put(100, avg100);
    }
  }

  private boolean isTimeBetter(Long oldVal, Long newVal) {
    if (oldVal == null && newVal != null && newVal > 0) {
      return true;
    } else if (oldVal != null && newVal != null && newVal < oldVal && newVal > 0) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public SolveAverages deleteTime(SolveTime solveTime) {
    int i = 0;
    syncCaches(solveTime.getSolveType());
    // remove from cache
    synchronized (cacheSyncHelper) {
      for (CachedTime ct : cachedSolveTimes) {
        if (ct.getSolveId() == solveTime.getId()) {
          cachedSolveTimes.remove(i);
          break;
        }
        i++;
      }
    }

    // remove from DB
    db.delete(DB.TABLE_TIMEHISTORY, DB.COL_ID + " = ?", getStringArray(solveTime.getId()));

    if (cachedSolveTimes.size() == CACHE_MIN_SIZE) { // re-read the cache if we reach the minimum size
      loadSolveTimes(solveTime.getSolveType().getId());
    }

    recalculateAverages(solveTime.getTimestamp(), solveTime.getSolveType());
    loadBestAverages(solveTime.getSolveType().getId());
    loadLifetimeBest(solveTime.getSolveType().getId());

    return getSolveAverages(solveTime.getSolveType());
  }

  @Override
  public SolveHistory getHistory(SolveType solveType) {
    return getHistory(solveType, System.currentTimeMillis());
  }

  @Override
  public SolveHistory getHistory(SolveType solveType, long from) {
    SolveHistory solveHistory = new SolveHistory();
    solveHistory.setSolveTimes(getHistoryTimes(solveType, from, HISTORY_PAGE_SIZE));
    solveHistory.setSolvesCount(getHistorySolvesCount(solveType));
    return solveHistory;
  }

  public List<SolveTime> getHistoryTimes(SolveType solveType, long from, Integer pageSize) {
    List<SolveTime> history = new ArrayList<SolveTime>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_ID);
    q.append("     , ").append(DB.COL_TIMEHISTORY_TIME);
    q.append("     , ").append(DB.COL_TIMEHISTORY_TIMESTAMP);
    q.append("     , ").append(DB.COL_TIMEHISTORY_SCRAMBLE);
    q.append("     , ").append(DB.COL_TIMEHISTORY_PLUSTWO);
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
    q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
    q.append("   AND ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" < ?");
    q.append(" ORDER BY ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" DESC");
    if (pageSize != null) {
      q.append(" LIMIT ").append(HISTORY_PAGE_SIZE);
    }
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveType.getId(), from));
    if (cursor != null) {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        SolveTime st = new SolveTime();
        st.setId(cursor.getInt(0));
        st.setTime(cursor.getInt(1));
        st.setTimestamp(cursor.getLong(2));
        st.setScramble(cursor.getString(3));
        st.setPlusTwo(cursor.getInt(4) == 1);
        st.setSolveType(solveType);
        if (solveType.hasSteps()) {
          List<Long> stepTimes = getSolveTimeSteps(st);
          st.setStepsTimes(stepTimes.size() == 0 ? null : stepTimes.toArray(new Long[0]));
        }
        history.add(st);
      }
      cursor.close();
    }
    return history;
  }

  private int getHistorySolvesCount(SolveType solveType) {
    int solvesCount = 0;
    StringBuilder q = new StringBuilder();
    q.append("SELECT COUNT(*)");
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
    q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveType.getId()));
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        solvesCount = cursor.getInt(0);
      }
    }
    return solvesCount;
  }

  private List<Long> getSolveTimeSteps(SolveTime solveTime) {
    List<Long> stepTimes = new ArrayList<Long>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_TIMEHISTORYSTEP_TIME);
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORYSTEP);
    q.append(" JOIN ").append(DB.TABLE_SOLVETYPESTEP);
    q.append("   ON ").append(DB.TABLE_SOLVETYPESTEP).append(".").append(DB.COL_ID);
    q.append("    = ").append(DB.TABLE_TIMEHISTORYSTEP).append(".").append(DB.COL_TIMEHISTORYSTEP_SOLVETYPESTEP_ID);
    q.append(" WHERE ").append(DB.COL_TIMEHISTORYSTEP_TIMEHISTORY_ID).append(" = ?");
    q.append(" ORDER BY ").append(DB.COL_SOLVETYPESTEP_POSITION);
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveTime.getId()));
    if (cursor != null) {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        stepTimes.add(cursor.getLong(0));
      }
      cursor.close();
    }
    return stepTimes;
  }

  @Override
  public void deleteHistory() {
    db.delete(DB.TABLE_TIMEHISTORY, null, null);
    clearCaches();
  }

  @Override
  public void deleteHistory(SolveType solveType) {
    for (SolveTypeStep step : solveType.getSteps()) {
      db.delete(DB.TABLE_TIMEHISTORYSTEP, DB.COL_TIMEHISTORYSTEP_SOLVETYPESTEP_ID + " = ?", getStringArray(step.getId()));
    }
    db.delete(DB.TABLE_TIMEHISTORY, DB.COL_TIMEHISTORY_SOLVETYPE_ID + " = ?", getStringArray(solveType.getId()));
    if (solveType.equals(currentSolveType)) {
      clearCaches();
    }
  }

  /**
   * Returns the last session solve times.
   * This returns the last n times, searching from the last session start,
   * or from the beginning if no session was created.
   * The number of returned results is limited by SESSION_TIMES_COUNT.
   * @param solveType the solve type
   * @return the list of most recent solve times
   */
  @Override
  public List<Long> getSessionTimes(SolveType solveType) {
    return getSessionTimes(solveType, SESSION_TIMES_COUNT);
  }

  public List<Long> getSessionTimes(SolveType solveType, Integer limit) {
    List<Long> sessionTimes = new ArrayList<Long>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_TIMEHISTORY_TIME);
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
    q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
    q.append("   AND ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" >= ");
    q.append("     (SELECT ").append(DB.COL_SOLVETYPE_SESSION_START);
    q.append("      FROM ").append(DB.TABLE_SOLVETYPE);
    q.append("      WHERE ").append(DB.COL_ID).append(" = ?)");
    q.append(" ORDER BY ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" DESC");
    if (limit != null) {
      q.append(" LIMIT ").append(limit);
    }
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveType.getId(), solveType.getId()));
    if (cursor != null) {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        sessionTimes.add(cursor.getLong(0));
      }
      cursor.close();
    }
    return sessionTimes;
  }

  @Override
  public void startNewSession(SolveType solveType, long startTs) {
    ContentValues values = new ContentValues();
    values.put(DB.COL_SOLVETYPE_SESSION_START, startTs);
    db.update(DB.TABLE_SOLVETYPE, values, DB.COL_ID + " = ?", getStringArray(solveType.getId()));
  }

  @Override
  public long getSessionStart(SolveType solveType) {
    long sessionStart = 0;
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_SOLVETYPE_SESSION_START);
    q.append(" FROM ").append(DB.TABLE_SOLVETYPE);
    q.append(" WHERE ").append(DB.COL_ID).append(" = ?");
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveType.getId()));
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        sessionStart = cursor.getLong(0);
      }
      cursor.close();
    }
    return sessionStart;
  }

  @Override
  public int addSolveType(SolveType solveType) {
    int position = -1;
    // retrieve the position (add it as the last solve type)
    StringBuilder q = new StringBuilder();
    q.append("SELECT MAX(").append(DB.COL_SOLVETYPE_POSITION).append(")");
    q.append(" FROM ").append(DB.TABLE_SOLVETYPE);
    q.append(" WHERE ").append(DB.COL_SOLVETYPE_CUBETYPE_ID).append(" = ?");
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveType.getCubeTypeId()));
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        position = cursor.getInt(0);
        position++;
      }
      cursor.close();
    }
    if (position < 0) {
      position = 1;
    }

    ContentValues values = new ContentValues();
    values.put(DB.COL_SOLVETYPE_NAME, solveType.getName());
    values.put(DB.COL_SOLVETYPE_POSITION, position);
    values.put(DB.COL_SOLVETYPE_CUBETYPE_ID, solveType.getCubeTypeId());
    values.put(DB.COL_SOLVETYPE_BLIND, solveType.isBlind() ? 1 : 0);
    int id = (int) db.insert(DB.TABLE_SOLVETYPE, null, values);
    solveType.setId(id);

    if (solveType.hasSteps()) {
      addSolveTypeSteps(solveType);
    }
    return id;
  }

  @Override
  public void addSolveTypeSteps(SolveType solveType) {
    // only add steps if it does not already have some (we can't edit the steps because it would mess up the times)
    if (solveType.getId() == 0 || getSolveTypeSteps(solveType).size() > 0) {
      return;
    }
    int i = 1;
    for (SolveTypeStep step : solveType.getSteps()) {
      ContentValues values = new ContentValues();
      values.put(DB.COL_SOLVETYPESTEP_NAME, step.getName());
      values.put(DB.COL_SOLVETYPESTEP_POSITION, i);
      values.put(DB.COL_SOLVETYPESTEP_SOLVETYPE_ID, solveType.getId());
      int stepId = (int) db.insert(DB.TABLE_SOLVETYPESTEP, null, values);
      step.setId(stepId);
      i++;
    }
  }

  @Override
  public void updateSolveType(SolveType solveType) {
    ContentValues values = new ContentValues();
    values.put(DB.COL_SOLVETYPE_NAME, solveType.getName());
    db.update(DB.TABLE_SOLVETYPE, values, DB.COL_ID + " = ?", getStringArray(solveType.getId()));
  }

  @Override
  public void deleteSolveType(SolveType solveType) {
    for (SolveTypeStep step : solveType.getSteps()) {
      db.delete(DB.TABLE_TIMEHISTORYSTEP, DB.COL_TIMEHISTORYSTEP_SOLVETYPESTEP_ID + " = ?", getStringArray(step.getId()));
    }
    db.delete(DB.TABLE_TIMEHISTORY, DB.COL_TIMEHISTORY_SOLVETYPE_ID + " = ?", getStringArray(solveType.getId()));
    db.delete(DB.TABLE_SOLVETYPESTEP, DB.COL_SOLVETYPESTEP_SOLVETYPE_ID + " = ?", getStringArray(solveType.getId()));
    db.delete(DB.TABLE_SOLVETYPE, DB.COL_ID + " = ?", getStringArray(solveType.getId()));
    if (solveType.equals(currentSolveType)) {
      clearCaches();
    }
  }

  @Override
  public void saveSolveTypesOrder(List<SolveType> solveTypes) {
    int i = 1;
    for (SolveType st : solveTypes) {
      ContentValues values = new ContentValues();
      values.put(DB.COL_SOLVETYPE_POSITION, i);
      db.update(DB.TABLE_SOLVETYPE, values, DB.COL_ID + " = ?", getStringArray(st.getId()));
      i++;
    }
  }

  @Override
  public SolveTimeAverages getSolveTimeAverages(SolveTime solveTime) {
    SolveTimeAverages sta = null;
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_ID);
    q.append("     , ").append(DB.COL_TIMEHISTORY_TIME);
    q.append("     , ").append(DB.COL_TIMEHISTORY_TIMESTAMP);
    q.append("     , ").append(DB.COL_TIMEHISTORY_SCRAMBLE);
    q.append("     , ").append(DB.COL_TIMEHISTORY_PLUSTWO);
    q.append("     , ").append(DB.COL_TIMEHISTORY_AVG5);
    q.append("     , ").append(DB.COL_TIMEHISTORY_AVG12);
    q.append("     , ").append(DB.COL_TIMEHISTORY_AVG50);
    q.append("     , ").append(DB.COL_TIMEHISTORY_AVG100);
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
    q.append(" WHERE ").append(DB.COL_ID).append(" = ?");
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveTime.getId()));
    if (cursor != null) {
      cursor.moveToFirst();
      if (!cursor.isAfterLast()) {
        sta = new SolveTimeAverages();
        sta.setId(cursor.getInt(0));
        sta.setTime(cursor.getInt(1));
        sta.setTimestamp(cursor.getLong(2));
        sta.setScramble(cursor.getString(3));
        sta.setPlusTwo(cursor.getInt(4) == 1);
        Long v = getCursorLong(cursor, 5);
        sta.setAvgOf5(v == null || v == -2 ? null : v);
        v = getCursorLong(cursor, 6);
        sta.setAvgOf12(v == null || v == -2 ? null : v);
        v = getCursorLong(cursor, 7);
        sta.setAvgOf50(v == null || v == -2 ? null : v);
        v = getCursorLong(cursor, 8);
        sta.setAvgOf100(v == null || v == -2 ? null : v);
        sta.setSolveType(solveTime.getSolveType());
      }
      cursor.close();
    }
    return sta;
  }

  /**
   * Returns the detailed list of session times, and the total solves count.
   * The list of times will only be set if a new session was created for this solve type.
   * It will be null if no session was ever created for this solve type.
   * @param solveType the solve type
   * @return the session details
   */
  @Override
  public SessionDetails getSessionDetails(SolveType solveType) {
    SessionDetails sessionDetails = new SessionDetails();
    sessionDetails.setTotalSolvesCount(getHistorySolvesCount(solveType));
    if (getSessionStart(solveType) > 0) { // if a new session was created
      sessionDetails.setSessionTimes(getSessionTimes(solveType, null));
    }
    return sessionDetails;
  }

  /**
   * Returns the solves count, starting from session start if a session was created, or from start if no session exists
   * @param solveType the solve type
   * @return the solves count
   */
  @Override
  public int getSolvesCount(SolveType solveType) {
    int solvesCount = 0;
    long sessionStart = getSessionStart(solveType);
    StringBuilder q = new StringBuilder();
    q.append("SELECT COUNT(*)");
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
    q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
    q.append("   AND ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" >= ?");
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveType.getId(), sessionStart));
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        solvesCount = cursor.getInt(0);
      }
    }
    return solvesCount;
  }

  private void recalculateAverages(long timestamp, SolveType solveType) {
    List<CachedTime> timesBefore = getTimesAroundTs(timestamp, solveType, true);
    List<CachedTime> timesAfter = getTimesAroundTs(timestamp, solveType, false);

    for (int i = timesAfter.size() - 1; i >= 0; i--) { // newest time is in pos 0
      CachedTime ct = timesAfter.get(i);
      List<Long> times = new ArrayList<Long>();
      for (int j = i; j < timesAfter.size() && times.size() < MAX_AVERAGE_COUNT; j++) {
        times.add(timesAfter.get(j).getTime());
      }
      for (int j = 0; j < timesBefore.size() && times.size() < MAX_AVERAGE_COUNT; j++) {
        times.add(timesBefore.get(j).getTime());
      }
      CubeBaseSession session = new CubeBaseSession(times);

      ContentValues values = new ContentValues();
      if (solveType.isBlind()) {
        values.put(DB.COL_TIMEHISTORY_AVG5, session.getMeanOf(3));
      } else {
        values.put(DB.COL_TIMEHISTORY_AVG5, session.getRAOf(5));
      }
      values.put(DB.COL_TIMEHISTORY_AVG12, session.getRAOf(12));
      values.put(DB.COL_TIMEHISTORY_AVG50, session.getRAOf(50));
      values.put(DB.COL_TIMEHISTORY_AVG100, session.getRAOf(100));

      db.update(DB.TABLE_TIMEHISTORY, values, DB.COL_ID + " = ?", getStringArray(ct.getSolveId()));
    }
  }

  private List<CachedTime> getTimesAroundTs(long timestamp, SolveType solveType, boolean before) {
    List<CachedTime> times = new ArrayList<CachedTime>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_ID);
    q.append("     , ").append(DB.COL_TIMEHISTORY_TIME);
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
    q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
    if (before) {
      q.append("   AND ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" < ?");
    } else {
      q.append("   AND ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" >= ?");
    }
    q.append(" ORDER BY ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" DESC");
    if (before) { // only limit if searching before ts (otherwise it would not return the times right after ts if there are more times than MAX)
      q.append(" LIMIT ").append(MAX_AVERAGE_COUNT);
    }
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveType.getId(), timestamp));
    if (cursor != null) {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        times.add(new CachedTime(cursor.getInt(0), cursor.getLong(1)));
      }
    }
    return times;
  }

  private void syncCaches(SolveType solveType) {
    if (cachedSolveTimes == null || currentSolveType == null || solveType.getId() != currentSolveType.getId()) {
      currentSolveType = solveType;
      loadSolveTimes(solveType.getId());
      loadBestAverages(solveType.getId());
      loadLifetimeBest(solveType.getId());
    }
  }

  private List<CachedTime> loadSolveTimes(int solveTypeId) {
    synchronized (cacheSyncHelper) {
      cachedSolveTimes = new ArrayList<CachedTime>();
      StringBuilder q = new StringBuilder();
      q.append("SELECT ").append(DB.COL_ID).append(", ").append(DB.COL_TIMEHISTORY_TIME);
      q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
      q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
      q.append(" ORDER BY ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" DESC");
      q.append(" LIMIT ").append(CACHE_MAX_SIZE);
      Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveTypeId));
      if (cursor != null) {
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
          cachedSolveTimes.add(new CachedTime(cursor.getInt(0), cursor.getLong(1)));
        }
        cursor.close();
      }
      return cachedSolveTimes;
    }
  }

  private Map<Integer, Long> loadBestAverages(int solveTypeId) {
    cachedBestAverages = new HashMap<Integer, Long>();
    Map<String, Integer> colToAvgMapping = new HashMap<String, Integer>();
    colToAvgMapping.put(DB.COL_TIMEHISTORY_AVG5, 5);
    colToAvgMapping.put(DB.COL_TIMEHISTORY_AVG12, 12);
    colToAvgMapping.put(DB.COL_TIMEHISTORY_AVG50, 50);
    colToAvgMapping.put(DB.COL_TIMEHISTORY_AVG100, 100);

    for (Entry<String, Integer> p : colToAvgMapping.entrySet()) {
      String colName = p.getKey();
      StringBuilder q = new StringBuilder();
      q.append("SELECT MIN(").append(colName).append(")");
      q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
      q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
      q.append("   AND ").append(colName).append(" > 0");
      Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveTypeId));
      if (cursor != null) {
        if (cursor.moveToFirst()) {
          cachedBestAverages.put(p.getValue(), getCursorLong(cursor, 0));
        }
        cursor.close();
      }
    }
    return cachedBestAverages;
  }

  private Long loadLifetimeBest(int solveTypeId) {
    cachedLifetimeBest = -2l; // N/A
    StringBuilder q = new StringBuilder();
    q.append("SELECT MIN(").append(DB.COL_TIMEHISTORY_TIME).append(")");
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
    q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
    q.append("   AND ").append(DB.COL_TIMEHISTORY_TIME).append(" > 0");
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveTypeId));
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        cachedLifetimeBest = getCursorLong(cursor, 0);
      }
      cursor.close();
    }
    return cachedLifetimeBest;
  }

  private void clearCaches() {
    cachedSolveTimes = null;
    cachedBestAverages = new HashMap<Integer, Long>();
    cachedLifetimeBest = -2l;
  }

  private Long getLastAvg5() {
    Long avg5;
    if (currentSolveType.isBlind()) {
      // Get mean of 3 instead
      List<Long> times = new ArrayList<Long>();
      synchronized (cacheSyncHelper) {
        for (int i = 0; i < cachedSolveTimes.size() && i < 3; i++) {
          times.add(cachedSolveTimes.get(i).getTime());
        }
      }
      CubeBaseSession session = new CubeBaseSession(times);
      long avg = session.getMeanOf(3);
      avg5 = (avg == -2) ? null : avg;
    } else {
      avg5 = getLastAvg(5);
    }
    return avg5;
  }

  /**
   * Calculate the last average based on the cached time.
   * The last average is the average of solve times, starting from the most recent time.
   * @param n number of solve times for which to retrieve the average
   * @return the last average
   */
  private Long getLastAvg(int n) {
    List<Long> times = new ArrayList<Long>();
    synchronized (cacheSyncHelper) {
      for (int i = 0; i < cachedSolveTimes.size() && i < n; i++) {
        times.add(cachedSolveTimes.get(i).getTime());
      }
    }
    CubeBaseSession session = new CubeBaseSession(times);
    long avg = session.getRAOf(n);
    return (avg == -2) ? null : avg;
  }

  /**
   * Calculates the last mean (not rolling average) based on the cached times, only counting non DNF times.
   * @param n number of solve times for which to retrieve the mean
   * @param calculateAll if true, returns the mean even if the number of solves did not reach n
   * @return the mean of n
   */
  private Long getLastMean(int n, boolean calculateAll) {
    long total = 0;
    int i = 0;
    synchronized (cacheSyncHelper) {
      for (CachedTime ct : cachedSolveTimes) {
        long t = ct.getTime();
        if (t > 0) { // if not a DNF
          total += t;
          i++;
          if (i == n) {
            return total / n;
          }
        }
      }
    }
    if (i > 0 && calculateAll) {
      return total / i;
    } else {
      return null;
    }
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

  private String[] getStringArray(long... values) {
    String[] ret = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      ret[i] = String.valueOf(values[i]);
    }
    return ret;
  }

  class CachedTime {
    private int solveId;
    private long time;

    public CachedTime(SolveTime solveTime) {
      this.solveId = solveTime.getId();
      this.time = solveTime.getTime();
    }

    public CachedTime(int solveId, long time) {
      this.solveId = solveId;
      this.time = time;
    }

    public int getSolveId() {
      return solveId;
    }

    public void setSolveId(int solveId) {
      this.solveId = solveId;
    }

    public long getTime() {
      return time;
    }

    public void setTime(long time) {
      this.time = time;
    }
  }

  private class SolveAverage {
    private int id;
    private long time;
    private long timestamp;
    private Long avg5;
    private Long avg12;
    private Long avg50;
    private Long avg100;

    public SolveAverage(int id, long time, long timestamp, Long avg5, Long avg12, Long avg50, Long avg100) {
      this.id = id;
      this.time = time;
      this.avg5 = avg5;
      this.avg12 = avg12;
      this.avg50 = avg50;
      this.avg100 = avg100;
    }

    public int getId() {
      return id;
    }

    public long getTime() {
      return time;
    }

    public long getTimestamp() {
      return timestamp;
    }

    public Long getAvg5() {
      return avg5;
    }

    public Long getAvg12() {
      return avg12;
    }

    public Long getAvg50() {
      return avg50;
    }

    public Long getAvg100() {
      return avg100;
    }
  }

}
