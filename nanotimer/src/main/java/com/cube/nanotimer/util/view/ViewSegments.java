package com.cube.nanotimer.util.view;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.cube.nanotimer.R;
import com.cube.nanotimer.util.helper.GUIUtils;

/**
 * Paints one option of a segmented control (the {@code ViewSegment} styles, inside a
 * {@code view_segment_group} container): the chosen one is filled and picked out, the rest carry
 * no background at all. Shared so every such control in the app reads the same way.
 */
public class ViewSegments {

  private ViewSegments() {
  }

  /**
   * The same, with the chosen option filled in {@code accent} and inked dark on it, for a control
   * that carries a step's colour. An accent of 0 is the plain style.
   */
  public static void style(TextView segment, boolean chosen, int accent) {
    if (accent == 0) {
      style(segment, chosen);
      return;
    }
    if (chosen) {
      GradientDrawable fill = new GradientDrawable();
      fill.setColor(accent);
      fill.setCornerRadius(14 * segment.getResources().getDisplayMetrics().density);
      segment.setBackground(fill);
    } else {
      segment.setBackground(null);
    }
    segment.setTextColor(ContextCompat.getColor(segment.getContext(),
        chosen ? R.color.gray900 : R.color.secondary_text));
    GUIUtils.setWeight(segment, chosen ? Typeface.BOLD : Typeface.NORMAL);
  }

  public static void style(TextView segment, boolean chosen) {
    if (chosen) {
      segment.setBackgroundResource(R.drawable.view_segment_active);
    } else {
      segment.setBackground(null);
    }
    segment.setTextColor(ContextCompat.getColor(segment.getContext(),
        chosen ? R.color.white : R.color.secondary_text));
    GUIUtils.setWeight(segment, chosen ? Typeface.BOLD : Typeface.NORMAL);
  }
}
