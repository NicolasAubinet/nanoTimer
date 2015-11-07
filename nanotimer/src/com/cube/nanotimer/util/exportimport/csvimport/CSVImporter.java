package com.cube.nanotimer.util.exportimport.csvimport;

import android.app.Activity;
import com.cube.nanotimer.R;
import com.cube.nanotimer.gui.widget.ResultListener;
import com.cube.nanotimer.util.exportimport.ErrorListener;
import com.cube.nanotimer.util.exportimport.csvimport.workers.CSVDataReader;
import com.cube.nanotimer.util.helper.DialogUtils;

import java.io.File;

public class CSVImporter {

  private Activity activity;
  private ResultListener resultListener;
  private ErrorListener errorListener;

  public static final String SUCCESS = "success";
  public static final String NO_DATA = "no_data";
  public static final String ERROR = "error";

  public CSVImporter(Activity activity, ResultListener resultListener, ErrorListener errorListener) {
    this.activity = activity;
    this.resultListener = resultListener;
    this.errorListener = errorListener;
  }

  public void importData(File file) {
    new CSVDataReader(activity, errorListener, new ImportResultListener() {
      @Override
      public void onResult(final String result, final Object... params) {
        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            if (result.equals(SUCCESS)) {
              if (resultListener != null) {
                resultListener.onResult();
              }
              int locInsertCount = (Integer) params[0];
              if (locInsertCount > 0) {
                DialogUtils.showOkDialog(activity, activity.getString(R.string.import_times), activity.getString(R.string.times_imported_successfully, locInsertCount));
              } else {
                DialogUtils.showOkDialog(activity, R.string.import_times, R.string.no_new_times_inserted);
              }
            } else if (result.equals(NO_DATA)) {
              DialogUtils.showOkDialog(activity, R.string.import_times, R.string.no_import_data_found);
            }
          }
        });
      }
    }).execute(file);
  }

}
