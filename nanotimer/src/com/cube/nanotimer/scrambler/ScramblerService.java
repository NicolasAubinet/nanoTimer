package com.cube.nanotimer.scrambler;

import android.content.Context;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.scrambler.randomstate.AlreadyGeneratingException;
import com.cube.nanotimer.scrambler.randomstate.RSScrambler;
import com.cube.nanotimer.scrambler.randomstate.RSThreeScrambler;
import com.cube.nanotimer.scrambler.randomstate.RSTwoScrambler;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent.GenerationLaunch;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent.State;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenListener;
import com.cube.nanotimer.scrambler.randomstate.ScrambleConfig;
import com.cube.nanotimer.util.helper.FileUtils;
import com.cube.nanotimer.util.helper.Utils;
import com.cube.nanotimer.vo.CubeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public enum ScramblerService {
  INSTANCE;

  public static final int MAX_SCRAMBLES_IN_MEMORY = 500;

  private Context context;
  private Map<Integer, LinkedList<String[]>> cachedScrambles = new HashMap<Integer, LinkedList<String[]>>(); // id: cube type id | value: scrambles
  private Map<Integer, RSScrambler> scramblers = new HashMap<Integer, RSScrambler>();
  private volatile Thread generationThread = null;

  private List<RandomStateGenListener> listeners = new ArrayList<RandomStateGenListener>();
  private RandomStateGenEvent curState = new RandomStateGenEvent(State.IDLE, null, null, 0, 0);

  final private Object genThreadHelper = new Object();
  final private Object listenersHelper = new Object();
  final private Object cacheMemHelper = new Object();
  final private Object scramblerHelper = new Object();
  final private Object cacheFileHelper = new Object();

  public void init(Context context) {
    this.context = context;
    if (Options.INSTANCE.isRandomStateScrambles()) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          checkScrambleCaches();
        }
      }).start();
    }
  }

  public void checkScrambleCaches() {
    checkCache(getRandomStateCubeTypes().get(0));
  }

  private void stopRandomState() {
    if (!Options.INSTANCE.isRandomStateScrambles() && !scramblers.isEmpty()) {
      synchronized (scramblerHelper) {
        for (RSScrambler scrambler : scramblers.values()) {
          scrambler.freeMemory();
        }
        scramblers.clear();
      }
      synchronized (cacheMemHelper) {
        cachedScrambles.clear();
      }
    }
  }

  private void checkCache(CubeType cubeType) {
    try {
      generateAndAddToCache(cubeType, -1, GenerationLaunch.AUTO);
    } catch (AlreadyGeneratingException e) {
      // Ignore. Scrambles are already being generated.
    }
  }

  private int loadCacheAndGetToGenCount(CubeType cubeType) {
    int minCacheSize = Options.INSTANCE.getScramblesMinCacheSize();
    if (getCache(cubeType).size() < minCacheSize) {
      loadCacheFromFile(cubeType); // see if there are some more scrambles in the file
      if (getCache(cubeType).size() < minCacheSize) {
        return Math.max(Options.INSTANCE.getScramblesMaxCacheSize() - getCache(cubeType).size(), 0);
      }
    }
    return 0;
  }

  private void generateAndAddToCache(final CubeType cubeType, int scramblesCount, GenerationLaunch generationLaunch) throws AlreadyGeneratingException {
    synchronized (genThreadHelper) {
      if (generationThread != null) {
        throw new AlreadyGeneratingException();
      }
      generationThread = getNewGenerationThread(cubeType, scramblesCount, generationLaunch);
      generationThread.start();
    }
  }

  private Thread getNewGenerationThread(final CubeType cubeType, final int scramblesCount, final GenerationLaunch generationLaunch) {
    return new Thread() {
      @Override
      public void run() {
        generateScrambles(cubeType, scramblesCount);
        for (CubeType rsCubeType : getRandomStateCubeTypes()) {
          generateScrambles(rsCubeType, -1);
        }
        sendGenStateToListeners(new RandomStateGenEvent(RandomStateGenEvent.State.IDLE, null, null, 0, 0));
        if (generationThread == Thread.currentThread()) {
          generationThread = null;
        }
      }

      private void generateScrambles(CubeType cubeType, int scramblesCount) {
        final RSScrambler rsScrambler = getScrambler(cubeType);
        if (generationThread != Thread.currentThread() || rsScrambler == null || !Options.INSTANCE.isRandomStateScrambles()) {
          return;
        }
        int n;
        if (scramblesCount > 0) {
          n = scramblesCount;
        } else {
          n = loadCacheAndGetToGenCount(cubeType);
        }
        if (n == 0) {
          return;
        }
        List<String[]> toSave = new ArrayList<String[]>();
        sendGenStateToListeners(new RandomStateGenEvent(RandomStateGenEvent.State.PREPARING, cubeType, generationLaunch, 0, n));
        synchronized (scramblerHelper) {
          rsScrambler.genTables();
        }
        for (int i = 0; i < n && generationThread == Thread.currentThread(); i++) {
          String[] scramble;
          synchronized (scramblerHelper) {
            sendGenStateToListeners(new RandomStateGenEvent(RandomStateGenEvent.State.GENERATING, cubeType, generationLaunch, i + 1, n));
            scramble = rsScrambler.getNewScramble(new ScrambleConfig(Utils.getRSScrambleLengthFromQuality(cubeType)));
          }
          if (scramble == null) { // was interrupted
            break;
          }
          if (getCache(cubeType).size() < MAX_SCRAMBLES_IN_MEMORY) {
            synchronized (cacheMemHelper) {
              getCache(cubeType).add(scramble);
//              Log.i("[NanoTimer]", "generate. size: " + getCache(cubeType).size());
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
      }
    };
  }

  private void sendGenStateToListeners(RandomStateGenEvent state) {
    curState = state;
    synchronized (listenersHelper) {
      for (int i = 0; i < listeners.size(); i++) {
        listeners.get(i).onStateUpdate(state);
      }
    }
  }

  private RSScrambler getNewRandomStateScrambler(CubeType cubeType) {
    switch (cubeType) {
      case THREE_BY_THREE:
        return new RSThreeScrambler();
      case TWO_BY_TWO:
        return new RSTwoScrambler();
      default:
        return null;
    }
  }

  private void loadCacheFromFile(CubeType cubeType) {
    List<String> scramblesStr;
    synchronized (cacheFileHelper) {
      scramblesStr = FileUtils.readLinesFromFile(context, getFileName(cubeType), MAX_SCRAMBLES_IN_MEMORY);
    }
    synchronized (cacheMemHelper) {
      getCache(cubeType).clear();
      for (String l : scramblesStr) {
        String[] scramble = l.split(" ");
        getCache(cubeType).add(scramble);
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
    if (Options.INSTANCE.isRandomStateScrambles() && getRandomStateCubeTypes().contains(cubeType)) {
      String[] scramble;
      if (!getCache(cubeType).isEmpty()) {
        synchronized (cacheMemHelper) {
          scramble = getCache(cubeType).remove();
        }
      } else {
        scramble = ScramblerFactory.getScrambler(cubeType).getNewScramble();
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
    if (getRandomStateCubeTypes().contains(cubeType)) {
      generateAndAddToCache(cubeType, nScrambles, GenerationLaunch.MANUAL);
    }
  }

  public void stopGeneration() {
    synchronized (genThreadHelper) {
      if (generationThread != null) {
        generationThread = null;
        sendGenStateToListeners(new RandomStateGenEvent(State.STOPPING, null, null, 0, 0));
      }
    }
    for (RSScrambler scrambler : scramblers.values()) {
      scrambler.stop();
    }
  }

  public void deleteCaches() {
    for (CubeType cubeType : getRandomStateCubeTypes()) {
      synchronized (cacheFileHelper) {
        FileUtils.deleteFile(context, getFileName(cubeType));
      }
      synchronized (cacheMemHelper) {
        getCache(cubeType).clear();
      }
    }
  }

  public void activateRandomStateScrambles(final boolean activate) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (activate) {
          checkScrambleCaches();
        } else {
          stopGeneration();
          stopRandomState();
        }
      }
    }).start();
  }

  public void addRandomStateGenListener(RandomStateGenListener listener) {
    synchronized (listenersHelper) {
      listeners.add(listener);
    }
    listener.onStateUpdate(curState);
  }

  public void removeRandomStateGenListener(RandomStateGenListener listener) {
    synchronized (listenersHelper) {
      listeners.remove(listener);
    }
  }

  private List<CubeType> getRandomStateCubeTypes() {
    return Arrays.asList(CubeType.THREE_BY_THREE, CubeType.TWO_BY_TWO);
  }

  private String getFileName(CubeType cubeType) {
    return "randomstate_scrambles_" + cubeType.getId();
  }

  private Queue<String[]> getCache(CubeType cubeType) {
    if (cachedScrambles.get(cubeType.getId()) == null) {
      cachedScrambles.put(cubeType.getId(), new LinkedList<String[]>());
    }
    return cachedScrambles.get(cubeType.getId());
  }

  private RSScrambler getScrambler(CubeType cubeType) {
    synchronized (scramblerHelper) {
      if (scramblers.get(cubeType.getId()) == null) {
        scramblers.put(cubeType.getId(), getNewRandomStateScrambler(cubeType));
      }
      return scramblers.get(cubeType.getId());
    }
  }

}
