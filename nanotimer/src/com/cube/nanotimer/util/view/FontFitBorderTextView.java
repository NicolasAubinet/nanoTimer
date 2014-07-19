package com.cube.nanotimer.util.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import com.cube.nanotimer.R;

public class FontFitBorderTextView extends FontFitTextView {

  private Paint paint = new Paint();
  private Integer borderColor;
  int borderTop;
  int borderBottom;
  int borderLeft;
  int borderRight;

  public FontFitBorderTextView(Context context) {
    super(context);
  }

  public FontFitBorderTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BorderTextView);
    if (a.hasValue(R.styleable.BorderTextView_borderColor)) {
      // Cell has a border
      borderColor = a.getColor(R.styleable.BorderTextView_borderColor, R.color.black);

      int defaultThickness = a.getInt(R.styleable.BorderTextView_border, 1);
      borderTop = a.getInt(R.styleable.BorderTextView_borderTop, defaultThickness);
      borderBottom = a.getInt(R.styleable.BorderTextView_borderBottom, defaultThickness);
      borderLeft = a.getInt(R.styleable.BorderTextView_borderLeft, defaultThickness);
      borderRight = a.getInt(R.styleable.BorderTextView_borderRight, defaultThickness);
    }
  }

  @Override
  public void onDraw(Canvas canvas) {
    BorderTextView.drawBorders(canvas, paint, borderColor, borderTop, borderBottom, borderLeft, borderRight,
        getMeasuredWidth(), getMeasuredHeight());
    super.onDraw(canvas);
  }

}
