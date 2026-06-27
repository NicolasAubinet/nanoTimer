package com.cube.nanotimer.util.view;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

public class DigitalTextView extends AppCompatTextView {

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
      setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
      setFontFeatureSettings("tnum");
    }
  }

}
