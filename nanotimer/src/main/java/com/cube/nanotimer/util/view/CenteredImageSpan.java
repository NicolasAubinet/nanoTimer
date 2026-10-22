package com.cube.nanotimer.util.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

/** An image centred on the text's line, which ImageSpan only offers from API 29. */
public class CenteredImageSpan extends ImageSpan {

  public CenteredImageSpan(Drawable drawable) {
    super(drawable);
  }

  /**
   * Room for the image evenly above and below the text's centre. ImageSpan's own room sits on the
   * baseline, so an image taller than the text grew the line upwards only and pushed the label low
   * in a centred button.
   */
  @Override
  public int getSize(Paint paint, CharSequence text, int start, int end,
      Paint.FontMetricsInt fm) {
    Rect bounds = getDrawable().getBounds();
    if (fm != null) {
      Paint.FontMetricsInt metrics = paint.getFontMetricsInt();
      int centre = (metrics.descent + metrics.ascent) / 2;
      int half = bounds.height() / 2;
      fm.ascent = Math.min(metrics.ascent, centre - half);
      fm.descent = Math.max(metrics.descent, centre + half);
      fm.top = Math.min(metrics.top, fm.ascent);
      fm.bottom = Math.max(metrics.bottom, fm.descent);
    }
    return bounds.right;
  }

  @Override
  public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y,
      int bottom, Paint paint) {
    Drawable drawable = getDrawable();
    Paint.FontMetricsInt metrics = paint.getFontMetricsInt();
    int centre = y + (metrics.descent + metrics.ascent) / 2;
    canvas.save();
    canvas.translate(x, centre - drawable.getBounds().height() / 2f);
    drawable.draw(canvas);
    canvas.restore();
  }
}
