package com.cube.nanotimer.util.helper;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileUtils {

  public static List<String> readLinesFromFile(Context context, String fileName) {
    return readLinesFromFile(context, fileName, -1);
  }

  public static List<String> readLinesFromFile(Context context, String fileName, int linesLimit) {
    List<String> lines = new ArrayList<String>();
    try {
      FileInputStream fis;
      try {
        fis = context.openFileInput(fileName);
      } catch (FileNotFoundException e) {
        return lines;
      }
      Scanner fileScanner = new Scanner(fis);
      int i = 0;
      while (fileScanner.hasNextLine() && (linesLimit < 0 || i < linesLimit)) {
        lines.add(fileScanner.nextLine());
        i++;
      }
      fileScanner.close();
      fis.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return lines;
  }

  public static void appendLinesToFile(Context context, String fileName, String[] linesToSave) {
    try {
      FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_APPEND);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
      for (String l : linesToSave) {
        writer.write(l);
        writer.newLine();
      }
      writer.close();
      fos.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void removeFirstLineFromFile(Context context, String fileName) {
    String tmpFileName = fileName + "_tmp";
    try {
      InputStream is = context.openFileInput(fileName);
      Scanner fileScanner = new Scanner(is);
      if (!fileScanner.hasNextLine()) {
        return; // file is empty
      }
      fileScanner.nextLine();

      FileOutputStream fos = context.openFileOutput(tmpFileName, Context.MODE_PRIVATE);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
      while (fileScanner.hasNextLine()) {
        writer.write(fileScanner.nextLine());
        writer.newLine();
      }
      writer.close();
      fos.close();

      renameFile(context, tmpFileName, fileName);

      fileScanner.close();
      is.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void renameFile(Context context, String oldName, String newName) {
    File tmpFile = new File(context.getFilesDir(), oldName);
    File destFile = new File(context.getFilesDir(), newName);
    tmpFile.renameTo(destFile);
  }

  public static int getFileLinesCount(Context context, String fileName) {
    int linesCount = 0;
    try {
      InputStream is;
      try {
        is = context.openFileInput(fileName);
      } catch (FileNotFoundException e) {
        return 0;
      }
      byte[] c = new byte[1024];
      int count = 0;
      int readChars = 0;
      boolean empty = true;
      while ((readChars = is.read(c)) != -1) {
        empty = false;
        for (int i = 0; i < readChars; ++i) {
          if (c[i] == '\n') {
            count++;
          }
        }
      }
      is.close();
      linesCount = (count == 0 && !empty) ? 1 : count;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return linesCount;
  }

  public static void deleteFile(Context context, String fileName) {
    File f = new File(context.getFilesDir(), fileName);
    f.delete();
  }

}
