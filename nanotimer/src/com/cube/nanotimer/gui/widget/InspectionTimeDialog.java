package com.cube.nanotimer.gui.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import com.cube.nanotimer.R;

public class InspectionTimeDialog extends DialogPreference {

  private EditText tfValue;

  public InspectionTimeDialog(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected View onCreateDialogView() {
    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View layout = inflater.inflate(R.layout.number_picker, null);
    tfValue = (EditText) layout.findViewById(R.id.tfValue);

    SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getContext());
    Integer defaultValue = getContext().getResources().getInteger(R.integer.inspection_time);
    Integer value = p.getInt(getKey(), defaultValue);
    tfValue.setText(value.toString());

    Button buPlus = (Button) layout.findViewById(R.id.buPlus);
    buPlus.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        int val = getValue();
        tfValue.setText(String.valueOf(val + 1));
      }
    });

    Button buMinus = (Button) layout.findViewById(R.id.buMinus);
    buMinus.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        int val = getValue();
        if (val >= 1) {
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
      int time = getValue();
      if (callChangeListener(time)) {
        persistInt(time);
      }
    }
  }

  private int getValue() {
    return Integer.parseInt(tfValue.getText().toString());
  }

}
