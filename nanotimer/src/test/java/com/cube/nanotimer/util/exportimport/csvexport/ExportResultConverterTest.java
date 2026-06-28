package com.cube.nanotimer.util.exportimport.csvexport;

import com.cube.nanotimer.vo.ExportResult;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

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

  // A comment must survive the encode/decode round-trip unchanged, including the
  // characters that used to corrupt the CSV: double-quotes, newlines, backslashes
  // and commas. (Round-tripping through fromCSVLine() is not possible in a plain
  // unit test because unformatSolveTime() touches App.INSTANCE.)
  @Test
  public void testCommentEncodingRoundTrips() {
    String comment = "He said \"hello\",\n then \\ DNFed";
    String decoded = ExportResultConverter.decodeComment(ExportResultConverter.encodeComment(comment));
    Assert.assertEquals(comment, decoded);
  }

  // The encoded comment must never contain a raw '"': the importer counts quotes to
  // stitch multi-line scrambles back together (CSVLineGrouper.group), so a quote in a
  // comment would desync that grouping. This is the root cause of the
  // "Nombre de colonnes invalide" import failure.
  @Test
  public void testEncodedCommentHasNoRawQuote() {
    String encoded = ExportResultConverter.encodeComment("odd \" quote \" count \"");
    Assert.assertEquals(-1, encoded.indexOf('"'));
  }

  // The importer (CSVLineGrouper.group) stitches multi-line scrambles back together by
  // counting '"' characters: a line with an odd number of quotes is treated as
  // "unterminated" and merged with the following line(s). An exported record must therefore
  // always have an even number of quotes, otherwise it swallows the next record and the
  // merged blob fails the column-count check.
  @Test
  public void testExportedLineHasEvenQuoteCount() {
    String comment = "odd \" quote count breaks line grouping";
    ExportResult result = new ExportResult("3x3x3", "Alex", 2005, 1700000000000L, false, false, null, "R U R'", comment);
    String csvLine = ExportResultConverter.toCSVLine(result);
    long quoteCount = csvLine.chars().filter(c -> c == '"').count();
    Assert.assertEquals("Exported line must contain an even number of quotes", 0, quoteCount % 2);
  }

  // A Square-1 scramble contains commas (e.g. "(1,0) / ..."). It is exported quoted, so the
  // field splitter must keep those commas inside the scramble field instead of treating them
  // as column separators.
  @Test
  public void testScrambleWithCommasKeepsColumnCount() {
    String scramble = "(1,0) / (-3,0) / (3,3) /";
    ExportResult result = new ExportResult("Square-1", "Default", 5000, 1700000000000L, false, false, null, scramble, null);
    String csvLine = ExportResultConverter.toCSVLine(result);
    List<String> fields = ExportResultConverter.getFieldsFromCSVLine(csvLine, 10);
    Assert.assertEquals(scramble, fields.get(8)); // index 8 = scramble field
  }

  // Commas inside a comment must stay in the (last) comment column, not split it into extra
  // columns.
  @Test
  public void testCommentWithCommasKeepsColumnCount() {
    String comment = "PB single, lucky skip, very nice";
    ExportResult result = new ExportResult("3x3x3", "Alex", 5000, 1700000000000L, false, false, null, "R U R'", comment);
    String csvLine = ExportResultConverter.toCSVLine(result);
    List<String> fields = ExportResultConverter.getFieldsFromCSVLine(csvLine, 10);
    Assert.assertEquals(10, fields.size());
    Assert.assertEquals(comment, ExportResultConverter.decodeComment(fields.get(9)));
  }

  // A spread of awkward comments must survive the encode/decode round-trip byte-for-byte.
  @Test
  public void testCommentEdgeCasesRoundTrip() {
    String[] comments = {
      "\"",                          // a lone quote
      "\"wrapped\"",                 // wrapped in quotes
      "literal backslash-n: \\n",    // user typed a backslash followed by 'n'
      "trailing newlines\n\n\n",     // mirrors the comment in the file that failed
      "",                            // empty
      "accents éàü, 你好, 🧩",        // unicode + commas
      "all of it: \" , \\ \n end",   // quote, comma, backslash and newline together
    };
    for (String comment : comments) {
      String decoded = ExportResultConverter.decodeComment(ExportResultConverter.encodeComment(comment));
      Assert.assertEquals(comment, decoded);
    }
  }

  // End-to-end invariant over a spread of nasty comments: every exported record must keep an
  // even quote count, parse back to the full 10 columns, and yield the original comment. This
  // generalizes the regression so a future free-text field can't silently reintroduce the bug.
  @Test
  public void testExportedLineInvariantForNastyComments() {
    String[] comments = {
      "plain",
      "one \" quote",
      "two \"\" quotes",
      "comma, comma, comma",
      "quote \" and comma , together",
      "embedded\nnewline",
      "",
    };
    for (String comment : comments) {
      ExportResult result = new ExportResult("3x3x3", "Alex", 5000, 1700000000000L, false, false, null, "R U R'", comment);
      String csvLine = ExportResultConverter.toCSVLine(result);

      long quotes = csvLine.chars().filter(c -> c == '"').count();
      Assert.assertEquals("even quote count for comment: " + comment, 0, quotes % 2);

      List<String> fields = ExportResultConverter.getFieldsFromCSVLine(csvLine, 10);
      Assert.assertEquals("column count for comment: " + comment, 10, fields.size());
      Assert.assertEquals("comment round-trip for comment: " + comment, comment, ExportResultConverter.decodeComment(fields.get(9)));
    }
  }
}
