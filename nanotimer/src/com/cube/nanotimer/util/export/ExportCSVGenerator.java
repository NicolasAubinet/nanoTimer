package com.cube.nanotimer.util.export;

import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.vo.ExportResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExportCSVGenerator implements CSVGenerator {

  private List<ExportResult> results;

  public ExportCSVGenerator(List<ExportResult> results) throws ReportStepsException {
    boolean hasSteps = false;
    Set<Integer> solveTypeIds = new HashSet<Integer>();
    for (ExportResult r : results) {
      if (r.hasSteps()) {
        hasSteps = true;
      }
      solveTypeIds.add(r.getSolveTypeId());
    }
    if (hasSteps && solveTypeIds.size() > 1) {
      // If a solve type has steps, we can't have more than one solve type in the list (because CSV column names come from steps of one solve type)
      throw new ReportStepsException();
    }
    this.results = results;
  }

  @Override
  public String getHeaderLine() {
    StringBuilder sb = new StringBuilder();
    sb.append("cubetype,solvetype,time,");
    if (!results.isEmpty() && results.get(0).hasSteps()) {
      for (String stepName : results.get(0).getStepsNames()) {
        String name = stepName.replaceAll(",", "");
        sb.append(name).append(",");
      }
    }
    sb.append("date,plustwo,scramble");
    return sb.toString();
  }

  @Override
  public String getExportLine(int n) {
    if (n < 0 || n >= results.size()) {
      return null;
    }
    ExportResult line = results.get(n);
    StringBuilder sb = new StringBuilder();
    sb.append(line.getCubeTypeName());
    sb.append(",");
    sb.append(line.getSolveTypeName());
    sb.append(",");
    sb.append(FormatterService.INSTANCE.formatSolveTime(line.getTime()));
    sb.append(",");
    if (line.hasSteps()) {
      for (Long t : line.getStepsTimes()) {
        sb.append(FormatterService.INSTANCE.formatSolveTime(t));
        sb.append(",");
      }
    }
    sb.append(FormatterService.INSTANCE.formatExportDateTime(line.getTimestamp()));
    sb.append(",");
    sb.append(line.isPlusTwo() ? "y" : "n");
    sb.append(",");
    sb.append(line.getScramble());
    return sb.toString();
  }
}
