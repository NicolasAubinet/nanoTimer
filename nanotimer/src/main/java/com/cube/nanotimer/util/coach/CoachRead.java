package com.cube.nanotimer.util.coach;

import android.os.Handler;
import android.os.Looper;

import com.cube.nanotimer.vo.CubeMethod;
import com.cube.nanotimer.vo.SolveType;

import java.util.HashMap;
import java.util.Map;

/**
 * One solve type's request for a plan as a screen sees it, which outlives the screen that asked: a
 * model call takes longer than a rotation, and the answer is kept whether or not anyone is still
 * looking. The asking itself is {@link CoachAsk}.
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

  private final SolveType solveType;
  private final Handler main = new Handler(Looper.getMainLooper());
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

  /** Main thread only, like everything else here that is not a callback. */
  public static CoachRead of(SolveType solveType) {
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

  public void setListener(Listener listener) {
    this.listener = listener;
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
    CoachAsk.ask(client, solveType, method, new CoachAsk.Listener() {
      @Override
      public void onAnswer(CoachReply failure, int count) {
        solveCount = count;
        post(failure == null ? State.IDLE : State.FAILED, failure);
      }
    });
  }

  private void post(final State state, final CoachReply failure) {
    main.post(new Runnable() {
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
