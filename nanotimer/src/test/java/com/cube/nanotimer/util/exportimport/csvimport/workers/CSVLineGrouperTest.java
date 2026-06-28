package com.cube.nanotimer.util.exportimport.csvimport.workers;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class CSVLineGrouperTest {

  private static final String HEADER = "cubetype,solvetype,time,date,steps,plustwo,blind,scrambleType,scramble,comment";

  // Two well-formed single-line records (each with a balanced number of quotes) must stay
  // separate.
  @Test
  public void testWellFormedRecordsAreNotMerged() {
    List<String> lines = new ArrayList<String>(Arrays.asList(
      HEADER,
      "3x3x3,\"Alex\",1:00.00,Feb 1 2024 - 18:45:05,,n,n,,\"R U R'\",",
      "3x3x3,\"Alex\",2:00.00,Feb 1 2024 - 18:46:05,,n,n,,\"L U L'\","
    ));
    List<String> grouped = CSVLineGrouper.group(lines);
    Assert.assertEquals(3, grouped.size());
  }

  // A Megaminx scramble contains real newlines inside its quoted field, so the record spans
  // several physical lines and must be stitched back into a single record.
  @Test
  public void testMultiLineScrambleIsMergedIntoOneRecord() {
    List<String> lines = new ArrayList<String>(Arrays.asList(
      HEADER,
      "Megaminx,\"Alex\",1:30.00,Feb 1 2024 - 18:45:05,,n,n,,\"R++ D-- R++ D--",
      "U' R-- D++ R-- D++ U\","
    ));
    List<String> grouped = CSVLineGrouper.group(lines);
    Assert.assertEquals(2, grouped.size());
    Assert.assertTrue(grouped.get(1).contains("R++ D-- R++ D--\nU' R-- D++ R-- D++ U"));
  }

  // Regression for the original bug: a comment used to be written with raw quotes, so an odd
  // number of them made the record swallow the following one ("Nombre de colonnes invalide").
  // encodeComment now emits no raw quote (quotes become "\q"), so the records stay separate.
  @Test
  public void testCommentWithEncodedQuoteDoesNotSwallowNextRecord() {
    List<String> lines = new ArrayList<String>(Arrays.asList(
      HEADER,
      "3x3x3,\"Alex\",DNF,Feb 1 2024 - 08:05:52,,n,n,,\"F2 D'\",He said \\qhi\\q then DNFed",
      "3x3x3,\"Alex\",1:15.15,Feb 1 2024 - 08:04:16,,n,n,,\"B2 D\","
    ));
    List<String> grouped = CSVLineGrouper.group(lines);
    Assert.assertEquals(3, grouped.size());
  }

  // An empty input must not blow up.
  @Test
  public void testEmptyInput() {
    Assert.assertEquals(0, CSVLineGrouper.group(new ArrayList<String>()).size());
  }
}
