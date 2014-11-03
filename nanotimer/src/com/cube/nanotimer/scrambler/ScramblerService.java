package com.cube.nanotimer.scrambler;

import android.content.Context;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.scrambler.randomstate.AlreadyGeneratingException;
import com.cube.nanotimer.scrambler.randomstate.RSScrambler;
import com.cube.nanotimer.scrambler.randomstate.RSThreeScrambler;
import com.cube.nanotimer.scrambler.randomstate.RSTwoScrambler;
import com.cube.nanotimer.scrambler.randomstate.RandomStateGenEvent;
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
  private static final int DEFAULT_CACHE_SIZE = 50; // TODO : should increase these two values
  private static final int MIN_CACHE_SIZE = 25;

  private Context context;
  private Map<Integer, LinkedList<String[]>> cachedScrambles = new HashMap<Integer, LinkedList<String[]>>(); // id: cube type id | value: scrambles
  private Map<Integer, RSScrambler> scramblers = new HashMap<Integer, RSScrambler>();
  private Thread generationThread = null;

  private List<RandomStateGenListener> listeners = new ArrayList<RandomStateGenListener>();
  private RandomStateGenEvent curState = new RandomStateGenEvent(State.IDLE, 0, 0);

  final private Object cacheMemHelper = new Object();
  final private Object scramblerHelper = new Object();
  final private Object cacheFileHelper = new Object();

  public void init(Context context) {
    this.context = context;
    if (Options.INSTANCE.isRandomStateScrambles()) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          for (CubeType rsCubeType : getRandomStateCubeTypes()) {
            initRandomState(rsCubeType);
          }
        }
      }).start();
    }
  }

  private void initRandomState(final CubeType cubeType) {
    synchronized (scramblerHelper) {
      loadCacheFromFile(cubeType);
      checkCache(cubeType);
    }
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
    System.out.println("[NanoTimer] checkCache. size: " + getCache(cubeType).size());
    if (getCache(cubeType).size() < MIN_CACHE_SIZE) {
      loadCacheFromFile(cubeType); // see if there are some more scrambles in the file
      if (getCache(cubeType).size() < MIN_CACHE_SIZE) {
        try {
          generateAndAddToCache(cubeType, DEFAULT_CACHE_SIZE - getCache(cubeType).size());
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
    final RSScrambler rsScrambler = getScrambler(cubeType);
    if (rsScrambler == null) {
      return;
    }
    generationThread = new Thread(new Runnable() {
      @Override
      public void run() {
        List<String[]> toSave = new ArrayList<String[]>();
        sendGenStateToListeners(new RandomStateGenEvent(State.PREPARING, 0, n));
        synchronized (scramblerHelper) {
          rsScrambler.genTables();
        }
        for (int i = 0; i < n && generationThread == Thread.currentThread(); i++) {
          String[] scramble;
          synchronized (scramblerHelper) {
            sendGenStateToListeners(new RandomStateGenEvent(State.GENERATING, i + 1, n));
            scramble = rsScrambler.getNewScramble(new ScrambleConfig(Utils.getRSScrambleLengthFromQuality(cubeType)));
          }
          if (getCache(cubeType).size() < MAX_SCRAMBLES_IN_MEMORY) {
            synchronized (cacheMemHelper) {
              getCache(cubeType).add(scramble);
              System.out.println("[NanoTimer] generate. size: " + getCache(cubeType).size());
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
    if (Options.INSTANCE.isRandomStateScrambles() && getRandomStateCubeTypes().contains(cubeType) && !getCache(cubeType).isEmpty()) {
      String[] scramble;
      synchronized (cacheMemHelper) {
        scramble = getCache(cubeType).remove();
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
      generateAndAddToCache(cubeType, nScrambles);
    }
  }

  public void stopGeneration() {
    sendGenStateToListeners(new RandomStateGenEvent(State.STOPPING, 0, 0));
    generationThread = null;
  }

  public void activateRandomStateScrambles(final boolean activate) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (activate) {
          initRandomState(Utils.getCurrentCubeType(context));
        } else {
          stopGeneration();
          stopRandomState();
        }
      }
    }).start();
  }

  public void addRandomStateGenListener(RandomStateGenListener listener) {
    listeners.add(listener);
    listener.onStateUpdate(curState);
  }

  public void removeRandomStateGenListener(RandomStateGenListener listener) {
    listeners.remove(listener);
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
    if (scramblers.get(cubeType.getId()) == null) {
      scramblers.put(cubeType.getId(), getNewRandomStateScrambler(cubeType));
    }
    return scramblers.get(cubeType.getId());
  }

}
