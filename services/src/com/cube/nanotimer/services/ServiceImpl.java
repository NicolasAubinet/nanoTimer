package com.cube.nanotimer.services;

import android.content.Context;
import com.cube.nanotimer.services.db.DBHelper;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.ExportResult;
import com.cube.nanotimer.vo.SessionDetails;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveHistory;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveTimeAverages;
import com.cube.nanotimer.vo.SolveType;

import java.util.List;

public class ServiceImpl extends DBHelper implements Service {

  private static ServiceImpl instance;

  private ServiceProvider provider;

  private ServiceImpl(Context context) {
    super(context);
    provider = new ServiceProviderImpl(db);
  }

  private ServiceImpl(Context context, String dbName) {
    super(context, dbName);
    provider = new ServiceProviderImpl(db);
  }

  public static ServiceImpl getInstance(Context context) {
    if (instance == null) {
      instance = new ServiceImpl(context);
    }
    return instance;
  }

  public static ServiceImpl getInstance(Context context, String dbName) {
    if (instance == null) {
      instance = new ServiceImpl(context, dbName);
    }
    return instance;
  }

  @Override
  public void getCubeTypes(final boolean getEmpty, final DataCallback<List<CubeType>> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.getCubeTypes(getEmpty));
      }
    });
  }

  @Override
  public void getSolveTypes(final CubeType cubeType, final DataCallback<List<SolveType>> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.getSolveTypes(cubeType));
      }
    });
  }

  @Override
  public void saveTime(final SolveTime solveTime, final DataCallback<SolveAverages> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.saveTime(solveTime));
      }
    });
  }

  @Override
  public void getSolveAverages(final SolveType solveType, final DataCallback<SolveAverages> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.getSolveAverages(solveType));
      }
    });
  }

  @Override
  public void deleteTime(final SolveTime solveTime, final DataCallback<SolveAverages> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.deleteTime(solveTime));
      }
    });
  }

  @Override
  public void getHistory(final SolveType solveType, final DataCallback<SolveHistory> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.getHistory(solveType));
      }
    });
  }

  @Override
  public void getHistory(final SolveType solveType, final long from, final DataCallback<SolveHistory> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.getHistory(solveType, from));
      }
    });
  }

  @Override
  public void deleteHistory(final DataCallback<Void> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        provider.deleteHistory();
        if (callback != null) {
          callback.onData(null);
        }
      }
    });
  }

  @Override
  public void deleteHistory(final SolveType solveType, final DataCallback<Void> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        provider.deleteHistory(solveType);
        if (callback != null) {
          callback.onData(null);
        }
      }
    });
  }

  @Override
  public void getSessionTimes(final SolveType solveType, final DataCallback<List<Long>> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.getSessionTimes(solveType));
      }
    });
  }

  @Override
  public void startNewSession(final SolveType solveType, final long startTs, final DataCallback<Void> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        provider.startNewSession(solveType, startTs);
        if (callback != null) {
          callback.onData(null);
        }
      }
    });
  }

  @Override
  public void getSessionStart(final SolveType solveType, final DataCallback<Long> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.getSessionStart(solveType));
      }
    });
  }

  @Override
  public void addSolveType(final SolveType solveType, final DataCallback<Integer> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.addSolveType(solveType));
      }
    });
  }

  @Override
  public void addSolveTypeSteps(final SolveType solveType, final DataCallback<Void> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        provider.addSolveTypeSteps(solveType);
        if (callback != null) {
          callback.onData(null);
        }
      }
    });
  }

  @Override
  public void updateSolveType(final SolveType solveType, final DataCallback<Void> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        provider.updateSolveType(solveType);
        if (callback != null) {
          callback.onData(null);
        }
      }
    });
  }

  @Override
  public void deleteSolveType(final SolveType solveType, final DataCallback<Void> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        provider.deleteSolveType(solveType);
        if (callback != null) {
          callback.onData(null);
        }
      }
    });
  }

  @Override
  public void saveSolveTypesOrder(final List<SolveType> solveTypes, final DataCallback<Void> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        provider.saveSolveTypesOrder(solveTypes);
        if (callback != null) {
          callback.onData(null);
        }
      }
    });
  }

  @Override
  public void getSolveTimeAverages(final SolveTime solveTime, final DataCallback<SolveTimeAverages> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        SolveTimeAverages sta = provider.getSolveTimeAverages(solveTime);
        if (callback != null) {
          callback.onData(sta);
        }
      }
    });
  }

  @Override
  public void getSessionDetails(final SolveType solveType, final DataCallback<SessionDetails> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.getSessionDetails(solveType));
      }
    });
  }

  @Override
  public void getSolvesCount(final SolveType solveType, final DataCallback<Integer> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.getSolvesCount(solveType));
      }
    });
  }

  @Override
  public void getExportFile(final List<Integer> solveTypeIds, final int limit, final DataCallback<List<ExportResult>> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.getExportResults(solveTypeIds, limit));
      }
    });
  }

  private void run(Runnable runnable) {
    new Thread(runnable).start();
  }

}
