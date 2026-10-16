package com.cube.nanotimer.gui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

/**
 * A Plan tab card lit in its own colour, over the card's lit surface: a glow from the top-left
 * corner and a 1dp rim that fades from the colour to almost nothing along the edge. The colour is
 * the step the card is about, so it is read before a word is.
 *
 * <p>The surface drawables leave their bottom 3dp to the shadow, so the light is drawn over the
 * face alone.
 */
final class PlanCardLight extends Drawable {

  private static final float RADIUS_DP = 16;
  private static final float SHADOW_DP = 3;
  private static final int GLOW_ALPHA = 0x29;
  private static final int RIM_ALPHA = 0x8C;
  private static final int RIM_MID = Color.argb(0x0D, 0xFF, 0xFF, 0xFF);
  private static final int RIM_END = Color.argb(0x05, 0xFF, 0xFF, 0xFF);
  /** The glow's reach from the corner, as shares of the card's width and height. */
  private static final float GLOW_WIDTH = 0.72f;
  private static final float GLOW_HEIGHT = 0.44f;
  /** The rim's direction, 150° clockwise from up: down and to the right. */
  private static final float RIM_DX = 0.5f;
  private static final float RIM_DY = 0.866f;

  private final int colour;
  private final float radius;
  private final float rimWidth;
  private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF face = new RectF();
  private final RectF edge = new RectF();

  private PlanCardLight(Context context, int colour) {
    float density = context.getResources().getDisplayMetrics().density;
    this.colour = colour;
    radius = RADIUS_DP * density;
    rimWidth = density;
    rim.setStyle(Paint.Style.STROKE);
    rim.setStrokeWidth(rimWidth);
  }

  /** Sets a card's background to the given surface lit in the given colour. */
  static void apply(View card, int surface, int colour) {
    Context context = card.getContext();
    LayerDrawable lit = new LayerDrawable(new Drawable[] {
        ContextCompat.getDrawable(context, surface), new PlanCardLight(context, colour)});
    float density = context.getResources().getDisplayMetrics().density;
    lit.setLayerInset(1, 0, 0, 0, Math.round(SHADOW_DP * density));
    int left = card.getPaddingLeft();
    int top = card.getPaddingTop();
    int right = card.getPaddingRight();
    int bottom = card.getPaddingBottom();
    card.setBackground(lit);
    card.setPadding(left, top, right, bottom);
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    face.set(bounds);
    float half = rimWidth / 2;
    edge.set(face.left + half, face.top + half, face.right - half, face.bottom - half);
    if (bounds.isEmpty()) {
      return;
    }
    RadialGradient corner = new RadialGradient(0, 0, 1,
        ColorUtils.setAlphaComponent(colour, GLOW_ALPHA), Color.TRANSPARENT, Shader.TileMode.CLAMP);
    Matrix reach = new Matrix();
    reach.setScale(bounds.width() * GLOW_WIDTH, bounds.height() * GLOW_HEIGHT);
    reach.postTranslate(bounds.left, bounds.top);
    corner.setLocalMatrix(reach);
    glow.setShader(corner);
    float length = bounds.width() * RIM_DX + bounds.height() * RIM_DY;
    rim.setShader(new LinearGradient(bounds.left, bounds.top, bounds.left + length * RIM_DX,
        bounds.top + length * RIM_DY,
        new int[] {ColorUtils.setAlphaComponent(colour, RIM_ALPHA), RIM_MID, RIM_END},
        new float[] {0, 0.4f, 1}, Shader.TileMode.CLAMP));
  }

  @Override
  public void draw(Canvas canvas) {
    if (face.isEmpty()) {
      return;
    }
    canvas.drawRoundRect(face, radius, radius, glow);
    canvas.drawRoundRect(edge, radius - rimWidth / 2, radius - rimWidth / 2, rim);
  }

  @Override
  public void setAlpha(int alpha) {
    glow.setAlpha(alpha);
    rim.setAlpha(alpha);
    invalidateSelf();
  }

  @Override
  public void setColorFilter(ColorFilter filter) {
    glow.setColorFilter(filter);
    rim.setColorFilter(filter);
    invalidateSelf();
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }
}
