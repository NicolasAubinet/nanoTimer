package com.cube.nanotimer.activity.widget.list;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;
import com.cube.nanotimer.R;

public class FieldAddDialog extends FieldDialog {

  private FieldCreator fieldCreator;

  public static FieldAddDialog newInstance(FieldCreator fieldCreator) {
    FieldAddDialog frag = new FieldAddDialog(fieldCreator);
    return frag;
  }

  private FieldAddDialog(FieldCreator fieldCreator) {
    this.fieldCreator = fieldCreator;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    dialog = getDialog(R.string.add);
    return dialog;
  }

  @Override
  protected void onConfirm() {
    EditText tfName = (EditText) editTextView.findViewById(R.id.tfName);
    if (fieldCreator.createField(tfName.getText().toString())) {
      dialog.dismiss();
    }
  }

}