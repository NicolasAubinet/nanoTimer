package com.cube.nanotimer;

import android.content.Context;
import android.media.MediaPlayer;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {
  private Map<Integer, MediaPlayer> loadedSounds = new HashMap<>();

  public void init() {
  }

  public void playSound(Context context, int soundResourceId) {
    MediaPlayer mp = loadedSounds.get(soundResourceId);

    if (mp == null) {
      mp = MediaPlayer.create(context, soundResourceId);
      loadedSounds.put(soundResourceId, mp);
    }

    if (!mp.isPlaying()) {
      mp.start();
    }
  }
}
