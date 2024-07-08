package com.cube.nanotimer.scrambler;

import android.content.Context;
import android.util.Log;
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
import com.cube.nanotimer.scrambler.randomstate.pyraminx.RSPyraminxScrambler;
import com.cube.nanotimer.scrambler.randomstate.skewb.RSSkewbScrambler;
import com.cube.nanotimer.scrambler.randomstate.square1.RSSquare1Scrambler;
import com.cube.nanotimer.util.ScrambleFormatterService;
import com.cube.nanotimer.util.helper.CpuUtils;
import com.cube.nanotimer.util.helper.FileUtils;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.ScrambleType;
import com.cube.nanotimer.vo.ScramblesQuality;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public enum ScramblerService {
  INSTANCE;

  public static final int MAX_SCRAMBLES_IN_MEMORY = 500;

  private Context context;
  private final Map<ScrambleCacheKey, LinkedList<String[]>> cachedScrambles = new HashMap<ScrambleCacheKey, LinkedList<String[]>>();
  private final List<RSScrambler> scramblers = new ArrayList<>();
  private volatile Thread generationThread = null;

  private final List<RandomStateGenListener> listeners = new ArrayList<RandomStateGenListener>();
  private RandomStateGenEvent curState = new RandomStateGenEvent(State.IDLE, null, 0, 0);

  final private Object genThreadHelper = new Object();
  final private Object cacheMemHelper = new Object();
  final private Object cacheFileHelper = new Object();

  public void init(Context context) {
    this.context = context;
  }

  public void checkScrambleCaches() {
    checkCache(getRandomStateCubeTypes().get(0));
  }

  private void stopRandomState() {
    stopGeneration();

    if (!Options.INSTANCE.isRandomStateScrambles()) {
      synchronized (cachedScrambles) {
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
//    if (isFromPhonePlugged) {
//      minCacheSize = Options.INSTANCE.getPluggedInScramblesGenerateCount();
//    }

    Queue<String[]> scramblesCache = getCache(cubeType, scrambleType);
    if (scramblesCache.size() < minCacheSize) {
      loadCacheFromFile(cubeType, scrambleType); // see if there are some more scrambles in the file
      if (scramblesCache.size() < minCacheSize) {
        int maxCacheSize = Options.INSTANCE.getScramblesMaxCacheSize();
//        if (isFromPhonePlugged) {
//          maxCacheSize = Options.INSTANCE.getPluggedInScramblesGenerateCount();
//        }
        return Math.max(maxCacheSize - scramblesCache.size(), 0);
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

            for (ScrambleType scrambleType : rsCubeType.getUsedScrambledTypes()) {
              if (!scrambleType.isDefault()) {
                generateScrambles(rsCubeType, scrambleType, -1);
              }
            }
          }
        }
        sendGenStateToListeners(new RandomStateGenEvent(RandomStateGenEvent.State.IDLE, null, 0, 0));

        synchronized (genThreadHelper) {
          if (generationThread == Thread.currentThread()) {
            //checkPluggedIn();
            generationThread = null;
          }
        }
      }

      private void generateScrambles(final CubeType cubeType, final ScrambleType scrambleType, int scramblesCount) {
        RSScrambler rsScrambler = getNewRandomStateScrambler(cubeType);
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
        int maxScrambleLength = getRSScrambleLength(cubeType, scrambleType, Options.INSTANCE.getScramblesQuality());

        sendGenStateToListeners(new RandomStateGenEvent(RandomStateGenEvent.State.PREPARING, cubeType, scrambleType, generationLaunch, 0, n));
        rsScrambler.prepareGenTables(context);
        rsScrambler.genTables();

        final int threadsCount = Math.max(1, CpuUtils.getNumberOfCores() - 1);
        Log.i("NanoTimer", "Generate " + n + " " + cubeType + "|" + scrambleType + " scrambles on " + threadsCount + " threads");

        final List<String[]> toSave = new ArrayList<>();
        final Semaphore threadsSemaphore = new Semaphore(threadsCount);
        final List<ScrambleGeneratedThread> scrambleGenerationThreads = new ArrayList<>();
        final AtomicInteger generatedScramblesCount = new AtomicInteger(1);
        final ScrambleConfig scrambleConfig = new ScrambleConfig(maxScrambleLength, scrambleType);

        sendGenStateToListeners(new RandomStateGenEvent(RandomStateGenEvent.State.GENERATING, cubeType, scrambleType,
          generationLaunch, generatedScramblesCount.get(), n));

        final ScrambleGeneratedHandler scrambleGeneratedHandler = new ScrambleGeneratedHandler() {
          @Override
          public void scrambleGenerated(String[] scramble, int toGenerateCount) {
            threadsSemaphore.release();

            if (scramble == null) { // was interrupted
              return;
            }

            synchronized (cacheMemHelper) {
              Queue<String[]> scramblesCache = getCache(cubeType, scrambleType);
              if (scramblesCache.size() < MAX_SCRAMBLES_IN_MEMORY) {
                scramblesCache.add(scramble);
              }
            }

            synchronized (toSave) {
              toSave.add(scramble);
              if (toSave.size() >= 10) {
                saveNewScramblesToFile(cubeType, scrambleType, toSave); // write new scrambles to file by batches
                toSave.clear();
              }
            }

            sendGenStateToListeners(new RandomStateGenEvent(RandomStateGenEvent.State.GENERATED, cubeType, scrambleType,
              generationLaunch, generatedScramblesCount.get(), toGenerateCount));

            sendGenStateToListeners(new RandomStateGenEvent(RandomStateGenEvent.State.GENERATING, cubeType, scrambleType,
              generationLaunch, generatedScramblesCount.incrementAndGet(), toGenerateCount));
          }
        };

        try {
          for (int i = 0; i < n && generationThread == Thread.currentThread(); i++) {
            threadsSemaphore.acquire();

            rsScrambler = getNewRandomStateScrambler(cubeType);
            synchronized (scramblers) {
              scramblers.add(rsScrambler);
            }

            ScrambleGeneratedThread scrambleGeneratedThread = new ScrambleGeneratedThread(rsScrambler, scrambleConfig, scrambleGeneratedHandler, n);
            scrambleGenerationThreads.add(scrambleGeneratedThread);
            scrambleGeneratedThread.start();
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        try {
          for (ScrambleGeneratedThread scrambleGenerationThread : scrambleGenerationThreads) {
            scrambleGenerationThread.join();
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        Log.i("NanoTimer", "Generated " + (generatedScramblesCount.get()-1) + " scrambles for cube type " + cubeType + "!");

        if (!toSave.isEmpty()) {
          saveNewScramblesToFile(cubeType, scrambleType, toSave);
        }
      }

      public int getRSScrambleLength(CubeType cubeType, ScrambleType scrambleType, ScramblesQuality scramblesQuality) {
        int maxScrambleLength = 0;

        if (scrambleType != null) {
          maxScrambleLength = scrambleType.getRSScrambleLengthFromQuality(scramblesQuality);
        }

        if (maxScrambleLength == 0) {
          switch (cubeType) {
            case TWO_BY_TWO:
              switch (scramblesQuality) {
                case NORMAL:
                  maxScrambleLength = 11;
                  break;
                case LOW:
                  maxScrambleLength = 12;
                  break;
              }
              break;
            case THREE_BY_THREE:
              switch (scramblesQuality) {
                case NORMAL:
                  maxScrambleLength = 21;
                  break;
                case LOW:
                  maxScrambleLength = 23;
                  break;
              }
              break;
            case PYRAMINX:
              maxScrambleLength = 11;
              break;
            case SQUARE1:
              break;
          }
        }

        return maxScrambleLength;
      }

//      private void checkPluggedIn() {
//        // call service to check if generation should be started or stopped
//        context.sendBroadcast(new Intent(ChargingStateReceiver.CHECK_ACTION_NAME));
//      }
    };
  }

  private void sendGenStateToListeners(RandomStateGenEvent state) {
    curState = state;
    synchronized (listeners) {
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
      case SKEWB:
        return new RSSkewbScrambler();
      case PYRAMINX:
        return new RSPyraminxScrambler();
      case SQUARE1:
        return new RSSquare1Scrambler();
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
      Queue<String[]> scramblesCache = getCache(cubeType, scrambleType);
      scramblesCache.clear();
      for (String l : scramblesStr) {
        String[] scramble = ScrambleFormatterService.INSTANCE.parseStringScrambleToArray(l, cubeType);
        scramblesCache.add(scramble);
      }
    }
  }

  private synchronized void saveNewScramblesToFile(CubeType cubeType, ScrambleType scrambleType, List<String[]> scramblesToSave) {
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
      String[] scramble = null;
      boolean foundScrambleInCache = false;

      synchronized (cacheMemHelper) {
        Queue<String[]> scramblesCache = getCache(cubeType, scrambleType);
        if (scramblesCache.size() > 0) {
          scramble = scramblesCache.remove();
          foundScrambleInCache = true;
        }
      }

      if (!foundScrambleInCache) {
        if (scrambleType == null || scrambleType.isDefault()) {
          scramble = ScramblerFactory.getScrambler(cubeType).getNewScramble();
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
        sendGenStateToListeners(new RandomStateGenEvent(State.STOPPING, null, 0, 0));
      }
    }
    synchronized (scramblers) {
      for (RSScrambler scrambler : scramblers) {
        scrambler.stop();
      }
      scramblers.clear();
    }
  }

  public void deleteCaches() {
    for (CubeType cubeType : getRandomStateCubeTypes()) {
      synchronized (cacheFileHelper) {
        FileUtils.deleteFile(context, getFileName(cubeType, null));
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
          stopRandomState();
        }
      }
    }).start();
  }

  public void addRandomStateGenListener(RandomStateGenListener listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
    listener.onStateUpdate(curState);
  }

  public void removeRandomStateGenListener(RandomStateGenListener listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
  }

  public List<CubeType> getRandomStateCubeTypes() {
    return Arrays.asList(CubeType.THREE_BY_THREE, CubeType.TWO_BY_TWO, /*CubeType.SKEWB,*/ CubeType.PYRAMINX, CubeType.SQUARE1);
  }

  private String getFileName(CubeType cubeType, ScrambleType scrambleType) {
    String fileName = "randomstate_scrambles_" + cubeType.getId();
    if (scrambleType != null && !scrambleType.isDefault()) {
      fileName += "_" + scrambleType.getName();
    }
    return fileName;
  }

  private Queue<String[]> getCache(CubeType cubeType, ScrambleType scrambleType) {
    ScrambleType cacheScrambleType = scrambleType;
    if (cacheScrambleType != null && cacheScrambleType.isDefault()) {
      cacheScrambleType = null;
    }
    ScrambleCacheKey scrambleCacheKey = new ScrambleCacheKey(cubeType.getId(), cacheScrambleType);

    LinkedList<String[]> scrambles;
    synchronized (cachedScrambles) {
      scrambles = cachedScrambles.get(scrambleCacheKey);
      if (scrambles == null) {
        scrambles = new LinkedList<>();
        cachedScrambles.put(scrambleCacheKey, scrambles);
      }
    }
    return scrambles;
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

  private class ScrambleGeneratedThread extends Thread {
    private RSScrambler rsScrambler;
    private ScrambleConfig scrambleConfig;
    private ScrambleGeneratedHandler scrambleGeneratedHandler;
    private int toGenerateCount;

    public ScrambleGeneratedThread(RSScrambler rsScrambler, ScrambleConfig scrambleConfig,
                                   ScrambleGeneratedHandler scrambleGeneratedHandler, int toGenerateCount) {
      this.rsScrambler = rsScrambler;
      this.scrambleConfig = scrambleConfig;
      this.scrambleGeneratedHandler = scrambleGeneratedHandler;
      this.toGenerateCount = toGenerateCount;
    }

    @Override
    public void run() {
      String[] scramble = rsScrambler.getNewScramble(scrambleConfig);
      synchronized (scramblers) {
        scramblers.remove(rsScrambler);
      }
      scrambleGeneratedHandler.scrambleGenerated(scramble, toGenerateCount);
    }
  }

  private abstract class ScrambleGeneratedHandler {
    public abstract void scrambleGenerated(String[] scramble, int toGenerateCount);
  }

}
