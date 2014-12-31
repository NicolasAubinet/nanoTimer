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

public class ChargingStateReceiver extends BroadcastReceiver {

  public static final String CHECK_ACTION_NAME = "com.cube.nanotimer.action.CHECK_PLUG_STATE";

  private Context context;

  @Override
  public void onReceive(Context context, Intent intent) {
    this.context = context;
    App.INSTANCE.setContext(context.getApplicationContext());
    boolean charging = Utils.isCurrentlyCharging();
    if (Options.INSTANCE.isGenerateScramblesWhenPluggedIn() && charging) {
      startPlugGeneration();
    } else {
      stopGenerationIfStartedFromPlug();
    }
  }

  private void startPlugGeneration() {
    int max = Options.INSTANCE.getPluggedInScramblesGenerateCount();
    for (CubeType cubeType : ScramblerService.INSTANCE.getRandomStateCubeTypes()) {
      int nScrambles = Math.max(max - ScramblerService.INSTANCE.getScramblesCount(cubeType), 0);
      if (nScrambles > 0) {
        Intent i = new Intent(context, ChargingStateService.class);
        i.putExtra(ChargingStateService.CUBE_TYPE_KEY, cubeType);
        i.putExtra(ChargingStateService.SCRAMBLES_COUNT_KEY, nScrambles);
        context.startService(i);
      }
    }
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
