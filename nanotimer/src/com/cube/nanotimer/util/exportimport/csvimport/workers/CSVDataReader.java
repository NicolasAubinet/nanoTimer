package com.cube.nanotimer.util.exportimport.csvimport.workers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.exportimport.CSVFormatException;
import com.cube.nanotimer.util.exportimport.ErrorListener;
import com.cube.nanotimer.util.exportimport.csvexport.ExportResultConverter;
import com.cube.nanotimer.util.exportimport.csvimport.CSVImporter;
import com.cube.nanotimer.util.exportimport.csvimport.ImportResultListener;
import com.cube.nanotimer.util.exportimport.csvimport.ImportTimesData;
import com.cube.nanotimer.util.helper.FileUtils;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.ExportResult;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;
import com.cube.nanotimer.vo.SolveTypeStep;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CSVDataReader extends AsyncTask<File, Void, ImportTimesData> {

  private Context context;
  private ProgressDialog progressDialog;
  private ErrorListener errorListener;
  private ImportResultListener dataListener;

  public CSVDataReader(Context context, ProgressDialog progressDialog, ErrorListener errorListener, ImportResultListener dataListener) {
    this.context = context;
    this.progressDialog = progressDialog;
    this.errorListener = errorListener;
    this.dataListener = dataListener;
  }

  @Override
  protected ImportTimesData doInBackground(File... files) {
    File file = files[0];
    ImportTimesData importData = null;
    try {
      importData = getImportData(file);
    } catch (CSVFormatException e) {
      errorListener.onError(e.getMessage());
    }
    return importData;
  }

  @Override
  protected void onPreExecute() {
    progressDialog.setMessage(context.getString(R.string.reading_import_file));
  }

  @Override
  protected void onPostExecute(ImportTimesData importData) {
    if (importData == null) {
      dataListener.onResult(CSVImporter.ERROR);
    } else {
      new SolveTypesVerifier(context, progressDialog, errorListener, dataListener).execute(importData);
    }
  }

  private ImportTimesData getImportData(File importFile) throws CSVFormatException {
    List<String> lines = FileUtils.readLinesFromFile(importFile, 0);
    groupQuotedLines(lines);
    List<ExportResult> exportResults = getExportResults(lines);

    ImportTimesData importData = new ImportTimesData(context);
    for (ExportResult exportResult : exportResults) {
      CubeType cubeType = CubeType.getCubeTypeFromName(exportResult.getCubeTypeName());
      if (cubeType == null) {
        throw new CSVFormatException(context.getString(R.string.could_not_find_cube_type, exportResult.getCubeTypeName()));
      }
      SolveType solveType = new SolveType(exportResult.getSolveTypeName(), exportResult.isBlindType(), cubeType.getId());
      if (exportResult.hasSteps()) {
        SolveTypeStep[] steps = new SolveTypeStep[exportResult.getStepsNames().length];
        for (int i = 0; i < exportResult.getStepsNames().length; i++) {
          SolveTypeStep step = new SolveTypeStep();
          step.setName(exportResult.getStepsNames()[i]);
        }
        solveType.setSteps(steps);
      }
      solveType = importData.addSolveTypeIfNotExists(cubeType, solveType);

      SolveTime solveTime = new SolveTime(exportResult.getTimestamp(), exportResult.getTime(),
        exportResult.isPlusTwo(), exportResult.getScramble(), solveType);
      solveTime.setStepsTimes(exportResult.getStepsTimes());
      importData.addSolveTime(solveType, solveTime);
    }
    return importData;
  }

  private void groupQuotedLines(List<String> lines) {
    // Group lines that are in the same quotes together (like those with Megaminx scrambles)
    List<String> grouped = new ArrayList<String>();
    boolean inUnendedQuotes = false;
    boolean prevLineInUnendedQuotes = false;
    for (String line : lines) {
      for (char c : line.toCharArray()) {
        if (c == '"') {
          inUnendedQuotes = !inUnendedQuotes;
        }
      }
      if (prevLineInUnendedQuotes) {
        int index = grouped.size() - 1;
        String l = grouped.get(index);
        grouped.set(index, l + "\n" + line);
      } else {
        grouped.add(line);
      }
      prevLineInUnendedQuotes = inUnendedQuotes;
    }
    lines.clear();
    lines.addAll(grouped);
  }

  private List<ExportResult> getExportResults(List<String> lines) throws CSVFormatException {
    List<ExportResult> results = new ArrayList<ExportResult>();
    for (int i = 1; i < lines.size(); i++) {
      try {
        results.add(ExportResultConverter.fromCSVLine(context, lines.get(i)));
      } catch (CSVFormatException e) {
        throw new CSVFormatException(e.getMessage() + "(" + context.getString(R.string.line) + ": " + (i+1) + ")");
      }
    }
    return results;
  }

}
