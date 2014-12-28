package com.cube.nanotimer.util;

import com.cube.nanotimer.vo.ExportResult;

import java.util.List;

public class ExportCSVGenerator implements CSVGenerator {

  private List<ExportResult> results;

  public ExportCSVGenerator(List<ExportResult> results) {
    this.results = results;
  }

  @Override
  public String getHeaderLine() {
    return "cubetype,solvetype,time,date,plustwo,scramble";
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
    sb.append(FormatterService.INSTANCE.formatExportDateTime(line.getTimestamp()));
    sb.append(",");
    sb.append(line.isPlusTwo() ? "y" : "n");
    sb.append(",");
    sb.append(line.getScramble());
    return sb.toString();
  }
}
