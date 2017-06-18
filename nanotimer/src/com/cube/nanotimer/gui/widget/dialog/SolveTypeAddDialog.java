package com.cube.nanotimer.gui.widget.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import com.cube.nanotimer.R;
import com.cube.nanotimer.vo.CubeType;

import java.util.Properties;

public class SolveTypeAddDialog extends ConfirmDialog {

  public static final String KEY_BLD = "key_bld";
  public static final String KEY_SCRAMBLE_TYPE = "key_scrambleType";

  private static final String ARG_CUBE_TYPE = "cubeType";

  private FieldCreator fieldCreator;

  private LinearLayout scrambleTypeLayout;
  private Spinner spScrambleType;

  public static SolveTypeAddDialog newInstance(FieldCreator fieldCreator, CubeType cubeType) {
    SolveTypeAddDialog frag = new SolveTypeAddDialog(fieldCreator);
    Bundle args = new Bundle();
    args.putString(ARG_CUBE_TYPE, cubeType.toString());
    frag.setArguments(args);
    return frag;
  }

  private SolveTypeAddDialog(FieldCreator fieldCreator) {
    this.fieldCreator = fieldCreator;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    dialog = getDialog(R.string.add);

    scrambleTypeLayout = (LinearLayout) view.findViewById(R.id.scrambleTypeLayout);

    if (!CubeType.THREE_BY_THREE.toString().equals(getArguments().getString(ARG_CUBE_TYPE))) {
      scrambleTypeLayout.setVisibility(View.GONE);

      spScrambleType = (Spinner) view.findViewById(R.id.spScrambleType);
      ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.scramble_type, R.layout.spinner_item);
      adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
      spScrambleType.setAdapter(adapter);
      spScrambleType.setSelection(0);
    }

    return dialog;
  }

  @Override
  protected void onConfirm() {
    EditText tfName = (EditText) view.findViewById(R.id.tfName);
    CheckBox cbBlind = (CheckBox) view.findViewById(R.id.cbBlind);

    Properties props = new Properties();
    props.put(KEY_BLD, String.valueOf(cbBlind.isChecked()));
    if (spScrambleType != null) {
      props.put(KEY_SCRAMBLE_TYPE, spScrambleType.getSelectedItemPosition());
    } else {
      props.put(KEY_SCRAMBLE_TYPE, -1);
    }

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
