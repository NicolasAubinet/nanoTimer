package com.cube.nanotimer.gui.widget;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cube.nanotimer.R;
import com.cube.nanotimer.smartcube.step.LastLayerDiagram;
import com.cube.nanotimer.util.view.GroupLearnedEntrance;

import java.util.ArrayList;
import java.util.List;

/**
 * The moment a coach learning drill completes a shape group: the group's cases together, a line
 * saying the shape is now always solvable in one algorithm, and how far into OLL that brings the
 * solver. Laid over the drill, which waits until it is closed.
 */
public final class GroupLearnedPanel {

  private static final int CASES_PER_ROW = 4;

  private GroupLearnedPanel() {
  }

  /**
   * @param cases the group's cases, {@code oll_7}
   * @param learned OLLs learned in all, this group included
   * @param total OLLs there are
   */
  public static void show(Activity activity, String group, List<String> cases, int learned,
      int total, final Runnable onClosed) {
    final ViewGroup root = activity.findViewById(android.R.id.content);
    final View panel = LayoutInflater.from(activity).inflate(R.layout.group_learned_panel, root,
        false);
    ((TextView) panel.findViewById(R.id.tvGroupLearnedTitle)).setText(group);
    ((TextView) panel.findViewById(R.id.tvGroupLearnedBody)).setText(activity.getResources()
        .getQuantityString(R.plurals.group_learned_body, cases.size(), cases.size(), group));
    ((TextView) panel.findViewById(R.id.tvGroupLearnedProgress))
        .setText(activity.getString(R.string.group_learned_progress, learned, total));
    List<View> charts = addCases(activity, (LinearLayout) panel.findViewById(
        R.id.llGroupLearnedCases), cases);
    ProgressBar bar = panel.findViewById(R.id.pbGroupLearned);
    int before = Math.round(1000f * (learned - cases.size()) / total);
    int after = Math.round(1000f * learned / total);
    bar.setProgress(before);
    panel.findViewById(R.id.btGroupLearnedContinue).setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            v.setEnabled(false); // a second tap would close it twice and skip a case
            GroupLearnedEntrance.leave(panel, new Runnable() {
              @Override
              public void run() {
                root.removeView(panel);
                if (onClosed != null) {
                  onClosed.run();
                }
              }
            });
          }
        });
    root.addView(panel);
    GroupLearnedEntrance.play(panel, panel.findViewById(R.id.llGroupLearnedCard),
        panel.findViewById(R.id.flGroupLearnedBadge), charts, bar, after,
        panel.findViewById(R.id.vGroupLearnedConfetti));
  }

  private static List<View> addCases(Activity activity, LinearLayout rows, List<String> cases) {
    int size = activity.getResources().getDimensionPixelSize(R.dimen.group_learned_case);
    int gap = activity.getResources().getDimensionPixelSize(R.dimen.group_learned_case_gap);
    List<View> charts = new ArrayList<View>();
    LinearLayout row = null;
    for (int i = 0; i < cases.size(); i++) {
      if (i % CASES_PER_ROW == 0) {
        row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        rows.addView(row, params);
      }
      LastLayerCaseView chart = new LastLayerCaseView(activity);
      chart.setDiagram(LastLayerDiagram.forCase(cases.get(i)));
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
      params.setMargins(gap, gap, gap, gap);
      row.addView(chart, params);
      charts.add(chart);
    }
    return charts;
  }
}
