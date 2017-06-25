package com.cube.nanotimer.scrambler.randomstate;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.cube.nanotimer.scrambler.ScramblerService;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent.GenerationLaunch;
import com.cube.nanotimer.vo.CubeType;

public class ChargingStateService extends Service {

  public static final String CUBE_TYPE_KEY = "cube_type";

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    CubeType cubeType = (CubeType) intent.getSerializableExtra(CUBE_TYPE_KEY);
    if (cubeType != null) {
      try {
        ScramblerService.INSTANCE.generateAndAddToCache(cubeType, -1, GenerationLaunch.PLUGGED);
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
