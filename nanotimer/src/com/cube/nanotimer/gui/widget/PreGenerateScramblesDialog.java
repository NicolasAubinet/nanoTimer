package com.cube.nanotimer.gui.widget;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.cube.nanotimer.R;
import com.cube.nanotimer.scrambler.AlreadyGeneratingException;
import com.cube.nanotimer.scrambler.ScramblerService;
import com.cube.nanotimer.util.helper.DialogUtils;
import com.cube.nanotimer.vo.CubeType.Type;

import java.util.ArrayList;
import java.util.List;

public class PreGenerateScramblesDialog extends DialogPreference {

  public PreGenerateScramblesDialog(Context context, AttributeSet attrs) {
    super(context, attrs);
    setPositiveButtonText("");
    setNegativeButtonText("");
  }

  @Override
  protected View onCreateDialogView() {
    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View layout = inflater.inflate(R.layout.pregen_scrambles_dialog, null);
    final EditText tfScramblesCount = (EditText) layout.findViewById(R.id.tfScramblesCount);
    final Button buAction = (Button) layout.findViewById(R.id.buAction);
    final ListView lvCubeTypes = (ListView) layout.findViewById(R.id.lvCubeType);

    Integer defaultValue = getContext().getResources().getInteger(R.integer.gen_scrambles_count);
    Integer value = getPersistedInt(defaultValue);
    tfScramblesCount.setText(value.toString());

    buAction.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          int nScrambles = Integer.parseInt(tfScramblesCount.getText().toString());
          persistInt(nScrambles);

          try {
            ScramblerService.INSTANCE.preGenerate(nScrambles);
            // TODO : display progression dialog with "Generating scramble 1 of 200" etc.
            // TODO :   also add a "Stop" button, and call binder.stopGeneration() if clicked (with confirmation msg)
            // TODO :   need a progression listener
          } catch (AlreadyGeneratingException e) {
            DialogUtils.showInfoMessage(getContext(), R.string.scrambles_already_generating);
          }
        } catch (NumberFormatException e) {
          DialogUtils.showInfoMessage(getContext(), R.string.scrambles_number_not_valid);
        }
      }
    });

    List<String> liCubeTypes = new ArrayList<String>();
    liCubeTypes.add(Type.THREE_BY_THREE.name());
    liCubeTypes.add(Type.TWO_BY_TWO.name());
    ArrayAdapter<String> adapter = new CustomAdapter(getContext(), R.layout.simple_list_item, liCubeTypes);
    lvCubeTypes.setAdapter(adapter);

    return layout;
  }

  protected class CustomAdapter extends ArrayAdapter<String> {
    private List<String> list;

    public CustomAdapter(Context context, int id, List<String> list) {
      super(context, id, list);
      this.list = list;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
      TextView view = new TextView(getContext());
      view.setText(list.get(position));
      return view;
    }
  }

}
