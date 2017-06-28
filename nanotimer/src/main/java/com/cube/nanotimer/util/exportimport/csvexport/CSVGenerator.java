package com.cube.nanotimer.util.exportimport.csvexport;

public interface CSVGenerator {
  String getHeaderLine();
  String getExportLine(int n);
}
