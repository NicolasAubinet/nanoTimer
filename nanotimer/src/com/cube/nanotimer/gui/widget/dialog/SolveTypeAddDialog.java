package com.cube.nanotimer.gui.widget.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import com.cube.nanotimer.R;

import java.util.Properties;

public class SolveTypeAddDialog extends ConfirmDialog {

  public static final String KEY_BLD = "key_bld";

  private FieldCreator fieldCreator;

  public static SolveTypeAddDialog newInstance(FieldCreator fieldCreator) {
    SolveTypeAddDialog frag = new SolveTypeAddDialog(fieldCreator);
    return frag;
  }

  private SolveTypeAddDialog(FieldCreator fieldCreator) {
    this.fieldCreator = fieldCreator;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    dialog = getDialog(R.string.add);
    return dialog;
  }

  @Override
  protected void onConfirm() {
    EditText tfName = (EditText) view.findViewById(R.id.tfName);
    CheckBox cbBlind = (CheckBox) view.findViewById(R.id.cbBlind);

    Properties props = new Properties();
    props.put(KEY_BLD, String.valueOf(cbBlind.isChecked()));

    if (fieldCreator.createField(tfName.getText().toString(), props)) {
      dialog.dismiss();
    }
  }

  @Override
  protected View getCustomView() {
    LayoutInflater factory = LayoutInflater.from(getActivity());
    return factory.inflate(R.layout.solvetype_add_dialog, null);
  }

}
