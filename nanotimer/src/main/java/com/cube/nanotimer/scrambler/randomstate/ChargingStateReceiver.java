package com.cube.nanotimer.scrambler.randomstate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.cube.nanotimer.App;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.scrambler.ScramblerService;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent.GenerationLaunch;
import com.cube.nanotimer.util.helper.Utils;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.ScrambleType;

import java.util.List;

public class ChargingStateReceiver extends BroadcastReceiver {

  public static final String CHECK_ACTION_NAME = "com.cube.nanotimer.action.CHECK_PLUG_STATE";

  private Context context;

  @Override
  public void onReceive(Context context, Intent intent) {
    this.context = context;
    App.INSTANCE.setApplicationContext(context.getApplicationContext());
    boolean charging = Utils.isCurrentlyCharging();
    if (Options.INSTANCE.isGenerateScramblesWhenPluggedIn() && charging) {
      startPlugGeneration();
    } else {
      stopGenerationIfStartedFromPlug();
    }
  }

  private void startPlugGeneration() {
    CubeType firstCubeTypeToGenerateScrambles = null;
    List<CubeType> randomStateCubeTypes = ScramblerService.INSTANCE.getRandomStateCubeTypes();
    for (int i = 0; i < randomStateCubeTypes.size() && firstCubeTypeToGenerateScrambles == null; i++) {
      CubeType cubeType = randomStateCubeTypes.get(i);
      List<ScrambleType> usedScrambledTypes = cubeType.getUsedScrambledTypes();
      if (usedScrambledTypes.isEmpty()) {
        if (isMissingScrambles(cubeType, null)) {
          firstCubeTypeToGenerateScrambles = cubeType;
        }
      } else {
        for (ScrambleType scrambleType : usedScrambledTypes) {
          if (isMissingScrambles(cubeType, scrambleType)) {
            firstCubeTypeToGenerateScrambles = cubeType;
            break;
          }
        }
      }
    }

    if (firstCubeTypeToGenerateScrambles != null) {
      Intent i = new Intent(context, ChargingStateService.class);
      i.putExtra(ChargingStateService.CUBE_TYPE_KEY, firstCubeTypeToGenerateScrambles);
      context.startService(i);
    }
  }

  private boolean isMissingScrambles(CubeType cubeType, ScrambleType scrambleType) {
    boolean isMissingScrambles = false;
    int max = Options.INSTANCE.getPluggedInScramblesGenerateCount();
    int nScrambles = Math.max(max - ScramblerService.INSTANCE.getScramblesCount(cubeType, scrambleType), 0);
    if (nScrambles > 0) {
      isMissingScrambles = true;
    }
    return isMissingScrambles;
  }

  private void stopGenerationIfStartedFromPlug() {
    ScramblerService.INSTANCE.addRandomStateGenListener(new RandomStateGenListener() {
      @Override
      public void onStateUpdate(RandomStateGenEvent event) {
        ScramblerService.INSTANCE.removeRandomStateGenListener(this);
        if (event.getGenerationLaunch() == GenerationLaunch.PLUGGED) {
          ScramblerService.INSTANCE.stopGeneration();
        }
      }
    });
  }

}
