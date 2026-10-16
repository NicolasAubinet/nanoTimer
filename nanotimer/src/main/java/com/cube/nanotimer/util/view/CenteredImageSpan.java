package com.cube.nanotimer.util.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

/** An image centred on the text's line, which ImageSpan only offers from API 29. */
public class CenteredImageSpan extends ImageSpan {

  public CenteredImageSpan(Drawable drawable) {
    super(drawable);
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
