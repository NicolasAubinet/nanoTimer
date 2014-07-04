package com.cube.nanotimer.activity.widget.list;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import com.cube.nanotimer.R;

public abstract class FieldDialog extends DialogFragment {

  protected Dialog dialog;
  protected FieldRenamer fieldRenamer;
  protected View editTextView;

  protected FieldDialog(FieldRenamer fieldRenamer) {
    this.fieldRenamer = fieldRenamer;
  }

  protected Dialog getDialog(int confirmText) {
    LayoutInflater factory = LayoutInflater.from(getActivity());
    editTextView = factory.inflate(R.layout.edittext_field, null);
    final AlertDialog d;

    d = new AlertDialog.Builder(getActivity())
        .setView(editTextView)
        .setPositiveButton(confirmText, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
          }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
          }
        })
        .create();
    d.setOnShowListener(new DialogInterface.OnShowListener() {
      @Override
      public void onShow(DialogInterface dialog) {
        Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            onConfirm();
          }
        });
      }
    });
    showSoftKeyboard(d);
    return d;
  }

  protected abstract void onConfirm();

  public void showSoftKeyboard(Dialog d) {
    d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
  }

}
