package com.cube.nanotimer.gui.widget.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import com.cube.nanotimer.R;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

public class WheelViewDialog extends DialogPreference {

  private WheelView wheelView;

  private int min = 0;
  private int max = 9999;
  private int defaultValue = 0;
  private boolean cyclic = false;

  public WheelViewDialog(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberLimit);
    if (a.hasValue(R.styleable.NumberLimit_min)) {
      min = a.getInt(R.styleable.NumberLimit_min, min);
    }
    if (a.hasValue(R.styleable.NumberLimit_max)) {
      max = a.getInt(R.styleable.NumberLimit_max, max);
    }
    if (a.hasValue(R.styleable.NumberLimit_defaultVal)) {
      defaultValue = a.getInt(R.styleable.NumberLimit_defaultVal, defaultValue);
    }
    if (a.hasValue(R.styleable.NumberLimit_cyclic)) {
      cyclic = a.getBoolean(R.styleable.NumberLimit_cyclic, cyclic);
    }
  }

  @Override
  protected View onCreateDialogView() {
    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View layout = inflater.inflate(R.layout.wheelview_dialog, null);
    wheelView = (WheelView) layout.findViewById(R.id.wheelView);
    wheelView.setViewAdapter(new NumericWheelAdapter(getContext(), min, max));
    wheelView.setCurrentItem(defaultValue);
    wheelView.setCyclic(cyclic);

    SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getContext());
    Integer value = p.getInt(getKey(), defaultValue);
    wheelView.setCurrentItem(value);

    return layout;
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    super.onDialogClosed(positiveResult);

    if (positiveResult) {
      int value = wheelView.getCurrentItem();
      if (callChangeListener(value)) {
        persistInt(value);
      }
    }
  }

}
