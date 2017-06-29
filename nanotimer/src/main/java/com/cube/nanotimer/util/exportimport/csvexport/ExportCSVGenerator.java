package com.cube.nanotimer.util.exportimport.csvexport;

import com.cube.nanotimer.vo.ExportResult;

import java.util.List;

public class ExportCSVGenerator implements CSVGenerator {

  public static final String CSV_HEADER_LINE = "cubetype,solvetype,time,date,steps,plustwo,blind,scrambleType,scramble";
  public static final String CSV_HEADER_LINE_OLD = "cubetype,solvetype,time,date,steps,plustwo,blind,scramble";

  private List<ExportResult> results;

  public ExportCSVGenerator(List<ExportResult> results) {
    this.results = results;
  }

  @Override
  public String getHeaderLine() {
    return CSV_HEADER_LINE;
  }

  @Override
  public String getExportLine(int n) {
    if (n < 0 || n >= results.size()) {
      return null;
    }
    ExportResult line = results.get(n);
    return ExportResultConverter.toCSVLine(line);
  }

}
