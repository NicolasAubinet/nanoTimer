package com.cube.nanotimer.util.export;

public interface CSVGenerator {
  String getHeaderLine();
  String getExportLine(int n);
}
