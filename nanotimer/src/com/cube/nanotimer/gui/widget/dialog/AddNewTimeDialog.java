package com.cube.nanotimer.gui.widget.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.gui.widget.ResultListener;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.helper.DialogUtils;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;

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

    tfMinutes.addTextChangedListener(new OnNumericFieldKeyListener(tfSeconds));
    tfSeconds.addTextChangedListener(new OnNumericFieldKeyListener(tfHundreds));
    tfHundreds.addTextChangedListener(new OnNumericFieldKeyListener(null));
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
      tfMinutes.requestFocus();
      DialogUtils.showInfoMessage(getContext(), getString(R.string.invalid_value_for_field, getString(R.string.minutes)));
      valid = false;
    } else if (seconds < 0 || seconds >= 60) {
      tfSeconds.requestFocus();
      DialogUtils.showInfoMessage(getContext(), getString(R.string.invalid_value_for_field, getString(R.string.seconds)));
      valid = false;
    } else if (hundreds < 0 || hundreds >= 100) {
      tfHundreds.requestFocus();
      DialogUtils.showInfoMessage(getContext(), getString(R.string.invalid_value_for_field, getString(R.string.hundreds)));
      valid = false;
    } else if (minutes == 0 && seconds == 0 && hundreds == 0) {
      DialogUtils.showInfoMessage(getContext(), R.string.please_set_a_value);
      valid = false;
    }
    return valid;
  }

  class OnNumericFieldKeyListener implements TextWatcher {
    private EditText next;

    public OnNumericFieldKeyListener(EditText next) {
      this.next = next;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
      if (s.length() >= 2) {
        if (next != null) {
          next.requestFocus();
        }
      }
    }
  }

}
