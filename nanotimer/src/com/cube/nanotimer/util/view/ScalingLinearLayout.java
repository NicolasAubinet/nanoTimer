package com.cube.nanotimer.util.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ScalingLinearLayout extends LinearLayout {

  private static final int LAYOUT_WIDTH = 480;
  private static final int LAYOUT_HEIGHT = 762;

  private int previousWidth;
  private int previousHeight;

	public ScalingLinearLayout(Context context) {
		super(context);
	}

	public ScalingLinearLayout(Context context, AttributeSet attributes) {
		super(context, attributes);
	}

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    int width = windowManager.getDefaultDisplay().getWidth();
    int height = windowManager.getDefaultDisplay().getHeight();

    if (width != previousWidth || height != previousHeight) {
      float xScale;
      float yScale;
      int orientation = getResources().getConfiguration().orientation;
      if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        xScale = (float) width / LAYOUT_WIDTH;
        yScale = (float) height / LAYOUT_HEIGHT;
      } else {
        xScale = (float) width / LAYOUT_HEIGHT;
        yScale = (float) height / LAYOUT_WIDTH;
      }
      float scale = Math.min(xScale, yScale);
      scaleViewAndChildren(this, scale, 0);

      previousWidth = width;
      previousHeight = height;
    }
  }

  // Scale the given view, its contents, and all of its children by the given factor.
  private void scaleViewAndChildren(View root, float scale, int canary) {
    // Retrieve the view's layout information
    ViewGroup.LayoutParams layoutParams = root.getLayoutParams();

    // Scale the View itself
    if (layoutParams.width != ViewGroup.LayoutParams.MATCH_PARENT && layoutParams.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
      layoutParams.width *= scale;
    }
    if (layoutParams.height != ViewGroup.LayoutParams.MATCH_PARENT && layoutParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
      layoutParams.height *= scale;
    }

    // If the View has margins, scale those too
    if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
      ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams)layoutParams;
      marginParams.leftMargin *= scale;
      marginParams.topMargin *= scale;
      marginParams.rightMargin *= scale;
      marginParams.bottomMargin *= scale;
    }
    root.setLayoutParams(layoutParams);

    // Same treatment for padding
    root.setPadding(
        (int)(root.getPaddingLeft() * scale),
        (int)(root.getPaddingTop() * scale),
        (int)(root.getPaddingRight() * scale),
        (int)(root.getPaddingBottom() * scale)
    );

    // If it's a TextView, scale the font size
    if (root instanceof TextView) {
      TextView tv = (TextView)root;
      tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.getTextSize() * scale);
    }

    // If it's a ViewGroup, recurse!
    if (root instanceof ViewGroup) {
      ViewGroup vg = (ViewGroup)root;
      for (int i = 0; i < vg.getChildCount(); i++) {
        scaleViewAndChildren(vg.getChildAt(i), scale, canary + 1);
      }
    }
  }

}
