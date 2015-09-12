package com.cube.nanotimer.util.exportimport.csvimport.workers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.exportimport.ErrorListener;
import com.cube.nanotimer.util.exportimport.csvimport.CSVImporter;
import com.cube.nanotimer.util.exportimport.csvimport.ImportResultListener;
import com.cube.nanotimer.util.exportimport.csvimport.ImportTimesData;

public class SolveTimesInserter extends AsyncTask<ImportTimesData, Void, String> {

  private Context context;
  private ProgressDialog progressDialog;
  private ErrorListener errorListener;
  private ImportResultListener dataListener;

  public SolveTimesInserter(Context context, ProgressDialog progressDialog, ErrorListener errorListener, ImportResultListener dataListener) {
    this.context = context;
    this.progressDialog = progressDialog;
    this.errorListener = errorListener;
    this.dataListener = dataListener;
  }

  @Override
  protected String doInBackground(ImportTimesData... data) {
    // TODO remove solve times that already exist from the list (to not have them twice in DB)
    // TODO insert solve times
    return null;
  }

  @Override
  protected void onPreExecute() {
    progressDialog.setMessage(context.getString(R.string.inserting_times));
    progressDialog.show();
  }

  @Override
  protected void onPostExecute(String s) {
    // TODO if an error happened, call dataListener.onResult(CSVImporter.ERROR);
    dataListener.onResult(CSVImporter.SUCCESS);
  }

}
