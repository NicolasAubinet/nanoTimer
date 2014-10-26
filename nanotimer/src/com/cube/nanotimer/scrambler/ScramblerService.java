package com.cube.nanotimer.scrambler;

import android.content.Context;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.scrambler.randomstate.RSThreeScrambler;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.CubeType.Type;

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

public enum ScramblerService {
  INSTANCE;

  private static final int DEFAULT_CACHE_SIZE = 50; // TODO : should increase these two values
  private static final int MIN_CACHE_SIZE = 25;

  private Context context;
  private Queue<String[]> cachedScrambles = new LinkedList<String[]>();
  private RSScrambler rsScrambler = null;
  private Thread generationThread = null;

  final private Object cacheSyncHelper = new Object();

  public void init(Context context) {
    this.context = context;
    if (Options.INSTANCE.isRandomStateScrambles() && rsScrambler == null) {
      initRandomState();
    }
  }

  private void initRandomState() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        rsScrambler = getRandomStateScrambler();
        loadCacheFromFile();
        checkCache();
      }
    }).start();
  }

  private void stopRandomState() {
    if (!Options.INSTANCE.isRandomStateScrambles() && rsScrambler != null) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          rsScrambler.freeMemory();
          synchronized (cacheSyncHelper) {
            rsScrambler = null;
            cachedScrambles.clear();
          }
        }
      }).start();
    }
  }

  private void checkCache() {
    System.out.println("[NanoTimer] checkCache. size: " + cachedScrambles.size());
    if (cachedScrambles.size() < MIN_CACHE_SIZE) {
      try {
        generateAndAddToCache(DEFAULT_CACHE_SIZE - cachedScrambles.size());
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
          String[] scramble = rsScrambler.getNewScramble();
          synchronized (cacheSyncHelper) {
            cachedScrambles.add(scramble);
            System.out.println("[NanoTimer] generate. size: " + cachedScrambles.size());
          }
        }
        saveCacheToFile();
        if (activateRSForGeneration) {
          stopRandomState();
        }
        generationThread = null;
      }
    });
    generationThread.start();
  }

  private RSScrambler getRandomStateScrambler() {
    return new RSThreeScrambler();
  }

  private synchronized void loadCacheFromFile() {
    try {
      FileInputStream fis = context.openFileInput("randomstate_scrambles");
      BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
      synchronized (cacheSyncHelper) {
        String strScramble;
        while ((strScramble = reader.readLine()) != null) {
          String[] scramble = strScramble.split(" ");
          cachedScrambles.add(scramble);
        }
      }
      reader.close();
      fis.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private synchronized void saveCacheToFile() {
    try {
      FileOutputStream fos = context.openFileOutput("randomstate_scrambles", Context.MODE_PRIVATE);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
      synchronized (cacheSyncHelper) {
        for (String[] scramble : cachedScrambles) {
          StringBuilder sb = new StringBuilder();
          for (String move : scramble) {
            sb.append(move).append(" ");
          }
          writer.write(sb.toString());
          writer.newLine();
        }
      }
      writer.close();
      fos.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String[] getScramble(CubeType cubeType) {
    if (Options.INSTANCE.isRandomStateScrambles() && isRandomStateForCubeType(cubeType) && !cachedScrambles.isEmpty()) {
      String[] scramble;
      synchronized (cacheSyncHelper) {
        scramble = cachedScrambles.remove();
      }
      new Thread(new Runnable() {
        @Override
        public void run() {
          saveCacheToFile();
          checkCache();
        }
      }).start();
      return scramble;
    } else {
      return ScramblerFactory.getScrambler(cubeType).getNewScramble();
    }
  }

  public void preGenerate(int nScrambles) throws AlreadyGeneratingException {
      generateAndAddToCache(nScrambles);
    }

  public void stopGeneration() {
      generationThread = null;
    }

  public void activateRandomStateScrambles(boolean activate) {
    if (activate) {
      initRandomState();
    } else {
      stopRandomState();
    }
  }

  private boolean isRandomStateForCubeType(CubeType cubeType) {
    return cubeType.getType() == Type.THREE_BY_THREE;
  }

}
