package com.cube.nanotimer.util.helper;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import junit.framework.Assert;

import java.util.List;

public class FileUtilsTest extends AndroidTestCase {

  private static final String FILENAME = "TestFile";

  @SmallTest
  public void testFileReadWrite() {
    FileUtils.deleteFile(getContext(), FILENAME);

    String[] lines = new String[] {
        "abc", "def", "ghi", "jkl", "mno"
    };
    FileUtils.appendLinesToFile(getContext(), FILENAME, lines);
    Assert.assertEquals(5, FileUtils.getFileLinesCount(getContext(), FILENAME));

    lines = new String[] {
        "pqr", "stu"
    };
    FileUtils.appendLinesToFile(getContext(), FILENAME, lines);
    Assert.assertEquals(7, FileUtils.getFileLinesCount(getContext(), FILENAME));

    List<String> read = FileUtils.readLinesFromFile(getContext(), FILENAME);
    Assert.assertEquals(7, read.size());
    Assert.assertEquals("abc", read.get(0));
    Assert.assertEquals("def", read.get(1));
    Assert.assertEquals("ghi", read.get(2));
    Assert.assertEquals("jkl", read.get(3));
    Assert.assertEquals("mno", read.get(4));
    Assert.assertEquals("pqr", read.get(5));
    Assert.assertEquals("stu", read.get(6));

    read = FileUtils.readLinesFromFile(getContext(), FILENAME, 3);
    Assert.assertEquals(3, read.size());
    Assert.assertEquals("abc", read.get(0));
    Assert.assertEquals("def", read.get(1));
    Assert.assertEquals("ghi", read.get(2));

    FileUtils.removeFirstLineFromFile(getContext(), FILENAME);
    read = FileUtils.readLinesFromFile(getContext(), FILENAME);
    Assert.assertEquals(6, read.size());
    Assert.assertEquals(6, FileUtils.getFileLinesCount(getContext(), FILENAME));

    read = FileUtils.readLinesFromFile(getContext(), FILENAME, 3);
    Assert.assertEquals(3, read.size());
    Assert.assertEquals("def", read.get(0));
    Assert.assertEquals("ghi", read.get(1));
    Assert.assertEquals("jkl", read.get(2));

    FileUtils.removeFirstLineFromFile(getContext(), FILENAME);
    FileUtils.removeFirstLineFromFile(getContext(), FILENAME);
    Assert.assertEquals(4, FileUtils.getFileLinesCount(getContext(), FILENAME));

    FileUtils.renameFile(getContext(), FILENAME, FILENAME + "_tmp");
    Assert.assertEquals(0, FileUtils.getFileLinesCount(getContext(), FILENAME));
    read = FileUtils.readLinesFromFile(getContext(), FILENAME);
    Assert.assertEquals(0, read.size());
    FileUtils.removeFirstLineFromFile(getContext(), FILENAME);
    Assert.assertEquals(0, FileUtils.getFileLinesCount(getContext(), FILENAME));
    FileUtils.renameFile(getContext(), FILENAME + "_tmp", FILENAME);
    Assert.assertEquals(4, FileUtils.getFileLinesCount(getContext(), FILENAME));

    FileUtils.removeFirstLineFromFile(getContext(), FILENAME);
    FileUtils.removeFirstLineFromFile(getContext(), FILENAME);
    FileUtils.removeFirstLineFromFile(getContext(), FILENAME);
    Assert.assertEquals(1, FileUtils.getFileLinesCount(getContext(), FILENAME));
    read = FileUtils.readLinesFromFile(getContext(), FILENAME, 3);
    Assert.assertEquals(1, read.size());
    Assert.assertEquals("stu", read.get(0));

    FileUtils.removeFirstLineFromFile(getContext(), FILENAME);
    Assert.assertEquals(0, FileUtils.getFileLinesCount(getContext(), FILENAME));
    FileUtils.removeFirstLineFromFile(getContext(), FILENAME);
    Assert.assertEquals(0, FileUtils.getFileLinesCount(getContext(), FILENAME));
    read = FileUtils.readLinesFromFile(getContext(), FILENAME, 3);
    Assert.assertEquals(0, read.size());
    FileUtils.removeFirstLineFromFile(getContext(), FILENAME);

    lines = new String[] {
        "yes", "no"
    };
    FileUtils.appendLinesToFile(getContext(), FILENAME, lines);
    Assert.assertEquals(2, FileUtils.getFileLinesCount(getContext(), FILENAME));

    read = FileUtils.readLinesFromFile(getContext(), FILENAME, 5);
    Assert.assertEquals(2, read.size());
    Assert.assertEquals("yes", read.get(0));
    Assert.assertEquals("no", read.get(1));

    read = FileUtils.readLinesFromFile(getContext(), FILENAME);
    Assert.assertEquals(2, read.size());
    Assert.assertEquals("yes", read.get(0));
    Assert.assertEquals("no", read.get(1));

    FileUtils.removeFirstLineFromFile(getContext(), FILENAME);
    read = FileUtils.readLinesFromFile(getContext(), FILENAME);
    Assert.assertEquals(1, read.size());
    Assert.assertEquals("no", read.get(0));

    FileUtils.deleteFile(getContext(), FILENAME);
    Assert.assertEquals(0, FileUtils.getFileLinesCount(getContext(), FILENAME));
    read = FileUtils.readLinesFromFile(getContext(), FILENAME);
    Assert.assertEquals(0, read.size());
  }

}
