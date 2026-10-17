package com.cube.nanotimer.gui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * A frame with a faint band of light that passes over it every few seconds, for content standing
 * in for something not there yet. The band is clipped to the frame's rounded corners. It runs only
 * while the frame is on screen, and does not move at all when the system has animations turned off.
 */
public class ShimmerLayout extends FrameLayout {

  private static final long SWEEP_MS = 1600;
  private static final long PAUSE_MS = 1800;
  private static final int BAND_ALPHA = 0x14;
  private static final float CORNER_DP = 16;
  private static final float SHADOW_DP = 3; // the card surfaces' shadow strip, left unlit

  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Matrix matrix = new Matrix();
  private final RectF bounds = new RectF();
  private final float corner;
  private final float shadow;
  private ValueAnimator animator;
  private float progress = -1;

  public ShimmerLayout(Context context) {
    this(context, null);
  }

  public ShimmerLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    setWillNotDraw(false);
    float density = context.getResources().getDisplayMetrics().density;
    corner = CORNER_DP * density;
    shadow = SHADOW_DP * density;
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    int light = BAND_ALPHA << 24 | 0xFFFFFF;
    paint.setShader(new LinearGradient(0, 0, w * 0.4f, h * 0.25f,
        new int[] {0x00FFFFFF, light, 0x00FFFFFF}, null, Shader.TileMode.CLAMP));
    bounds.set(0, 0, w, h - shadow);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    sync();
  }

  @Override
  protected void onVisibilityChanged(View changedView, int visibility) {
    super.onVisibilityChanged(changedView, visibility);
    sync();
  }

  @Override
  protected void onWindowVisibilityChanged(int visibility) {
    super.onWindowVisibilityChanged(visibility);
    sync();
  }

  @Override
  protected void onDetachedFromWindow() {
    stop();
    super.onDetachedFromWindow();
  }

  /** Runs the band while the frame can be seen: a hidden tab or a stopped screen leaves it idle. */
  private void sync() {
    boolean seen = isAttachedToWindow() && isShown() && getWindowVisibility() == VISIBLE;
    if (seen && animator == null) {
      start();
    } else if (!seen) {
      stop();
    }
  }

  private void start() {
    // One repeating cycle, the pause included, so a zero animator scale ends it rather than spins.
    final float sweep = (float) SWEEP_MS / (SWEEP_MS + PAUSE_MS);
    final AccelerateDecelerateInterpolator ease = new AccelerateDecelerateInterpolator();
    animator = ValueAnimator.ofFloat(0, 1);
    animator.setDuration(SWEEP_MS + PAUSE_MS);
    animator.setRepeatCount(ValueAnimator.INFINITE);
    animator.setInterpolator(null);
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        float cycle = animation.getAnimatedFraction();
        progress = cycle < sweep ? ease.getInterpolation(cycle / sweep) : -1;
        invalidate();
      }
    });
    animator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        progress = -1;
        invalidate();
      }
    });
    animator.start();
  }

  private void stop() {
    ValueAnimator running = animator;
    animator = null;
    if (running != null) {
      running.cancel();
    }
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);
    if (progress < 0 || getWidth() == 0) {
      return;
    }
    matrix.setTranslate((progress * 1.8f - 0.6f) * getWidth(), 0);
    paint.getShader().setLocalMatrix(matrix);
    canvas.drawRoundRect(bounds, corner, corner, paint);
  }
}
