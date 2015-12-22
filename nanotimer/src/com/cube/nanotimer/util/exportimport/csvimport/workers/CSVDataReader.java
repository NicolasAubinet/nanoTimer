package com.cube.nanotimer.util.exportimport.csvimport.workers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.exportimport.CSVFormatException;
import com.cube.nanotimer.util.exportimport.ErrorListener;
import com.cube.nanotimer.util.exportimport.csvexport.ExportCSVGenerator;
import com.cube.nanotimer.util.exportimport.csvexport.ExportResultConverter;
import com.cube.nanotimer.util.exportimport.csvimport.CSVImporter;
import com.cube.nanotimer.util.exportimport.csvimport.ImportResultListener;
import com.cube.nanotimer.util.exportimport.csvimport.ImportTimesData;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.ExportResult;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;
import com.cube.nanotimer.vo.SolveTypeStep;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class CSVDataReader extends AsyncTask<File, Void, ImportTimesData> {

  private Context context;
  private ProgressDialog progressDialog;
  private ErrorListener errorListener;
  private ImportResultListener dataListener;

  public CSVDataReader(Context context, ErrorListener errorListener, ImportResultListener dataListener) {
    this.context = context;
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
    progressDialog = new ProgressDialog(context);
    progressDialog.setIndeterminate(true);
    progressDialog.setCancelable(false);
    progressDialog.setMessage(context.getString(R.string.reading_import_file));
    progressDialog.show();
  }

  @Override
  protected void onPostExecute(ImportTimesData importData) {
    progressDialog.hide();
    progressDialog.dismiss();

    if (importData == null) {
      dataListener.onResult(CSVImporter.ERROR);
    } else if (!importData.isEmpty()) {
      new SolveTypesVerifier(context, errorListener, dataListener).execute(importData);
    }
  }

  private ImportTimesData getImportData(File importFile) throws CSVFormatException {
    List<String> lines = readCSVFile(importFile);
    groupQuotedLines(lines);
    List<ExportResult> exportResults = getExportResults(lines);
    ImportTimesData importData = new ImportTimesData(context);
    if (exportResults.size() == 0) {
      dataListener.onResult(CSVImporter.NO_DATA);
      return importData;
    }

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
          steps[i] = step;
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

  private List<String> readCSVFile(File file) throws CSVFormatException {
    List<String> lines = new ArrayList<String>();
    try {
      FileInputStream fis = new FileInputStream(file);
      Scanner fileScanner = new Scanner(fis);
      int i = 0;
      while (fileScanner.hasNextLine()) {
        String line = fileScanner.nextLine();
        if (i == 0 && !line.equals(ExportCSVGenerator.CSV_HEADER_LINE)) {
          throw new CSVFormatException(context.getString(R.string.invalid_import_file_format));
        }
        lines.add(line);
        i++;
      }
      fileScanner.close();
      fis.close();
    } catch (FileNotFoundException e) {
      errorListener.onError(context.getString(R.string.error_reading_import_file, e.getMessage()));
    } catch (IOException e) {
      errorListener.onError(context.getString(R.string.error_reading_import_file, e.getMessage()));
    }
    return lines;
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
        throw new CSVFormatException(context.getString(R.string.error_in_line, (i+1)) + ": " + e.getMessage());
      }
    }
    Collections.reverse(results); // sort from oldest to newest
    return results;
  }

}
