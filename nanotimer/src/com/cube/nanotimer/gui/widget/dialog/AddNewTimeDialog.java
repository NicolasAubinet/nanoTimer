package com.cube.nanotimer.gui.widget.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.gui.widget.ResultListener;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.helper.DialogUtils;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

public class AddNewTimeDialog extends ConfirmDialog {

  private EditText tfMinutes;
  private EditText tfSeconds;
  private EditText tfHundreds;

  private ResultListener resultListener;
  private SolveType solveType;
  private String scramble;

  public static AddNewTimeDialog newInstance(ResultListener resultListener, SolveType solveType, String scramble) {
    AddNewTimeDialog frag = new AddNewTimeDialog(resultListener, solveType, scramble);
    return frag;
  }

  private AddNewTimeDialog(ResultListener resultListener, SolveType solveType, String scramble) {
    this.resultListener = resultListener;
    this.solveType = solveType;
    this.scramble = scramble;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    dialog = getDialog(R.string.add);
    return dialog;
  }

  @Override
  protected void onConfirm() {
    int minutes;
    int seconds;
    int hundreds;
    try {
      minutes = getIntValue(tfMinutes);
      seconds = getIntValue(tfSeconds);
      hundreds = getIntValue(tfHundreds);
    } catch (NumberFormatException e) {
      DialogUtils.showInfoMessage(getContext(), R.string.invalid_integer_value);
      return;
    }
    if (!checkTimeValues(minutes, seconds, hundreds)) {
      return;
    }
    long time = minutes * 60000;
    time += seconds * 1000;
    time += hundreds * 10;

    SolveTime solveTime = new SolveTime();
    solveTime.setTime(time);
    solveTime.setTimestamp(System.currentTimeMillis());
    solveTime.setSolveType(solveType);
    solveTime.setScramble(scramble);

    App.INSTANCE.getService().saveTime(solveTime, new DataCallback<SolveAverages>() {
      @Override
      public void onData(SolveAverages data) {
        if (resultListener != null) {
          resultListener.onResult(data);
        }
      }
    });

    dialog.dismiss();
  }

  @Override
  protected View getCustomView() {
    LayoutInflater factory = LayoutInflater.from(getActivity());
    View view = factory.inflate(R.layout.add_new_time_dialog, null);
    tfMinutes = (EditText) view.findViewById(R.id.tfMinutes);
    tfSeconds = (EditText) view.findViewById(R.id.tfSeconds);
    tfHundreds = (EditText) view.findViewById(R.id.tfHundreds);

    tfMinutes.setOnKeyListener(new OnNumericFieldKeyListener(null, tfMinutes, tfSeconds));
    tfSeconds.setOnKeyListener(new OnNumericFieldKeyListener(tfMinutes, tfSeconds, tfHundreds));
    tfHundreds.setOnKeyListener(new OnNumericFieldKeyListener(tfSeconds, tfHundreds, null));
    return view;
  }

  private int getIntValue(EditText editText) throws NumberFormatException {
    int value = 0;
    String stringValue = editText.getText().toString();
    if (!stringValue.trim().equals("")) {
      value = Integer.parseInt(stringValue);
    }
    return value;
  }

  private boolean checkTimeValues(int minutes, int seconds, int hundreds) {
    boolean valid = true;
    if (minutes < 0) {
      DialogUtils.showInfoMessage(getContext(), getString(R.string.invalid_value_for_field, getString(R.string.minutes)));
      valid = false;
    } else if (seconds < 0 || seconds >= 60) {
      DialogUtils.showInfoMessage(getContext(), getString(R.string.invalid_value_for_field, getString(R.string.seconds)));
      valid = false;
    } else if (hundreds < 0 || hundreds >= 100) {
      DialogUtils.showInfoMessage(getContext(), getString(R.string.invalid_value_for_field, getString(R.string.hundreds)));
      valid = false;
    }
    return valid;
  }

  class OnNumericFieldKeyListener implements OnKeyListener {
    private EditText previous;
    private EditText current;
    private EditText next;

    public OnNumericFieldKeyListener(EditText previous, EditText current, EditText next) {
      this.previous = previous;
      this.current = current;
      this.next = next;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_DEL || keyCode == KeyEvent.KEYCODE_FORWARD_DEL) {
        return false;
      }
      if (current.getText().toString().length() >= 2) {
        if (next != null) {
          next.requestFocus();
        }
        return true;
      }
      return false;
    }
  }

}
