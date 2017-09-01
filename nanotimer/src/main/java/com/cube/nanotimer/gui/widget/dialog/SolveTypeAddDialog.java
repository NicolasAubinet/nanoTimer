package com.cube.nanotimer.gui.widget.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.helper.Utils;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.ScrambleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SolveTypeAddDialog extends ConfirmDialog {

  public static final String KEY_BLD = "key_bld";
  public static final String KEY_SCRAMBLE_TYPE = "key_scrambleType";

  private static final String ARG_FIELD_CREATOR = "fieldCreator";
  private static final String ARG_CUBE_TYPE = "cubeType";

  private EditText tfName;
  private LinearLayout scrambleTypeLayout;
  private Spinner spScrambleType;

  private ScrambleType previousScrambleType;

  public static SolveTypeAddDialog newInstance(FieldCreator fieldCreator, CubeType cubeType) {
    SolveTypeAddDialog frag = new SolveTypeAddDialog();
    Bundle args = new Bundle();
    args.putSerializable(ARG_FIELD_CREATOR, fieldCreator);
    args.putString(ARG_CUBE_TYPE, cubeType.toString());
    frag.setArguments(args);
    return frag;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    dialog = getDialog(R.string.add);

    tfName = view.findViewById(R.id.tfName);

    scrambleTypeLayout = (LinearLayout) view.findViewById(R.id.scrambleTypeLayout);

    final CubeType cubeType = CubeType.valueOf(getArguments().getString(ARG_CUBE_TYPE));
    ScrambleType[] scrambleTypes = cubeType.getAvailableScrambleTypes();
    if (scrambleTypes.length > 0) {
      scrambleTypeLayout.setVisibility(View.VISIBLE);

      List<CharSequence> scrambleTypesNames = new ArrayList<>();
      for (ScrambleType locScrambleType : scrambleTypes) {
        scrambleTypesNames.add(getScrambleTypeTextString(locScrambleType));
      }

      spScrambleType = (Spinner) view.findViewById(R.id.spScrambleType);
      ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, scrambleTypesNames);
      adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
      spScrambleType.setAdapter(adapter);
      spScrambleType.setSelection(0);

      spScrambleType.setOnItemSelectedListener(new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
          String tfNameText = tfName.getText().toString().trim();
          // adapt solve type name automatically if the field is empty, or if it contains the value of the previously selected scramble type
          if (pos > 0 && (tfNameText.isEmpty() || (previousScrambleType != null && tfNameText.equals(getScrambleTypeTextString(previousScrambleType))))) {
            ScrambleType scrambleType = cubeType.getAvailableScrambleTypes()[pos];
            tfName.setText(getScrambleTypeTextString(scrambleType));
            previousScrambleType = scrambleType;
          }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
      });
    } else {
      scrambleTypeLayout.setVisibility(View.GONE);
    }

    return dialog;
  }

  private String getScrambleTypeTextString(ScrambleType scrambleType) {
    int nameStringResourceId = Utils.getStringIdentifier(getContext(), "scramble_type_" + scrambleType.getName());
    return getString(nameStringResourceId);
  }

  @Override
  protected void onConfirm() {
    CheckBox cbBlind = view.findViewById(R.id.cbBlind);

    Properties props = new Properties();
    props.put(KEY_BLD, String.valueOf(cbBlind.isChecked()));
    int scrambleTypeItemPosition = -1;
    if (spScrambleType != null) {
      scrambleTypeItemPosition = spScrambleType.getSelectedItemPosition();
    }
    props.put(KEY_SCRAMBLE_TYPE, String.valueOf(scrambleTypeItemPosition));

    FieldCreator fieldCreator = (FieldCreator) getArguments().getSerializable(ARG_FIELD_CREATOR);
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
