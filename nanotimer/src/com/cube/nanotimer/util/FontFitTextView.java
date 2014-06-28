package com.cube.nanotimer.util;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class FontFitTextView extends TextView {

  private Paint mTestPaint;
  private float initialTextSize;

  public FontFitTextView(Context context) {
    super(context);
    initialTextSize = getTextSize();
    init();
  }

  public FontFitTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialTextSize = getTextSize();
    init();
  }

  private void init() {
    mTestPaint = new Paint();
    mTestPaint.set(getPaint());
    // max size defaults to the initially specified text size unless it is too small
  }

  /*
   * Re size the font so the specified text fits in the text box
   * assuming the text box is the specified width.
   */
  private void refitText(String text, int textWidth) {
    if (textWidth <= 0) {
      return;
    }
    int targetWidth = textWidth - getPaddingLeft() - getPaddingRight();
    float hi = initialTextSize;
    float lo = 2;
    final float threshold = 0.5f; // How close we have to be

    String longestLine = "";
    for (String line : text.split("\n")) {
      if (line.length() > longestLine.length()) {
        longestLine = line;
      }
    }
    mTestPaint.set(getPaint());

    while ((hi - lo) > threshold) {
      float size = (hi+lo) / 2;
      mTestPaint.setTextSize(size);
      if (mTestPaint.measureText(longestLine) >= targetWidth) {
        hi = size; // too big
      } else {
        lo = size; // too small
      }
    }
    // Use lo so that we undershoot rather than overshoot
    setTextSize(TypedValue.COMPLEX_UNIT_PX, lo);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
    int height = getMeasuredHeight();
    refitText(getText().toString(), parentWidth);
    this.setMeasuredDimension(parentWidth, height);
  }

  @Override
  protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
    refitText(text.toString(), getWidth());
  }

  @Override
  protected void onSizeChanged (int w, int h, int oldw, int oldh) {
    if (w != oldw) {
      refitText(getText().toString(), w);
    }
  }

  @Override
  public void setTextSize(float size) {
    initialTextSize = size;
    super.setTextSize(size);
  }

}
