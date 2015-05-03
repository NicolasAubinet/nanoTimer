package com.cube.nanotimer.util.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import com.cube.nanotimer.util.helper.GUIUtils;

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
      Typeface font = GUIUtils.createFont(getContext(), "Digital_dream_Fat_Skew_Narrow.ttf");
      setTypeface(font);
    }
  }

}
