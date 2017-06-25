package com.cube.nanotimer.scrambler;

import android.content.Context;
import android.content.Intent;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.scrambler.randomstate.AlreadyGeneratingException;
import com.cube.nanotimer.scrambler.randomstate.ChargingStateReceiver;
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
import com.cube.nanotimer.vo.ScrambleType;

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
  private Map<ScrambleCacheKey, LinkedList<String[]>> cachedScrambles = new HashMap<ScrambleCacheKey, LinkedList<String[]>>();
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

  private int loadCacheAndGetToGenCount(CubeType cubeType, ScrambleType scrambleType, boolean isFromPhonePlugged) {
    int minCacheSize = Options.INSTANCE.getScramblesMinCacheSize();
    if (isFromPhonePlugged) {
      minCacheSize = Options.INSTANCE.getPluggedInScramblesGenerateCount();
    }
    if (getCache(cubeType, scrambleType).size() < minCacheSize) {
      loadCacheFromFile(cubeType, scrambleType); // see if there are some more scrambles in the file
      if (getCache(cubeType, scrambleType).size() < minCacheSize) {
        int maxCacheSize = Options.INSTANCE.getScramblesMaxCacheSize();
        if (isFromPhonePlugged) {
          maxCacheSize = Options.INSTANCE.getPluggedInScramblesGenerateCount();
        }
        return Math.max(maxCacheSize - getCache(cubeType, scrambleType).size(), 0);
      }
    }
    return 0;
  }

  public void generateAndAddToCache(final CubeType cubeType, int scramblesCount, GenerationLaunch generationLaunch) throws AlreadyGeneratingException {
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
        if (Options.INSTANCE.isRandomStateScrambles()) {
          generateScrambles(cubeType, null, scramblesCount);
          for (CubeType rsCubeType : getRandomStateCubeTypes()) {
            generateScrambles(rsCubeType, null, -1);
          }
        }
        for (CubeType rsCubeType : getRandomStateCubeTypes()) {
          for (ScrambleType scrambleType : rsCubeType.getUsedScrambledTypes()) {
            if (!scrambleType.isDefault()) {
              generateScrambles(rsCubeType, scrambleType, -1);
            }
          }
        }
        sendGenStateToListeners(new RandomStateGenEvent(RandomStateGenEvent.State.IDLE, null, null, 0, 0));
        if (generationThread == Thread.currentThread()) {
          checkPluggedIn();
          generationThread = null;
        }
      }

      private void generateScrambles(CubeType cubeType, ScrambleType scrambleType, int scramblesCount) {
        final RSScrambler rsScrambler = getScrambler(cubeType);
        if (generationThread != Thread.currentThread() || rsScrambler == null || (!Options.INSTANCE.isRandomStateScrambles() && (scrambleType == null || scrambleType.isDefault()))) {
          return;
        }
        int n;
        if (scramblesCount > 0) {
          n = scramblesCount;
        } else {
          n = loadCacheAndGetToGenCount(cubeType, scrambleType, (generationLaunch == GenerationLaunch.PLUGGED));
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
            scramble = rsScrambler.getNewScramble(new ScrambleConfig(Utils.getRSScrambleLengthFromQuality(cubeType), scrambleType));
            sendGenStateToListeners(new RandomStateGenEvent(RandomStateGenEvent.State.GENERATED, cubeType, generationLaunch, i + 1, n));
          }
          if (scramble == null) { // was interrupted
            break;
          }
          if (getCache(cubeType, scrambleType).size() < MAX_SCRAMBLES_IN_MEMORY) {
            synchronized (cacheMemHelper) {
              getCache(cubeType, scrambleType).add(scramble);
//              Log.i("[NanoTimer]", "generate. size: " + getCache(cubeType).size());
            }
          }
          toSave.add(scramble);
          if (toSave.size() >= 10) {
            saveNewScramblesToFile(cubeType, scrambleType, toSave); // write new scrambles to file by batches
            toSave.clear();
          }
        }
        if (!toSave.isEmpty()) {
          saveNewScramblesToFile(cubeType, scrambleType, toSave);
        }
      }

      private void checkPluggedIn() {
        // call service to check if generation should be started or stopped
        context.sendBroadcast(new Intent(ChargingStateReceiver.CHECK_ACTION_NAME));
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

  private void loadCacheFromFile(CubeType cubeType, ScrambleType scrambleType) {
    List<String> scramblesStr;
    synchronized (cacheFileHelper) {
      scramblesStr = FileUtils.readLinesFromFile(context, getFileName(cubeType, scrambleType), MAX_SCRAMBLES_IN_MEMORY);
    }
    synchronized (cacheMemHelper) {
      getCache(cubeType, scrambleType).clear();
      for (String l : scramblesStr) {
        String[] scramble = l.split(" ");
        getCache(cubeType, scrambleType).add(scramble);
      }
    }
  }

  private void saveNewScramblesToFile(CubeType cubeType, ScrambleType scrambleType, List<String[]> scramblesToSave) {
    List<String> scramblesStr = new ArrayList<String>(scramblesToSave.size());
    for (String[] scramble : scramblesToSave) {
      StringBuilder sb = new StringBuilder();
      for (String move : scramble) {
        sb.append(move).append(" ");
      }
      scramblesStr.add(sb.toString());
    }
    synchronized (cacheFileHelper) {
      FileUtils.appendLinesToFile(context, getFileName(cubeType, scrambleType), scramblesStr.toArray(new String[scramblesStr.size()]));
    }
  }

  private void removeFirstScrambleFromFile(CubeType cubeType, ScrambleType scrambleType) {
    synchronized (cacheFileHelper) {
      FileUtils.removeFirstLineFromFile(context, getFileName(cubeType, scrambleType));
    }
  }

  public String[] getScramble(final CubeType cubeType, final ScrambleType scrambleType) {
    if (getRandomStateCubeTypes().contains(cubeType) && (Options.INSTANCE.isRandomStateScrambles() || (scrambleType != null && !scrambleType.isDefault()))) {
      String[] scramble;
      if (getCache(cubeType, scrambleType).size() > 0) {
        synchronized (cacheMemHelper) {
          scramble = getCache(cubeType, scrambleType).remove();
        }
      } else {
        if (scrambleType == null || scrambleType.isDefault()) {
          scramble = ScramblerFactory.getScrambler(cubeType).getNewScramble();
        } else {
          scramble = null;
        }
      }
      new Thread(new Runnable() {
        @Override
        public void run() {
          removeFirstScrambleFromFile(cubeType, scrambleType);
          checkCache(cubeType);
        }
      }).start();
      return scramble;
    } else {
      return ScramblerFactory.getScrambler(cubeType).getNewScramble();
    }
  }

  public int getScramblesCount(CubeType cubeType, ScrambleType scrambleType) {
    int scramblesCount;
    synchronized (cacheFileHelper) {
      scramblesCount = FileUtils.getFileLinesCount(context, getFileName(cubeType, scrambleType));
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
        for (ScrambleType usedScrambleType : cubeType.getUsedScrambledTypes()) {
          FileUtils.deleteFile(context, getFileName(cubeType, usedScrambleType));
        }
      }
      synchronized (cacheMemHelper) {
        getCache(cubeType, null).clear();
        for (ScrambleType usedScrambleType : cubeType.getUsedScrambledTypes()) {
          getCache(cubeType, usedScrambleType).clear();
        }
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

  public List<CubeType> getRandomStateCubeTypes() {
    return Arrays.asList(CubeType.THREE_BY_THREE, CubeType.TWO_BY_TWO);
  }

  private String getFileName(CubeType cubeType, ScrambleType scrambleType) {
    String fileName = "randomstate_scrambles_" + cubeType.getId();
    if (scrambleType != null && !scrambleType.isDefault()) {
      fileName += "_" + scrambleType.getName();
    }
    return fileName;
  }

  private Queue<String[]> getCache(CubeType cubeType, ScrambleType scrambleType) {
    ScrambleCacheKey scrambleCacheKey = new ScrambleCacheKey(cubeType.getId(), scrambleType);
    LinkedList<String[]> scrambles = cachedScrambles.get(scrambleCacheKey);
    if (scrambles == null) {
      scrambles = new LinkedList<>();
      cachedScrambles.put(scrambleCacheKey, scrambles);
    }
    return scrambles;
  }

  private RSScrambler getScrambler(CubeType cubeType) {
    synchronized (scramblerHelper) {
      if (scramblers.get(cubeType.getId()) == null) {
        scramblers.put(cubeType.getId(), getNewRandomStateScrambler(cubeType));
      }
      return scramblers.get(cubeType.getId());
    }
  }

  private class ScrambleCacheKey {
    private int cubeTypeId;
    private ScrambleType scrambleType;

    public ScrambleCacheKey(int cubeTypeId, ScrambleType scrambleType) {
      this.cubeTypeId = cubeTypeId;
      this.scrambleType = scrambleType;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ScrambleCacheKey)) return false;

      ScrambleCacheKey that = (ScrambleCacheKey) o;

      if (cubeTypeId != that.cubeTypeId) return false;
      return !(scrambleType != null ? !scrambleType.equals(that.scrambleType) : that.scrambleType != null);
    }

    @Override
    public int hashCode() {
      int result = cubeTypeId;
      result = 31 * result + (scrambleType != null ? scrambleType.hashCode() : 0);
      return result;
    }
  }

}
