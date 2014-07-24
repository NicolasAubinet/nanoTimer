package com.cube.nanotimer.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.cube.nanotimer.services.db.DB;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;
import com.cube.nanotimer.vo.SolveTypeStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ServiceProviderImpl implements ServiceProvider {

  private SQLiteDatabase db;

  private SolveType currentSolveType;
  private List<CachedTime> cachedSolveTimes;
  private Map<Integer, Long> cachedBestAverages;
  private Long cachedLifetimeBest;

  private final int CACHE_MAX_SIZE = 1010;
  private final int CACHE_MIN_SIZE = 1000;
  private final int HISTORY_PAGE_SIZE = 20;
  private final int SESSION_TIMES_COUNT = 12;
  private final int MAX_AVERAGE_COUNT = 100;

  public ServiceProviderImpl(SQLiteDatabase db) {
    this.db = db;
  }

  @Override
  public List<CubeType> getCubeTypes() {
    List<CubeType> cubeTypes = new ArrayList<CubeType>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_ID).append(", ").append(DB.COL_CUBETYPE_NAME);
    q.append(" FROM ").append(DB.TABLE_CUBETYPE);
    q.append(" WHERE 0 < (");
    q.append("     SELECT COUNT(*) FROM ").append(DB.TABLE_SOLVETYPE);
    q.append("     WHERE ").append(DB.TABLE_SOLVETYPE).append(".").append(DB.COL_SOLVETYPE_CUBETYPE_ID);
    q.append("         = ").append(DB.TABLE_CUBETYPE).append(".").append(DB.COL_ID).append(")");
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
    q.append(", ").append(DB.COL_SOLVETYPE_CUBETYPE_ID);
    q.append(" FROM ").append(DB.TABLE_SOLVETYPE);
    q.append(" WHERE ").append(DB.COL_SOLVETYPE_CUBETYPE_ID).append(" = ?");
    q.append(" ORDER BY ").append(DB.COL_SOLVETYPE_POSITION);
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(cubeType.getId()));
    if (cursor != null) {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        SolveType st = new SolveType(cursor.getInt(0), cursor.getString(1), cursor.getInt(2));
        List<SolveTypeStep> steps = getSolveTypeSteps(st);
        st.setSteps(steps.size() == 0 ? null : steps.toArray(new SolveTypeStep[0]));
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
    for (CachedTime ct : cachedSolveTimes) {
      if (ct.getSolveId() == solveTime.getId()) {
        ct.setTime(solveTime.getTime());
        break;
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
    cachedSolveTimes.add(0, cachedTime);
    if (cachedSolveTimes.size() > CACHE_MAX_SIZE) {
      cachedSolveTimes.remove(cachedSolveTimes.size() - 1);
    }
    Long avg5 = getLastAvg(5, false);
    Long avg12 = getLastAvg(12, false);
    Long avg100 = getLastAvg(100, false);

    syncBestAveragesWithCurrent(avg5, avg12, avg100);
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
      solveAverages.setAvgOf5(getLastAvg(5, false));
      solveAverages.setAvgOf12(getLastAvg(12, false));
      solveAverages.setAvgOf100(getLastAvg(100, false));
      solveAverages.setAvgOfLifetime(getLastAvg(1000, true));
      solveAverages.setBestOf5(cachedBestAverages.get(5));
      solveAverages.setBestOf12(cachedBestAverages.get(12));
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
    q.append("SELECT ").append(DB.TABLE_TIMEHISTORY).append(".").append(DB.COL_ID).append(",");
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
    q.append(" ORDER BY ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" DESC, ").append(DB.COL_SOLVETYPESTEP_POSITION);
    q.append(" LIMIT ").append(MAX_AVERAGE_COUNT);
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveType.getId()));
    if (cursor != null) {
      int curHistoryId = -1;
      List<Long> curSteps = new ArrayList<Long>();
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        int historyId = cursor.getInt(0);
        if (historyId != curHistoryId) {
          if (curSteps.size() > 0) {
            stepsTimes.add(curSteps);
          }
          curSteps = new ArrayList<Long>();
          curHistoryId = historyId;
        }
        curSteps.add(cursor.getLong(1));
      }
      if (curSteps.size() > 0) {
        stepsTimes.add(curSteps);
      }
      cursor.close();
    }

    if (stepsTimes.size() > 0) {
      int i = 0;
      long[] stepsSums = new long[stepsTimes.get(0).size()];
      for (List<Long> st : stepsTimes) {
        for (int j = 0; j < st.size(); j++) {
          stepsSums[j] += st.get(j);
        }
        if (i == 5-1 || i == 12-1 || i == 100-1) {
          List<Long> avgs = new ArrayList<Long>();
          for (Long l : stepsSums) {
            avgs.add(l / (i+1));
          }
          solveAverages.setStepsAvgOf(i + 1, avgs);
        }
        i++;
      }
      List<Long> lifeAvgs = new ArrayList<Long>();
      for (Long l : stepsSums) {
        lifeAvgs.add(l / i);
      }
      solveAverages.setStepsAvgOfLifetime(lifeAvgs);
    }
  }

  private void syncBestAveragesWithCurrent(Long avg5, Long avg12, Long avg100) {
    if (isTimeBetter(cachedBestAverages.get(5), avg5) || avg5 == null) {
      cachedBestAverages.put(5, avg5);
    }
    if (isTimeBetter(cachedBestAverages.get(12), avg12) || avg12 == null) {
      cachedBestAverages.put(12, avg12);
    }
    if (isTimeBetter(cachedBestAverages.get(100), avg100) || avg100 == null) {
      cachedBestAverages.put(100, avg100);
    }
  }

  private boolean isTimeBetter(Long oldVal, Long newVal) {
    if (oldVal == null && newVal != null) {
      return true;
    } else if (oldVal != null && newVal != null && newVal < oldVal && newVal > 0) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public SolveAverages removeTime(SolveTime solveTime) {
    int i = 0;
    syncCaches(solveTime.getSolveType());
    // remove from cache
    for (CachedTime ct : cachedSolveTimes) {
      if (ct.getSolveId() == solveTime.getId()) {
        cachedSolveTimes.remove(i);
        break;
      }
      i++;
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
  public List<SolveTime> getHistory(SolveType solveType) {
    return getHistory(solveType, System.currentTimeMillis());
  }

  @Override
  public List<SolveTime> getHistory(SolveType solveType, long from) {
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
    q.append(" LIMIT ").append(HISTORY_PAGE_SIZE);
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
  }

  @Override
  public void deleteHistory(SolveType solveType) {
    db.delete(DB.TABLE_TIMEHISTORY, DB.COL_TIMEHISTORY_SOLVETYPE_ID + " = ?", getStringArray(solveType.getId()));
  }

  public List<Long> getSessionTimes(SolveType solveType) {
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
    q.append(" LIMIT ").append(SESSION_TIMES_COUNT);
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveType.getId(), solveType.getId()));
    if (cursor != null) {
      for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
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
    int id = (int) db.insert(DB.TABLE_SOLVETYPE, null, values);

    int i = 1;
    for (SolveTypeStep step : solveType.getSteps()) {
      values = new ContentValues();
      values.put(DB.COL_SOLVETYPESTEP_NAME, step.getName());
      values.put(DB.COL_SOLVETYPESTEP_POSITION, i);
      values.put(DB.COL_SOLVETYPESTEP_SOLVETYPE_ID, id);
      int stepId = (int) db.insert(DB.TABLE_SOLVETYPESTEP, null, values);
      step.setId(stepId);
      i++;
    }
    return id;
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

  private void recalculateAverages(long timestamp, SolveType solveType) {
    long total5 = 0, total12 = 0, total100 = 0;
    List<CachedTime> allTimes = getTimesAroundTs(timestamp, solveType, true);
    List<CachedTime> timesAfter = getTimesAroundTs(timestamp, solveType, false);
    allTimes.addAll(timesAfter);

    boolean inAfter = false;
    int i = 0;
    if (timesAfter.size() > 0) {
      for (CachedTime ct : allTimes) {
        // adjust averages
        total5 += ct.getTime();
        total12 += ct.getTime();
        total100 += ct.getTime();
        if (i >= 5) {
          total5 -= allTimes.get(i - 5).getTime();
        }
        if (i >= 12) {
          total12 -= allTimes.get(i - 12).getTime();
        }
        if (i >= 100) {
          total100 -= allTimes.get(i - 100).getTime();
        }
        if (ct.getSolveId() == timesAfter.get(0).getSolveId()) {
          inAfter = true;
        }
        if (inAfter) {
          // update averages in DB
          ContentValues values = new ContentValues();
          if (i >= 5 - 1) {
            values.put(DB.COL_TIMEHISTORY_AVG5, (total5 / 5));
          } else {
            values.put(DB.COL_TIMEHISTORY_AVG5, (Long) null);
          }
          if (i >= 12 - 1) {
            values.put(DB.COL_TIMEHISTORY_AVG12, (total12 / 12));
          } else {
            values.put(DB.COL_TIMEHISTORY_AVG12, (Long) null);
          }
          if (i >= 100 - 1) {
            values.put(DB.COL_TIMEHISTORY_AVG100, (total100 / 100));
          } else {
            values.put(DB.COL_TIMEHISTORY_AVG100, (Long) null);
          }
          db.update(DB.TABLE_TIMEHISTORY, values, DB.COL_ID + " = ?", getStringArray(ct.getSolveId()));
        }
        i++;
      }
    }

    // Handle DNFs
    Long lastAvg5 = (inAfter && i >= 5) ? total5 / 5 : null;
    Long lastAvg12 = (inAfter && i >= 12) ? total12 / 12 : null;
    Long lastAvg100 = (inAfter && i >= 100) ? total100 / 100 : null;
    List<SolveAverage> solveAverages = getAveragesTimesAfter(timestamp, solveType);
    int nonDNFCount = 0;
    for (SolveAverage sa : solveAverages) {
      if (sa.getTime() == -1) { // DNF
        ContentValues values = new ContentValues();
        values.put(DB.COL_TIMEHISTORY_AVG5, lastAvg5);
        values.put(DB.COL_TIMEHISTORY_AVG12, lastAvg12);
        values.put(DB.COL_TIMEHISTORY_AVG100, lastAvg100);
        db.update(DB.TABLE_TIMEHISTORY, values, DB.COL_ID + " = ?", getStringArray(sa.getId()));
      } else {
        lastAvg5 = sa.getAvg5();
        lastAvg12 = sa.getAvg12();
        lastAvg100 = sa.getAvg100();
        nonDNFCount++;
      }
      if (nonDNFCount > 100) {
        break;
      }
    }
  }

  private List<CachedTime> getTimesAroundTs(long timestamp, SolveType solveType, boolean before) {
    final int biggestAvg = 100;
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
    q.append("   AND ").append(DB.COL_TIMEHISTORY_TIME).append(" > 0");
    q.append(" ORDER BY ").append(DB.COL_TIMEHISTORY_TIMESTAMP);
    q.append(" LIMIT ").append(biggestAvg);
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveType.getId(), timestamp));
    if (cursor != null) {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        times.add(new CachedTime(cursor.getInt(0), cursor.getLong(1)));
      }
    }
    return times;
  }

  private List<SolveAverage> getAveragesTimesAfter(long timestamp, SolveType solveType) {
    List<SolveAverage> times = new ArrayList<SolveAverage>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_ID);
    q.append("     , ").append(DB.COL_TIMEHISTORY_TIME);
    q.append("     , ").append(DB.COL_TIMEHISTORY_AVG5);
    q.append("     , ").append(DB.COL_TIMEHISTORY_AVG12);
    q.append("     , ").append(DB.COL_TIMEHISTORY_AVG100);
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
    q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
    q.append("   AND ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" >= ?");
    q.append(" ORDER BY ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" DESC");
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveType.getId(), timestamp));
    if (cursor != null) {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        times.add(new SolveAverage(cursor.getInt(0), cursor.getInt(1), getCursorLong(cursor, 2), getCursorLong(cursor, 3), getCursorLong(cursor, 4)));
      }
      cursor.close();
    }
    return times;
  }

  private void syncCaches(SolveType solveType) {
    if (cachedSolveTimes == null || solveType.getId() != currentSolveType.getId()) {
      currentSolveType = solveType;
      loadSolveTimes(solveType.getId());
      loadBestAverages(solveType.getId());
      loadLifetimeBest(solveType.getId());
    }
  }

  private List<CachedTime> loadSolveTimes(int solveTypeId) {
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

  private Map<Integer, Long> loadBestAverages(int solveTypeId) {
    cachedBestAverages = new HashMap<Integer, Long>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT MIN(").append(DB.COL_TIMEHISTORY_AVG5).append(")");
    q.append(", MIN(").append(DB.COL_TIMEHISTORY_AVG12).append(")");
    q.append(", MIN(").append(DB.COL_TIMEHISTORY_AVG100).append(")");
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
    q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveTypeId));
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        cachedBestAverages.put(5, getCursorLong(cursor, 0));
        cachedBestAverages.put(12, getCursorLong(cursor, 1));
        cachedBestAverages.put(100, getCursorLong(cursor, 2));
      }
      cursor.close();
    }
    return cachedBestAverages;
  }

  private Long loadLifetimeBest(int solveTypeId) {
    StringBuilder q = new StringBuilder();
    q.append("SELECT MIN(").append(DB.COL_TIMEHISTORY_TIME).append(")");
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
    q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
    q.append("   AND ").append(DB.COL_TIMEHISTORY_TIME).append(" > 0");
    Cursor cursor = db.rawQuery(q.toString(), getStringArray(solveTypeId));
    if (cursor != null) {
      if (cursor != null) {
        if (cursor.moveToFirst()) {
          cachedLifetimeBest = getCursorLong(cursor, 0);
        }
      }
      cursor.close();
    }
    return cachedLifetimeBest;
  }

  /**
   * Calculate the last average based on the cached time.
   * The last average is the average of solve times, starting from the most recent time.
   * @param n number of solve times for which to retrieve the average
   * @param calculateAll if true, returns the average even if the number of solves did not reach n
   * @return the last average
   */
  private Long getLastAvg(int n, boolean calculateAll) {
    long total = 0;
    int i = 0;
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

  class SolveAverage {
    private int id;
    private long time;
    private Long avg5;
    private Long avg12;
    private Long avg100;

    public SolveAverage(int id, long time, Long avg5, Long avg12, Long avg100) {
      this.id = id;
      this.time = time;
      this.avg5 = avg5;
      this.avg12 = avg12;
      this.avg100 = avg100;
    }

    public int getId() {
      return id;
    }

    public long getTime() {
      return time;
    }

    public Long getAvg5() {
      return avg5;
    }

    public Long getAvg12() {
      return avg12;
    }

    public Long getAvg100() {
      return avg100;
    }
  }

}
