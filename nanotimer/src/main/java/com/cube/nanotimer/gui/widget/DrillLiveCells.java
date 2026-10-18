package com.cube.nanotimer.gui.widget;

import android.app.Activity;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.cube.nanotimer.R;
import com.cube.nanotimer.smartcube.drill.DrillRep;
import com.cube.nanotimer.util.FormatterService;

import java.util.List;

/**
 * The running drill's three figures ({@code drill_live_cells}): the mean of the reps so far, the
 * mean of their reading, and the best. Skipped and practice reps are left out, as the summary leaves
 * them out of its own.
 */
public final class DrillLiveCells {

  private DrillLiveCells() {
  }

  public static void show(Activity activity, List<DrillRep> reps) {
    long total = 0;
    long recognition = 0;
    long best = Long.MAX_VALUE;
    int timed = 0;
    for (DrillRep rep : reps) {
      if (rep.isAbandoned() || rep.isPractice()) {
        continue;
      }
      total += rep.getTotalMs();
      recognition += rep.getRecognitionMs();
      best = Math.min(best, rep.getTotalMs());
      timed++;
    }
    if (timed == 0) {
      return; // the layout's dashes stand until there is a figure
    }
    FormatterService format = FormatterService.INSTANCE;
    ((TextView) activity.findViewById(R.id.tvDrillLiveMean)).setText(
        format.formatSolveTime(total / timed));
    ((TextView) activity.findViewById(R.id.tvDrillLiveRecognition)).setText(
        format.formatSolveTime(recognition / timed));
    TextView bestView = activity.findViewById(R.id.tvDrillLiveBest);
    bestView.setText(format.formatSolveTime(best));
    bestView.setTextColor(ContextCompat.getColor(activity, R.color.green_soft));
  }
}
