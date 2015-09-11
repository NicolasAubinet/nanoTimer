package com.cube.nanotimer.util.exportimport.csvimport.workers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.YesNoListener;
import com.cube.nanotimer.util.exportimport.ErrorListener;
import com.cube.nanotimer.util.exportimport.csvimport.ImportResultListener;
import com.cube.nanotimer.util.exportimport.csvimport.ImportTimesData;
import com.cube.nanotimer.util.helper.DialogUtils;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class SolveTypesVerifier extends AsyncTask<ImportTimesData, Void, List<SolveType>> {

  private Context context;
  private ProgressDialog progressDialog;
  private ErrorListener errorListener;
  private ImportResultListener dataListener;
  private ImportTimesData importData;

  public SolveTypesVerifier(Context context, ProgressDialog progressDialog, ErrorListener errorListener, ImportResultListener dataListener) {
    this.context = context;
    this.progressDialog = progressDialog;
    this.errorListener = errorListener;
    this.dataListener = dataListener;
  }

  @Override
  protected List<SolveType> doInBackground(ImportTimesData... importTimesData) {
    importData = importTimesData[0];
    return getMissingSolveTypesAndUpdateIds(importData);
  }

  @Override
  protected void onPreExecute() {
  }

  @Override
  protected void onPostExecute(List<SolveType> missingSolveTypes) {
    if (missingSolveTypes.size() > 0) {
      DialogUtils.showYesNoConfirmation(context, context.getString(R.string.solve_types_do_not_exist, formatSolveTypes(missingSolveTypes)), new YesNoListener() {
        @Override
        public void onYes() {
          new SolveTypesInserter(context, progressDialog, errorListener, dataListener).execute(importData);
        }
      });
    } else {
      new SolveTimesInserter(context, progressDialog, errorListener, dataListener).execute(importData);
    }
  }

  private String formatSolveTypes(List<SolveType> solveTypes) {
    return ""; // TODO
  }

  private List<SolveType> getMissingSolveTypesAndUpdateIds(ImportTimesData importData) {
    List<SolveType> missingSolveTypes = new ArrayList<SolveType>();
    for (Entry<CubeType, List<SolveType>> solveTypesEntry : importData.getSolveTypes().entrySet()) {
      CubeType cubeType = solveTypesEntry.getKey();
      List<SolveType> importSolveTypes = solveTypesEntry.getValue();
      List<SolveType> dbSolveTypes = App.INSTANCE.getService().getProviderAccess().getSolveTypes(cubeType);
      for (SolveType importSolveType : importSolveTypes) {
        boolean foundSolveType = false;
        for (SolveType dbSolveType : dbSolveTypes) {
          if (importSolveType.getName().toUpperCase().equals(dbSolveType.getName().toUpperCase())) {
            foundSolveType = true;
            importSolveType.setId(dbSolveType.getId());
            break;
          }
        }
        if (!foundSolveType) {
          missingSolveTypes.add(importSolveType);
        }
      }
    }
    return missingSolveTypes;
  }

}
