package com.cube.nanotimer.scrambler;

import android.content.Context;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.scrambler.RandomStateGenEvent.State;
import com.cube.nanotimer.scrambler.randomstate.RSThreeScrambler;
import com.cube.nanotimer.util.helper.FileUtils;
import com.cube.nanotimer.util.helper.Utils;
import com.cube.nanotimer.vo.CubeType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public enum ScramblerService {
  INSTANCE;

  public static final int MAX_SCRAMBLES_IN_MEMORY = 500;
  private static final int DEFAULT_CACHE_SIZE = 50; // TODO : should increase these two values
  private static final int MIN_CACHE_SIZE = 25;
  private static CubeType defaultCubeType;

  private Context context;
  private Queue<String[]> cachedScrambles = new LinkedList<String[]>();
  private RSScrambler rsScrambler = null;
  private Thread generationThread = null;

  private List<RandomStateGenListener> listeners = new ArrayList<RandomStateGenListener>();
  private RandomStateGenEvent curState = new RandomStateGenEvent(State.IDLE, 0, 0);

  final private Object cacheMemHelper = new Object();
  final private Object scramblerHelper = new Object();
  final private Object cacheFileHelper = new Object();

  public void init(Context context) {
    this.context = context;
    defaultCubeType = Utils.getCurrentCubeType(context);
    if (Options.INSTANCE.isRandomStateScrambles() && rsScrambler == null) {
      initRandomState(defaultCubeType);
    }
  }

  private void initRandomState(final CubeType cubeType) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        rsScrambler = getRandomStateScrambler(cubeType);
        if (rsScrambler != null) {
          loadCacheFromFile(cubeType);
          checkCache(cubeType);
        }
      }
    }).start();
  }

  private void stopRandomState() {
    if (!Options.INSTANCE.isRandomStateScrambles() && rsScrambler != null) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          synchronized (scramblerHelper) {
            rsScrambler.freeMemory();
            rsScrambler = null;
          }
          synchronized (cacheMemHelper) {
            cachedScrambles.clear();
          }
        }
      }).start();
    }
  }

  private void checkCache(CubeType cubeType) {
    System.out.println("[NanoTimer] checkCache. size: " + cachedScrambles.size());
    if (cachedScrambles.size() < MIN_CACHE_SIZE) {
      loadCacheFromFile(cubeType); // see if there are some more scrambles in the file
      if (cachedScrambles.size() < MIN_CACHE_SIZE) {
        try {
          generateAndAddToCache(cubeType, DEFAULT_CACHE_SIZE - cachedScrambles.size());
        } catch (AlreadyGeneratingException e) {
          // Ignore. Scrambles are already being generated, no panic.
        }
      }
    }
  }

  private void generateAndAddToCache(final CubeType cubeType, final int n) throws AlreadyGeneratingException {
    if (generationThread != null) {
      throw new AlreadyGeneratingException();
    }
    final boolean activateRSForGeneration = (rsScrambler == null);
    generationThread = new Thread(new Runnable() {
      @Override
      public void run() {
        if (activateRSForGeneration) { // TODO : see what to do about this... what happens if rs is disabled in options while tables are generating?
          // TODO : should maybe make the "pre-gen" option grayed out when rs is disabled, and make the rs checkbox disabled when it's generating?
          // TODO :    or make sure that generation is stopped correctly when rs checkbox is disabled? (b/c might want to disable when implicitly generating (like for the first time))
          initRandomState(cubeType);
        }
        List<String[]> toSave = new ArrayList<String[]>();
        sendGenStateToListeners(new RandomStateGenEvent(State.PREPARING, 0, n));
        synchronized (scramblerHelper) {
          rsScrambler.genTables();
        }
        for (int i = 0; i < n && generationThread == Thread.currentThread(); i++) {
          String[] scramble;
          synchronized (scramblerHelper) {
            sendGenStateToListeners(new RandomStateGenEvent(State.GENERATING, i + 1, n));
            scramble = rsScrambler.getNewScramble();
          }
          if (cachedScrambles.size() < MAX_SCRAMBLES_IN_MEMORY) {
            synchronized (cacheMemHelper) {
              cachedScrambles.add(scramble);
              System.out.println("[NanoTimer] generate. size: " + cachedScrambles.size());
            }
          }
          toSave.add(scramble);
          if (toSave.size() >= 10) {
            saveNewScramblesToFile(cubeType, toSave); // write new scrambles to file by batches
            toSave.clear();
          }
        }
        if (!toSave.isEmpty()) {
          saveNewScramblesToFile(cubeType, toSave);
        }
        if (activateRSForGeneration) {
          stopRandomState();
        }
        sendGenStateToListeners(new RandomStateGenEvent(State.IDLE, 0, 0));
        generationThread = null;
      }
    });
    generationThread.start();
  }

  private void sendGenStateToListeners(RandomStateGenEvent state) {
    curState = state;
    for (RandomStateGenListener listener : listeners) {
      listener.onStateUpdate(state);
    }
  }

  private RSScrambler getRandomStateScrambler(CubeType cubeType) {
    synchronized (scramblerHelper) {
      switch (cubeType) {
        case THREE_BY_THREE:
          return new RSThreeScrambler();
        default:
          return null;
      }
    }
  }

  private void loadCacheFromFile(CubeType cubeType) {
    List<String> scramblesStr;
    synchronized (cacheFileHelper) {
      scramblesStr = FileUtils.readLinesFromFile(context, getFileName(cubeType), MAX_SCRAMBLES_IN_MEMORY);
    }
    synchronized (cacheMemHelper) {
      for (String l : scramblesStr) {
        String[] scramble = l.split(" ");
        cachedScrambles.add(scramble);
      }
    }
  }

  private void saveNewScramblesToFile(CubeType cubeType, List<String[]> scramblesToSave) {
    List<String> scramblesStr = new ArrayList<String>(scramblesToSave.size());
    for (String[] scramble : scramblesToSave) {
      StringBuilder sb = new StringBuilder();
      for (String move : scramble) {
        sb.append(move).append(" ");
      }
      scramblesStr.add(sb.toString());
    }
    synchronized (cacheFileHelper) {
      FileUtils.appendLinesToFile(context, getFileName(cubeType), scramblesStr.toArray(new String[scramblesStr.size()]));
    }
  }

  private void removeFirstScrambleFromFile(CubeType cubeType) {
    synchronized (cacheFileHelper) {
      FileUtils.removeFirstLineFromFile(context, getFileName(cubeType));
    }
  }

  public String[] getScramble(final CubeType cubeType) {
    if (Options.INSTANCE.isRandomStateScrambles() && isRandomStateForCubeType(cubeType) && !cachedScrambles.isEmpty()) {
      String[] scramble;
      synchronized (cacheMemHelper) {
        scramble = cachedScrambles.remove();
      }
      new Thread(new Runnable() {
        @Override
        public void run() {
          removeFirstScrambleFromFile(cubeType);
          checkCache(cubeType);
        }
      }).start();
      return scramble;
    } else {
      return ScramblerFactory.getScrambler(cubeType).getNewScramble();
    }
  }

  public int getScramblesCount(CubeType cubeType) {
    int scramblesCount;
    synchronized (cacheFileHelper) {
      scramblesCount = FileUtils.getFileLinesCount(context, getFileName(cubeType));
    }
    return scramblesCount;
  }

  public void preGenerate(CubeType cubeType, int nScrambles) throws AlreadyGeneratingException {
    if (isRandomStateForCubeType(cubeType)) {
      generateAndAddToCache(cubeType, nScrambles);
    }
  }

  public void stopGeneration() {
    // TODO : Handle tables generation stop
    // TODO :   In StateTables, set all the tables to null if it was interrupted while they were being generated
    sendGenStateToListeners(new RandomStateGenEvent(State.STOPPING, 0, 0));
    generationThread = null;
  }

  public void activateRandomStateScrambles(boolean activate) {
    if (activate) {
      initRandomState(defaultCubeType);
    } else {
      stopGeneration();
      stopRandomState();
    }
  }

  public void addRandomStateGenListener(RandomStateGenListener listener) {
    listeners.add(listener);
    listener.onStateUpdate(curState);
  }

  public void removeRandomStateGenListener(RandomStateGenListener listener) {
    listeners.remove(listener);
  }

  private boolean isRandomStateForCubeType(CubeType cubeType) {
    return cubeType == CubeType.THREE_BY_THREE;
  }

  private String getFileName(CubeType cubeType) {
    return "randomstate_scrambles_" + cubeType.getId();
  }

}
