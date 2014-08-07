package com.cube.nanotimer.util.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TableLayout;
import android.widget.TableRow;
import com.cube.nanotimer.R;

public class BorderTableLayout extends TableLayout {

  private Paint paint = new Paint();
  private Rect rect = new Rect();
  private LayoutParams params = new LayoutParams();

  private Integer borderColor;
  private int borderSize;

  public BorderTableLayout(Context context) {
    super(context);
    setWillNotDraw(false);
  }

  public BorderTableLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    borderColor = getResources().getColor(R.color.neon);
    borderSize = 2;

    int margin = Math.min(borderSize / 2, 1);
    params.setMargins(margin, margin, margin, margin);

    setWillNotDraw(false);
  }

  @Override
  public void onDraw(Canvas canvas) {
    getGlobalVisibleRect(rect);
    int tableTop = rect.top;
    int tableLeft = rect.left;
    if (borderColor == null) {
      return;
    }
    paint.setColor(borderColor);
    canvas.drawRect(0, 0, getMeasuredWidth(), borderSize, paint); // top line
    canvas.drawRect(0, 0, borderSize, getMeasuredHeight(), paint); // left line
    canvas.drawRect(getMeasuredWidth() - borderSize, 0, getMeasuredWidth(), getMeasuredHeight(), paint); // right line
    canvas.drawRect(0, getMeasuredHeight() - borderSize, getMeasuredWidth(), getMeasuredHeight(), paint); // bottom line
    // draw row lines
    for (int row = 0; row < getChildCount() - 1; row++) {
      TableRow tr = (TableRow) getChildAt(row);
      /*if (tr.getVisibility() == View.GONE) {
        continue;
      }
      for (int col = 0; col < tr.getChildCount(); col++) {
        View v = tr.getChildAt(col);
        if (v.getVisibility() == View.GONE || "noborder".equals(v.getTag())) {
          continue;
        }
        // TODO : take care of v visibility (could be GONE)
        v.getGlobalVisibleRect(rect);

//        int left = rect.left - tableLeft;
//        int top = rect.top - tableTop;
//        int width = v.getMeasuredWidth();
//        int height = v.getMeasuredHeight();
//        drawBorders(canvas, left, top, width, height);
      }*/
      tr.getGlobalVisibleRect(rect);
      int bottom = rect.bottom - tableTop;
      canvas.drawRect(0, bottom, getMeasuredWidth(), bottom + borderSize, paint);
    }
    // draw column lines
    if (getChildCount() > 0) {
      TableRow tr = (TableRow) getChildAt(0);
      for (int col = 0; col < tr.getChildCount() - 1; col++) {
        tr.getChildAt(col).getGlobalVisibleRect(rect);
        int right = rect.right - tableLeft;
        canvas.drawRect(right, 0, right + borderSize, getMeasuredHeight(), paint);
      }
    }
    super.onDraw(canvas);
  }

  private void drawBorders(Canvas canvas, int x, int y, int canvasWidth, int canvasHeight) {
    // top
    canvas.drawRect(x - borderSize, y, x + canvasWidth + borderSize, y - borderSize, paint);
    // left
    canvas.drawRect(x, y - borderSize, x - borderSize, y + canvasHeight + borderSize, paint);
    // right
    canvas.drawRect(x + canvasWidth, y - borderSize, x + canvasWidth + borderSize, y + canvasHeight + borderSize, paint);
    // bottom
    canvas.drawRect(x - borderSize, y + canvasHeight, x + canvasWidth + borderSize, y + canvasHeight + borderSize, paint);
  }

}
