package com.cube.nanotimer.util.exportimport;

import android.content.Context;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.helper.FileUtils;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.ExportResult;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;
import com.cube.nanotimer.vo.SolveTypeStep;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CSVImporter {

  private Context context;

  public CSVImporter(Context context) {
    this.context = context;
  }

  public void importTimes(File importFile) throws CSVFormatException {
    ImportTimesData importData = getImportData(importFile);
    // TODO parse solve types while reading them from DB and set the id's of those that exist + keep list of those that don't exist
    // TODO show confirm dialog with solve types that will be inserted (display solve/cube types in dialog and ask if ok or cancel)
    // TODO if confirmed, insert solve types and read them to get the ids
    // TODO update solve times of inserted solve types to point to the inserted solve type (with the correct id)

    // TODO remove solve times that already exist from the list (to not have them twice in DB)
    // TODO insert solve times
  }

  private ImportTimesData getImportData(File importFile) throws CSVFormatException {
    List<String> lines = FileUtils.readLinesFromFile(importFile, 0);
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

  private List<ExportResult> getExportResults(List<String> lines) throws CSVFormatException {
    List<ExportResult> results = new ArrayList<ExportResult>();
    for (int i = 0; i < lines.size(); i++) {
      try {
        results.add(ExportResultConverter.fromCSVLine(context, lines.get(i)));
      } catch (CSVFormatException e) {
        throw new CSVFormatException(e.getMessage() + "(" + R.string.line + ": " + (i+1) + ")");
      }
    }
    return results;
  }

}
