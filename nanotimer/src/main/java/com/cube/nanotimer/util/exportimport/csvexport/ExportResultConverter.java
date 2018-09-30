package com.cube.nanotimer.util.exportimport.csvexport;

import android.content.Context;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.util.exportimport.CSVFormatException;
import com.cube.nanotimer.util.helper.Utils;
import com.cube.nanotimer.vo.ExportResult;

import java.util.ArrayList;
import java.util.List;

public class ExportResultConverter {

  public static String toCSVLine(ExportResult result) {
    StringBuilder sb = new StringBuilder();
    sb.append(result.getCubeTypeName());
    sb.append(",");
    sb.append(escapeString(result.getSolveTypeName()));
    sb.append(",");
    sb.append(FormatterService.INSTANCE.formatSolveTime(result.getTime()));
    sb.append(",");
    sb.append(FormatterService.INSTANCE.formatExportDateTime(result.getTimestamp()));
    sb.append(",");
    if (result.hasSteps()) {
      sb.append(formatSteps(result.getStepsNames(), result.getStepsTimes()));
    }
    sb.append(",");
    sb.append(result.isPlusTwo() ? "y" : "n");
    sb.append(",");
    sb.append(result.isBlindType() ? "y" : "n");
    sb.append(",");
    if (result.getScrambleTypeName() != null) {
      sb.append(result.getScrambleTypeName());
    }
    sb.append(",");
    if (result.getScramble() != null) {
      sb.append(escapeString(result.getScramble()));
    }
    sb.append(",");
    if (result.getComment() != null) {
      sb.append(result.getComment());
    }
    return sb.toString();
  }

  public static ExportResult fromCSVLine(Context context, String line) throws CSVFormatException {
    List<String> fields = getFieldsFromCSVLine(line);
    if (fields.size() < 8) { // 8 fields is older version, 9 fields contains the scramble type, 10 fields contains comment
      throw new CSVFormatException(context.getString(R.string.import_invalid_columns_count));
    }
    String cubeTypeName = fields.get(0);
    String solveTypeName = fields.get(1);
    Long time = FormatterService.INSTANCE.unformatSolveTime(fields.get(2));
    if (time == null) {
      throw new CSVFormatException(context.getString(R.string.could_not_convert_time, fields.get(2)));
    }
    Long timestamp = FormatterService.INSTANCE.unformatExportDateTime(fields.get(3));
    if (timestamp == null) {
      throw new CSVFormatException(context.getString(R.string.could_not_convert_date, fields.get(3)));
    }
    boolean plusTwo = (fields.get(5).equals("y"));
    boolean blindType = (fields.get(6).equals("y"));

    int scrambleFieldIndex;
    String scrambleTypeName = null;
    if (fields.size() == 8) {
      scrambleFieldIndex = 7;
    } else {
      scrambleTypeName = fields.get(7);
      scrambleFieldIndex = 8;
    }
    String scramble = fields.get(scrambleFieldIndex);
    if ("".equals(scramble.trim())) {
      scramble = null;
    }

    String comment = null;
    if (fields.size() > 9) {
      comment = fields.get(9);
    }

    ExportResult exportResult = new ExportResult(cubeTypeName, solveTypeName, time, timestamp, plusTwo, blindType, scrambleTypeName, scramble, comment);
    String stepsField = fields.get(4);
    exportResult.setStepsTimes(getStepsTimes(context, stepsField));
    exportResult.setStepsNames(getStepsNames(context, stepsField));
    return exportResult;
  }

  private static Long[] getStepsTimes(Context context, String stepsField) throws CSVFormatException {
    String[] stepsTimesStr = getStepsField(context, stepsField, 1);
    if (stepsTimesStr == null) {
      return null;
    }
    Long[] stepsTimes = new Long[stepsTimesStr.length];
    for (int i = 0; i < stepsTimesStr.length; i++) {
      Long stepTime = FormatterService.INSTANCE.unformatSolveTime(stepsTimesStr[i]);
      if (stepTime == null) {
        throw new CSVFormatException(context.getString(R.string.could_not_convert_step_time, stepsTimesStr[i]));
      }
      stepsTimes[i] = stepTime;
    }
    return stepsTimes;
  }

  private static String[] getStepsNames(Context context, String stepsField) throws CSVFormatException {
    return getStepsField(context, stepsField, 0);
  }

  private static String[] getStepsField(Context context, String stepsField, int fieldIndex) throws CSVFormatException {
    if (stepsField == null || stepsField.equals("")) {
      return null;
    }
    String[] split = stepsField.split("\\|");
    String[] stepNames = new String[split.length];
    for (int i = 0; i < split.length; i++) {
      String[] stepSplit = split[i].split("=");
      if (stepSplit.length != 2) {
        String stepName = (stepSplit.length > 0) ? stepSplit[0] : "";
        throw new CSVFormatException(context.getString(R.string.invalid_step_format, stepName));
      }
      stepNames[i] = stepSplit[fieldIndex];
    }
    return stepNames;
  }

  private static List<String> getFieldsFromCSVLine(String line) {
    final char escapeChar = '"';
    boolean inEscapedString = false;
    List<String> fields = new ArrayList<String>();
    StringBuilder currentField = new StringBuilder();
    for (char c : line.toCharArray()) {
      if (c == escapeChar) {
        inEscapedString = !inEscapedString;
      } else {
        if (c == ',') {
          if (inEscapedString) {
            currentField.append(c);
          } else {
            fields.add(currentField.toString());
            currentField.delete(0, currentField.length());
          }
        } else {
          currentField.append(c);
        }
      }
    }
    fields.add(currentField.toString());
    return fields;
  }

  private static String formatSteps(String[] stepsNames, Long[] stepsTimes) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < stepsTimes.length; i++) {
      String stepName = stepsNames[i];
      for (char c : Utils.FORBIDDEN_NAME_CHARACTERS) {
        stepName = stepName.replace(c, ' ');
      }
      sb.append(stepName).append('=').append(FormatterService.INSTANCE.formatSolveTime(stepsTimes[i]));
      if (i < stepsTimes.length - 1) {
        sb.append('|');
      }
    }
    return escapeString(sb.toString());
  }

  private static String escapeString(String content) {
    // Adds quotes around string to escape it for CSV export (mostly for scrambles like Megaminx containing "\n", or Square-1 containing ",")
    if (content == null || content.equals("")) {
      return content;
    }
    if (content.length() >= 2 && content.charAt(0) == '"' && content.charAt(content.length() - 1) == '"') {
      return content; // already escaped
    }
    return '"' + content + '"';
  }

}
