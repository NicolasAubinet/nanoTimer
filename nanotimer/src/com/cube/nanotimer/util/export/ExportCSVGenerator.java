package com.cube.nanotimer.util.export;

import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.vo.ExportResult;
import com.cube.nanotimer.vo.SolveTypeStep;

import java.util.List;

public class ExportCSVGenerator implements CSVGenerator {

  private List<ExportResult> results;

  public ExportCSVGenerator(List<ExportResult> results) {
    this.results = results;
  }

  @Override
  public String getHeaderLine() {
    return "cubetype,solvetype,time,date,steps,plustwo,blind,scramble";
  }

  @Override
  public String getExportLine(int n) {
    if (n < 0 || n >= results.size()) {
      return null;
    }
    ExportResult line = results.get(n);
    StringBuilder sb = new StringBuilder();
    sb.append(line.getCubeTypeName());
    sb.append(",");
    sb.append(escapeString(line.getSolveTypeName()));
    sb.append(",");
    sb.append(FormatterService.INSTANCE.formatSolveTime(line.getTime()));
    sb.append(",");
    sb.append(FormatterService.INSTANCE.formatExportDateTime(line.getTimestamp()));
    sb.append(",");
    if (line.hasSteps()) {
      sb.append(formatSteps(line.getStepsNames(), line.getStepsTimes()));
    }
    sb.append(",");
    sb.append(line.isPlusTwo() ? "y" : "n");
    sb.append(",");
    sb.append(line.isBlindType() ? "y" : "n");
    sb.append(",");
    sb.append(escapeString(line.getScramble()));
    return sb.toString();
  }

  private String formatSteps(String[] stepsNames, Long[] stepsTimes) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < stepsTimes.length; i++) {
      String stepName = stepsNames[i];
      for (char c : SolveTypeStep.FORBIDDEN_NAME_CHARACTERS) {
        stepName = stepName.replace(c, ' ');
      }
      sb.append(stepName).append('=').append(FormatterService.INSTANCE.formatSolveTime(stepsTimes[i]));
      if (i < stepsTimes.length - 1) {
        sb.append('|');
      }
    }
    return escapeString(sb.toString());
  }

  private String escapeString(String content) {
    // Adds quotes around string to escape it for CSV export (mostly for scrambles like Megaminx containing "\n", or Square-1 containing ",")
    if (content == null || content.equals("")) {
      return content;
    }
    if (content.length() >= 2 && content.charAt(0) == '"' && content.charAt(content.length() - 1) == '"') {
      return content; // already escaped
    }
    return '"' + content + '"';
  }
}
