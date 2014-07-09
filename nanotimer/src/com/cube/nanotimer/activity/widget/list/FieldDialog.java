package com.cube.nanotimer.activity.widget.list;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
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
            onConfirm();
          }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
          }
        })
        .create();
    showSoftKeyboard(d);
    return d;
  }

  protected abstract void onConfirm();

  public void showSoftKeyboard(Dialog d) {
    d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
  }

}
