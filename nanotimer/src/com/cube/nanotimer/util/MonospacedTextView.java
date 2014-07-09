package com.cube.nanotimer.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class MonospacedTextView extends TextView {

  public MonospacedTextView(Context context) {
    super(context);
    setFont();
  }

  public MonospacedTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setFont();
  }

  public MonospacedTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setFont();
  }

  public void setFont() {
    Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/DroidSansMono.ttf");
    setTypeface(font);
  }

}
