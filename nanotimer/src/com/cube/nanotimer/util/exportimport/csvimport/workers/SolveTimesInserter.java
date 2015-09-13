package com.cube.nanotimer.util.exportimport.csvimport.workers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.exportimport.ErrorListener;
import com.cube.nanotimer.util.exportimport.csvimport.CSVImporter;
import com.cube.nanotimer.util.exportimport.csvimport.ImportResultListener;
import com.cube.nanotimer.util.exportimport.csvimport.ImportTimesData;
import com.cube.nanotimer.vo.SolveHistory;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class SolveTimesInserter extends AsyncTask<ImportTimesData, Integer, String> {

  private Context context;
  private ProgressDialog progressDialog;
  private ErrorListener errorListener;
  private ImportResultListener dataListener;

  public SolveTimesInserter(Context context, ErrorListener errorListener, ImportResultListener dataListener) {
    this.context = context;
    this.errorListener = errorListener;
    this.dataListener = dataListener;
  }

  @Override
  protected String doInBackground(ImportTimesData... data) {
    ImportTimesData importData = data[0];
    int solveTimesCount = importData.getSolveTimesCount();
    int processedCount = 0;
    for (Entry<SolveType, List<SolveTime>> entry : importData.getSolveTimes().entrySet()) {
      SolveType solveType = entry.getKey();
      List<SolveTime> solveTimes = entry.getValue();

      processedCount += removeAlreadyExistingSolveTimes(solveType, solveTimes);
      publishProgress(solveTimesCount, processedCount);
      for (SolveTime solveTime : solveTimes) {
        App.INSTANCE.getService().getProviderAccess().saveTime(solveTime);
        processedCount++;
        publishProgress(solveTimesCount, processedCount);
      }
    }
    return null;
  }

  @Override
  protected void onPreExecute() {
    progressDialog = new ProgressDialog(context);
    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    progressDialog.setCancelable(false);
    progressDialog.setMessage(context.getString(R.string.inserting_times));
    progressDialog.show();
  }

  @Override
  protected void onPostExecute(String s) {
    progressDialog.hide();
    progressDialog.dismiss();
    dataListener.onResult(CSVImporter.SUCCESS);
  }

  @Override
  protected void onProgressUpdate(Integer... values) {
    int solveTimesCount = values[0];
    int processedCount = values[1];
    progressDialog.setMax(solveTimesCount);
    progressDialog.setProgress(processedCount);
  }

  private int removeAlreadyExistingSolveTimes(SolveType solveType, List<SolveTime> solveTimes) {
    int removedCount = 0;
    long from = getOldestTimestamp(solveTimes);
    SolveHistory solveHistory = App.INSTANCE.getService().getProviderAccess().getHistory(solveType, from);
    Iterator<SolveTime> iterator = solveTimes.iterator();
    while (iterator.hasNext()) {
      SolveTime importSolveTime = iterator.next();
      boolean existsInDb = false;
      for (SolveTime dbSolveTime : solveHistory.getSolveTimes()) {
        if (getComparableTimestamp(importSolveTime.getTimestamp()) == getComparableTimestamp(dbSolveTime.getTimestamp())
        &&  getComparableSolveTime(importSolveTime.getTime()) == getComparableSolveTime(dbSolveTime.getTime())) {
          existsInDb = true;
        }
      }
      if (existsInDb) {
        iterator.remove();
        removedCount++;
      }
    }
    return removedCount;
  }

  private long getComparableTimestamp(long timestamp) {
    return timestamp - (timestamp % 1000);
  }

  private long getComparableSolveTime(long solveTime) {
    return solveTime - (solveTime % 10);
  }

  private long getOldestTimestamp(List<SolveTime> solveTimes) {
    long oldestTs = Long.MAX_VALUE;
    for (SolveTime solveTime : solveTimes) {
      if (solveTime.getTimestamp() < oldestTs) {
        oldestTs = solveTime.getTimestamp();
      }
    }
    return oldestTs;
  }

}
