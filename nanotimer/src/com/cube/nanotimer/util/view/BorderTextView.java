package com.cube.nanotimer.util.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;
import com.cube.nanotimer.R;

public class BorderTextView extends TextView {

  private Paint paint = new Paint();
  private Integer borderColor;
  int borderTop;
  int borderBottom;
  int borderLeft;
  int borderRight;

  public BorderTextView(Context context) {
    super(context);
  }

  public BorderTextView(Context context, AttributeSet attrs) {
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
    drawBorders(canvas, paint, borderColor, borderTop, borderBottom, borderLeft, borderRight,
        getMeasuredWidth(), getMeasuredHeight());
    super.onDraw(canvas);
  }

  public static void drawBorders(Canvas canvas, Paint paint, Integer borderColor,
                                 int borderTop, int borderBottom, int borderLeft, int borderRight,
                                 int canvasWidth, int canvasHeight) {
    if (borderColor != null) {
      paint.setColor(borderColor);
      // top
      canvas.drawRect(0, 0, canvasWidth, borderTop, paint);
      // left
      canvas.drawRect(0, 0, borderLeft, canvasHeight, paint);
      // right
      canvas.drawRect(canvasWidth - borderRight, 0, canvasWidth, canvasHeight, paint);
      // bottom
      canvas.drawRect(0, canvasHeight - borderBottom, canvasWidth, canvasHeight, paint);
    }
  }

}