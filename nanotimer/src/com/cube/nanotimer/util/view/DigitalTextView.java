package com.cube.nanotimer.util.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class DigitalTextView extends TextView {

  public DigitalTextView(Context context) {
    super(context);
    setFont();
  }

  public DigitalTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setFont();
  }

  public DigitalTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setFont();
  }

  public void setFont() {
    if (!isInEditMode()) {
      Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/Digital_dream_Fat_Skew_Narrow.ttf");
      setTypeface(font);
    }
  }

}
