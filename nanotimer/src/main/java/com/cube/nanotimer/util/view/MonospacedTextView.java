package com.cube.nanotimer.util.view;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

public class MonospacedTextView extends AppCompatTextView {

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
    if (!isInEditMode()) {
      Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/DroidSansMono.ttf");
      setTypeface(font);
    }
  }

}
