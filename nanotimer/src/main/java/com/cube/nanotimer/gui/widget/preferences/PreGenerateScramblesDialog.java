package com.cube.nanotimer.gui.widget.preferences;

import android.app.Activity;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import com.cube.nanotimer.R;
import com.cube.nanotimer.scrambler.ScramblerService;
import com.cube.nanotimer.scrambler.randomstate.AlreadyGeneratingException;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent.State;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenListener;
import com.cube.nanotimer.util.helper.DialogUtils;
import com.cube.nanotimer.vo.CubeType;

public class PreGenerateScramblesDialog extends DialogPreference implements RandomStateGenListener {

  private Button buGenerate;
  private Button buStopGeneration;
  private TextView tvState;
  private TextView tvTotalScramblesCount;
  private ViewGroup generateLayout;
  private ViewGroup generatingLayout;
  private CubeType cubeType;

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
    buGenerate = (Button) layout.findViewById(R.id.buGenerate);
    buStopGeneration = (Button) layout.findViewById(R.id.buStopGeneration);
    generateLayout = (ViewGroup) layout.findViewById(R.id.generateLayout);
    generatingLayout = (ViewGroup) layout.findViewById(R.id.generatingLayout);
    tvState = (TextView) layout.findViewById(R.id.tvGenState);
    tvTotalScramblesCount = (TextView) layout.findViewById(R.id.tvTotalScramblesCount);
    final RadioButton rbThreeByThree = (RadioButton) layout.findViewById(R.id.rbThreeByThree);
    final RadioButton rbTwoByTwo = (RadioButton) layout.findViewById(R.id.rbTwoByTwo);
    final RadioButton rbSquare1 = (RadioButton) layout.findViewById(R.id.rbSquare1);

    cubeType = CubeType.THREE_BY_THREE;
    rbThreeByThree.setChecked(true);
    ((RadioGroup) layout.findViewById(R.id.radioGroup)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if (rbThreeByThree.isChecked()) {
          cubeType = CubeType.THREE_BY_THREE;
        } else if (rbTwoByTwo.isChecked()) {
          cubeType = CubeType.TWO_BY_TWO;
        } else if (rbSquare1.isChecked()) {
          cubeType = CubeType.SQUARE1;
        }
        updateTotalScramblesCount();
      }
    });

    ScramblerService.INSTANCE.addRandomStateGenListener(PreGenerateScramblesDialog.this);

    Integer defaultValue = getContext().getResources().getInteger(R.integer.gen_scrambles_count);
    Integer value = getPersistedInt(defaultValue);
    tfScramblesCount.setText(value.toString());
    updateTotalScramblesCount();

    buGenerate.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          int nScrambles = Integer.parseInt(tfScramblesCount.getText().toString());
          persistInt(nScrambles);
          try {
            ScramblerService.INSTANCE.preGenerate(cubeType, nScrambles);
          } catch (AlreadyGeneratingException e) {
            DialogUtils.showInfoMessage(getContext(), R.string.scrambles_already_generating);
          }
        } catch (NumberFormatException e) {
          DialogUtils.showInfoMessage(getContext(), R.string.scrambles_count_not_valid);
        }
      }
    });

    buStopGeneration.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ScramblerService.INSTANCE.stopGeneration();
      }
    });

    return layout;
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    ScramblerService.INSTANCE.removeRandomStateGenListener(this);
    super.onDialogClosed(positiveResult);
  }

  @Override
  public void onStateUpdate(final RandomStateGenEvent event) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        State state = event.getState();
        if (state == State.IDLE) {
          generateLayout.setVisibility(View.VISIBLE);
          generatingLayout.setVisibility(View.GONE);
          updateTotalScramblesCount();
        } else if (state == State.STOPPING) {
          tvState.setText(R.string.stopping_generation);
          buStopGeneration.setVisibility(View.GONE);
        } else {
          generateLayout.setVisibility(View.GONE);
          generatingLayout.setVisibility(View.VISIBLE);
          buStopGeneration.setVisibility(View.VISIBLE);
          if (state == State.GENERATING) {
            tvState.setText(getContext().getString(R.string.generating_cube_scramble, event.getCubeTypeName(),
                event.getCurScramble(), event.getTotalToGenerate()));
          } else if (state == State.PREPARING) {
            tvState.setText(R.string.preparing_generation);
          }
        }
      }
    });
  }

  private void updateTotalScramblesCount() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        final int scramblesCount = ScramblerService.INSTANCE.getScramblesCount(cubeType, null);
//        Log.i("[NanoTimer]", "Total count: " + scramblesCount);
        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            tvTotalScramblesCount.setText(getContext().getString(R.string.total_scrambles_count, scramblesCount));
          }
        });
      }
    }).start();
  }

  private Activity getActivity() {
    return (Activity) getContext();
  }

}
