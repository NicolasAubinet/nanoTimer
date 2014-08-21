package com.cube.nanotimer.util.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TableLayout;
import android.widget.TableRow;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.ScaleUtils;

public class BorderTableLayout extends TableLayout {

  private Paint paint = new Paint();
  private Rect rect = new Rect();

  private Integer borderColor;
  private int borderSize;
  private boolean scaleBorders;

  public BorderTableLayout(Context context) {
    super(context);
    init();
  }

  public BorderTableLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BorderTableLayout);
    if (a.hasValue(R.styleable.BorderTableLayout_borderColor)) {
      // Cell has a border
      borderColor = a.getColor(R.styleable.BorderTableLayout_borderColor, R.color.neon);
      borderSize = a.getInt(R.styleable.BorderTableLayout_borderSize, 2);
      scaleBorders = a.getBoolean(R.styleable.BorderTableLayout_scaleBorders, true);
    }
    init();
  }

  private void init() {
    setWillNotDraw(false);
  }

  private void drawBorders(Canvas canvas) {
    getGlobalVisibleRect(rect);
    int tableTop = rect.top;
    int tableLeft = rect.left;
    int borderSizeVert = scaleBorders ? (int) (borderSize * ScaleUtils.getXScale(getContext())) : borderSize;
    int borderSizeHoriz = scaleBorders ? (int) (borderSize * ScaleUtils.getYScale(getContext())) : borderSize;
    paint.setColor(borderColor);
    canvas.drawRect(0, 0, getMeasuredWidth(), borderSizeHoriz, paint); // top line
    canvas.drawRect(0, 0, borderSizeVert, getMeasuredHeight(), paint); // left line
    canvas.drawRect(getMeasuredWidth() - borderSizeVert, 0, getMeasuredWidth(), getMeasuredHeight(), paint); // right line
    canvas.drawRect(0, getMeasuredHeight() - borderSizeHoriz, getMeasuredWidth(), getMeasuredHeight(), paint); // bottom line
    // draw row inner lines
    for (int row = 0; row < getChildCount() - 1; row++) {
      TableRow tr = (TableRow) getChildAt(row);
      tr.getGlobalVisibleRect(rect);
      int bottom = rect.bottom - tableTop;
      canvas.drawRect(0, bottom, getMeasuredWidth(), bottom + borderSizeHoriz, paint);
    }
    // draw column inner lines
    if (getChildCount() > 0) {
      TableRow tr = (TableRow) getChildAt(0);
      for (int col = 0; col < tr.getChildCount() - 1; col++) {
        tr.getChildAt(col).getGlobalVisibleRect(rect);
        int right = rect.right - tableLeft;
        canvas.drawRect(right, 0, right + borderSizeVert, getMeasuredHeight(), paint);
      }
    }
    super.onDraw(canvas);
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);
    if (borderColor != null) {
      drawBorders(canvas);
    }
  }

}
