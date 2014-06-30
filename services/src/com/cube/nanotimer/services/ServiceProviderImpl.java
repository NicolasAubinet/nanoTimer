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
  private List<CachedTime> cachedSolveTimes;
  private Map<Integer, Long> cachedBestAverages;
  private Long cachedLifetimeBest;

  private final int CACHE_MAX_SIZE = 1010;
  private final int CACHE_MIN_SIZE = 1000;
  private final int HISTORY_PAGE_SIZE = 20;
  private final int SESSION_TIMES_COUNT = 12;

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
    if (solveTime.getId() > 0) {
      return updateTime(solveTime);
    } else {
      return createTime(solveTime);
    }
  }

  private SolveAverages updateTime(SolveTime solveTime) {
    ContentValues values = new ContentValues();
    values.put(DB.COL_TIMEHISTORY_TIME, solveTime.getTime());
    db.update(DB.TABLE_TIMEHISTORY, values, DB.COL_ID + " = ?", new String[] { String.valueOf(solveTime.getId()) });
    for (CachedTime ct : cachedSolveTimes) {
      if (ct.getSolveId() == solveTime.getId()) {
        ct.setTime(solveTime.getTime());
        break;
      }
    }

    Long avg5 = getLastAvg(5, false);
    Long avg12 = getLastAvg(12, false);
    Long avg100 = getLastAvg(100, false);
    Long avgLifetime = getLastAvg(1000, true);
//    syncBestAveragesWithCurrent(avg5, avg12, avg100); // useless because calling loadBestAverages after

    recalculateAverages(solveTime.getTimestamp(), solveTime.getSolveType());

    loadBestAverages(solveTime.getSolveType().getId());
    loadLifetimeBest(solveTime.getSolveType().getId());

    return getSolveAverages(solveTime, avg5, avg12, avg100, avgLifetime);
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
    Long avgLifetime = getLastAvg(1000, true);

    syncBestAveragesWithCurrent(avg5, avg12, avg100);
    if (isTimeBetter(cachedLifetimeBest, solveTime.getTime())) {
      cachedLifetimeBest = solveTime.getTime();
    }

    ContentValues values = new ContentValues();
    values.put(DB.COL_TIMEHISTORY_TIME, solveTime.getTime());
    values.put(DB.COL_TIMEHISTORY_SOLVETYPE_ID, solveTime.getSolveType().getId());
    values.put(DB.COL_TIMEHISTORY_SCRAMBLE, solveTime.getScramble());
    values.put(DB.COL_TIMEHISTORY_TIMESTAMP, solveTime.getTimestamp());
    values.put(DB.COL_TIMEHISTORY_AVG5, avg5);
    values.put(DB.COL_TIMEHISTORY_AVG12, avg12);
    values.put(DB.COL_TIMEHISTORY_AVG100, avg100);
    long id = db.insert(DB.TABLE_TIMEHISTORY, null, values);
    cachedTime.setSolveId((int) id);
    solveTime.setId((int) id);

    return getSolveAverages(solveTime, avg5, avg12, avg100, avgLifetime);
  }

  private SolveAverages getSolveAverages(SolveTime solveTime, Long avg5, Long avg12, Long avg100, Long avgLifetime) {
    SolveAverages solveAverages = new SolveAverages();
    solveAverages.setSolveTime(solveTime);
    solveAverages.setAvgOf5(avg5);
    solveAverages.setAvgOf12(avg12);
    solveAverages.setAvgOf100(avg100);
    solveAverages.setAvgOfLifetime(avgLifetime);
    solveAverages.setBestOf5(cachedBestAverages.get(5));
    solveAverages.setBestOf12(cachedBestAverages.get(12));
    solveAverages.setBestOf100(cachedBestAverages.get(100));
    solveAverages.setBestOfLifetime(cachedLifetimeBest);
    return solveAverages;
  }

  public void deleteAllHistory() {
    db.delete(DB.TABLE_TIMEHISTORY, null, null);
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
  public SolveAverages getSolveAverages(SolveType solveType) {
    syncCaches(solveType);
    SolveAverages solveAverages = new SolveAverages();
    solveAverages.setAvgOf5(getLastAvg(5, false));
    solveAverages.setAvgOf12(getLastAvg(12, false));
    solveAverages.setAvgOf100(getLastAvg(100, false));
    solveAverages.setAvgOfLifetime(getLastAvg(1000, true));
    solveAverages.setBestOf5(cachedBestAverages.get(5));
    solveAverages.setBestOf12(cachedBestAverages.get(12));
    solveAverages.setBestOf100(cachedBestAverages.get(100));
    solveAverages.setBestOfLifetime(cachedLifetimeBest);
    return solveAverages;
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
    db.delete(DB.TABLE_TIMEHISTORY, DB.COL_ID + " = ?", new String[] { String.valueOf(solveTime.getId()) });

    if (cachedSolveTimes.size() == CACHE_MIN_SIZE) { // re-read the cache if we reach the minimum size
      loadSolveTimes(solveTime.getSolveType().getId());
    }

    recalculateAverages(solveTime.getTimestamp(), solveTime.getSolveType());
    loadBestAverages(solveTime.getSolveType().getId());
    loadLifetimeBest(solveTime.getSolveType().getId());

    Long avg5 = getLastAvg(5, false);
    Long avg12 = getLastAvg(12, false);
    Long avg100 = getLastAvg(100, false);
    Long avgLifetime = getLastAvg(1000, true);
    return getSolveAverages(null, avg5, avg12, avg100, avgLifetime);
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

  public List<Long> getSessionTimes(SolveType solveType) {
    List<Long> sessionTimes = new ArrayList<Long>();
    long sessionStartTs = 0; // TODO : retrieve the session start time (when implemented also on client side)
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_TIMEHISTORY_TIME);
    q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
    q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
    q.append("   AND ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" >= ?");
    q.append(" ORDER BY ").append(DB.COL_TIMEHISTORY_TIMESTAMP).append(" DESC");
    q.append(" LIMIT ").append(SESSION_TIMES_COUNT);
    Cursor cursor = db.rawQuery(q.toString(), new String[] { String.valueOf(solveType.getId()), String.valueOf(sessionStartTs) });
    if (cursor != null) {
      for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
        sessionTimes.add(cursor.getLong(0));
      }
      cursor.close();
    }
    return sessionTimes;
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
          db.update(DB.TABLE_TIMEHISTORY, values, DB.COL_ID + " = ?", new String[] { String.valueOf(ct.getSolveId()) });
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
        db.update(DB.TABLE_TIMEHISTORY, values, DB.COL_ID + " = ?", new String[] { String.valueOf(sa.getId()) });
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
    Cursor cursor = db.rawQuery(q.toString(),
        new String[] { String.valueOf(solveType.getId()), String.valueOf(timestamp) });
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
    Cursor cursor = db.rawQuery(q.toString(), new String[] { String.valueOf(solveType.getId()), String.valueOf(timestamp) });
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
    Cursor cursor = db.rawQuery(q.toString(), new String[] { String.valueOf(solveTypeId) });
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
    Cursor cursor = db.rawQuery(q.toString(), new String[] { String.valueOf(solveTypeId) });
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
    Cursor cursor = db.rawQuery(q.toString(), new String[] { String.valueOf(solveTypeId) });
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
