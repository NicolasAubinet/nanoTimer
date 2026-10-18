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
 *
 * <p>The running drill draws each group as a segmented bar stretched to share the row (see
 * {@link #setBars}), wrapping onto more rows when there are too many cases to fit. The group of the
 * rep just finished is ringed: that case is already named beside its time, so the ring gives nothing
 * away about the one on the cube.
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

  private static final float BAR_CELL_DP = 16;
  private static final float BAR_HEIGHT_DP = 6;
  private static final float BAR_INSET_DP = 4;
  private static final float BAR_SEGMENT_GAP_DP = 2;
  private static final float BAR_CELL_GAP_DP = 8;
  private static final float BAR_ROW_GAP_DP = 6;
  private static final float BAR_MIN_CELL_DP = 34;
  private static final int BAR_EMPTY_ALPHA = 46;

  private final float density;
  private final Paint filled = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint wash = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint tick = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint empty = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint mark = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF capsule = new RectF();
  private final RectF segment = new RectF();
  private final Path check = new Path();
  private final int markNeutral;
  private final int markWon;

  /** Per case, its wins so far, or -1 once it has cleared. */
  private final List<Integer> groups = new ArrayList<Integer>();
  private int dotsPerGroup = 3;
  private boolean bars;
  private int marked = -1;
  private boolean markedWon;

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
    empty.setColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(context, R.color.white),
        BAR_EMPTY_ALPHA));
    mark.setStyle(Paint.Style.STROKE);
    mark.setStrokeWidth(RING_DP * density);
    markNeutral = ContextCompat.getColor(context, R.color.color_accent);
    markWon = good;
  }

  /** Draws each case as a segmented bar sharing the row, for the running drill's card. */
  public void setBars(boolean bars) {
    this.bars = bars;
    requestLayout();
    invalidate();
  }

  /** Redraws from the tally, one group per case in the order given. */
  public void show(List<String> cases, CaseTally tally, int winsToClear) {
    show(cases, tally, winsToClear, null, false);
  }

  /**
   * The same, ringing the group of the rep just finished.
   *
   * @param lastCase the finished rep's case, or null to ring nothing
   * @param won whether that rep beat its target, which rings it green rather than in the accent
   */
  public void show(List<String> cases, CaseTally tally, int winsToClear, String lastCase,
      boolean won) {
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
    marked = lastCase == null ? -1 : cases.indexOf(lastCase);
    markedWon = won;
    setContentDescription(getContext().getString(R.string.drill_cases_cleared, cleared,
        cases.size()));
    requestLayout();
    invalidate();
  }

  private float groupWidth() {
    return (dotsPerGroup * DOT_DP + (dotsPerGroup - 1) * DOT_GAP_DP) * density;
  }

  /** How many bars share a row at this width. */
  private int columns(int width) {
    float gap = BAR_CELL_GAP_DP * density;
    int fit = (int) ((width + gap) / (BAR_MIN_CELL_DP * density + gap));
    return Math.max(1, Math.min(groups.size(), fit));
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int count = groups.size();
    if (bars) {
      int width = MeasureSpec.getSize(widthMeasureSpec);
      int inner = width - getPaddingLeft() - getPaddingRight();
      int rows = count == 0 ? 0 : (count + columns(inner) - 1) / columns(inner);
      float height = rows == 0 ? 0
          : rows * BAR_CELL_DP * density + (rows - 1) * BAR_ROW_GAP_DP * density;
      setMeasuredDimension(width, resolveSize(
          Math.round(height) + getPaddingTop() + getPaddingBottom(), heightMeasureSpec));
      return;
    }
    float width = count == 0 ? 0 : count * groupWidth() + (count - 1) * GROUP_GAP_DP * density;
    setMeasuredDimension(
        resolveSize(Math.round(width) + getPaddingLeft() + getPaddingRight(), widthMeasureSpec),
        resolveSize(Math.round(CHECK_HEIGHT_DP * density) + getPaddingTop() + getPaddingBottom(),
            heightMeasureSpec));
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (bars) {
      drawBars(canvas);
      return;
    }
    float centreY = getPaddingTop() + (getHeight() - getPaddingTop() - getPaddingBottom()) / 2f;
    float dot = DOT_DP * density;
    float x = getPaddingLeft();
    for (int wins : groups) {
      if (wins < 0) {
        drawCheck(canvas, x, groupWidth(), centreY);
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

  private void drawBars(Canvas canvas) {
    int inner = getWidth() - getPaddingLeft() - getPaddingRight();
    int cols = columns(inner);
    float gap = BAR_CELL_GAP_DP * density;
    float cell = (inner - (cols - 1) * gap) / cols;
    float cellHeight = BAR_CELL_DP * density;
    float inset = BAR_INSET_DP * density;
    float segGap = BAR_SEGMENT_GAP_DP * density;
    float barHeight = BAR_HEIGHT_DP * density;
    float radius = 2 * density;
    for (int g = 0; g < groups.size(); g++) {
      float left = getPaddingLeft() + (g % cols) * (cell + gap);
      float top = getPaddingTop() + (g / cols) * (cellHeight + BAR_ROW_GAP_DP * density);
      float centreY = top + cellHeight / 2;
      int wins = groups.get(g);
      if (wins < 0) {
        drawCheck(canvas, left, cell, centreY);
      } else {
        float segmentWidth = (cell - 2 * inset - (dotsPerGroup - 1) * segGap) / dotsPerGroup;
        for (int i = 0; i < dotsPerGroup; i++) {
          float x = left + inset + i * (segmentWidth + segGap);
          segment.set(x, centreY - barHeight / 2, x + segmentWidth, centreY + barHeight / 2);
          canvas.drawRoundRect(segment, radius, radius, i < wins ? filled : empty);
        }
      }
      if (g == marked) {
        mark.setColor(markedWon ? markWon : markNeutral);
        float half = mark.getStrokeWidth() / 2;
        capsule.set(left + half, top + half, left + cell - half, top + cellHeight - half);
        canvas.drawRoundRect(capsule, cellHeight / 2, cellHeight / 2, mark);
      }
    }
  }

  /** A cleared case: a capsule as wide as its dots were, so the row does not move when it clears. */
  private void drawCheck(Canvas canvas, float left, float width, float centreY) {
    float half = CHECK_HEIGHT_DP * density / 2;
    capsule.set(left, centreY - half, left + width, centreY + half);
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
