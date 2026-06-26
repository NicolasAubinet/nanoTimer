package com.cube.nanotimer.util.exportimport.csvexport;

import com.cube.nanotimer.vo.ExportResult;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ExportResultConverterTest {

  // The exported time must always keep full millisecond precision, regardless of the
  // "high precision timer" display setting. Otherwise an export/import round-trip rounds
  // times to centiseconds (every imported time ends in 0).
  @Test
  public void testExportKeepsMillisecondPrecision() {
    // 2005 ms displays as "2.01" with high precision off, but must export as "2.005".
    ExportResult result = new ExportResult("3x3", "Default", 2005, 0, false, false, null, null, null);
    String csvLine = ExportResultConverter.toCSVLine(result);
    String timeField = csvLine.split(",")[2];
    Assert.assertEquals("2.005", timeField);
  }
}
