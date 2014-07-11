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

  int baseWidth;
	int baseHeight;
	boolean alreadyScaled;
	float scale;
	int expectedWidth;
	int expectedHeight;

  int previousWidth;
  int previousHeight;

  Integer myWidth;

	public ScalingLinearLayout(Context context) {
		super(context);

//		Log.d("notcloud.view", "ScalingLinearLayout: width=" + this.getWidth() + ", height=" + this.getHeight());
		this.alreadyScaled = false;
	}

	public ScalingLinearLayout(Context context, AttributeSet attributes) {
		super(context, attributes);

//		Log.d("notcloud.view", "ScalingLinearLayout: width=" + this.getWidth() + ", height=" + this.getHeight());
		this.alreadyScaled = false;
	}

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//    int width = getMeasuredWidth();
//    int height = getMeasuredHeight();
//    Point size = new Point();
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

  /*public void onFinishInflate() {
		Log.d("notcloud.view", "ScalingLinearLayout::onFinishInflate: 1 width=" + this.getWidth() + ", height=" + this.getHeight());

		// Do an initial measurement of this layout with no major restrictions on size.
		// This will allow us to figure out what the original desired width and height are.
		this.measure(1000, 1000); // Adjust this up if necessary.
		this.baseWidth = this.getMeasuredWidth();
		this.baseHeight = this.getMeasuredHeight();
		Log.d("notcloud.view", "ScalingLinearLayout::onFinishInflate: 2 width=" + this.getWidth() + ", height=" + this.getHeight());

		Log.d("notcloud.view", "ScalingLinearLayout::onFinishInflate: alreadyScaled=" + this.alreadyScaled);
		Log.d("notcloud.view", "ScalingLinearLayout::onFinishInflate: scale=" + this.scale);
//    setLayoutParams(new ViewGroup.LayoutParams(
//        ViewGroup.LayoutParams.MATCH_PARENT,
//        ViewGroup.LayoutParams.MATCH_PARENT));
//		if(this.alreadyScaled) {
//			Scale.scaleViewAndChildren((LinearLayout)this, this.scale, 0);
//		}
    setWillNotDraw(false);
	}

  public void onDraw(Canvas canvas) {
		// Get the current width and height.
		int width = this.getWidth();
		int height = this.getHeight();

		// Figure out if we need to scale the layout.
		// We may need to scale if:
		//    1. We haven't scaled it before.
		//    2. The width has changed.
		//    3. The height has changed.
		if(!this.alreadyScaled || width != this.expectedWidth || height != this.expectedHeight) {
			// Figure out the x-scaling.
//			float xScale = (float)width / this.baseWidth;
//			if(this.alreadyScaled && width != this.expectedWidth) {
//				xScale = (float)width / this.expectedWidth;
//			}
			// Figure out the y-scaling.
//			float yScale = (float)height / this.baseHeight;
//			if(this.alreadyScaled && height != this.expectedHeight) {
//				yScale = (float)height / this.expectedHeight;
//			}

			// Scale the layout.
//			this.scale = Math.min(xScale, yScale);
//			Log.d("notcloud.view", "ScalingLinearLayout::onLayout: Scaling!");
      if (myWidth == null) {
        myWidth = width;
      }
      this.scale = ((float) myWidth / 800); // TODO : test, remove line above if ok
                                              // TODO : also, if keep this kind of thing, also check for the height
			Scale.scaleViewAndChildren(this, this.scale, 0);

			// Mark that we've already scaled this layout, and what
			// the width and height were when we did so.
			this.alreadyScaled = true;
			this.expectedWidth = width;
			this.expectedHeight = height;

			// Finally, return.
			return;
		}

		super.onDraw(canvas);
	}*/

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
