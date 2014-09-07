package com.cube.nanotimer.services.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.cube.nanotimer.vo.SolveTime;

import java.util.ArrayList;
import java.util.List;

public class DBUpgradeScripts {

  public static void calculateAndUpdateAvg50(SQLiteDatabase db) {
    List<Integer> solveTypeIds = getAllSolveTypesIds(db);
    Log.i("[Avg50]", solveTypeIds.size() + " solve types.");
    for (int solveTypeId : solveTypeIds) {
      Log.i("[Avg50]", "\n      --> Processing solve type " + solveTypeId + "...\n");
      List<SolveTime> solveTimes = new ArrayList<SolveTime>();
      StringBuilder q = new StringBuilder();
      q.append("SELECT ").append(DB.COL_ID).append(", ").append(DB.COL_TIMEHISTORY_TIME);
      q.append(" FROM ").append(DB.TABLE_TIMEHISTORY);
      q.append(" WHERE ").append(DB.COL_TIMEHISTORY_SOLVETYPE_ID).append(" = ?");
      q.append("   AND ").append(DB.COL_TIMEHISTORY_TIME).append(" > 0");
      q.append(" ORDER BY ").append(DB.COL_TIMEHISTORY_TIMESTAMP);
      Cursor cursor = db.rawQuery(q.toString(), new String[] { String.valueOf(solveTypeId) });
      if (cursor != null) {
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
          SolveTime st = new SolveTime();
          st.setId(cursor.getInt(0));
          st.setTime(cursor.getInt(1));
          solveTimes.add(st);
        }
        cursor.close();
      }
      Log.i("[Avg50]", "Found " + solveTimes.size() + " times. Start updating...");
      long total = 0;
      for (int i = 0; i < solveTimes.size(); i++) {
        SolveTime st = solveTimes.get(i);
        total += st.getTime();
        Log.i("[Avg50]", "  Time: " + st.getTime() + ". New total: " + total);
        if (i >= 50 - 1) {
          if (i >= 50) {
            total -= solveTimes.get(i - 50).getTime();
            Log.i("[Avg50]", "   Removed " + solveTimes.get(i - 50).getTime() + ". New total: " + total);
          }
          ContentValues values = new ContentValues();
          values.put(DB.COL_TIMEHISTORY_AVG50, (total / 50));
          Log.i("[Avg50]", "  Update " + st.getId() + " with value " + (total / 50));
          db.update(DB.TABLE_TIMEHISTORY, values, DB.COL_ID + " = ?", new String[] { String.valueOf(st.getId()) });
        }
      }
    }
  }

  private static List<Integer> getAllSolveTypesIds(SQLiteDatabase db) {
    List<Integer> solveTypeIds = new ArrayList<Integer>();
    StringBuilder q = new StringBuilder();
    q.append("SELECT ").append(DB.COL_ID);
    q.append(" FROM ").append(DB.TABLE_SOLVETYPE);
    Cursor cursor = db.rawQuery(q.toString(), new String[] { });
    if (cursor != null) {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        solveTypeIds.add(cursor.getInt(0));
      }
      cursor.close();
    }
    return solveTypeIds;
  }

}
