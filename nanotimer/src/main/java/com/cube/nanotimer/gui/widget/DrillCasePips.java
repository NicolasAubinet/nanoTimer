package com.cube.nanotimer.gui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.cube.nanotimer.R;
import com.cube.nanotimer.smartcube.drill.CaseTally;

import java.util.ArrayList;
import java.util.List;

/**
 * How far a drill that retires its cases has got: a group of dots per case, one filled for each time
 * the case beat its target, and a check once it has left the drill.
 *
 * <p><b>The groups are never named</b>, and stay in the order the drill was given. A name here would
 * sit beside the case being worked; a group only fills once its rep is over, so it says nothing about
 * the case on the cube until that case has been solved.
 */
public class DrillCasePips extends View {

  private static final float DOT_DP = 8;
  private static final float DOT_GAP_DP = 4;
  private static final float GROUP_GAP_DP = 12;
  private static final float RING_DP = 1.5f;
  private static final float CHECK_HEIGHT_DP = 14;
  private static final float CHECK_STROKE_DP = 1.6f;
  private static final int RING_ALPHA = 90;
  private static final float CHECK_WASH = 0.2f;

  private final float density;
  private final Paint filled = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint wash = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint tick = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF capsule = new RectF();
  private final Path check = new Path();

  /** Per case, its wins so far, or -1 once it has cleared. */
  private final List<Integer> groups = new ArrayList<Integer>();
  private int dotsPerGroup = 3;

  public DrillCasePips(Context context, AttributeSet attributes) {
    super(context, attributes);
    density = getResources().getDisplayMetrics().density;
    int good = ContextCompat.getColor(context, R.color.green_soft);
    filled.setColor(good);
    ring.setStyle(Paint.Style.STROKE);
    ring.setStrokeWidth(RING_DP * density);
    ring.setColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(context, R.color.white),
        RING_ALPHA));
    wash.setColor(ColorUtils.setAlphaComponent(good, Math.round(255 * CHECK_WASH)));
    tick.setStyle(Paint.Style.STROKE);
    tick.setStrokeWidth(CHECK_STROKE_DP * density);
    tick.setStrokeCap(Paint.Cap.ROUND);
    tick.setStrokeJoin(Paint.Join.ROUND);
    tick.setColor(good);
  }

  /** Redraws from the tally, one group per case in the order given. */
  public void show(List<String> cases, CaseTally tally, int winsToClear) {
    groups.clear();
    int cleared = 0;
    for (String code : cases) {
      if (tally.isCleared(code)) {
        groups.add(-1);
        cleared++;
      } else {
        groups.add(Math.min(tally.getWins(code), winsToClear));
      }
    }
    dotsPerGroup = Math.max(1, winsToClear);
    setContentDescription(getContext().getString(R.string.drill_cases_cleared, cleared,
        cases.size()));
    requestLayout();
    invalidate();
  }

  private float groupWidth() {
    return (dotsPerGroup * DOT_DP + (dotsPerGroup - 1) * DOT_GAP_DP) * density;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int count = groups.size();
    float width = count == 0 ? 0 : count * groupWidth() + (count - 1) * GROUP_GAP_DP * density;
    setMeasuredDimension(
        resolveSize(Math.round(width) + getPaddingLeft() + getPaddingRight(), widthMeasureSpec),
        resolveSize(Math.round(CHECK_HEIGHT_DP * density) + getPaddingTop() + getPaddingBottom(),
            heightMeasureSpec));
  }

  @Override
  protected void onDraw(Canvas canvas) {
    float centreY = getPaddingTop() + (getHeight() - getPaddingTop() - getPaddingBottom()) / 2f;
    float dot = DOT_DP * density;
    float x = getPaddingLeft();
    for (int wins : groups) {
      if (wins < 0) {
        drawCheck(canvas, x, centreY);
      } else {
        for (int i = 0; i < dotsPerGroup; i++) {
          float cx = x + i * (DOT_DP + DOT_GAP_DP) * density + dot / 2;
          if (i < wins) {
            canvas.drawCircle(cx, centreY, dot / 2, filled);
          } else {
            canvas.drawCircle(cx, centreY, (dot - ring.getStrokeWidth()) / 2, ring);
          }
        }
      }
      x += groupWidth() + GROUP_GAP_DP * density;
    }
  }

  /** A cleared case: a capsule as wide as its dots were, so the row does not move when it clears. */
  private void drawCheck(Canvas canvas, float left, float centreY) {
    float half = CHECK_HEIGHT_DP * density / 2;
    capsule.set(left, centreY - half, left + groupWidth(), centreY + half);
    canvas.drawRoundRect(capsule, half, half, wash);
    float cx = capsule.centerX();
    float unit = half * 0.5f;
    check.reset();
    check.moveTo(cx - unit, centreY);
    check.lineTo(cx - unit * 0.25f, centreY + unit * 0.7f);
    check.lineTo(cx + unit, centreY - unit * 0.7f);
    canvas.drawPath(check, tick);
  }
}
