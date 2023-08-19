package com.cube.nanotimer.util.helper;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class FileUtilsTest {

  private static final String FILENAME = "TestFile";

  @Test
  public void testFileReadWrite() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    FileUtils.deleteFile(context, FILENAME);

    String[] lines = new String[] {
        "abc", "def", "ghi", "jkl", "mno"
    };
    FileUtils.appendLinesToFile(context, FILENAME, lines);
    Assert.assertEquals(5, FileUtils.getFileLinesCount(context, FILENAME));

    lines = new String[] {
        "pqr", "stu"
    };
    FileUtils.appendLinesToFile(context, FILENAME, lines);
    Assert.assertEquals(7, FileUtils.getFileLinesCount(context, FILENAME));

    List<String> read = FileUtils.readLinesFromFile(context, FILENAME);
    Assert.assertEquals(7, read.size());
    Assert.assertEquals("abc", read.get(0));
    Assert.assertEquals("def", read.get(1));
    Assert.assertEquals("ghi", read.get(2));
    Assert.assertEquals("jkl", read.get(3));
    Assert.assertEquals("mno", read.get(4));
    Assert.assertEquals("pqr", read.get(5));
    Assert.assertEquals("stu", read.get(6));

    read = FileUtils.readLinesFromFile(context, FILENAME, 3);
    Assert.assertEquals(3, read.size());
    Assert.assertEquals("abc", read.get(0));
    Assert.assertEquals("def", read.get(1));
    Assert.assertEquals("ghi", read.get(2));

    FileUtils.removeFirstLineFromFile(context, FILENAME);
    read = FileUtils.readLinesFromFile(context, FILENAME);
    Assert.assertEquals(6, read.size());
    Assert.assertEquals(6, FileUtils.getFileLinesCount(context, FILENAME));

    read = FileUtils.readLinesFromFile(context, FILENAME, 3);
    Assert.assertEquals(3, read.size());
    Assert.assertEquals("def", read.get(0));
    Assert.assertEquals("ghi", read.get(1));
    Assert.assertEquals("jkl", read.get(2));

    FileUtils.removeFirstLineFromFile(context, FILENAME);
    FileUtils.removeFirstLineFromFile(context, FILENAME);
    Assert.assertEquals(4, FileUtils.getFileLinesCount(context, FILENAME));

    FileUtils.renameFile(context, FILENAME, FILENAME + "_tmp");
    Assert.assertEquals(0, FileUtils.getFileLinesCount(context, FILENAME));
    read = FileUtils.readLinesFromFile(context, FILENAME);
    Assert.assertEquals(0, read.size());
    FileUtils.removeFirstLineFromFile(context, FILENAME);
    Assert.assertEquals(0, FileUtils.getFileLinesCount(context, FILENAME));
    FileUtils.renameFile(context, FILENAME + "_tmp", FILENAME);
    Assert.assertEquals(4, FileUtils.getFileLinesCount(context, FILENAME));

    FileUtils.removeFirstLineFromFile(context, FILENAME);
    FileUtils.removeFirstLineFromFile(context, FILENAME);
    FileUtils.removeFirstLineFromFile(context, FILENAME);
    Assert.assertEquals(1, FileUtils.getFileLinesCount(context, FILENAME));
    read = FileUtils.readLinesFromFile(context, FILENAME, 3);
    Assert.assertEquals(1, read.size());
    Assert.assertEquals("stu", read.get(0));

    FileUtils.removeFirstLineFromFile(context, FILENAME);
    Assert.assertEquals(0, FileUtils.getFileLinesCount(context, FILENAME));
    FileUtils.removeFirstLineFromFile(context, FILENAME);
    Assert.assertEquals(0, FileUtils.getFileLinesCount(context, FILENAME));
    read = FileUtils.readLinesFromFile(context, FILENAME, 3);
    Assert.assertEquals(0, read.size());
    FileUtils.removeFirstLineFromFile(context, FILENAME);

    lines = new String[] {
        "yes", "no"
    };
    FileUtils.appendLinesToFile(context, FILENAME, lines);
    Assert.assertEquals(2, FileUtils.getFileLinesCount(context, FILENAME));

    read = FileUtils.readLinesFromFile(context, FILENAME, 5);
    Assert.assertEquals(2, read.size());
    Assert.assertEquals("yes", read.get(0));
    Assert.assertEquals("no", read.get(1));

    read = FileUtils.readLinesFromFile(context, FILENAME);
    Assert.assertEquals(2, read.size());
    Assert.assertEquals("yes", read.get(0));
    Assert.assertEquals("no", read.get(1));

    FileUtils.removeFirstLineFromFile(context, FILENAME);
    read = FileUtils.readLinesFromFile(context, FILENAME);
    Assert.assertEquals(1, read.size());
    Assert.assertEquals("no", read.get(0));

    FileUtils.deleteFile(context, FILENAME);
    Assert.assertEquals(0, FileUtils.getFileLinesCount(context, FILENAME));
    read = FileUtils.readLinesFromFile(context, FILENAME);
    Assert.assertEquals(0, read.size());
  }

}
