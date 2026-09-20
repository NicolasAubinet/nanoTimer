package com.cube.nanotimer.services.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * What was turned for each case of a solve, kept so it is read out of the solve only once.
 *
 * <p>Nothing stores a case's moves: they are a slice of the solve, so reading them means solving
 * the scramble again and setting every execution against the algorithms its case is taught with,
 * and the matching alone is four fifths of that. A window of five hundred solves is read by the
 * coach and by the Analysis card alike, over and over, and the answer for a solve that happened
 * cannot change.
 *
 * <p><b>It is a cache and nothing more.</b> A row is written by a version of the reading and is
 * ignored by any other, so whatever is missing is simply read again; nothing here is the only copy
 * of anything, and the table may be emptied at any point. The reading itself lives with the code
 * that can replay a solve, which this module cannot call, so rows arrive as text and leave as text.
 */
public final class SolveReadingStore {

  /** How many solve ids go into one {@code IN} clause: SQLite's own limit is 999 parameters. */
  private static final int BATCH = 400;

  private SolveReadingStore() {
  }

  /**
   * The readings held for the given solves, by solve id, leaving out any written by another
   * version. A solve missing from the answer has never been read, or was read by an older one.
   */
  public static Map<Long, String> read(SQLiteDatabase db, List<Long> solveIds, int version) {
    Map<Long, String> readings = new HashMap<Long, String>();
    for (int from = 0; from < solveIds.size(); from += BATCH) {
      List<Long> batch = solveIds.subList(from, Math.min(from + BATCH, solveIds.size()));
      StringBuilder places = new StringBuilder();
      String[] args = new String[batch.size() + 1];
      args[0] = String.valueOf(version);
      for (int i = 0; i < batch.size(); i++) {
        places.append(i == 0 ? "?" : ",?");
        args[i + 1] = String.valueOf(batch.get(i));
      }
      Cursor cursor = db.rawQuery("SELECT " + DB.COL_SOLVE_READING_SOLVE_ID + ", "
          + DB.COL_SOLVE_READING_READING + " FROM " + DB.TABLE_SOLVE_READING
          + " WHERE " + DB.COL_SOLVE_READING_VERSION + " = ?"
          + " AND " + DB.COL_SOLVE_READING_SOLVE_ID + " IN (" + places + ")", args);
      try {
        while (cursor.moveToNext()) {
          readings.put(Long.valueOf(cursor.getLong(0)), cursor.getString(1));
        }
      } finally {
        cursor.close();
      }
    }
    return readings;
  }

  /** Keeps what was just read, replacing anything an older version left for the same solves. */
  public static void write(SQLiteDatabase db, Map<Long, String> readings, int version) {
    if (readings.isEmpty()) {
      return;
    }
    db.beginTransaction();
    try {
      for (Map.Entry<Long, String> reading : readings.entrySet()) {
        ContentValues values = new ContentValues();
        values.put(DB.COL_SOLVE_READING_SOLVE_ID, reading.getKey());
        values.put(DB.COL_SOLVE_READING_VERSION, version);
        values.put(DB.COL_SOLVE_READING_READING, reading.getValue());
        db.replace(DB.TABLE_SOLVE_READING, null, values);
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }

  /** Throws away one solve's reading, for a solve that is going or is about to be read again. */
  public static void forget(SQLiteDatabase db, long solveId) {
    db.delete(DB.TABLE_SOLVE_READING, DB.COL_SOLVE_READING_SOLVE_ID + " = ?",
        new String[] { String.valueOf(solveId) });
  }

  /** Throws away the readings of every solve of one solve type, which is about to lose them. */
  public static void forgetSolveType(SQLiteDatabase db, int solveTypeId) {
    db.delete(DB.TABLE_SOLVE_READING, DB.COL_SOLVE_READING_SOLVE_ID + " IN (SELECT " + DB.COL_ID
        + " FROM " + DB.TABLE_TIMEHISTORY + " WHERE " + DB.COL_TIMEHISTORY_SOLVETYPE_ID + " = ?)",
        new String[] { String.valueOf(solveTypeId) });
  }

  /** Throws every reading away. */
  public static void forgetAll(SQLiteDatabase db) {
    db.delete(DB.TABLE_SOLVE_READING, null, null);
  }
}
