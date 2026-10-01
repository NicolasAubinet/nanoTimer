package com.cube.nanotimer.util.coach;

import android.os.Handler;
import android.os.Looper;

import com.cube.nanotimer.Options;
import com.cube.nanotimer.vo.CubeMethod;
import com.cube.nanotimer.vo.SolveType;

import java.util.HashMap;
import java.util.Map;

/**
 * One solve type's request for a plan as a screen sees it, which outlives the screen that asked: a
 * model call takes longer than a rotation, and the answer is kept whether or not anyone is still
 * looking. The asking itself is {@link CoachAsk}, and an ask the weekly job started shows here like
 * one the screen started.
 */
public final class CoachRead {

  public enum State {
    IDLE,
    READING,
    /** The last ask ended without a plan, and {@link #getFailure} says how. */
    FAILED
  }

  public interface Listener {
    /** Called on the main thread whenever the state moves, a plan having been saved first. */
    void onReadChanged(CoachRead read);
  }

  private static final Map<Integer, CoachRead> READS = new HashMap<Integer, CoachRead>();
  private static final Handler MAIN = new Handler(Looper.getMainLooper());

  static {
    CoachAsk.setWatcher(new CoachAsk.Watcher() {
      @Override
      public CoachAsk.Listener onAsking(SolveType solveType) {
        final CoachRead read = of(solveType);
        MAIN.post(new Runnable() {
          @Override
          public void run() {
            if (read.state != State.READING) {
              read.moveTo(State.READING, null);
            }
          }
        });
        return read.answered;
      }
    });
  }

  private final SolveType solveType;
  private final CoachAsk.Listener answered = new CoachAsk.Listener() {
    @Override
    public void onAnswer(CoachReply failure, int count) {
      solveCount = count;
      post(failure == null ? State.IDLE : State.FAILED, failure);
    }
  };
  private State state = State.IDLE;
  private CoachReply failure;
  /** When the service said to ask again, as a time rather than a wait: it is shown later. */
  private long askAgainAt;
  /** How many solves the method has been read from, or -1 when a resent request never counted. */
  private int solveCount = -1;
  private Listener listener;

  private CoachRead(SolveType solveType) {
    this.solveType = solveType;
  }

  /** From any thread. Everything else here that is not a callback is main thread only. */
  public static synchronized CoachRead of(SolveType solveType) {
    CoachRead read = READS.get(solveType.getId());
    if (read == null) {
      read = new CoachRead(solveType);
      READS.put(solveType.getId(), read);
    }
    return read;
  }

  public State getState() {
    return state;
  }

  /** How the last ask failed, only in {@link State#FAILED}. */
  public CoachReply getFailure() {
    return failure;
  }

  /** When the service said it could be asked again, 0 when it did not say. */
  public long getAskAgainAt() {
    return askAgainAt;
  }

  public int getSolveCount() {
    return solveCount;
  }

  /** Takes the screen now showing this solve type, which sees an ask already under way. */
  public void setListener(Listener listener) {
    this.listener = listener;
    follow();
  }

  /** Lets go of a screen on its way out, unless a newer one has taken its place. */
  public void removeListener(Listener listener) {
    if (this.listener == listener) {
      this.listener = null;
    }
  }

  /** Asks for a plan, sending again what went unanswered if anything did. */
  public void start(CoachClient client, CubeMethod method) {
    if (state == State.READING) {
      return;
    }
    moveTo(State.READING, null);
    CoachAsk.ask(client, solveType, method, Options.INSTANCE.getCoachLastPlanAt(), answered);
  }

  /** Waits for an ask somebody else started for this solve type, if there is one. */
  private void follow() {
    if (state != State.READING && CoachAsk.join(solveType, answered)) {
      moveTo(State.READING, null);
    }
  }

  private void post(final State state, final CoachReply failure) {
    MAIN.post(new Runnable() {
      @Override
      public void run() {
        moveTo(state, failure);
      }
    });
  }

  private void moveTo(State state, CoachReply failure) {
    this.state = state;
    this.failure = failure;
    askAgainAt = failure == null || failure.getRetryAfterSeconds() == 0 ? 0
        : System.currentTimeMillis() + failure.getRetryAfterSeconds() * 1000L;
    if (listener != null) {
      listener.onReadChanged(this);
    }
  }
}
