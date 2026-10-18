package com.cube.nanotimer.gui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.cube.nanotimer.R;

/**
 * A cross drill's reps in the order dealt, one segment each, coloured by how the rep finished:
 * the shortest cross, a longer one, or none at all. A rep with no shortest to compare against is
 * drawn as a plain wash.
 */
public class DrillOutcomeStrip extends View {

  public static final int OPTIMAL = 0;
  public static final int LONGER = 1;
  public static final int MISSED = 2;
  public static final int UNSCORED = 3;

  private static final float GAP_DP = 4;
  private static final float CORNER_DP = 3;
  private static final float DASH_DP = 3;

  private final float density;
  private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint outline = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF segment = new RectF();
  private final int[] colors;
  private int[] outcomes = new int[0];

  public DrillOutcomeStrip(Context context) {
    this(context, null);
  }

  public DrillOutcomeStrip(Context context, AttributeSet attributes) {
    super(context, attributes);
    density = getResources().getDisplayMetrics().density;
    colors = new int[] {color(R.color.green_soft), color(R.color.warning),
        color(R.color.white_wash_50), color(R.color.white_wash_20)};
    outline.setStyle(Paint.Style.STROKE);
    outline.setStrokeWidth(density);
    outline.setColor(colors[MISSED]);
    outline.setPathEffect(new DashPathEffect(new float[] {DASH_DP * density, DASH_DP * density}, 0));
  }

  /** @param outcomes one of {@link #OPTIMAL}, {@link #LONGER}, {@link #MISSED}, {@link #UNSCORED} per rep */
  public void setOutcomes(int[] outcomes) {
    this.outcomes = outcomes.clone();
    invalidate();
  }

  /** The colour a finish is drawn in, for the key under the strip. */
  public int colorOf(int outcome) {
    return colors[outcome];
  }

  @Override
  protected void onDraw(Canvas canvas) {
    int count = outcomes.length;
    if (count == 0) {
      return;
    }
    float gap = GAP_DP * density;
    float width = (getWidth() - gap * (count - 1)) / count;
    float corner = CORNER_DP * density;
    float half = outline.getStrokeWidth() / 2;
    for (int i = 0; i < count; i++) {
      float left = i * (width + gap);
      if (outcomes[i] == MISSED) {
        segment.set(left + half, half, left + width - half, getHeight() - half);
        canvas.drawRoundRect(segment, corner, corner, outline);
      } else {
        segment.set(left, 0, left + width, getHeight());
        fill.setColor(colors[outcomes[i]]);
        canvas.drawRoundRect(segment, corner, corner, fill);
      }
    }
  }

  private int color(int res) {
    return ContextCompat.getColor(getContext(), res);
  }
}
