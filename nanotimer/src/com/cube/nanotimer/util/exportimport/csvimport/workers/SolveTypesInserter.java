package com.cube.nanotimer.util.exportimport.csvimport.workers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.exportimport.ErrorListener;
import com.cube.nanotimer.util.exportimport.csvimport.ImportResultListener;
import com.cube.nanotimer.util.exportimport.csvimport.ImportTimesData;

public class SolveTypesInserter extends AsyncTask<ImportTimesData, Void, String> {

  private Context context;
  private ProgressDialog progressDialog;
  private ErrorListener errorListener;
  private ImportResultListener dataListener;
  private ImportTimesData importData;

  public SolveTypesInserter(Context context, ProgressDialog progressDialog, ErrorListener errorListener, ImportResultListener dataListener) {
    this.context = context;
    this.progressDialog = progressDialog;
    this.errorListener = errorListener;
    this.dataListener = dataListener;
  }

  @Override
  protected String doInBackground(ImportTimesData... importTimesData) {
    importData = importTimesData[0];
    // TODO insert solve types
    return null;
  }

  @Override
  protected void onPreExecute() {
    progressDialog.setMessage(context.getString(R.string.inserting_solve_types));
  }

  @Override
  protected void onPostExecute(String s) {
    // TODO if an error happened, call dataListener.onResult(CSVImporter.ERROR);
    new SolveTimesInserter(context, progressDialog, errorListener, dataListener).execute(importData);
  }

}
