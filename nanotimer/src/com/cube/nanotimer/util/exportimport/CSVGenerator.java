package com.cube.nanotimer.util.exportimport;

public interface CSVGenerator {
  String getHeaderLine();
  String getExportLine(int n);
}
