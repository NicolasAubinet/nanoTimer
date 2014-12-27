package com.cube.nanotimer.util;

public interface CSVGenerator {
  String getHeaderLine();
  String getExportLine(int n);
}
