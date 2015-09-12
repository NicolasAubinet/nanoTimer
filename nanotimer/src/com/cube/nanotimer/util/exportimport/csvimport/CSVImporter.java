package com.cube.nanotimer.util.exportimport.csvimport;

import android.app.Activity;
import android.app.ProgressDialog;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.exportimport.ErrorListener;
import com.cube.nanotimer.util.exportimport.csvimport.workers.CSVDataReader;
import com.cube.nanotimer.util.helper.DialogUtils;

import java.io.File;

public class CSVImporter {

  private Activity activity;
  private ErrorListener errorListener;

  public static final String SUCCESS = "success";
  public static final String NO_DATA = "no_data";
  public static final String ERROR = "error";

  public CSVImporter(Activity activity, ErrorListener errorListener) {
    this.activity = activity;
    this.errorListener = errorListener;
  }

  public void importData(File file) {
    final ProgressDialog progressDialog = new ProgressDialog(activity);
    progressDialog.setIndeterminate(true);
    progressDialog.setCancelable(false);

    new CSVDataReader(activity, progressDialog, errorListener, new ImportResultListener() {
      @Override
      public void onResult(final String result) {
        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            if (result.equals(SUCCESS)) {
              DialogUtils.showOkDialog(activity, R.string.import_times, R.string.times_imported_successfully);
            } else if (result.equals(NO_DATA)) {
              DialogUtils.showOkDialog(activity, R.string.import_times, R.string.no_import_data_found);
            }
            progressDialog.hide();
            progressDialog.dismiss();
          }
        });
      }
    }).execute(file);
  }

}
