package com.cube.nanotimer.util.exportimport.csvimport.workers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.scrambler.ScramblerService;
import com.cube.nanotimer.util.exportimport.ErrorListener;
import com.cube.nanotimer.util.exportimport.csvimport.ImportResultListener;
import com.cube.nanotimer.util.exportimport.csvimport.ImportTimesData;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.ScrambleType;
import com.cube.nanotimer.vo.SolveType;

import java.util.List;

public class SolveTypesInserter extends AsyncTask<List<SolveType>, Void, String> {

  private Context context;
  private ProgressDialog progressDialog;
  private ErrorListener errorListener;
  private ImportResultListener dataListener;
  private ImportTimesData importData;

  public SolveTypesInserter(Context context, ErrorListener errorListener, ImportResultListener dataListener, ImportTimesData importData) {
    this.context = context;
    this.errorListener = errorListener;
    this.dataListener = dataListener;
    this.importData = importData;
  }

  @Override
  protected String doInBackground(List<SolveType>... missingSolveTypesParams) {
    List<SolveType> missingSolveTypes = missingSolveTypesParams[0];
    boolean addedSpecialScrambleType = false;

    for (SolveType missingSolveType : missingSolveTypes) {
      ScrambleType scrambleType = missingSolveType.getScrambleType();
      if (scrambleType != null && !scrambleType.isDefault()) {
        CubeType cubeType = CubeType.getCubeType(missingSolveType.getCubeTypeId());
        if (cubeType != null) {
          addedSpecialScrambleType |= cubeType.addUsedScrambleType(scrambleType);
        }
      }
      App.INSTANCE.getService().getProviderAccess().addSolveType(missingSolveType);

      if (addedSpecialScrambleType) {
        ScramblerService.INSTANCE.checkScrambleCaches();
      }
    }
    return null;
  }

  @Override
  protected void onPreExecute() {
    progressDialog = new ProgressDialog(context);
    progressDialog.setIndeterminate(true);
    progressDialog.setCancelable(false);
    progressDialog.setMessage(context.getString(R.string.inserting_solve_types));
    progressDialog.show();
  }

  @Override
  protected void onPostExecute(String s) {
    progressDialog.hide();
    progressDialog.dismiss();
    new SolveTimesInserter(context, errorListener, dataListener).execute(importData);
  }

}
