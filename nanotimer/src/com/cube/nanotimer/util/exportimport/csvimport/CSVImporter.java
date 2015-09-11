package com.cube.nanotimer.util.exportimport.csvimport;

import android.app.ProgressDialog;
import android.content.Context;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.exportimport.ErrorListener;
import com.cube.nanotimer.util.exportimport.csvimport.workers.CSVDataReader;
import com.cube.nanotimer.util.helper.DialogUtils;

import java.io.File;

public class CSVImporter {

  private Context context;
  private ErrorListener errorListener;

  public static final String SUCCESS = "success";
  public static final String ERROR = "error";

  public CSVImporter(Context context, ErrorListener errorListener) {
    this.context = context;
    this.errorListener = errorListener;
  }

  public void importData(File file) {
    final ProgressDialog progressDialog = new ProgressDialog(context);
    progressDialog.setIndeterminate(true);
    progressDialog.setCancelable(false);
    progressDialog.show();

    new CSVDataReader(context, progressDialog, errorListener, new ImportResultListener() {
      @Override
      public void onResult(String result) {
        if (result.equals(SUCCESS)) {
          DialogUtils.showInfoMessage(context, R.string.times_imported_successfully);
        }
        progressDialog.hide();
        progressDialog.dismiss();
      }
    }).execute(file);
  }

}
