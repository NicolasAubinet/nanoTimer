package com.cube.nanotimer.util.exportimport.csvexport;

import com.cube.nanotimer.vo.ExportResult;

import java.util.Arrays;
import java.util.List;

public class ExportCSVGenerator implements CSVGenerator {

  public static final String CSV_HEADER_LINE = "cubetype,solvetype,time,date,steps,plustwo,blind,scrambleType,scramble,comment";
  public static final List<String> OLD_CSV_HEADER_LINES = Arrays.asList(
    "cubetype,solvetype,time,date,steps,plustwo,blind,scramble",
    "cubetype,solvetype,time,date,steps,plustwo,blind,scrambleType,scramble");

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

  public static boolean isHeaderLegit(String parHeaderLine) {
    boolean locFoundValidHeader = false;

    if (parHeaderLine.toLowerCase().equals(ExportCSVGenerator.CSV_HEADER_LINE.toLowerCase())) {
      locFoundValidHeader = true;
    }

    if (!locFoundValidHeader) {
      for (String locOldCsvHeaderLine : OLD_CSV_HEADER_LINES) {
        if (locOldCsvHeaderLine.toLowerCase().equals(parHeaderLine.toLowerCase())) {
          locFoundValidHeader = true;
        }
      }
    }

    return locFoundValidHeader;
  }

}
