package com.cube.nanotimer.gui.widget.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;
import com.cube.nanotimer.R;

public class FieldEditDialog extends FieldDialog {

  private int pos;
  private EditText tfName;
  private FieldRenamer fieldRenamer;

  private static final String ARG_POS = "pos";
  private static final String ARG_DEFAULT_NAME = "defaultName";

  public static FieldEditDialog newInstance(FieldRenamer fieldRenamer, int pos, String defaultName) {
    FieldEditDialog frag = new FieldEditDialog(fieldRenamer);
    Bundle args = new Bundle();
    args.putInt(ARG_POS, pos);
    args.putString(ARG_DEFAULT_NAME, defaultName);
    frag.setArguments(args);
    return frag;
  }

  private FieldEditDialog(FieldRenamer fieldRenamer) {
    this.fieldRenamer = fieldRenamer;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    dialog = getDialog(R.string.confirm);

    pos = getArguments().getInt(ARG_POS);
    String defaultName = getArguments().getString(ARG_DEFAULT_NAME);

    tfName = (EditText) editTextView.findViewById(R.id.tfName);
    tfName.setText(defaultName);
    tfName.setSelection(0, tfName.length());
    return dialog;
  }

  @Override
  protected void onConfirm() {
    if (fieldRenamer.renameField(pos, tfName.getText().toString())) {
      dialog.dismiss();
    }
  }

}
