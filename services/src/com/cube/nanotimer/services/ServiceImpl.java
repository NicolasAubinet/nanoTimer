package com.cube.nanotimer.services;

import android.content.Context;
import com.cube.nanotimer.services.db.DBHelper;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;
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
  public void getCubeTypes(final DataCallback<List<CubeType>> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.getCubeTypes());
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
  public void removeTime(final SolveTime solveTime, final DataCallback<SolveAverages> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.removeTime(solveTime));
      }
    });
  }

  @Override
  public void getHistory(final SolveType solveType, final DataCallback<List<SolveTime>> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.getHistory(solveType));
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
  public void addSolveType(final SolveType solveType, final DataCallback<Integer> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        callback.onData(provider.addSolveType(solveType));
      }
    });
  }

  @Override
  public void updateSolveType(final SolveType solveType, final DataCallback<Void> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        provider.updateSolveType(solveType);
        callback.onData(null);
      }
    });
  }

  @Override
  public void deleteSolveType(final SolveType solveType, final DataCallback<Void> callback) {
    run(new Runnable() {
      @Override
      public void run() {
        provider.deleteSolveType(solveType);
        callback.onData(null);
      }
    });
  }

  private void run(Runnable runnable) {
    new Thread(runnable).start();
  }

}
