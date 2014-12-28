package com.cube.nanotimer.scrambler.randomstate;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.cube.nanotimer.scrambler.ScramblerService;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent.GenerationLaunch;
import com.cube.nanotimer.vo.CubeType;

public class ChargingStateService extends Service {

  public static final String CUBE_TYPE_KEY = "cube_type";
  public static final String SCRAMBLES_COUNT_KEY = "scrambles_count";

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    CubeType cubeType = (CubeType) intent.getSerializableExtra(CUBE_TYPE_KEY);
    int nScrambles = intent.getIntExtra(SCRAMBLES_COUNT_KEY, -1);
    if (cubeType != null && nScrambles > 0) {
      try {
        ScramblerService.INSTANCE.generateAndAddToCache(cubeType, nScrambles, GenerationLaunch.PLUGGED);
      } catch (AlreadyGeneratingException e) {
        // already generating, skip
      }
    }
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

}
