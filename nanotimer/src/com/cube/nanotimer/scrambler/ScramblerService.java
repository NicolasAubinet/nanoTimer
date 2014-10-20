package com.cube.nanotimer.scrambler;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.scrambler.randomstate.RSThreeScrambler;
import com.cube.nanotimer.vo.CubeType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.Queue;

public class ScramblerService extends Service {

  private static final int DEFAULT_CACHE_SIZE = 50;
  private static final int MIN_CACHE_SIZE = 25;

  private IBinder binder = new LocalBinder();
  private Queue<String[]> cachedScrambles = new LinkedList<String[]>();
  private Scrambler rsScrambler = null;
  private Thread generationThread = null;

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    initRandomState();
  }

  private void initRandomState() {
    if (Options.INSTANCE.isRandomStateScrambles() && rsScrambler == null) {
      rsScrambler = getRandomStateScrambler();
      loadCacheFromFile();
      checkCache();
    }
  }

  private void stopRandomState() {
    if (!Options.INSTANCE.isRandomStateScrambles() && rsScrambler != null) {
      // TODO : free up memory
      rsScrambler = null;
      cachedScrambles.clear();
    }
  }

  private void checkCache() {
    if (cachedScrambles.size() < MIN_CACHE_SIZE) {
      try {
        generateAndAddToCache(DEFAULT_CACHE_SIZE - MIN_CACHE_SIZE);
      } catch (AlreadyGeneratingException e) {
        // Ignore. Scrambles are already being generated, no panic.
      }
    }
  }

  private void generateAndAddToCache(final int n) throws AlreadyGeneratingException {
    if (generationThread != null) {
      throw new AlreadyGeneratingException();
    }
    final boolean activateRSForGeneration = (rsScrambler == null);
    if (activateRSForGeneration) {
      initRandomState();
    }
    generationThread = new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < n && generationThread == Thread.currentThread(); i++) {
          cachedScrambles.add(rsScrambler.getNewScramble());
        }
        saveCacheToFile();
        if (activateRSForGeneration) {
          stopRandomState();
        }
      }
    });
    generationThread.start();
  }

  private Scrambler getRandomStateScrambler() {
    return new RSThreeScrambler();
  }

  private void loadCacheFromFile() {
    try {
      FileInputStream fis = openFileInput("randomstate_scrambles");
      BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
      String strScramble;
      while ((strScramble = reader.readLine()) != null) {
        String[] scramble = strScramble.split(" ");
        cachedScrambles.add(scramble);
      }
      reader.close();
      fis.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void saveCacheToFile() {
    try {
      FileOutputStream fos = openFileOutput("randomstate_scrambles", Context.MODE_APPEND);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
      for (String[] scramble : cachedScrambles) {
        StringBuilder sb = new StringBuilder();
        for (String move : scramble) {
          sb.append(move).append(" ");
        }
        writer.write(sb.toString());
        writer.newLine();
      }
      writer.close();
      fos.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public class LocalBinder extends Binder {

    public String[] getScramble(CubeType cubeType) {
      if (Options.INSTANCE.isRandomStateScrambles() && !cachedScrambles.isEmpty()) {
        String[] scramble = cachedScrambles.remove();
        checkCache();
        return scramble;
      } else {
        return ScramblerFactory.getScrambler(cubeType).getNewScramble();
      }
    }

    public void preGenerate(CubeType cubeType, int nScrambles) throws AlreadyGeneratingException {
      // TODO : take care of cubeType when random-state is also implemented for 2x2x2
      generateAndAddToCache(nScrambles);
    }

    public void stopGeneration() {
      generationThread = null;
    }

    // TODO : call when changing random-state option
    public void activateRandomStateScrambles(boolean activate) {
      if (activate) {
        initRandomState();
      } else {
        stopRandomState();
      }
    }
  }

}
