package com.cube.nanotimer.gui.widget.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import com.cube.nanotimer.R;

public class NumberEntryDialog extends DialogPreference {

  private EditText tfValue;

  private int min = 0;
  private int max = 99999;

  public NumberEntryDialog(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberLimit);
    if (a.hasValue(R.styleable.NumberLimit_min)) {
      min = a.getInt(R.styleable.NumberLimit_min, min);
    }
    if (a.hasValue(R.styleable.NumberLimit_max)) {
      max = a.getInt(R.styleable.NumberLimit_max, max);
    }
  }

  @Override
  protected View onCreateDialogView() {
    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View layout = inflater.inflate(R.layout.number_picker, null);
    tfValue = (EditText) layout.findViewById(R.id.tfValue);

    SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getContext());
    Integer value = p.getInt(getKey(), 0); // TODO : retrieve the default value for the second parameter
    tfValue.setText(value.toString());

    final int maxTextSizeLength = String.valueOf(max).length();
    tfValue.setFilters( new InputFilter[] { new InputFilter.LengthFilter(maxTextSizeLength) } );

    Button buPlus = (Button) layout.findViewById(R.id.buPlus);
    buPlus.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        int val = getValue();
        if (val < max) {
          tfValue.setText(String.valueOf(val + 1));
        }
      }
    });

    Button buMinus = (Button) layout.findViewById(R.id.buMinus);
    buMinus.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        int val = getValue();
        if (val >= (min + 1)) {
          tfValue.setText(String.valueOf(val - 1));
        }
      }
    });

    return layout;
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    super.onDialogClosed(positiveResult);

    if (positiveResult) {
      int time = adjustNumberToLimits(getValue());
      if (callChangeListener(time)) {
        persistInt(time);
      }
    }
  }

  private int adjustNumberToLimits(int n) {
    n = Math.max(n, min);
    n = Math.min(n, max);
    return n;
  }

  private int getValue() {
    return Integer.parseInt(tfValue.getText().toString());
  }

}
